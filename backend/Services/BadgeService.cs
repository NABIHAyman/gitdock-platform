using backend.Data;
using backend.Domain;
using backend.DTOs;
using Microsoft.EntityFrameworkCore;

namespace backend.Services;

public class BadgeService
{
    private readonly ApplicationDbContext _context;

    public BadgeService(ApplicationDbContext context)
    {
        _context = context;
    }

    // 1. GET ALL
    public async Task<List<BadgeResponseDto>> GetAllBadgesAsync()
    {
        var badges = await _context.Badges.ToListAsync();
        return badges.Select(b => MapToDto(b)).ToList();
    }

    // 2. CREATE (Logique manquante précédemment)
    public async Task<BadgeResponseDto> CreateBadgeAsync(CreateBadgeDto dto)
    {
        var badge = new Badge
        {
            Id = Guid.NewGuid(),
            Title = dto.Title,
            Description = dto.Description,
            Xp = dto.Xp,
            Icon = dto.Icon,
            Color = dto.Color,
            // Conversion du string du Front vers l'Enum du Back
            Type = Enum.Parse<BadgeType>(dto.Type)
        };

        _context.Badges.Add(badge);
        await _context.SaveChangesAsync();

        return MapToDto(badge);
    }

    // 3. DELETE (Soft Delete logique)
    public async Task<bool> DeleteBadgeAsync(Guid id)
    {
        var badge = await _context.Badges.FindAsync(id);
        if (badge == null) return false;

        badge.DeletedAt = DateTime.UtcNow; // On marque comme supprimé
        await _context.SaveChangesAsync();
        return true;
    }
    public async Task<bool> UpdateBadgeAsync(Guid id, CreateBadgeDto dto)
    {
        var badge = await _context.Badges.FindAsync(id);

        if (badge == null) return false;

        // Mise à jour des propriétés
        badge.Title = dto.Title;
        badge.Description = dto.Description;
        badge.Xp = dto.Xp; // Le mapping correct
        badge.Icon = dto.Icon;
        badge.Color = dto.Color;
        badge.Type = Enum.Parse<BadgeType>(dto.Type);
        badge.UpdatedAt = DateTime.UtcNow; // Optionnel : pour ton log de suivi

        await _context.SaveChangesAsync();
        return true;
    }
    // Méthode d'aide pour respecter le principe DRY (Don't Repeat Yourself)
    private BadgeResponseDto MapToDto(Badge b)
    {
        return new BadgeResponseDto
        {
            Id = b.Id,
            Title = b.Title,
            Description = b.Description,
            Xp = b.Xp,
            Icon = b.Icon,
            Color = b.Color,
            Type = b.Type.ToString()
        };
    }
}