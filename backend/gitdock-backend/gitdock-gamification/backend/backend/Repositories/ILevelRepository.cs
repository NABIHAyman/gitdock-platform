using backend.Domain;

namespace backend.Repositories;

public interface ILevelRepository
{
    Task<IEnumerable<Level>> GetAllWithRequirementsAsync();
    Task<Level?> GetByIdWithRequirementsAsync(Guid id);
    Task<Level?> GetLevelByXpAsync(int totalXp);
    Task AddAsync(Level level);
    Task SaveChangesAsync();
    void RemoveRequirements(IEnumerable<LevelTagRequirement> requirements);
    Task<Level?> GetLevelByRankAsync(int rank);
}