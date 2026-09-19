// ContributorMapper.cs
using backend.Clients;
using backend.Domain;
using backend.DTOs;

namespace backend.Mappers;

public static class ContributorMapper
{
    public static ContributorResponseDto ToDto(
        UserProgress progress,
        UserSummaryDto? userInfo)
    {
        return new ContributorResponseDto
        {
            UserId = progress.UserId,
            FullName = userInfo != null
                ? $"{userInfo.FirstName} {userInfo.LastName}".Trim()
                : $"Utilisateur #{progress.UserId}",
            AvatarUrl = userInfo?.AvatarUrl,
            TotalExperience = progress.TotalExperience,
            CurrentLevel = progress.Level?.LevelRank ?? 0,
            LevelName = progress.Level?.Name ?? "Débutant",
            Badges = MapBadges(progress.UserBadges)
        };
    }

    public static ContributorResponseDto ToDtoFromCollaborator(
        CollaboratorDto collaborator,
        UserProgress? progress,
        UserSummaryDto? userInfo)
    {
        return new ContributorResponseDto
        {
            UserId = collaborator.Id,
            FullName = userInfo != null
                ? $"{userInfo.FirstName} {userInfo.LastName}".Trim()
                : $"{collaborator.FirstName} {collaborator.LastName}".Trim(),
            AvatarUrl = userInfo?.AvatarUrl,
            TotalExperience = progress?.TotalExperience ?? 0,
            CurrentLevel = progress?.Level?.LevelRank ?? 0,
            LevelName = progress?.Level?.Name ?? "Débutant",
            Badges = progress != null ? MapBadges(progress.UserBadges) : new()
        };
    }

    public static ContributorsByProjectDto ToProjectDto(
        CollaboratorsGroupedDto project,
        List<UserProgress> progressions,
        List<UserSummaryDto>? userSummaries)
    {
        var contributors = project.Collaborators
            .Where(c => c.Role == "DEVELOPER")
            .Select(c =>
            {
                var progress = progressions.FirstOrDefault(p => p.UserId == c.Id);
                var userInfo = userSummaries?.FirstOrDefault(u => u.Id == c.Id);
                return ToDtoFromCollaborator(c, progress, userInfo);
            })
            .ToList();

        return new ContributorsByProjectDto
        {
            ProjectId = project.ProjectId,
            ProjectName = project.ProjectName,
            Contributors = contributors
        };
    }

    private static List<ContributorBadgeDto> MapBadges(ICollection<UserBadge> userBadges)
    {
        return userBadges.Select(ub => new ContributorBadgeDto
        {
            BadgeId = ub.BadgeId,
            Title = ub.Badge?.Title ?? string.Empty,
            Icon = ub.Badge?.Icon ?? string.Empty,
            Color = ub.Badge?.Color ?? "#cccccc",
            UnlockedAt = ub.UnlockedAt
        }).ToList();
    }
}