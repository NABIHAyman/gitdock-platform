namespace backend.Messaging.Events;
using System.Text.Json.Serialization;

public class CommitSavedEvent
{
    // Doit matcher 'authorUserId' du DTO Java
    [JsonPropertyName("authorUserId")]
    public long AuthorUserId { get; set; }

    // Doit matcher 'hash' du DTO Java
    public string Hash { get; set; }

    // Doit matcher 'projectId' du DTO Java
    public long ProjectId { get; set; }

    // Optionnels mais utiles
    public int Additions { get; set; }
    public int Deletions { get; set; }
}