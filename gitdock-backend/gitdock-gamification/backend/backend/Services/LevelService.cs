using backend.Domain;
using backend.DTOs;
using backend.Mappers;
using backend.Repositories;

namespace backend.Services;

public class LevelService : ILevelService
{
    private readonly ILevelRepository _levelRepository;

    public LevelService(ILevelRepository levelRepository)
    {
        _levelRepository = levelRepository;
    }

    public async Task<List<LevelResponseDto>> GetAllWithRequirementsAsync()
    {
        var levels = await _levelRepository.GetAllWithRequirementsAsync();
        return levels.Select(l => l.ToDto()).ToList();
    }

    public async Task<LevelResponseDto> CreateLevelAsync(CreateLevelDto dto)
    {
        var level = dto.ToEntity();
        await _levelRepository.AddAsync(level);
        await _levelRepository.SaveChangesAsync();
        return level.ToDto();
    }

    public async Task<bool> UpdateLevelAsync(Guid id, CreateLevelDto updateDto)
    {
        var existingLevel = await _levelRepository.GetByIdWithRequirementsAsync(id);
        if (existingLevel == null) return false;

        // Mise à jour des champs simples
        existingLevel.Name = updateDto.Name;
        existingLevel.LevelRank = updateDto.LevelRank;
        existingLevel.RequiredXP = updateDto.RequiredXP;
        existingLevel.UpdatedAt = DateTime.UtcNow;

        // Gestion des Requirements (on délègue au Repo pour nettoyer les anciens)
        _levelRepository.RemoveRequirements(existingLevel.LevelTagRequirements);

        if (updateDto.Requirements != null)
        {
            foreach (var reqDto in updateDto.Requirements)
            {
                existingLevel.LevelTagRequirements.Add(new LevelTagRequirement
                {
                    LevelId = id,
                    TagId = reqDto.TagId,
                    RequiredOccurrences = reqDto.RequiredOccurrences
                });
            }
        }

        await _levelRepository.SaveChangesAsync();
        return true;
    }

    public async Task<bool> DeleteLevelAsync(Guid id)
    {
        var level = await _levelRepository.GetByIdWithRequirementsAsync(id);
        if (level == null) return false;

        // Soft Delete sur le niveau et ses requirements
        level.IsDeleted = true;
        level.DeletedAt = DateTime.UtcNow;

        foreach (var req in level.LevelTagRequirements)
        {
            req.IsDeleted = true;
        }

        await _levelRepository.SaveChangesAsync();
        return true;
    }
}