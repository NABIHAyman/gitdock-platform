using backend.Data;
using backend.Domain;
using Microsoft.EntityFrameworkCore;

namespace backend.Repositories;

public class XpConfigRepository : IXpConfigRepository
{
    private readonly ApplicationDbContext _context;
    public XpConfigRepository(ApplicationDbContext context) => _context = context;

    public async Task<XpConfig?> GetCurrentConfigAsync() => await _context.XpConfigs.FirstOrDefaultAsync();

    public async Task UpdateAsync(XpConfig config)
    {
        _context.XpConfigs.Update(config);
        await Task.CompletedTask;
    }

    public async Task SaveChangesAsync() => await _context.SaveChangesAsync();
}