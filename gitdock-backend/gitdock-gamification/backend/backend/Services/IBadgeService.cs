using backend.DTOs;
using backend.Domain; // Pour UserProgress

namespace backend.Services;

public interface IBadgeService
{
    // Tes méthodes actuelles (Catalogue)
    Task<List<BadgeResponseDto>> GetAllBadgesAsync();
    Task<BadgeResponseDto> CreateBadgeAsync(CreateBadgeDto dto);
    Task<bool> UpdateBadgeAsync(Guid id, CreateBadgeDto dto);
    Task<bool> DeleteBadgeAsync(Guid id);

    // La méthode nécessaire pour l'automatisme
    Task CheckAndAwardBadgesAsync(UserProgress progress);
}