using backend.Domain;
using backend.DTOs;

namespace backend.Mappers;

public static class UserBadgeMapper
{
    /// <summary>
    /// Transforme une entité UserBadge (la relation) en DTO pour le Front-end.
    /// </summary>
    public static UserBadgeResponseDto ToDto(UserBadge userBadge)
    {
        if (userBadge == null) return null;

        // Sécurité : Si la relation .Badge n'a pas été chargée via .Include() dans le Repo
        if (userBadge.Badge == null)
        {
            return new UserBadgeResponseDto
            {
                BadgeId = userBadge.BadgeId,
                UnlockedAt = userBadge.UnlockedAt,
                Title = "Badge non chargé",
                Description = "Les détails du badge manquent.",
                Icon = "default-icon",
                Color = "#CCCCCC"
            };
        }

        return new UserBadgeResponseDto
        {
            BadgeId = userBadge.BadgeId,
            Title = userBadge.Badge.Title,
            Description = userBadge.Badge.Description,
            XpReward = userBadge.Badge.Xp, // Assure-toi que le DTO a bien ce nom de propriété
            Icon = userBadge.Badge.Icon,
            Color = userBadge.Badge.Color,
            UnlockedAt = userBadge.UnlockedAt
        };
    }

    /// <summary>
    /// Pour transformer une liste complète d'entités en liste de DTOs
    /// </summary>
    public static List<UserBadgeResponseDto> ToDtoList(IEnumerable<UserBadge> userBadges)
    {
        if (userBadges == null) return new List<UserBadgeResponseDto>();

        return userBadges.Select(ub => ToDto(ub)).ToList();
    }
}