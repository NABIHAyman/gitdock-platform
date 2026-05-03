using backend.Domain;
using backend.DTOs;
using backend.Mappers;
using backend.Repositories;

namespace backend.Services;

public class UserBadgeService : IUserBadgeService
{
    private readonly IUserBadgeRepository _userBadgeRepository;

    public UserBadgeService(IUserBadgeRepository userBadgeRepository)
    {
        _userBadgeRepository = userBadgeRepository;
    }

    public async Task AwardBadgeAsync(long userId, Guid badgeId)
    {
        var alreadyHasBadge = await _userBadgeRepository.HasBadgeAsync(userId, badgeId);

        if (!alreadyHasBadge)
        {
            await _userBadgeRepository.AddAsync(new UserBadge
            {
                Id = Guid.NewGuid(),
                UserId = userId,
                BadgeId = badgeId,
                UnlockedAt = DateTime.UtcNow
            });
            await _userBadgeRepository.SaveChangesAsync();
        }
    }

    public async Task<List<UserBadgeResponseDto>> GetUserBadgesAsync(long userId)
    {
        var userBadges = await _userBadgeRepository.GetByUserIdAsync(userId);
        return UserBadgeMapper.ToDtoList(userBadges);
    }
}