namespace backend.Domain
{
    public class LevelTagRequirement
    {
        public Guid Id { get; set; } = Guid.NewGuid();
        public Guid LevelId { get; set; }
        public Guid TagId { get; set; }
        public int RequiredOccurrences { get; set; } // [cite: 214]

        public bool IsDeleted { get; set; } = false;

        // Navigation properties
        public Level? Level { get; set; }
        public Tag? Tag { get; set; }
    }
}
