namespace backend.Messaging.Events;

public class BugFixedEvent
{
    public long UserId { get; set; }
    public string IssueKey { get; set; } = string.Empty;// Ex: "BUG-123"
}