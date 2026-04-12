using backend.Data;
using backend.DTOs;
using backend.Mappers;
using Microsoft.EntityFrameworkCore;

namespace backend.Services
{
    public class XpConfigService
    {
        private readonly ApplicationDbContext _context;

        public XpConfigService(ApplicationDbContext context) => _context = context;

        public async Task<XpConfigDto> GetConfigAsync()
        {
            var config = await _context.XpConfigs.FirstOrDefaultAsync();

            // Si aucune config n'existe, on renvoie des valeurs par défaut
            if (config == null)
                return new XpConfigDto { CommitXp = 10, PrXp = 50, BugFixXp = 100 };

            return config.ToDto();
        }

        public async Task UpdateConfigAsync(XpConfigDto dto)
        {
            var existing = await _context.XpConfigs.FirstOrDefaultAsync();

            if (existing == null)
            {
                _context.XpConfigs.Add(dto.ToEntity());
            }
            else
            {
                existing.CommitXp = dto.CommitXp;
                existing.PrXp = dto.PrXp;
                existing.BugFixXp = dto.BugFixXp;
                existing.UpdatedAt = DateTime.UtcNow;
            }

            await _context.SaveChangesAsync();
        }
    }
}
