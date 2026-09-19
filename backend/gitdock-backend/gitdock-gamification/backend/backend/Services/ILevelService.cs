using backend.DTOs;

namespace backend.Services;

public interface ILevelService
{
    Task<List<LevelResponseDto>> GetAllWithRequirementsAsync();
    Task<LevelResponseDto?> GetByIdAsync(Guid id);
    Task<LevelResponseDto> CreateLevelAsync(CreateLevelDto dto);
    Task<bool> UpdateLevelAsync(Guid id, CreateLevelDto updateDto);
    Task<bool> DeleteLevelAsync(Guid id);
}