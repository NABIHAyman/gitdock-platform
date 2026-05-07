using backend.Domain;
using backend.DTOs;
using backend.Mappers;
using backend.Repositories;

namespace backend.Services;

public class TagService : ITagService
{
    private readonly ITagRepository _tagRepository;

    public TagService(ITagRepository tagRepository)
    {
        _tagRepository = tagRepository;
    }

    public async Task<List<TagResponseDto>> GetAllTagsAsync()
    {
        var tags = await _tagRepository.GetAllAsync();
        return tags.Select(t => t.ToDto()).ToList();
    }

    public async Task<TagResponseDto> CreateTagAsync(CreateTagDto dto)
    {
        var tag = dto.ToEntity();
        await _tagRepository.AddAsync(tag);
        await _tagRepository.SaveChangesAsync();
        return tag.ToDto();
    }

    public async Task<TagResponseDto?> UpdateTagAsync(Guid id, CreateTagDto dto)
    {
        var tag = await _tagRepository.GetByIdAsync(id);
        if (tag == null) return null;

        tag.MapUpdate(dto);
        await _tagRepository.SaveChangesAsync();
        return tag.ToDto();
    }

    public async Task<bool> DeleteTagAsync(Guid id)
    {
        var tag = await _tagRepository.GetByIdAsync(id);
        if (tag == null) return false;

        tag.IsDeleted = true;
        await _tagRepository.SaveChangesAsync(); // Repository gère le SaveChanges
        return true;
    }
}