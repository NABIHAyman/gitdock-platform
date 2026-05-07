namespace backend.Messaging.Events;

public class TaskCompletedEvent
{
    public long UserId { get; set; }
    public string TaskTitle { get; set; } = string.Empty;
    public string Difficulty { get; set; } = string.Empty;

    // IL MANQUE PROBABLEMENT CETTE LIGNE :
    public string TagName { get; set; } = string.Empty;

    public DateTime CompletedAt { get; set; }
}