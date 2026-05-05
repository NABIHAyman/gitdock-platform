using backend.DTOs;
using System.Net.Http.Json;

namespace backend.Clients;

public class ProjectServiceClient : IProjectServiceClient
{
    private readonly HttpClient _httpClient;
    private readonly ILogger<ProjectServiceClient> _logger;
    private readonly IHttpContextAccessor _httpContextAccessor;

    public ProjectServiceClient(
        HttpClient httpClient,
        ILogger<ProjectServiceClient> logger,
        IHttpContextAccessor httpContextAccessor)
    {
        _httpClient = httpClient;
        _logger = logger;
        _httpContextAccessor = httpContextAccessor;
    }

    public async Task<List<CollaboratorsGroupedDto>> GetCollaboratorsGroupedAsync()
    {
        try
        {
            // Récupérer le JWT depuis la requête entrante
            var token = _httpContextAccessor.HttpContext?
                .Request.Headers["Authorization"]
                .ToString();

            // Ajouter le token dans la requête vers gitdock-project
            var requestMessage = new HttpRequestMessage(
                HttpMethod.Get,
                "api/projects/dashboard/collaborators-grouped");

            if (!string.IsNullOrEmpty(token))
                requestMessage.Headers.Add("Authorization", token);

            var response = await _httpClient.SendAsync(requestMessage);

            if (response.IsSuccessStatusCode)
                return await response.Content
                    .ReadFromJsonAsync<List<CollaboratorsGroupedDto>>() ?? new();

            _logger.LogWarning("gitdock-project a répondu {StatusCode}", response.StatusCode);
            return new();
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erreur communication gitdock-project");
            return new();
        }
    }
}