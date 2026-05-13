// IContributorRepository.cs
using backend.Domain;

namespace backend.Repositories;

public interface IContributorRepository
{
    Task<List<UserProgress>> GetAllWithDetailsAsync();
    Task<List<UserProgress>> GetByUserIdsAsync(List<long> userIds);
}