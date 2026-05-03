using backend.Data;
using backend.Domain;
using backend.DTOs;
using backend.Repositories;
using backend.Mappers;
using backend.Strategies;
using Microsoft.EntityFrameworkCore;

namespace backend.Services;

public class BadgeService
public class BadgeService : IBadgeService
{
    private readonly IBadgeRepository _repository;
    private readonly IUserBadgeRepository _userBadgeRepository;
    private readonly BadgeStrategyFactory _factory;
    private readonly ApplicationDbContext _context;

    public BadgeService(IBadgeRepository repository, IUserBadgeRepository userBadgeRepository, BadgeStrategyFactory factory)
    public BadgeService(ApplicationDbContext context)
    {
        _repository = repository;
        _userBadgeRepository = userBadgeRepository;
        _factory = factory;
        _context = context;
    }

    // --- Gestion du Catalogue ---

    // 1. GET ALL
    public async Task<List<BadgeResponseDto>> GetAllBadgesAsync()
    {
        var badges = await _context.Badges.ToListAsync();
        return badges.Select(b => MapToDto(b)).ToList();
        // Utilisation de ton nom exact : GetAllActiveAsync
        var badges = await _repository.GetAllActiveAsync();
        return badges.Select(b => b.ToDto()).ToList();
    }

    // 2. CREATE (Logique manquante précédemment)
    public async Task<BadgeResponseDto> CreateBadgeAsync(CreateBadgeDto dto)
    {
        var badge = new Badge
        {
            Id = Guid.NewGuid(),
            Title = dto.Title,
            Description = dto.Description,
            Xp = dto.Xp,
            Icon = dto.Icon,
            Color = dto.Color,
            // Conversion du string du Front vers l'Enum du Back
            Type = Enum.Parse<BadgeType>(dto.Type, true)
        };

        _context.Badges.Add(badge);
        await _context.SaveChangesAsync();

        return MapToDto(badge);
        var badge = dto.ToEntity();
        await _repository.AddAsync(badge);
        await _repository.SaveChangesAsync(); // Indispensable pour ton Repo
        return badge.ToDto();
    }

    // 3. DELETE (Soft Delete logique)
    public async Task<bool> DeleteBadgeAsync(Guid id)
    {
        var badge = await _context.Badges.FindAsync(id);
        if (badge == null) return false;

        badge.DeletedAt = DateTime.UtcNow; // On marque comme supprimé
        await _context.SaveChangesAsync();
        return true;
    }
    public async Task<bool> UpdateBadgeAsync(Guid id, CreateBadgeDto dto)
    {
        var badge = await _repository.GetByIdAsync(id);
        var badge = await _context.Badges.FindAsync(id);

        if (badge == null) return false;

        // Mise à jour des propriétés (Title, Xp, etc.)
        // Mise à jour des propriétés
        badge.Title = dto.Title;
        badge.Description = dto.Description;
        badge.Xp = dto.Xp;
        badge.Xp = dto.Xp; // Le mapping correct
        badge.Icon = dto.Icon;
        badge.Color = dto.Color;
        badge.Type = dto.Type;
        badge.UpdatedAt = DateTime.UtcNow;

        // Avec ton Repo, on sauve juste les changements suivis par l'Entity Manager
        await _repository.SaveChangesAsync();
        return true;
    }
        badge.Type = Enum.Parse<BadgeType>(dto.Type, true);
        badge.UpdatedAt = DateTime.UtcNow; // Optionnel : pour ton log de suivi

        await _context.SaveChangesAsync();
    public async Task<bool> DeleteBadgeAsync(Guid id)
    {
        var badge = await _repository.GetByIdAsync(id);
        if (badge == null) return false;

        // Pour le Delete, on utilise souvent un Soft Delete (DeletedAt)
        badge.DeletedAt = DateTime.UtcNow;

        await _repository.SaveChangesAsync();
        return true;
    }
    // Méthode d'aide pour respecter le principe DRY (Don't Repeat Yourself)
    private BadgeResponseDto MapToDto(Badge b)

    // --- Logique d'attribution automatique ---

    public async Task CheckAndAwardBadgesAsync(UserProgress progress)
    {
        return new BadgeResponseDto
        var allBadges = await _repository.GetAllActiveAsync();
        var userBadges = await _userBadgeRepository.GetByUserIdAsync(progress.UserId);
        var ownedIds = userBadges.Select(ub => ub.BadgeId).ToHashSet();

        foreach (var badge in allBadges)
        {
            Id = b.Id,
            Title = b.Title,
            Description = b.Description,
            Xp = b.Xp,
            Icon = b.Icon,
            Color = b.Color,
            Type = b.Type.ToString()
        };
            if (ownedIds.Contains(badge.Id)) continue;

            var strategy = _factory.GetStrategy(badge.Type);
            if (strategy.IsEligible(progress, badge))
            {
                await _userBadgeRepository.AddAsync(new UserBadge
                {
                    UserId = progress.UserId,
                    BadgeId = badge.Id,
                    UnlockedAt = DateTime.UtcNow
                });
            }
        }

        // On n'oublie pas de sauver les nouveaux badges attribués !
        await _userBadgeRepository.SaveChangesAsync();
    }
}