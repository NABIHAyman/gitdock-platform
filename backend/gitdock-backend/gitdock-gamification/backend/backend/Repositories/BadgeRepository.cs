using backend.Data;
using backend.Domain;
using backend.Enums;
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
    // BadgeRepository.cs — ajoute cette méthode
    public async Task<List<Badge>> GetAllAutoBadgesAsync()
    {
        return await _context.Badges
            .Where(b => b.DeletedAt == null && b.Type == BadgeType.Auto)
            .ToListAsync();
    }
}