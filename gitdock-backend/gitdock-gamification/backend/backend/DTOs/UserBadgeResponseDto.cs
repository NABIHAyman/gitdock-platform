namespace backend.DTOs;

public class UserBadgeResponseDto
{
    public Guid BadgeId { get; set; }
    public string Title { get; set; } = string.Empty;
    public string Description { get; set; } = string.Empty;
    public int XpReward { get; set; }
    public string Icon { get; set; } = string.Empty;
    public string Color { get; set; } = string.Empty;
    public DateTime UnlockedAt { get; set; }
}