namespace backend.DTOs;

public class CollaboratorsGroupedDto
{
    public long ProjectId { get; set; }
    public string ProjectName { get; set; } = string.Empty;
    public List<CollaboratorDto> Collaborators { get; set; } = new();
}

public class CollaboratorDto
{
    public long Id { get; set; }
    public string FirstName { get; set; } = string.Empty;
    public string LastName { get; set; } = string.Empty;
    public string Email { get; set; } = string.Empty;
    public string Role { get; set; } = string.Empty;
    public string Status { get; set; } = string.Empty;
}