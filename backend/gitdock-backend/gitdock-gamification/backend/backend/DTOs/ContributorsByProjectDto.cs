namespace backend.DTOs;

public class ContributorsByProjectDto
{
    public long ProjectId { get; set; }
    public string ProjectName { get; set; } = string.Empty;
    public List<ContributorResponseDto> Contributors { get; set; } = new();
}