// CollaboratorAddedConsumer.cs
using MassTransit;
using backend.Messaging.Events;
using backend.Data;
using backend.Domain;
using Microsoft.EntityFrameworkCore;

namespace backend.Messaging.Consumers;

public class CollaboratorAddedConsumer : IConsumer<CollaboratorAddedEvent>
{
    private readonly ApplicationDbContext _context;

    public CollaboratorAddedConsumer(ApplicationDbContext context)
    {
        _context = context;
    }

    public async Task Consume(ConsumeContext<CollaboratorAddedEvent> context)
    {
        var userId = context.Message.UserId;
        var projectId = context.Message.ProjectId;

        // Vérifier si UserProgress existe déjà
        var existing = await _context.UserProgresses
            .FirstOrDefaultAsync(u => u.UserId == userId);

        if (existing == null)
        {
            // Récupérer le level 1
            // ✅ Level 1 = celui avec le plus petit LevelRank
            var level1 = await _context.Levels
                .Where(l => !l.IsDeleted)
                .OrderBy(l => l.LevelRank)
                .FirstOrDefaultAsync();

            var progress = new UserProgress
            {
                UserId = userId,
                TotalExperience = 0,
                CurrentLevelId = level1?.Id ?? Guid.Empty,
                UpdatedAt = DateTime.UtcNow
            };

            _context.UserProgresses.Add(progress);
            await _context.SaveChangesAsync();

            Console.WriteLine($"[GAMIFICATION] UserProgress initialisé pour userId={userId}");
        }
        else
        {
            Console.WriteLine($"[GAMIFICATION] UserProgress déjà existant pour userId={userId}, skip.");
        }
    }
}