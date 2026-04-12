namespace backend.DTOs;

public class CreateBadgeDto
{
    public string Title { get; set; } = string.Empty;
    public string Description { get; set; } = string.Empty;
    public int Xp { get; set; }
    public string Icon { get; set; } = "mdi-trophy-outline";
    public string Color { get; set; } = "#5b13ec";
    public string Type { get; set; } = "Contribution";
}