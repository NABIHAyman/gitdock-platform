using backend.Domain;

namespace backend.Repositories;

public interface IBadgeRepository
{
    Task<IEnumerable<Badge>> GetAllActiveAsync();
    Task<Badge?> GetByIdAsync(Guid id);
    Task AddAsync(Badge badge);
    Task SaveChangesAsync();
}