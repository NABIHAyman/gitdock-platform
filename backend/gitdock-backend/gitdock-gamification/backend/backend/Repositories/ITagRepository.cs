using backend.Domain;

namespace backend.Repositories;

public interface ITagRepository
{
    Task<IEnumerable<Tag>> GetAllAsync();
    Task<Tag?> GetByIdAsync(Guid id);
    Task AddAsync(Tag tag);
    Task SaveChangesAsync();
    void Remove(Tag tag);
}