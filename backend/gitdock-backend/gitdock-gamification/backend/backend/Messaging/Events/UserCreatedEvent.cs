namespace backend.Messaging.Events;

public class UserCreatedEvent
{
    public long UserId { get; set; }
    public string Email { get; set; }
    public string FullName { get; set; }
    public DateTime CreatedAt { get; set; }
}