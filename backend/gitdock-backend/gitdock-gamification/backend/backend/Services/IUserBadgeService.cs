namespace backend.Services;
using backend.DTOs;

public interface IUserBadgeService
{
    Task AwardBadgeAsync(long userId, Guid badgeId);
    Task<List<UserBadgeResponseDto>> GetUserBadgesAsync(long userId);
}