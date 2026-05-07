using MassTransit;
using backend.Messaging.Events;
using backend.Data;
using Microsoft.EntityFrameworkCore;

namespace backend.Messaging.Consumers;

public class BugFixedConsumer : IConsumer<BugFixedEvent>
{
    private readonly ApplicationDbContext _context;

    public BugFixedConsumer(ApplicationDbContext context)
    {
        _context = context;
    }

    public async Task Consume(ConsumeContext<BugFixedEvent> context)
    {
        var userId = context.Message.UserId;

        var xpConfig = await _context.XpConfigs.FirstOrDefaultAsync();
        int xpToAdd = xpConfig?.BugFixXp ?? 100;

        var progress = await _context.UserProgresses
            .FirstOrDefaultAsync(u => u.UserId == userId);

        if (progress != null)
        {
            progress.TotalExperience += xpToAdd;
            progress.UpdatedAt = DateTime.UtcNow;
            await _context.SaveChangesAsync();
            Console.WriteLine($"[GAMIFICATION] +{xpToAdd} XP (BUG FIX) pour l'utilisateur {userId}");
        }
    }
}