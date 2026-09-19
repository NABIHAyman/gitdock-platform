using backend.DTOs;

namespace backend.Services;

public interface IUserBadgeService
{
    Task AwardBadgeAsync(long userId, Guid badgeId);
    Task<List<UserBadgeResponseDto>> GetUserBadgesAsync(long userId);
}