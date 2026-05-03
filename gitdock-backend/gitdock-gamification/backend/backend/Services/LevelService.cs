namespace backend.Services;

using backend.Domain;
using backend.Data;
using backend.DTOs;
using backend.Mappers; // INDISPENSABLE pour utiliser .ToDto() et .ToEntity()
using Microsoft.EntityFrameworkCore;

public class LevelService
{
    private readonly ApplicationDbContext _context;

    public LevelService(ApplicationDbContext context) => _context = context;

    // 1. GET ALL (avec Mapping de sortie)
    public async Task<List<LevelResponseDto>> GetAllWithRequirementsAsync()
    {
        var levels = await _context.Levels
            .Include(l => l.LevelTagRequirements)
            .ThenInclude(r => r.Tag)
            .ToListAsync();

        // On transforme chaque Level en LevelResponseDto
        return levels.Select(l => l.ToDto()).ToList();
    }
    // 2. CREATE (avec Mapping d'entrée et de sortie)
    public async Task<LevelResponseDto> CreateLevelAsync(CreateLevelDto dto)
    {
        // Utilise ton nouveau ToEntity() dans le Mapper
        var level = dto.ToEntity();

        _context.Levels.Add(level);
        await _context.SaveChangesAsync();

        // Renvoie le DTO pour confirmer au Front
        return level.ToDto();
    }
    // Dans LevelService.cs
    public async Task<bool> UpdateLevelAsync(Guid id, CreateLevelDto updateDto)
    {
        try
        {
            // 1. Récupérer le niveau avec ses relations
            var existingLevel = await _context.Levels
                .Include(l => l.LevelTagRequirements)
                .FirstOrDefaultAsync(l => l.Id == id);

            if (existingLevel == null)
            {
                Console.WriteLine($"DEBUG: Level avec ID {id} non trouvé en DB !");
                return false;
            }

            // 2. Mise à jour des champs simples
            existingLevel.Name = updateDto.Name;
            existingLevel.LevelRank = updateDto.LevelRank;
            existingLevel.RequiredXP = updateDto.RequiredXP;
            existingLevel.UpdatedAt = DateTime.UtcNow;

            // 3. Mise à jour des Requirements (Table de jointure)
            // On supprime les anciens
            _context.LevelTagRequirements.RemoveRange(existingLevel.LevelTagRequirements);

            // On ajoute les nouveaux
            if (updateDto.Requirements != null)
            {
                foreach (var reqDto in updateDto.Requirements)
                {
                    _context.LevelTagRequirements.Add(new LevelTagRequirement
                    {
                        LevelId = id,
                        TagId = reqDto.TagId,
                        RequiredOccurrences = reqDto.RequiredOccurrences
                    });
                }
            }

            // 4. Sauvegarde
            await _context.SaveChangesAsync();
            Console.WriteLine("DEBUG: Update réussi en base de données !");
            return true;
        }
        catch (Exception ex)
        {
            // C'est ici qu'on verra l'erreur de clé étrangère si un TagId est mauvais
            Console.WriteLine("ERREUR DB: " + ex.Message);
            if (ex.InnerException != null)
                Console.WriteLine("INNER: " + ex.InnerException.Message);
            return false;
        }
    }
    public async Task<bool> DeleteLevelAsync(Guid id)
    {
        var level = await _context.Levels
            .Include(l => l.LevelTagRequirements)
            .FirstOrDefaultAsync(l => l.Id == id);

        if (level == null) return false;

        // Mettre IsDeleted à true pour chaque requirement
        foreach (var req in level.LevelTagRequirements)
        {
            req.IsDeleted = true; // À condition que cette colonne existe dans cette table
        }

        level.IsDeleted = true;
        level.DeletedAt = DateTime.UtcNow;

        await _context.SaveChangesAsync();
        return true;
    }
}