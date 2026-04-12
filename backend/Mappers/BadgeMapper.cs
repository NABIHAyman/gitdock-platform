namespace backend.Mappers;
using backend.Domain;
using backend.DTOs;

public static class BadgeMapper
{
    public static BadgeResponseDto ToDto(this Badge badge)
    {
        return new BadgeResponseDto
        {
            Id = badge.Id,
            Title = badge.Title,
            Description = badge.Description,
            Xp = badge.Xp,
            Icon = badge.Icon,
            Color = badge.Color,
            Type = badge.Type.ToString() // Enum -> String
        };
    }

    public static Badge ToEntity(this CreateBadgeDto dto)
    {
        return new Badge
        {
            Id = Guid.NewGuid(),
            Title = dto.Title,
            Description = dto.Description,
            Xp = dto.Xp,
            Icon = dto.Icon,
            Color = dto.Color,
            Type = Enum.Parse<BadgeType>(dto.Type) // String -> Enum
        };
    }
}