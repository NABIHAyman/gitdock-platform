using backend.Domain;

namespace backend.Repositories;

public interface IUserTagProgressRepository
{
    Task<UserTagProgress?> GetAsync(long userId, Guid tagId);
    Task AddAsync(UserTagProgress progress);
    Task<IEnumerable<UserTagProgress>> GetUserAllProgressAsync(long userId);
    Task SaveChangesAsync();
}