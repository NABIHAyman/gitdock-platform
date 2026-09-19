using backend.Domain;

namespace backend.Repositories;

public interface ILevelRepository
{
    Task<IEnumerable<Level>> GetAllWithRequirementsAsync();
    Task<Level?> GetByIdWithRequirementsAsync(Guid id);
    Task<Level?> GetLevelByXpAsync(int totalXp);
    Task<Level?> GetLevelByRankAsync(int rank);
    Task AddAsync(Level level);
    void Delete(Level level); // Ajouté pour résoudre l'erreur CS1061
    Task SaveChangesAsync();
    // ILevelRepository.cs
    Task DeleteRequirementsForLevelAsync(Guid levelId);
    Task AddRequirementsAsync(List<LevelTagRequirement> requirements);
}