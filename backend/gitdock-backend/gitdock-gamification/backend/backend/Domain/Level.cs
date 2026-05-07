namespace backend.Domain
{
    public class Level
    {
        public Guid Id { get; set; } = Guid.NewGuid();
        public string Name { get; set; } = string.Empty;
        public int LevelRank { get; set; } // 1, 2, 3...
        public int RequiredXP { get; set; } // XP pour débloquer ce niveau
        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
        public DateTime? UpdatedAt { get; set; }
        public bool IsDeleted { get; set; } = false; // Le Soft Delete
        public DateTime? DeletedAt { get; set; }
        public virtual ICollection<LevelTagRequirement> LevelTagRequirements { get; set; } = new List<LevelTagRequirement>();
    }
}
