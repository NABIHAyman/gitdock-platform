using backend.Domain;
using backend.DTOs;
using backend.Mappers;
using backend.Repositories;
using backend.Strategies;
using Microsoft.Extensions.Caching.Distributed; // Obligatoire
using System.Text.Json; // Pour sérialiser en JSON dans Redis

namespace backend.Services;

public class UserProgressService : IUserProgressService
{
    private readonly IUserProgressRepository _userProgressRepository;
    private readonly ILevelRepository _levelRepository;
    private readonly IXpConfigRepository _xpConfigRepository;
    private readonly IBadgeRepository _badgeRepository;
    private readonly BadgeStrategyFactory _strategyFactory;
    private readonly IUserBadgeService _userBadgeService;
    private readonly IDistributedCache _cache; // Injection du cache Redis

    public UserProgressService(
        IUserProgressRepository userProgressRepository,
        ILevelRepository levelRepository,
        IXpConfigRepository xpConfigRepository,
        IBadgeRepository badgeRepository,
        BadgeStrategyFactory strategyFactory,
        IUserBadgeService userBadgeService,
        IDistributedCache cache) // Ajouté ici
    {
        _userProgressRepository = userProgressRepository;
        _levelRepository = levelRepository;
        _xpConfigRepository = xpConfigRepository;
        _badgeRepository = badgeRepository;
        _strategyFactory = strategyFactory;
        _userBadgeService = userBadgeService;
        _cache = cache;
    }

    public async Task<UserProgressResponseDto> ProcessActivityAsync(ProcessActivityRequest request)
    {
        // 1. Calcul de l'XP
        var config = await _xpConfigRepository.GetCurrentConfigAsync()
                     ?? throw new Exception("Configuration XP inexistante.");

        int pointsToAdd = request.ActivityType.ToLower() switch {
            "commit" => config.CommitXp,
            "pr"     => config.PrXp,
            "bugfix" => config.BugFixXp,
            _        => 0
        };

        // 2. Récupération ou Création
        var progress = await _userProgressRepository.GetByUserIdAsync(request.UserId);

        if (progress == null)
        {
            var initialLevel = await _levelRepository.GetLevelByRankAsync(1);
            if (initialLevel == null) throw new Exception("Erreur : Niveau 1 introuvable.");

            progress = new UserProgress {
                UserId = request.UserId,
                TotalExperience = 0,
                CurrentLevelId = initialLevel.Id,
                UpdatedAt = DateTime.UtcNow
            };
            await _userProgressRepository.AddAsync(progress);
        }

        // 3. Mise à jour
        progress.TotalExperience += pointsToAdd;
        progress.UpdatedAt = DateTime.UtcNow;

        // 4. Level Up
        var targetLevel = await _levelRepository.GetLevelByXpAsync(progress.TotalExperience);
        if (targetLevel != null && targetLevel.Id != progress.CurrentLevelId)
        {
            progress.CurrentLevelId = targetLevel.Id;
            progress.Level = targetLevel;
        }

        // 5. Sauvegarde DB
        await _userProgressRepository.SaveChangesAsync();

        // 6. INVALIDATION DU CACHE : Comme les données ont changé, on supprime l'ancienne version du cache
        string cacheKey = $"user_progress_{request.UserId}";
        await _cache.RemoveAsync(cacheKey);

        return progress.ToDto();
    }

    public async Task<UserProgressResponseDto> GetUserProgressAsync(long userId)
    {
        string cacheKey = $"user_progress_{userId}";

        // 1. Tentative de lecture depuis Redis
        var cachedData = await _cache.GetStringAsync(cacheKey);
        if (!string.IsNullOrEmpty(cachedData))
        {
            // Si trouvé, on désérialise le JSON
            return JsonSerializer.Deserialize<UserProgressResponseDto>(cachedData);
        }

        // 2. Si absent de Redis, on lit depuis PostgreSQL
        var progress = await _userProgressRepository.GetByUserIdAsync(userId);
        var dto = progress.ToDto();

        if (dto != null)
        {
            // 3. On stocke dans Redis pour les prochains appels (Expire après 30 min)
            var options = new DistributedCacheEntryOptions()
                .SetAbsoluteExpiration(TimeSpan.FromMinutes(30));

            var jsonData = JsonSerializer.Serialize(dto);
            await _cache.SetStringAsync(cacheKey, jsonData, options);
        }

        return dto;
    }
}