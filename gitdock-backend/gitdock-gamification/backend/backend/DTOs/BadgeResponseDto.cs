using backend.Enums;

namespace backend.DTOs;

public class BadgeResponseDto
{
    public Guid Id { get; set; }
    public string Title { get; set; } = string.Empty;
    public string Description { get; set; } = string.Empty;
    public int Xp { get; set; }
    public string Icon { get; set; } = string.Empty;
    public string Color { get; set; } = string.Empty;
    public BadgeType Type { get; set; } = BadgeType.Auto;
}