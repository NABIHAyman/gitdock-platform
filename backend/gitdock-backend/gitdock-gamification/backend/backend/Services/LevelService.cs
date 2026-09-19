using Microsoft.EntityFrameworkCore;
using backend.Repositories;
using backend.DTOs;
using backend.Domain;
using backend.Mappers;

namespace backend.Services
{
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

        public async Task<LevelResponseDto?> GetByIdAsync(Guid id)
        {
            var level = await _levelRepository.GetByIdWithRequirementsAsync(id);
            return level?.ToDto();
        }

        public async Task<LevelResponseDto> CreateLevelAsync(CreateLevelDto levelDto)
        {
            var level = levelDto.ToEntity();
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

            // Supprimer PHYSIQUEMENT les anciens requirements
            await _levelRepository.DeleteRequirementsForLevelAsync(id);

            // Insérer les nouveaux requirements proprement
            var newRequirements = updateDto.LevelTagRequirements.Select(r => new LevelTagRequirement
            {
                Id = Guid.NewGuid(),
                LevelId = id,
                TagId = r.TagId,
                RequiredOccurrences = r.RequiredOccurrences,
                IsDeleted = false
            }).ToList();

            await _levelRepository.AddRequirementsAsync(newRequirements);
            await _levelRepository.SaveChangesAsync();
            return true;
        }

        public async Task<bool> DeleteLevelAsync(Guid id)
        {
            var level = await _levelRepository.GetByIdWithRequirementsAsync(id);
            if (level == null) return false;

            _levelRepository.Delete(level);
            await _levelRepository.SaveChangesAsync();
            return true;
        }
    }
}