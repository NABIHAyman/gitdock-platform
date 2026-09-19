using MassTransit;
using backend.Messaging.Events;
using backend.Data;
using Microsoft.EntityFrameworkCore;

namespace backend.Messaging.Consumers;

public class PullRequestMergedConsumer : IConsumer<PullRequestMergedEvent>
{
    private readonly ApplicationDbContext _context;

    public PullRequestMergedConsumer(ApplicationDbContext context)
    {
        _context = context;
    }

    public async Task Consume(ConsumeContext<PullRequestMergedEvent> context)
    {
        var userId = context.Message.UserId;

        var xpConfig = await _context.XpConfigs.FirstOrDefaultAsync();
        int xpToAdd = xpConfig?.PrXp ?? 50;

        var progress = await _context.UserProgresses
            .FirstOrDefaultAsync(u => u.UserId == userId);

        if (progress != null)
        {
            progress.TotalExperience += xpToAdd;
            progress.UpdatedAt = DateTime.UtcNow;
            await _context.SaveChangesAsync();
            Console.WriteLine($"[GAMIFICATION] +{xpToAdd} XP (PR) pour l'utilisateur {userId}");
        }
    }
}