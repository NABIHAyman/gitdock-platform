using backend.DTOs;

namespace backend.Clients;

public interface IProjectServiceClient
{
    Task<List<CollaboratorsGroupedDto>> GetCollaboratorsGroupedAsync();
}