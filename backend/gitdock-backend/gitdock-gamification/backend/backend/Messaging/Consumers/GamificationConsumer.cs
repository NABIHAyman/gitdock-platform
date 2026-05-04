using MassTransit;
using backend.Messaging.Events;
using backend.Data;
using Microsoft.EntityFrameworkCore;
using backend.Domain;

namespace backend.Messaging.Consumers;

public class GamificationConsumer :
    IConsumer<CommitSavedEvent>,
    IConsumer<PullRequestMergedEvent>,
    IConsumer<AssignBadgeCommand>
{
    private readonly ApplicationDbContext _context;
    private readonly ILogger<GamificationConsumer> _logger;

    public GamificationConsumer(ApplicationDbContext context, ILogger<GamificationConsumer> logger)
    {
        _context = context;
        _logger = logger;
    }

    // 1. Pour les Commits
    public async Task Consume(ConsumeContext<CommitSavedEvent> context)
    {
        var msg = context.Message;
        if (msg.AuthorUserId <= 0) return;

        var config = await _context.XpConfigs.FirstOrDefaultAsync();
        int xp = config?.CommitXp ?? 10;

        await AddXpAndNotify(msg.AuthorUserId, xp, $"Commit: {msg.Hash}");
        await CheckAndAssignBadge(msg.AuthorUserId, "First Contribution");
    }

    // 2. Pour les PRs
    public async Task Consume(ConsumeContext<PullRequestMergedEvent> context)
    {
        var msg = context.Message;
        if (msg.UserId <= 0) return;

        int xp = msg.Difficulty == "High" ? 50 : 20;
        await AddXpAndNotify(msg.UserId, xp, $"PR Merged: {msg.PrTitle}");
    }

    // 3. Pour les Badges manuels
    public async Task Consume(ConsumeContext<AssignBadgeCommand> context)
    {
        var msg = context.Message;
        if (msg.UserId <= 0) return;

        await EnsureUserExists(msg.UserId);

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
        }
    }

    // --- LOGIQUE COMMUNE ---

    private async Task AddXpAndNotify(long userId, int amount, string reason)
    {
        var profile = await _context.UserProgresses.FirstOrDefaultAsync(p => p.UserId == userId);

        if (profile == null)
        {
            var firstLevel = await _context.Levels.OrderBy(l => l.LevelRank).FirstOrDefaultAsync();
            if (firstLevel == null) return;

            profile = new UserProgress {
                UserId = userId,
                TotalExperience = 0,
                CurrentLevelId = firstLevel.Id,
                UpdatedAt = DateTime.UtcNow
            };
            _context.UserProgresses.Add(profile);
            try { await _context.SaveChangesAsync(); }
            catch { _context.ChangeTracker.Clear(); profile = await _context.UserProgresses.FirstAsync(p => p.UserId == userId); }
        }

        profile.TotalExperience += amount;
        profile.UpdatedAt = DateTime.UtcNow;
        await _context.SaveChangesAsync();
        _logger.LogInformation("+{Amount} XP pour {UserId} ({Reason})", amount, userId, reason);
    }

    private async Task EnsureUserExists(long userId)
    {
        var exists = await _context.UserProgresses.AnyAsync(p => p.UserId == userId);
        if (!exists)
        {
            var firstLevel = await _context.Levels.OrderBy(l => l.LevelRank).FirstOrDefaultAsync();
            if (firstLevel == null) return;

            try {
                _context.UserProgresses.Add(new UserProgress {
                    UserId = userId, TotalExperience = 0, CurrentLevelId = firstLevel.Id, UpdatedAt = DateTime.UtcNow
                });
                await _context.SaveChangesAsync();
            } catch { _context.ChangeTracker.Clear(); }
        }
    }

    private async Task CheckAndAssignBadge(long userId, string badgeTitle)
    {
        var badge = await _context.Badges.IgnoreQueryFilters().FirstOrDefaultAsync(b => b.Title.ToLower() == badgeTitle.ToLower());
        if (badge == null) return;

        await EnsureUserExists(userId);
        if (!await _context.UserBadges.AnyAsync(ub => ub.UserId == userId && ub.BadgeId == badge.Id))
        {
            _context.UserBadges.Add(new UserBadge { Id = Guid.NewGuid(), UserId = userId, BadgeId = badge.Id, UnlockedAt = DateTime.UtcNow });
            await _context.SaveChangesAsync();
        }
    }
}