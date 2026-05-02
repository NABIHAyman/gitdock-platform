using backend.Domain;
using backend.DTOs;
using backend.Repositories;
using backend.Mappers;
using backend.Strategies;

namespace backend.Services;

public class BadgeService : IBadgeService
{
    private readonly IBadgeRepository _repository;
    private readonly IUserBadgeRepository _userBadgeRepository;
    private readonly BadgeStrategyFactory _factory;

    public BadgeService(IBadgeRepository repository, IUserBadgeRepository userBadgeRepository, BadgeStrategyFactory factory)
    {
        _repository = repository;
        _userBadgeRepository = userBadgeRepository;
        _factory = factory;
    }

    // --- Gestion du Catalogue ---

    public async Task<List<BadgeResponseDto>> GetAllBadgesAsync()
    {
        // Utilisation de ton nom exact : GetAllActiveAsync
        var badges = await _repository.GetAllActiveAsync();
        return badges.Select(b => b.ToDto()).ToList();
    }

    public async Task<BadgeResponseDto> CreateBadgeAsync(CreateBadgeDto dto)
    {
        var badge = dto.ToEntity();
        await _repository.AddAsync(badge);
        await _repository.SaveChangesAsync(); // Indispensable pour ton Repo
        return badge.ToDto();
    }

    public async Task<bool> UpdateBadgeAsync(Guid id, CreateBadgeDto dto)
    {
        var badge = await _repository.GetByIdAsync(id);
        if (badge == null) return false;

        // Mise à jour des propriétés (Title, Xp, etc.)
        badge.Title = dto.Title;
        badge.Description = dto.Description;
        badge.Xp = dto.Xp;
        badge.Icon = dto.Icon;
        badge.Color = dto.Color;
        badge.Type = dto.Type;
        badge.UpdatedAt = DateTime.UtcNow;

        // Avec ton Repo, on sauve juste les changements suivis par l'Entity Manager
        await _repository.SaveChangesAsync();
        return true;
    }

    public async Task<bool> DeleteBadgeAsync(Guid id)
    {
        var badge = await _repository.GetByIdAsync(id);
        if (badge == null) return false;

        // Pour le Delete, on utilise souvent un Soft Delete (DeletedAt)
        badge.DeletedAt = DateTime.UtcNow;

        await _repository.SaveChangesAsync();
        return true;
    }

    // --- Logique d'attribution automatique ---

    public async Task CheckAndAwardBadgesAsync(UserProgress progress)
    {
        var allBadges = await _repository.GetAllActiveAsync();
        var userBadges = await _userBadgeRepository.GetByUserIdAsync(progress.UserId);
        var ownedIds = userBadges.Select(ub => ub.BadgeId).ToHashSet();

        foreach (var badge in allBadges)
        {
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