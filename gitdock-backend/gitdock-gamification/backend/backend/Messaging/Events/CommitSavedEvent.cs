namespace backend.Messaging.Events;

public class CommitSavedEvent
{
    // L'identifiant unique de l'utilisateur (relié à GitDock-Auth)
    public long UserId { get; set; }

    // Le message du commit (ex: "feat: add login safety")
    public string Message { get; set; } = string.Empty;

    // Le hash du commit (facultatif, mais utile pour l'unicité)
    public string CommitHash { get; set; } = string.Empty;

    // La date à laquelle le commit a été enregistré
    public DateTime PushedAt { get; set; }
}