using MassTransit;
using backend.DTOs;

namespace backend.Messaging.Producers;

public interface INotificationProducer
{
    Task PublishNotificationAsync(long userId, string message, string type);
}

public class NotificationProducer : INotificationProducer
{
    private readonly IPublishEndpoint _publishEndpoint;

    public NotificationProducer(IPublishEndpoint publishEndpoint)
    {
        _publishEndpoint = publishEndpoint;
    }

    public async Task PublishNotificationAsync(long userId, string message, string type)
    {
        await _publishEndpoint.Publish(new NotificationEventDto
        {
            UserId = userId,
            Message = message,
            Type = type
        });
    }
}