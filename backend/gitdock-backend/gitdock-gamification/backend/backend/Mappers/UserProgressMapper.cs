using backend.Domain;
using backend.DTOs;
using System.Collections.Generic;
using System.Linq;

namespace backend.Mappers
{
    public static class UserProgressMapper
    {
        public static UserProgressResponseDto ToDto(this UserProgress? entity)
        {
            if (entity == null) return null;

            return new UserProgressResponseDto
            {
                UserId = entity.UserId,
                TotalExperience = entity.TotalExperience,

                // Utilisation de .Level (le nouveau nom) et vérification du null
                CurrentLevelRank = entity.Level?.LevelRank ?? 1,
                LevelName = entity.Level?.Name ?? "Niveau Inconnu",

                // Mappe aussi la liste des badges si ton DTO le permet
                Badges = entity.UserBadges?
                    .Select(ub => ub.Badge.Title)
                    .ToList() ?? new List<string>(),

                // Initialisation de la liste pour éviter les erreurs null sur le Front
                TagProgress = new List<UserTagProgressDto>()
            };
        }
    }
}