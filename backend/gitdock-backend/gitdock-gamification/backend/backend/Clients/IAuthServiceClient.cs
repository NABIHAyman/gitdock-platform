using backend.DTOs;

namespace backend.Clients;

public interface IAuthServiceClient
{
    // Garde celle-ci pour l'affichage (Vue.js)
    Task<List<UserSummaryDto>> GetUserSummariesAsync(List<long> userIds);

    // Ajoute celle-ci pour la sécurité (RabbitMQ)
    Task<bool> UserExistsAsync(long userId);
}