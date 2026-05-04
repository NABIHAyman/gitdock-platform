using System.Text.Json.Serialization;

namespace backend.Messaging.Events;

public class PullRequestMergedEvent
{
    [JsonPropertyName("userId")]
    public long UserId { get; set; }

    [JsonPropertyName("prTitle")]
    public string PrTitle { get; set; } = string.Empty;

    [JsonPropertyName("repositoryName")]
    public string RepositoryName { get; set; } = string.Empty;

    [JsonPropertyName("difficulty")]
    public string Difficulty { get; set; } = "Easy";
}