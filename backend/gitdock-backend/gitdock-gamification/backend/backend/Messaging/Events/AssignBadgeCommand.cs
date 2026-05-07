namespace backend.Messaging.Events;

public class AssignBadgeCommand
{
    public long UserId { get; set; }
    public Guid BadgeId { get; set; }
}