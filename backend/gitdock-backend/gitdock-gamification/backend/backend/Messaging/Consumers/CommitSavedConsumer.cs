using MassTransit;
using backend.Messaging.Events;
using backend.Data;
using backend.Domain; // Assure-toi d'importer tes modèles
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

        // 1. Récupérer la config d'XP
        var xpConfig = await _context.XpConfigs.FirstOrDefaultAsync();
        int xpToAdd = xpConfig?.CommitXp ?? 10;

        // 2. Chercher le profil de l'utilisateur
        var progress = await _context.UserProgresses.FirstOrDefaultAsync(u => u.UserId == userId);

        if (progress != null)
        {
            progress.TotalExperience += xpToAdd;
            progress.UpdatedAt = DateTime.UtcNow;
        }
        else
        {
            // Optionnel : Créer le profil s'il n'existe pas encore
            progress = new UserProgress
            {
                UserId = userId,
                TotalExperience = xpToAdd,
                UpdatedAt = DateTime.UtcNow
            };
            _context.UserProgresses.Add(progress);
        }

        await _context.SaveChangesAsync();
        Console.WriteLine($"[GAMIFICATION] +{xpToAdd} XP ajoutés (COMMIT) pour l'ID {userId}");
    }
}