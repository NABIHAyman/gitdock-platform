using backend.Data;
using backend.Domain;
using backend.DTOs;
using backend.Mappers;
using backend.Repositories;

namespace backend.Services;

public class UserTagProgressService : IUserTagProgressService
{
    private readonly IUserTagProgressRepository _userTagProgressRepository;

    public UserTagProgressService(IUserTagProgressRepository userTagProgressRepository)
    {
        _userTagProgressRepository = userTagProgressRepository;
    }

    public async Task IncrementTagOccurrenceAsync(TagActivityRequest request)
    {
        var tagStat = await _userTagProgressRepository.GetAsync(request.UserId, request.TagId);

        if (tagStat == null)
        {
            await _userTagProgressRepository.AddAsync(new UserTagProgress
            {
                Id = Guid.NewGuid(),
                UserId = request.UserId,
                TagId = request.TagId,
                Occurrences = 1,
                LastUpdatedAt = DateTime.UtcNow
            });
        }
        else
        {
            tagStat.Occurrences++;
            tagStat.LastUpdatedAt = DateTime.UtcNow;
        }

        await _userTagProgressRepository.SaveChangesAsync();
    }

    public async Task<List<UserTagProgressDto>> GetUserTagsAsync(long userId)
    {
        var tags = await _userTagProgressRepository.GetUserAllProgressAsync(userId);
        return tags.Select(tp => tp.ToDto()).ToList();
    }
}