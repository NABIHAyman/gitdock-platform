// ContributorRepository.cs
using backend.Data;
using backend.Domain;
using Microsoft.EntityFrameworkCore;

namespace backend.Repositories;

public class ContributorRepository : IContributorRepository
{
    private readonly ApplicationDbContext _context;

    public ContributorRepository(ApplicationDbContext context)
    {
        _context = context;
    }

    public async Task<List<UserProgress>> GetAllWithDetailsAsync()
    {
        return await _context.UserProgresses
            .Include(p => p.Level)
            .Include(p => p.UserBadges).ThenInclude(ub => ub.Badge)
            .OrderByDescending(p => p.TotalExperience)
            .ToListAsync();
    }

    public async Task<List<UserProgress>> GetByUserIdsAsync(List<long> userIds)
    {
        return await _context.UserProgresses
            .Include(p => p.Level)
            .Include(p => p.UserBadges).ThenInclude(ub => ub.Badge)
            .Where(p => userIds.Contains(p.UserId))
            .ToListAsync();
    }
}