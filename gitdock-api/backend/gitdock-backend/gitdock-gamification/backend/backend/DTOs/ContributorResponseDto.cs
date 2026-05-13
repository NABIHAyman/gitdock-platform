namespace backend.DTOs;

public class ContributorResponseDto
{
    public long UserId { get; set; }
    public string FullName { get; set; } = string.Empty;
    public string? AvatarUrl { get; set; }
    public int TotalExperience { get; set; }
    public int CurrentLevel { get; set; }
    public string LevelName { get; set; } = string.Empty;
    public List<ContributorBadgeDto> Badges { get; set; } = new();
}

public class ContributorBadgeDto
{
    public Guid BadgeId { get; set; }
    public string Title { get; set; } = string.Empty;
    public string Icon { get; set; } = string.Empty;
    public string Color { get; set; } = string.Empty;
    public DateTime UnlockedAt { get; set; }
}