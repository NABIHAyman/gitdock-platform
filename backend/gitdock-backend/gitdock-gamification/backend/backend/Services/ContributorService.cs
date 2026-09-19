// ContributorService.cs
using backend.Clients;
using backend.DTOs;
using backend.Mappers;
using backend.Repositories;

namespace backend.Services;

public class ContributorService : IContributorService
{
    private readonly IContributorRepository _contributorRepository;
    private readonly IAuthServiceClient _authServiceClient;
    private readonly IProjectServiceClient _projectServiceClient;

    public ContributorService(
        IContributorRepository contributorRepository,
        IAuthServiceClient authServiceClient,
        IProjectServiceClient projectServiceClient)
    {
        _contributorRepository = contributorRepository;
        _authServiceClient = authServiceClient;
        _projectServiceClient = projectServiceClient;
    }

    public async Task<List<ContributorResponseDto>> GetAllContributorsAsync()
    {
        var progressions = await _contributorRepository.GetAllWithDetailsAsync();

        if (!progressions.Any())
            return new List<ContributorResponseDto>();

        var userIds = progressions.Select(p => p.UserId).ToList();
        var userSummaries = await _authServiceClient.GetUserSummariesAsync(userIds);

        return progressions
            .Select(p => ContributorMapper.ToDto(p, userSummaries?.FirstOrDefault(u => u.Id == p.UserId)))
            .ToList();
    }

    public async Task<List<ContributorsByProjectDto>> GetContributorsByProjectAsync()
    {
        var projectGroups = await _projectServiceClient.GetCollaboratorsGroupedAsync();

        if (!projectGroups.Any())
            return new List<ContributorsByProjectDto>();

        var allUserIds = projectGroups
            .SelectMany(p => p.Collaborators)
            .Select(c => c.Id)
            .Distinct()
            .ToList();

        var progressions = await _contributorRepository.GetByUserIdsAsync(allUserIds);
        var userSummaries = await _authServiceClient.GetUserSummariesAsync(allUserIds);

        return projectGroups
            .Select(p => ContributorMapper.ToProjectDto(p, progressions, userSummaries))
            .Where(p => p.Contributors.Any())
            .ToList();
    }
}