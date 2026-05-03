namespace backend.Messaging.Events;

public class PullRequestMergedEvent
{
    public long UserId { get; set; }
    public string PrTitle { get; set; }
    public string RepositoryName { get; set; }
}