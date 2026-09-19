using backend.Enums;
namespace backend.Domain
{
    public class Badge
    {
        public Guid Id { get; set; }
        public string Title { get; set; } = string.Empty;
        public string Description { get; set; } = string.Empty;
        public int Xp { get; set; }
        public string Icon { get; set; } = "mdi-trophy-outline";
        public string Color { get; set; } = "#5b13ec";
        public BadgeType Type { get; set; }
        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
        public DateTime? UpdatedAt { get; set; }
        public DateTime? DeletedAt { get; set; }
    }
}
