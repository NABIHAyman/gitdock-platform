using backend.Domain;

namespace backend.Repositories;

public interface IUserBadgeRepository
{
    Task<bool> HasBadgeAsync(long userId, Guid badgeId);
    Task AddAsync(UserBadge userBadge);
    Task<IEnumerable<UserBadge>> GetByUserIdAsync(long userId);
    Task SaveChangesAsync();
}