namespace backend.Services;
using backend.Domain;
using backend.Data;
using backend.DTOs;
using backend.Mappers; // N'oublie pas l'using !
using Microsoft.EntityFrameworkCore;
using backend.Mappers;
using backend.Repositories;

namespace backend.Services;

public class TagService
public class TagService : ITagService
{
    private readonly ApplicationDbContext _context;
    public TagService(ApplicationDbContext context) => _context = context;
    private readonly ITagRepository _tagRepository;

    public TagService(ITagRepository tagRepository)
    {
        _tagRepository = tagRepository;
    }

    // 1. GET ALL
    public async Task<List<TagResponseDto>> GetAllTagsAsync()
    {
        // Rappel : Le filtre global (!IsDeleted) dans DbContext
        // s'occupe de cacher les tags supprimés automatiquement.
        var tags = await _context.Tags.ToListAsync();
        var tags = await _tagRepository.GetAllAsync();
        return tags.Select(t => t.ToDto()).ToList();
    }

    // 2. CREATE
    public async Task<TagResponseDto> CreateTagAsync(CreateTagDto dto)
    {
        var tag = dto.ToEntity();
        _context.Tags.Add(tag);
        await _context.SaveChangesAsync();
        await _tagRepository.AddAsync(tag);
        await _tagRepository.SaveChangesAsync();
        return tag.ToDto();
    }

    public async Task<TagResponseDto?> UpdateTagAsync(Guid id, CreateTagDto dto)
    {
        var tag = await _context.Tags.FirstOrDefaultAsync(t => t.Id == id && !t.IsDeleted);
        var tag = await _tagRepository.GetByIdAsync(id);
        if (tag == null) return null;

        // SOLID : On délègue la responsabilité de la mise à jour au mapper
        tag.MapUpdate(dto);
        await _tagRepository.SaveChangesAsync();

        await _context.SaveChangesAsync();
        return tag.ToDto();
    }

    // 4. DELETE (Soft Delete)
    public async Task<bool> DeleteTagAsync(Guid id)
    {
        var tag = await _tagRepository.GetByIdAsync(id);
        var tag = await _context.Tags.FindAsync(id);
        if (tag == null) return false;

        tag.IsDeleted = true; // Soft Delete
        await _tagRepository.SaveChangesAsync();
        tag.IsDeleted = true;
        // tag.DeletedAt = DateTime.UtcNow; // Si tu as ajouté ce champ dans ton entité

        await _context.SaveChangesAsync();
        return true;
    }
}