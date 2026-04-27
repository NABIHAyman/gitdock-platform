namespace backend.DTOs;

public class LevelResponseDto
{
    public Guid Id { get; set; }
    public string Name { get; set; } = string.Empty;
    public int LevelRank { get; set; }
    public int RequiredXP { get; set; }
    // Vérifie bien l'orthographe ici (avec un 's' à la fin)
    public List<AddLevelRequirementDto> LevelTagRequirements { get; set; } = new();
}