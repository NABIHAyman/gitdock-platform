using backend.Domain;
using backend.DTOs;

namespace backend.Mappers;

public static class BadgeMapper
{
    public static BadgeResponseDto ToDto(this Badge badge)
    {
        if (badge == null) return null!;
        return new BadgeResponseDto
        {
            Id = badge.Id,
            Title = badge.Title, // On utilise Title comme dans ton Domain
            Description = badge.Description,
            Xp = badge.Xp,
            Icon = badge.Icon,
            Color = badge.Color,
            Type = badge.Type
        };
    }

    public static Badge ToEntity(this CreateBadgeDto dto)
    {
        if (dto == null) return null!;
        return new Badge
        {
            Title = dto.Title, // On utilise Title comme dans ton Domain
            Description = dto.Description,
            Xp = dto.Xp,
            Icon = dto.Icon,
            Color = dto.Color,
            Type = dto.Type
        };
    }
}