using MassTransit;
using backend.Messaging.Events;
using backend.Data;
using backend.Messaging.Producers;
using Microsoft.EntityFrameworkCore;
using backend.Domain;

namespace backend.Messaging.Consumers;

public class GamificationConsumer :
    IConsumer<CommitSavedEvent>,
    IConsumer<TaskCompletedEvent>,
    IConsumer<AssignBadgeCommand>
{
    private readonly ApplicationDbContext _context;
    private readonly INotificationProducer _notificationProducer;
    private readonly ILogger<GamificationConsumer> _logger;

    public GamificationConsumer(ApplicationDbContext context, INotificationProducer notificationProducer, ILogger<GamificationConsumer> logger)
    {
        _context = context;
        _notificationProducer = notificationProducer;
        _logger = logger;
    }

    // 1. Gestion des Commits (Automatique)
    public async Task Consume(ConsumeContext<CommitSavedEvent> context)
    {
        var msg = context.Message;

        // Sécurité : On vérifie l'existence avant toute opération
        if (!await UserExists(msg.UserId)) return;

        var config = await _context.XpConfigs.FirstOrDefaultAsync();
        int xp = config?.CommitXp ?? 10;

        await AddXpAndNotify(msg.UserId, xp, $"Nouveau commit : {msg.Message}");
        await CheckAndAssignBadge(msg.UserId, "First Contribution");
    }

    // 2. Gestion des Tâches (Automatique)
    public async Task Consume(ConsumeContext<TaskCompletedEvent> context)
    {
        var msg = context.Message;

        // Sécurité : On vérifie l'existence avant toute opération
        if (!await UserExists(msg.UserId)) return;

        int xp = msg.Difficulty == "High" ? 50 : 20;

        await AddXpAndNotify(msg.UserId, xp, $"Tâche terminée : {msg.TaskTitle}");

        if (!string.IsNullOrEmpty(msg.TagName))
        {
            await UpdateTagProgress(msg.UserId, msg.TagName);
        }

        if (msg.TaskTitle.Contains("bug", StringComparison.OrdinalIgnoreCase))
        {
            await CheckAndAssignBadge(msg.UserId, "Bug fixer");
        }
    }

    // 3. Gestion Manuelle (AssignBadgeCommand)
    public async Task Consume(ConsumeContext<AssignBadgeCommand> context)
    {
        var msg = context.Message;

        // Sécurité : On vérifie l'existence
        if (!await UserExists(msg.UserId))
        {
            _logger.LogError("ERREUR : Impossible d'attribuer un badge. L'utilisateur {UserId} n'existe pas dans UserProgresses.", msg.UserId);
            return;
        }

        var hasBadge = await _context.UserBadges
            .AnyAsync(ub => ub.UserId == msg.UserId && ub.BadgeId == msg.BadgeId);

        if (!hasBadge)
        {
            _context.UserBadges.Add(new UserBadge {
                Id = Guid.NewGuid(),
                UserId = msg.UserId,
                BadgeId = msg.BadgeId,
                UnlockedAt = DateTime.UtcNow
            });

            await _context.SaveChangesAsync();
            _logger.LogInformation("!!! SUCCÈS !!! Badge manuel {BadgeId} inséré pour l'utilisateur {UserId}", msg.BadgeId, msg.UserId);
        }
    }

    // --- MÉTHODES PRIVÉES DE LOGIQUE ---

    private async Task<bool> UserExists(long userId)
    {
        var exists = await _context.UserProgresses.AnyAsync(p => p.UserId == userId);
        if (!exists)
        {
            _logger.LogWarning("SÉCURITÉ : Action rejetée. L'ID utilisateur {UserId} est inconnu du système de Gamification.", userId);
        }
        return exists;
    }

    private async Task AddXpAndNotify(long userId, int amount, string reason)
    {
        var profile = await _context.UserProgresses.FirstAsync(p => p.UserId == userId);
        profile.TotalExperience += amount;
        profile.UpdatedAt = DateTime.UtcNow;
        await _context.SaveChangesAsync();
        _logger.LogInformation("+{Amount} XP pour {UserId} ({Reason})", amount, userId, reason);
    }

    private async Task UpdateTagProgress(long userId, string tagName)
    {
        var tag = await _context.Tags.IgnoreQueryFilters()
            .FirstOrDefaultAsync(t => t.Name.ToLower() == tagName.ToLower());

        if (tag == null) return;

        var tagProgress = await _context.UserTagProgresses
            .FirstOrDefaultAsync(tp => tp.UserId == userId && tp.TagId == tag.Id);

        if (tagProgress == null)
        {
            _context.UserTagProgresses.Add(new UserTagProgress {
                Id = Guid.NewGuid(),
                UserId = userId,
                TagId = tag.Id,
                Occurrences = 1,
                LastUpdatedAt = DateTime.UtcNow
            });
        }
        else
        {
            tagProgress.Occurrences += 1;
            tagProgress.LastUpdatedAt = DateTime.UtcNow;
        }
        await _context.SaveChangesAsync();
        _logger.LogInformation("Progression du Tag '{TagName}' mise à jour pour {UserId}", tagName, userId);
    }

    private async Task CheckAndAssignBadge(long userId, string badgeTitle)
    {
        var badge = await _context.Badges.IgnoreQueryFilters()
            .FirstOrDefaultAsync(b => b.Title.ToLower() == badgeTitle.ToLower());

        if (badge == null) return;

        var hasBadge = await _context.UserBadges
            .AnyAsync(ub => ub.UserId == userId && ub.BadgeId == badge.Id);

        if (!hasBadge)
        {
            _context.UserBadges.Add(new UserBadge {
                Id = Guid.NewGuid(),
                UserId = userId,
                BadgeId = badge.Id,
                UnlockedAt = DateTime.UtcNow
            });
            await _context.SaveChangesAsync();
            _logger.LogInformation("Badge automatique '{Title}' débloqué pour {UserId}.", badge.Title, userId);
        }
    }
}