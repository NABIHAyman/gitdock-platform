// CollaboratorAddedEvent.cs
namespace backend.Messaging.Events;

public class CollaboratorAddedEvent
{
    public long UserId { get; set; }
    public long ProjectId { get; set; }
    public string Role { get; set; }
}