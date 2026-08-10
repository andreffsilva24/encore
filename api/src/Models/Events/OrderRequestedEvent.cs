namespace EncoreApi.Models.Events;

public record OrderRequestedEvent(
    Guid OrderId,
    Guid EventId,
    Guid UserId,
    List<string> SeatIds,
    DateTimeOffset RequestedAt
);