using backend.Clients;
using backend.Data;
using backend.DTOs;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace backend.Controllers;

[ApiController]
[Route("api/[controller]")]
public class ContributorsController : ControllerBase
{
    private readonly ApplicationDbContext _context;
    private readonly IAuthServiceClient _authServiceClient;

    public ContributorsController(
        ApplicationDbContext context,
        IAuthServiceClient authServiceClient)
    {
        _context = context;
        _authServiceClient = authServiceClient;
    }

    // GET /api/contributors
    // Retourne tous les contributeurs avec leur progression et badges
    [HttpGet]
    public async Task<IActionResult> GetAllContributors()
    {
        // 1. Charger toutes les progressions avec leurs badges et levels
        var progressions = await _context.UserProgresses
            .Include(p => p.Level)
            .Include(p => p.UserBadges)
                .ThenInclude(ub => ub.Badge)
            .OrderByDescending(p => p.TotalExperience)
            .ToListAsync();

        if (!progressions.Any())
            return Ok(new List<ContributorResponseDto>());

        // 2. Récupérer les noms depuis le service Auth
        var userIds = progressions.Select(p => p.UserId).ToList();
        var userSummaries = await _authServiceClient.GetUserSummariesAsync(userIds);

        // 3. Construire le DTO final
        var contributors = progressions.Select(p =>
        {
            var userInfo = userSummaries?.FirstOrDefault(u => u.Id == p.UserId);

            return new ContributorResponseDto
            {
                UserId = p.UserId,
                FullName = userInfo != null
                    ? $"{userInfo.FirstName} {userInfo.LastName}".Trim()
                    : $"Utilisateur #{p.UserId}",
                AvatarUrl = userInfo?.AvatarUrl,
                TotalExperience = p.TotalExperience,
                CurrentLevel = p.Level?.LevelRank ?? 1,
                LevelName = p.Level?.Name ?? "Débutant",
                Badges = p.UserBadges.Select(ub => new ContributorBadgeDto
                {
                    BadgeId = ub.BadgeId,
                    Title = ub.Badge?.Title ?? string.Empty,
                    Icon = ub.Badge?.Icon ?? string.Empty,
                    Color = ub.Badge?.Color ?? "#cccccc",
                    UnlockedAt = ub.UnlockedAt
                }).ToList()
            };
        }).ToList();

        return Ok(contributors);
    }
}