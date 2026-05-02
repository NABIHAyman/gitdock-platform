namespace backend.Services;
using backend.DTOs;

public interface IUserProgressService
{
    // Utilise le DTO pour regrouper UserId et ActivityType
    Task<UserProgressResponseDto> ProcessActivityAsync(ProcessActivityRequest request);

    Task<UserProgressResponseDto> GetUserProgressAsync(long userId);
}