using backend.Data;
using backend.Domain;
using Microsoft.EntityFrameworkCore;

namespace backend.Repositories;

public class UserBadgeRepository : IUserBadgeRepository
{
    private readonly ApplicationDbContext _context;
    public UserBadgeRepository(ApplicationDbContext context) => _context = context;

    public async Task<bool> HasBadgeAsync(long userId, Guid badgeId) =>
        await _context.UserBadges.AnyAsync(ub => ub.UserId == userId && ub.BadgeId == badgeId);

    public async Task AddAsync(UserBadge userBadge) => await _context.UserBadges.AddAsync(userBadge);

    public async Task<IEnumerable<UserBadge>> GetByUserIdAsync(long userId) =>
        await _context.UserBadges
            .Include(ub => ub.Badge) // Crucial pour le Mapper
            .Where(ub => ub.UserId == userId)
            .ToListAsync();

    public async Task SaveChangesAsync() => await _context.SaveChangesAsync();
}