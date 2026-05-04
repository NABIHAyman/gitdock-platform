using backend.Data;
using backend.Domain;
using Microsoft.EntityFrameworkCore;

namespace backend.Repositories;

public class TagRepository : ITagRepository
{
    private readonly ApplicationDbContext _context;
    public TagRepository(ApplicationDbContext context) => _context = context;

    public async Task<IEnumerable<Tag>> GetAllAsync()
        => await _context.Tags
            .Where(t => !t.IsDeleted) // NE RÉCUPÉRER QUE CEUX NON SUPPRIMÉS
            .ToListAsync();

    public async Task<Tag?> GetByIdAsync(Guid id) => await _context.Tags.FindAsync(id);

    public async Task AddAsync(Tag tag) => await _context.Tags.AddAsync(tag);

    public void Remove(Tag tag) => _context.Tags.Remove(tag);

    public async Task SaveChangesAsync() => await _context.SaveChangesAsync();
}