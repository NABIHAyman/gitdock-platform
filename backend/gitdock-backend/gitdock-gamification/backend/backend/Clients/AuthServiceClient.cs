using System.Net.Http.Json;
using backend.DTOs;

namespace backend.Clients;

public class AuthServiceClient : IAuthServiceClient
{
    private readonly HttpClient _httpClient;
    private readonly ILogger<AuthServiceClient> _logger;

    public AuthServiceClient(HttpClient httpClient, ILogger<AuthServiceClient> logger)
    {
        _httpClient = httpClient;
        _logger = logger;
    }

    // --- ANCIEN CODE (INDISPENSABLE POUR L'AFFICHAGE) ---
    public async Task<List<UserSummaryDto>> GetUserSummariesAsync(List<long> userIds)
    {
        try {
            var response = await _httpClient.PostAsJsonAsync("api/auth/users/summaries", userIds);
            return response.IsSuccessStatusCode
                ? await response.Content.ReadFromJsonAsync<List<UserSummaryDto>>() ?? new List<UserSummaryDto>()
                : new List<UserSummaryDto>();
        } catch (Exception ex) {
            _logger.LogError(ex, "Erreur summaries");
            return new List<UserSummaryDto>();
        }
    }

    // --- NOUVEAU CODE (POUR FILTRER LES IDS FANTÔMES) ---
    public async Task<bool> UserExistsAsync(long userId)
    {
        try {
            // Appelle l'endpoint qu'on vient de créer en Java
            var response = await _httpClient.GetAsync($"api/auth/users/{userId}/exists");
            return response.IsSuccessStatusCode; // True si 200, False si 404
        } catch {
            return false;
        }
    }
}