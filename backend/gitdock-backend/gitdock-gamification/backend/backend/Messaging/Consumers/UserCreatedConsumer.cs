using MassTransit;
using backend.Messaging.Events;
using backend.Data;
using backend.Domain;
using Microsoft.EntityFrameworkCore;
using backend.Clients;

namespace backend.Messaging.Consumers;

public class UserCreatedConsumer : IConsumer<UserCreatedEvent>
{
    private readonly IAuthServiceClient _authService;
    private readonly ApplicationDbContext _context;

    public UserCreatedConsumer(IAuthServiceClient authService, ApplicationDbContext context)
    {
        _authService = authService;
        _context = context;
    }

    public async Task Consume(ConsumeContext<UserCreatedEvent> context)
    {
        // On récupère l'ID depuis le message de l'événement
        var userId = context.Message.UserId;

        // --- 1. PHASE DE VÉRIFICATION DE SÉCURITÉ ---
        // On vérifie auprès du service d'authentification que cet utilisateur existe réellement
        bool existsInAuth = await _authService.UserExistsAsync(userId);

        if (!existsInAuth)
        {
            Console.WriteLine($"[SÉCURITÉ] Tentative de création de progrès pour l'ID {userId} qui n'existe pas dans Auth.");
            return;
        }

        // --- 2. VÉRIFICATION D'IDEMPOTENCE ---
        // On évite de créer des doublons si le message est reçu plusieurs fois
        var alreadyExistsLocally = await _context.UserProgresses.AnyAsync(u => u.UserId == userId);

        if (!alreadyExistsLocally)
        {
            // --- 3. RÉCUPÉRATION DU NIVEAU INITIAL ---
            // On cherche le premier niveau (Ordre le plus bas)
            var startingLevel = await _context.Levels
                .OrderBy(l => l.Id)
                .FirstOrDefaultAsync();

            // Si la base est vide (pas de niveaux seedés), on lève une exception
            // Cela permet à MassTransit de retenter plus tard ou d'envoyer en file d'erreur (_error)
            if (startingLevel == null)
            {
                Console.WriteLine("[ERREUR] Problème de configuration : La table 'Levels' est vide.");
                throw new InvalidOperationException("Impossible d'initialiser le progrès : aucun niveau trouvé en base de données.");
            }

            // --- 4. CRÉATION DU PROGRÈS ---
            var progress = new UserProgress
            {
                UserId = userId,
                CurrentLevelId = startingLevel.Id,
                TotalExperience = 0,
                UpdatedAt = DateTime.UtcNow
            };

            _context.UserProgresses.Add(progress);

            try
            {
                await _context.SaveChangesAsync();
                Console.WriteLine($"[OK] Profil de progression créé pour l'utilisateur {userId} (Niveau initial : {startingLevel.Name}).");
            }
            catch (DbUpdateException ex)
            {
                Console.WriteLine($"[ERREUR SQL] Erreur lors de l'insertion en base : {ex.Message}");
                throw; // On propage pour que MassTransit gère l'échec
            }
        }
        else
        {
            Console.WriteLine($"[INFO] L'utilisateur {userId} possède déjà un profil de progression. Ignoré.");
        }
    }
}