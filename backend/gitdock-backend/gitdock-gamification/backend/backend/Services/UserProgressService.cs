using backend.Domain;
using backend.DTOs;
using backend.Mappers;
using backend.Repositories;
using backend.Strategies;
using Microsoft.Extensions.Caching.Distributed;
using System.Text.Json;

namespace backend.Services;

public class UserProgressService : IUserProgressService
{
    private readonly IUserProgressRepository _userProgressRepository;
    private readonly ILevelRepository _levelRepository;
    private readonly IXpConfigRepository _xpConfigRepository;
    private readonly IDistributedCache _cache;

    public UserProgressService(
        IUserProgressRepository userProgressRepository,
        ILevelRepository levelRepository,
        IXpConfigRepository xpConfigRepository,
        IDistributedCache cache)
    {
        _userProgressRepository = userProgressRepository;
        _levelRepository = levelRepository;
        _xpConfigRepository = xpConfigRepository;
        _cache = cache;
    }

    // --- CETTE MÉTHODE CORRIGE L'ERREUR CS0535 ---
    public async Task<UserProgressResponseDto> ProcessActivityAsync(ProcessActivityRequest request)
    {
        var config = await _xpConfigRepository.GetCurrentConfigAsync()
                     ?? throw new Exception("Configuration XP inexistante.");

        int pointsToAdd = request.ActivityType.ToLower() switch {
            "commit" => config.CommitXp,
            "pr"     => config.PrXp,
            "bugfix" => config.BugFixXp,
            _        => 0
        };

        var progress = await _userProgressRepository.GetByUserIdAsync(request.UserId);

        if (progress == null)
        {
            var initialLevel = await _levelRepository.GetLevelByRankAsync(1);
            progress = new UserProgress {
                UserId = request.UserId,
                TotalExperience = 0,
                CurrentLevelId = initialLevel?.Id ?? Guid.Empty,
                UpdatedAt = DateTime.UtcNow
            };
            await _userProgressRepository.AddAsync(progress);
        }

        progress.TotalExperience += pointsToAdd;
        progress.UpdatedAt = DateTime.UtcNow;

        var targetLevel = await _levelRepository.GetLevelByXpAsync(progress.TotalExperience);
        if (targetLevel != null && targetLevel.Id != progress.CurrentLevelId)
        {
            progress.CurrentLevelId = targetLevel.Id;
        }

        await _userProgressRepository.SaveChangesAsync();
        await _cache.RemoveAsync($"user_progress_{request.UserId}");

        return progress.ToDto();
    }

    public async Task UpdateUserExperienceAsync(long userId, int xpToAdd)
    {
        var progress = await _userProgressRepository.GetByUserIdAsync(userId);

        if (progress == null)
        {
            var initialLevel = await _levelRepository.GetLevelByRankAsync(1);
            progress = new UserProgress {
                UserId = userId,
                TotalExperience = 0,
                CurrentLevelId = initialLevel?.Id ?? Guid.Empty,
                UpdatedAt = DateTime.UtcNow
            };
            await _userProgressRepository.AddAsync(progress);
        }

        progress.TotalExperience += xpToAdd;
        progress.UpdatedAt = DateTime.UtcNow;

        var targetLevel = await _levelRepository.GetLevelByXpAsync(progress.TotalExperience);
        if (targetLevel != null && targetLevel.Id != progress.CurrentLevelId)
        {
            progress.CurrentLevelId = targetLevel.Id;
        }

        await _userProgressRepository.SaveChangesAsync();
        await _cache.RemoveAsync($"user_progress_{userId}");
    }

    public async Task<UserProgressResponseDto> GetUserProgressAsync(long userId)
    {
        string cacheKey = $"user_progress_{userId}";
        var cachedData = await _cache.GetStringAsync(cacheKey);

        if (!string.IsNullOrEmpty(cachedData))
        {
            return JsonSerializer.Deserialize<UserProgressResponseDto>(cachedData)!;
        }

        var progress = await _userProgressRepository.GetByUserIdAsync(userId);
        if (progress == null) return null!;

        var dto = progress.ToDto();
        var options = new DistributedCacheEntryOptions().SetAbsoluteExpiration(TimeSpan.FromMinutes(30));
        await _cache.SetStringAsync(cacheKey, JsonSerializer.Serialize(dto), options);

        return dto;
    }
}