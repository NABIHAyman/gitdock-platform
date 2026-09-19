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
                // 🛡️ ON RÉCUPÈRE LES HEADERS INJECTÉS PAR LA GATEWAY
                var userId = _httpContextAccessor.HttpContext?.Request.Headers["X-User-Id"].ToString();
                var roles = _httpContextAccessor.HttpContext?.Request.Headers["X-User-Roles"].ToString();

                var requestMessage = new HttpRequestMessage(
                    HttpMethod.Get,
                    "api/projects/dashboard/collaborators-grouped");

                // 🛡️ ON LES TRANSFÈRE AU SERVICE PROJECT
                if (!string.IsNullOrEmpty(userId))
                    requestMessage.Headers.Add("X-User-Id", userId);

                if (!string.IsNullOrEmpty(roles))
                    requestMessage.Headers.Add("X-User-Roles", roles);

                var response = await _httpClient.SendAsync(requestMessage);

                if (response.IsSuccessStatusCode)
                    return await response.Content.ReadFromJsonAsync<List<CollaboratorsGroupedDto>>() ?? new();

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