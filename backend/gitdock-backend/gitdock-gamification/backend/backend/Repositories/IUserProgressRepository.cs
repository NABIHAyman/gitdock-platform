using backend.Domain;

namespace backend.Repositories;

public interface IUserProgressRepository
{
    Task<UserProgress?> GetByUserIdAsync(long userId);
    Task AddAsync(UserProgress progress);
    Task SaveChangesAsync();
}