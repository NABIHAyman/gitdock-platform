using backend.Data;
using backend.Domain;
using Microsoft.EntityFrameworkCore;

namespace backend.Repositories;

public class BadgeRepository : IBadgeRepository
{
    private readonly ApplicationDbContext _context;

    public BadgeRepository(ApplicationDbContext context)
    {
        _context = context;
    }

    public async Task<IEnumerable<Badge>> GetAllActiveAsync()
    {
        // On utilise le filtre global déjà défini dans ton DbContext
        return await _context.Badges.ToListAsync();
    }

    public async Task<Badge?> GetByIdAsync(Guid id)
    {
        return await _context.Badges.FindAsync(id);
    }

    public async Task AddAsync(Badge badge)
    {
        await _context.Badges.AddAsync(badge);
    }

    public async Task SaveChangesAsync()
    {
        await _context.SaveChangesAsync();
    }
}