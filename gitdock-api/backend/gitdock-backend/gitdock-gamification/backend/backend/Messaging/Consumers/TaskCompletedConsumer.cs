using MassTransit;
using backend.Messaging.Events;
using backend.Data;
using Microsoft.EntityFrameworkCore;
using backend.Domain;

namespace backend.Messaging.Consumers;

public class TaskCompletedConsumer : IConsumer<TaskCompletedEvent>
{
    private readonly ApplicationDbContext _context;

    public TaskCompletedConsumer(ApplicationDbContext context)
    {
        _context = context;
    }

    public async Task Consume(ConsumeContext<TaskCompletedEvent> context)
    {
        var msg = context.Message;
        if (msg.UserId <= 0) return;

        var profile = await _context.UserProgresses.FirstOrDefaultAsync(p => p.UserId == msg.UserId);

        if (profile != null)
        {
            profile.TotalExperience += msg.XpReward;
            profile.UpdatedAt = DateTime.UtcNow;
            await _context.SaveChangesAsync();
            Console.WriteLine($"[GAMIFICATION] +{msg.XpReward} XP (TASK DONE) pour l'utilisateur {msg.UserId}");
        }
    }
}