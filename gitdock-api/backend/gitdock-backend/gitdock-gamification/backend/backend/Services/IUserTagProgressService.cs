namespace backend.Services;
using backend.DTOs;

public interface IUserTagProgressService
{
    // Utilise le DTO dédié aux tags
    Task IncrementTagOccurrenceAsync(TagActivityRequest request);

    // Récupère toutes les spécialisations d'un utilisateur
    Task<List<UserTagProgressDto>> GetUserTagsAsync(long userId);
}