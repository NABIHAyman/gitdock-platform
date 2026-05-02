namespace backend.DTOs;

public class NotificationEventDto
{
    public long UserId { get; set; }
    public string Message { get; set; } = string.Empty;
    public string Type { get; set; } = "Gamification"; // Ex: Achievement, LevelUp
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
}