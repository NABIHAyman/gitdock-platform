namespace backend.DTOs
{
    public class AddLevelRequirementDto
    {
        public Guid TagId { get; set; }
        public int RequiredOccurrences { get; set; }
    }
}
