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
            // On utilise FirstOrDefault pour éviter l'exception si la table est vide
            var config = await _context.XpConfigs.FirstOrDefaultAsync();

            // Si la base est vide, on renvoie une configuration par défaut
            if (config == null)
            {
                return new XpConfigDto
                {
                    CommitXp = 10,
                    PrXp = 20,
                    BugFixXp = 15
                };
            }

            // Sinon, on retourne la vraie configuration
            return new XpConfigDto
            {
                CommitXp = config.CommitXp,
                PrXp = config.PrXp,
                BugFixXp = config.BugFixXp
            };
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
