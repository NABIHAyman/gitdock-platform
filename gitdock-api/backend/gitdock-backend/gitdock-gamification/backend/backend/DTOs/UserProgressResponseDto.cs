namespace backend.DTOs;

public class UserProgressResponseDto
{
    public long UserId { get; set; }
    public int TotalExperience { get; set; }
    public int CurrentLevelRank { get; set; }
    public string LevelName { get; set; } = string.Empty;

    // AJOUTE CETTE LIGNE
    public List<string> Badges { get; set; } = new List<string>();

    public List<UserTagProgressDto> TagProgress { get; set; } = new List<UserTagProgressDto>();
}