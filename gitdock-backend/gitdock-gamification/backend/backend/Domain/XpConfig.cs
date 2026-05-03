namespace backend.Domain
{
    public class XpConfig
    {
        public Guid Id { get; set; } = Guid.NewGuid();
        public int CommitXp { get; set; }
        public int PrXp { get; set; }
        public int BugFixXp { get; set; }
        public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;
    }
}
