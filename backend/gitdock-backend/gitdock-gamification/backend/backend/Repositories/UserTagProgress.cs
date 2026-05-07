using backend.Data;
using backend.Domain;
using Microsoft.EntityFrameworkCore;

namespace backend.Repositories;

public class UserTagProgressRepository : IUserTagProgressRepository
{
    private readonly ApplicationDbContext _context;
    public UserTagProgressRepository(ApplicationDbContext context) => _context = context;

    public async Task<UserTagProgress?> GetAsync(long userId, Guid tagId) =>
        await _context.UserTagProgresses.FirstOrDefaultAsync(p => p.UserId == userId && p.TagId == tagId);

    public async Task AddAsync(UserTagProgress progress) => await _context.UserTagProgresses.AddAsync(progress);

    public async Task<IEnumerable<UserTagProgress>> GetUserAllProgressAsync(long userId) =>
        await _context.UserTagProgresses.Include(p => p.Tag).Where(p => p.UserId == userId).ToListAsync();

    public async Task SaveChangesAsync() => await _context.SaveChangesAsync();
}