namespace backend.DTOs
{
    public class AwardBadgeRequest
    {
        public long UserId { get; set; }
        public Guid BadgeId { get; set; }
    }
}