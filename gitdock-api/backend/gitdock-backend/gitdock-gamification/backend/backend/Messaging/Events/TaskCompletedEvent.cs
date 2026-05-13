using System.Text.Json.Serialization;

namespace backend.Messaging.Events;

public class TaskCompletedEvent
{
    [JsonPropertyName("taskId")]
    public long TaskId { get; set; }

    [JsonPropertyName("userId")]
    public long UserId { get; set; }

    [JsonPropertyName("taskLevel")]
    public int? TaskLevel { get; set; } // Nullable car PHP envoie ?int

    [JsonPropertyName("xpReward")]
    public int XpReward { get; set; }
}