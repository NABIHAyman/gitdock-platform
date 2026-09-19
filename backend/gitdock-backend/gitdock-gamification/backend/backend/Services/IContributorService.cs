// IContributorService.cs
using backend.DTOs;

namespace backend.Services;

public interface IContributorService
{
    Task<List<ContributorResponseDto>> GetAllContributorsAsync();
    Task<List<ContributorsByProjectDto>> GetContributorsByProjectAsync();
}