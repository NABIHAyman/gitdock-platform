using MassTransit;
using backend.Messaging.Events;
using backend.Data;
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
        var userId = context.Message.UserId;

        // 1. Récupérer la config d'XP depuis la base (Capture d'écran 2026-04-30 104224.png)
        var xpConfig = await _context.XpConfigs.FirstOrDefaultAsync();
        int xpToAdd = xpConfig?.CommitXp ?? 10; // Utilise 10 si la table est vide

        // 2. Chercher le profil de l'utilisateur
        var progress = await _context.UserProgresses.FirstOrDefaultAsync(u => u.UserId == userId);

        if (progress != null)
        {
            progress.TotalExperience += xpToAdd;
            progress.UpdatedAt = DateTime.UtcNow;
            await _context.SaveChangesAsync();
            Console.WriteLine($"[GAMIFICATION] +{xpToAdd} XP ajoutés (COMMIT) pour l'ID {userId}");
        }
    }
}