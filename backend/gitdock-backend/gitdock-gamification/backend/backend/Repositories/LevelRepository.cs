using backend.Data;
using backend.Domain;
using Microsoft.EntityFrameworkCore;

namespace backend.Repositories;

public class LevelRepository : ILevelRepository
{
    private readonly ApplicationDbContext _context;

    public LevelRepository(ApplicationDbContext context) => _context = context;

    public async Task<IEnumerable<Level>> GetAllWithRequirementsAsync()
    {
        return await _context.Levels
            .Include(l => l.LevelTagRequirements)
            .ThenInclude(r => r.Tag)
            .Where(l => !l.IsDeleted)
            .ToListAsync();
    }

    public async Task<Level?> GetByIdWithRequirementsAsync(Guid id)
    {
        // IgnoreQueryFilters pour charger TOUS les requirements (y compris IsDeleted=true)
        // afin de pouvoir les réactiver sans conflit de clé
        return await _context.Levels
            .IgnoreQueryFilters()
            .Include(l => l.LevelTagRequirements)
            .FirstOrDefaultAsync(l => l.Id == id && !l.IsDeleted);
    }

    public async Task<Level?> GetLevelByXpAsync(int totalXp)
    {
        return await _context.Levels
            .Where(l => l.RequiredXP <= totalXp && !l.IsDeleted)
            .OrderByDescending(l => l.LevelRank)
            .FirstOrDefaultAsync();
    }

    public async Task<Level?> GetLevelByRankAsync(int rank)
    {
        return await _context.Levels
            .FirstOrDefaultAsync(l => l.LevelRank == rank && !l.IsDeleted);
    }

    public async Task AddAsync(Level level) => await _context.Levels.AddAsync(level);

    public void Delete(Level level) => _context.Levels.Remove(level);

    public async Task SaveChangesAsync() => await _context.SaveChangesAsync();
    // LevelRepository.cs
    public async Task DeleteRequirementsForLevelAsync(Guid levelId)
    {
        var requirements = await _context.LevelTagRequirements
            .IgnoreQueryFilters()
            .Where(r => r.LevelId == levelId)
            .ToListAsync();
        _context.LevelTagRequirements.RemoveRange(requirements);
    }

    public async Task AddRequirementsAsync(List<LevelTagRequirement> requirements)
    {
        await _context.LevelTagRequirements.AddRangeAsync(requirements);
    }
}