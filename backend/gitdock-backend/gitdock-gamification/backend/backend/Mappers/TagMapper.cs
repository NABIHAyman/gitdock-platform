namespace backend.Mappers;
using backend.Domain;
using backend.DTOs;

public static class TagMapper
{
    public static TagResponseDto ToDto(this Tag tag)
    {
        return new TagResponseDto
        {
            Id = tag.Id,
            Name = tag.Name,
            Color = tag.Color,
            Type = tag.Type
        };
    }

    public static Tag ToEntity(this CreateTagDto dto)
    {
        return new Tag
        {
            Name = dto.Name,
            Color = dto.Color,
            Type = dto.Type
        };
    }
    public static void MapUpdate(this Tag tag, CreateTagDto dto)
    {
        tag.Name = dto.Name;
        tag.Color = dto.Color; // Ajouté pour être complet
        tag.Type = dto.Type;   // Ajouté pour être complet
        tag.UpdatedAt = DateTime.UtcNow;
    }
}