namespace backend.Mappers;

using backend.Domain;
using backend.DTOs;
using System;
using System.Collections.Generic;
using System.Linq;

public static class LevelMapper
{
    // =========================
    // ENTITY -> DTO RESPONSE
    // =========================
    public static LevelResponseDto ToDto(this Level level)
    {
        return new LevelResponseDto
        {
            Id = level.Id,
            Name = level.Name,
            LevelRank = level.LevelRank,
            RequiredXP = level.RequiredXP,
            // On mappe vers la liste du DTO de réponse
            LevelTagRequirements = level.LevelTagRequirements != null
                ? level.LevelTagRequirements.Select(r => new AddLevelRequirementDto
                {
                    TagId = r.TagId,
                    RequiredOccurrences = r.RequiredOccurrences
                }).ToList()
                : new List<AddLevelRequirementDto>()
        };
    }

    // =========================
    // CREATE DTO -> ENTITY
    // =========================
    public static Level ToEntity(this CreateLevelDto dto)
    {
        var requirements = new List<LevelTagRequirement>();

        // CORRECTION : On utilise LevelTagRequirements au lieu de Requirements
        if (dto.LevelTagRequirements != null)
        {
            foreach (var r in dto.LevelTagRequirements)
            {
                requirements.Add(new LevelTagRequirement
                {
                    Id = Guid.NewGuid(),
                    TagId = r.TagId,
                    RequiredOccurrences = r.RequiredOccurrences
                });
            }
        }

        return new Level
        {
            Id = Guid.NewGuid(),
            Name = dto.Name,
            LevelRank = dto.LevelRank,
            RequiredXP = dto.RequiredXP,
            LevelTagRequirements = requirements,
            CreatedAt = DateTime.UtcNow,
            IsDeleted = false
        };
    }

    // =========================
    // UPDATE SAFE (MAP)
    // =========================
    public static void MapUpdate(this Level level, CreateLevelDto dto)
    {
        level.Name = dto.Name;
        level.LevelRank = dto.LevelRank;
        level.RequiredXP = dto.RequiredXP;
        level.UpdatedAt = DateTime.UtcNow;

        // On nettoie la collection pour que EF gère le remplacement proprement
        level.LevelTagRequirements ??= new List<LevelTagRequirement>();
        level.LevelTagRequirements.Clear();

        // CORRECTION : On utilise LevelTagRequirements au lieu de Requirements
        if (dto.LevelTagRequirements != null)
        {
            foreach (var req in dto.LevelTagRequirements)
            {
                level.LevelTagRequirements.Add(new LevelTagRequirement
                {
                    Id = Guid.NewGuid(),
                    LevelId = level.Id,
                    TagId = req.TagId,
                    RequiredOccurrences = req.RequiredOccurrences
                });
            }
        }
    }
}