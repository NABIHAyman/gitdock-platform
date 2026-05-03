using backend.DTOs;

public class CreateLevelDto
{
    public string Name { get; set; } = string.Empty;
    public int LevelRank { get; set; }
    public int RequiredXP { get; set; }

    // Le manager envoie aussi les pré-requis ici
    public List<AddLevelRequirementDto> Requirements { get; set; } = new();
}