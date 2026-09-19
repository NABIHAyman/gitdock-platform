using backend.DTOs;

namespace backend.Services;

public interface ITagService
{
    Task<List<TagResponseDto>> GetAllTagsAsync();
    Task<TagResponseDto> CreateTagAsync(CreateTagDto dto);
    Task<TagResponseDto?> UpdateTagAsync(Guid id, CreateTagDto dto);
    Task<bool> DeleteTagAsync(Guid id);
}