using backend.Data;
using backend.Domain;
using Microsoft.EntityFrameworkCore;

namespace backend.Repositories;

public class UserProgressRepository : IUserProgressRepository
{
    private readonly ApplicationDbContext _context;

    public UserProgressRepository(ApplicationDbContext context)
    {
        _context = context;
    }

    public async Task<UserProgress?> GetByUserIdAsync(long userId)
    {
        // Remplacement de .CurrentLevel par .Level
        return await _context.UserProgresses
            .Include(p => p.Level) // Correction ici
            .Include(p => p.UserBadges) // Optionnel : utile pour le mapper plus tard
                .ThenInclude(ub => ub.Badge)
            .FirstOrDefaultAsync(p => p.UserId == userId);
    }

    public async Task AddAsync(UserProgress progress)
    {
        await _context.UserProgresses.AddAsync(progress);
    }

    public async Task SaveChangesAsync()
    {
        await _context.SaveChangesAsync();
    }
}