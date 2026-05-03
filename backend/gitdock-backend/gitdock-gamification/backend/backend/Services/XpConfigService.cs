using backend.Data;
using backend.DTOs;
using backend.Mappers;
using Microsoft.EntityFrameworkCore;
using backend.Repositories;
using backend.Domain;

namespace backend.Services
{
    public class XpConfigService
    {
        private readonly ApplicationDbContext _context;
public class XpConfigService
{
    private readonly IXpConfigRepository _xpConfigRepository;

    public XpConfigService(IXpConfigRepository xpConfigRepository)
    {
        _xpConfigRepository = xpConfigRepository;
    }

    public async Task<XpConfigDto> GetConfigAsync()
    {
        var config = await _xpConfigRepository.GetCurrentConfigAsync();

        // Valeurs par défaut si la table est vide
        if (config == null) return new XpConfigDto { CommitXp = 10, PrXp = 20, BugFixXp = 15 };

        return config.ToDto();
    }

    public async Task UpdateConfigAsync(XpConfigDto dto)
    {
        var existing = await _xpConfigRepository.GetCurrentConfigAsync();

        if (existing == null)
        {
            await _xpConfigRepository.UpdateAsync(dto.ToEntity());
        }
        else
        {
            existing.CommitXp = dto.CommitXp;
            existing.PrXp = dto.PrXp;
            existing.BugFixXp = dto.BugFixXp;
            existing.UpdatedAt = DateTime.UtcNow;
        }

        await _xpConfigRepository.SaveChangesAsync();
    }
}