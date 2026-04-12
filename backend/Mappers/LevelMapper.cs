namespace backend.Mappers;
using backend.Domain;
using backend.DTOs;

public static class LevelMapper
{
    public static LevelResponseDto ToDto(this Level level)
    {
        return new LevelResponseDto
        {
            Id = level.Id,
            Name = level.Name,
            LevelRank = level.LevelRank,
            RequiredXP = level.RequiredXP,
            // On réutilise directement AddLevelRequirementDto pour ne pas créer 50 fichiers
            LevelTagRequirements = level.LevelTagRequirements?.Select(r => new AddLevelRequirementDto
            {
                TagId = r.TagId,
                RequiredOccurrences = r.RequiredOccurrences
            }).ToList() ?? new List<AddLevelRequirementDto>()
        };
    }
    public static Level ToEntity(this CreateLevelDto dto)
    {
        var level = new Level
        {
            Id = Guid.NewGuid(),
            Name = dto.Name,
            LevelRank = dto.LevelRank,
            RequiredXP = dto.RequiredXP,
            // On mappe la liste des requirements reçue du Manager
            LevelTagRequirements = dto.Requirements.Select(r => new LevelTagRequirement
            {
                TagId = r.TagId,
                RequiredOccurrences = r.RequiredOccurrences
                // Le LevelId sera lié automatiquement par Entity Framework
            }).ToList()
        };
        return level;
    }
    public static void MapUpdate(this Level level, CreateLevelDto dto)
    {
        level.Name = dto.Name;
        level.LevelRank = dto.LevelRank;
        level.RequiredXP = dto.RequiredXP;
        level.UpdatedAt = DateTime.UtcNow;

        // Mise à jour des Requirements : on remplace l'ancienne liste
        level.LevelTagRequirements.Clear();
        foreach (var req in dto.Requirements)
        {
            level.LevelTagRequirements.Add(new LevelTagRequirement
            {
                TagId = req.TagId,
                RequiredOccurrences = req.RequiredOccurrences
            });
        }
    }
}