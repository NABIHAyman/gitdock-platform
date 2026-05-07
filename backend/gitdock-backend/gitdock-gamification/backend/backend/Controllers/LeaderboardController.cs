using Microsoft.AspNetCore.Mvc;
using backend.Data;
using backend.Clients;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Caching.Distributed;
using System.Text.Json;
using System.Linq;

namespace backend.Controllers;

[ApiController]
[Route("api/[controller]")]
public class LeaderboardController : ControllerBase
{
    private readonly ApplicationDbContext _context;
    private readonly IAuthServiceClient _authServiceClient;
    private readonly IDistributedCache _cache;
    private const string LeaderboardCacheKey = "leaderboard_top_players";

    public LeaderboardController(
        ApplicationDbContext context,
        IAuthServiceClient authServiceClient,
        IDistributedCache cache) // Injection de Redis
    {
        _context = context;
        _authServiceClient = authServiceClient;
        _cache = cache;
    }

    [HttpGet]
    public async Task<IActionResult> GetTopPlayers([FromQuery] int count = 10)
    {
        // 1. Tenter de récupérer le leaderboard depuis Redis
        var cachedLeaderboard = await _cache.GetStringAsync(LeaderboardCacheKey);

        if (!string.IsNullOrEmpty(cachedLeaderboard))
        {
            // Si trouvé, on le retourne immédiatement (Super rapide !)
            var data = JsonSerializer.Deserialize<List<object>>(cachedLeaderboard);
            return Ok(data);
        }

        // 2. Si non trouvé en cache, on fait le travail lourd en base de données
        var topProgressions = await _context.UserProgresses
            .Include(p => p.Level)
            .Include(p => p.UserBadges)
                .ThenInclude(ub => ub.Badge)
            .Include(p => p.UserTagProgresses)
                .ThenInclude(utp => utp.Tag)
            .OrderByDescending(p => p.TotalExperience)
            .Take(count)
            .ToListAsync();

        if (!topProgressions.Any()) return Ok(new List<object>());

        // 3. Appel au service Auth pour récupérer les noms réels
        var userIds = topProgressions.Select(p => p.UserId).ToList();
        var userNames = await _authServiceClient.GetUserSummariesAsync(userIds);

        // 4. Construction de l'objet final
        var leaderboard = topProgressions.Select((p, index) => {
            var details = userNames?.FirstOrDefault(u => u.Id == p.UserId);

            return new {
                Rank = index + 1,
                UserId = p.UserId,
                FullName = details != null ? $"{details.FirstName} {details.LastName}" : "Utilisateur Inconnu",
                XP = p.TotalExperience,
                LevelLabel = p.Level?.Name ?? "Niveau Inconnu",
                Badges = p.UserBadges.Select(ub => ub.Badge.Title).ToList(),
                TopSkills = p.UserTagProgresses
                    .OrderByDescending(tp => tp.Occurrences)
                    .Take(3)
                    .Select(tp => new {
                        TagName = tp.Tag.Name,
                        Count = tp.Occurrences
                    })
                    .ToList()
            };
        }).ToList();

        // 5. Stockage du résultat dans Redis (Expire après 5 minutes pour rester frais)
        var cacheOptions = new DistributedCacheEntryOptions()
            .SetAbsoluteExpiration(TimeSpan.FromMinutes(5));

        await _cache.SetStringAsync(
            LeaderboardCacheKey,
            JsonSerializer.Serialize(leaderboard),
            cacheOptions
        );

        return Ok(leaderboard);
    }
}