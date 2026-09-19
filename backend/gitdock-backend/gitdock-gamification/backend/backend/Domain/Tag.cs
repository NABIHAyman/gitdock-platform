namespace backend.Domain
{
    public class Tag
    {
        public Guid Id { get; set; } = Guid.NewGuid();
        public string Name { get; set; } = string.Empty;
        public string Color { get; set; } = "#5b13ec";
        public string Type { get; set; } = "Commit"; // ex: Fix, Feature,
        // --- Audit & Soft Delete ---
        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
        public DateTime? UpdatedAt { get; set; }
        public bool IsDeleted { get; set; } = false;
        public virtual ICollection<LevelTagRequirement> LevelTagRequirements { get; set; } = new List<LevelTagRequirement>();
    }
}
