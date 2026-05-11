using backend.Domain;
using backend.DTOs;
using backend.Mappers;
using backend.Repositories;

namespace backend.Services;

public class UserBadgeService : IUserBadgeService
{
    private readonly IUserBadgeRepository _userBadgeRepository;
    private readonly IBadgeService _badgeService;
    private readonly IUserProgressService _userProgressService;

    public UserBadgeService(
        IUserBadgeRepository userBadgeRepository,
        IBadgeService badgeService,
        IUserProgressService userProgressService)
    {
        _userBadgeRepository = userBadgeRepository;
        _badgeService = badgeService;
        _userProgressService = userProgressService;
    }

    public async Task AwardBadgeAsync(long userId, Guid badgeId)
    {
        var alreadyHasBadge = await _userBadgeRepository.HasBadgeAsync(userId, badgeId);

        if (!alreadyHasBadge)
        {
            var badge = await _badgeService.GetByIdAsync(badgeId);

            if (badge != null)
            {
                // ✅ 1. Créer UserProgress d'abord (ajoute 0 XP = crée la ligne si absente)
                await _userProgressService.UpdateUserExperienceAsync(userId, 0);

                // ✅ 2. Maintenant la FK existe, on peut insérer le badge
                await _userBadgeRepository.AddAsync(new UserBadge
                {
                    Id = Guid.NewGuid(),
                    UserId = userId,
                    BadgeId = badgeId,
                    UnlockedAt = DateTime.UtcNow
                });
                await _userBadgeRepository.SaveChangesAsync();

                // ✅ 3. Ajouter les XP du badge
                await _userProgressService.UpdateUserExperienceAsync(userId, badge.Xp);
            }
        }
    }

    public async Task<List<UserBadgeResponseDto>> GetUserBadgesAsync(long userId)
    {
        var userBadges = await _userBadgeRepository.GetByUserIdAsync(userId);
        return UserBadgeMapper.ToDtoList(userBadges);
    }
}