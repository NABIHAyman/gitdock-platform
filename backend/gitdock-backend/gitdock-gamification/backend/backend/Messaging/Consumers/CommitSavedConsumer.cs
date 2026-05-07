using MassTransit;
using backend.Messaging.Events;
using backend.Data;
using backend.Domain;
using backend.Enums; // ← AJOUTER CETTE LIGNE
using Microsoft.EntityFrameworkCore;

namespace backend.Messaging.Consumers;

public class CommitSavedConsumer : IConsumer<CommitSavedEvent>
{
    private readonly ApplicationDbContext _context;

    public CommitSavedConsumer(ApplicationDbContext context)
    {
        _context = context;
    }

    public async Task Consume(ConsumeContext<CommitSavedEvent> context)
    {
        var userId = context.Message.AuthorUserId;

        var xpConfig = await _context.XpConfigs.FirstOrDefaultAsync();
        int xpToAdd = xpConfig?.CommitXp ?? 10;

        var progress = await _context.UserProgresses
            .FirstOrDefaultAsync(u => u.UserId == userId);

        if (progress != null)
        {
            progress.TotalExperience += xpToAdd;
            progress.UpdatedAt = DateTime.UtcNow;
        }
        else
        {
            var level1 = await _context.Levels
                .Where(l => !l.IsDeleted)
                .OrderBy(l => l.LevelRank)
                .FirstOrDefaultAsync();

            progress = new UserProgress
            {
                UserId = userId,
                TotalExperience = xpToAdd,
                CurrentLevelId = level1?.Id ?? Guid.Empty,
                UpdatedAt = DateTime.UtcNow
            };
            _context.UserProgresses.Add(progress);
        }

        // ✅ 1. Sauvegarder l'XP D'ABORD
        await _context.SaveChangesAsync();

        // ✅ 2. Vérifier level up APRÈS
        var targetLevel = await _context.Levels
            .Where(l => l.RequiredXP <= progress.TotalExperience && !l.IsDeleted)
            .OrderByDescending(l => l.RequiredXP)
            .FirstOrDefaultAsync();

        if (targetLevel != null && targetLevel.Id != progress.CurrentLevelId)
        {
            progress.CurrentLevelId = targetLevel.Id;
            await _context.SaveChangesAsync();
        }

        // ✅ 3. Vérifier badges AUTO APRÈS avec le bon XP total
        var autoBadges = await _context.Badges
            .Where(b => b.Type == BadgeType.Auto && b.Xp <= progress.TotalExperience)
            .ToListAsync();

        Console.WriteLine($"[DEBUG] XP actuel: {progress.TotalExperience}, Badges AUTO trouvés: {autoBadges.Count}");

        foreach (var badge in autoBadges)
        {
            bool alreadyHas = await _context.UserBadges
                .AnyAsync(ub => ub.UserId == userId && ub.BadgeId == badge.Id);

            if (!alreadyHas)
            {
                _context.UserBadges.Add(new UserBadge
                {
                    Id = Guid.NewGuid(),
                    UserId = userId,
                    BadgeId = badge.Id,
                    UnlockedAt = DateTime.UtcNow
                });
                Console.WriteLine($"[GAMIFICATION] Badge AUTO '{badge.Title}' débloqué pour userId={userId}");
            }
        }

        await _context.SaveChangesAsync();
        Console.WriteLine($"[GAMIFICATION] +{xpToAdd} XP pour userId={userId}, total={progress.TotalExperience}");
    }
}