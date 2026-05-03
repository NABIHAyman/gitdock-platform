using backend.Data;
using backend.Domain;
using Microsoft.EntityFrameworkCore;

namespace backend.Repositories;

public class LevelRepository : ILevelRepository
{
    private readonly ApplicationDbContext _context;
    public LevelRepository(ApplicationDbContext context) => _context = context;

    public async Task<IEnumerable<Level>> GetAllWithRequirementsAsync() =>
        await _context.Levels.Include(l => l.LevelTagRequirements).ThenInclude(r => r.Tag).ToListAsync();

    public async Task<Level?> GetByIdWithRequirementsAsync(Guid id) =>
        await _context.Levels.Include(l => l.LevelTagRequirements).FirstOrDefaultAsync(l => l.Id == id);

    public async Task<Level?> GetLevelByXpAsync(int totalXp) =>
        await _context.Levels.Where(l => l.RequiredXP <= totalXp)
                             .OrderByDescending(l => l.LevelRank)
                             .FirstOrDefaultAsync();

    public async Task AddAsync(Level level) => await _context.Levels.AddAsync(level);

    public void RemoveRequirements(IEnumerable<LevelTagRequirement> requirements) =>
        _context.LevelTagRequirements.RemoveRange(requirements);

    public async Task SaveChangesAsync() => await _context.SaveChangesAsync();
    public async Task<Level?> GetLevelByRankAsync(int rank)
    {
        return await _context.Levels
            .FirstOrDefaultAsync(l => l.LevelRank == rank);
    }
}