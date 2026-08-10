using EncoreApi.Kafka;
using EncoreApi.Models.Events;
using EncoreApi.Models.Requests;
using Microsoft.AspNetCore.Mvc;

namespace EncoreApi.Controllers;

[ApiController]
[Route("api/[controller]")]
[Produces("application/json")]
public class OrdersController : ControllerBase
{
    private readonly KafkaProducerService _producer;
    private readonly ILogger<OrdersController> _logger;

    public OrdersController(
        KafkaProducerService producer,
        ILogger<OrdersController> logger)
    {
        _producer = producer;
        _logger = logger;
    }

    [HttpPost]
    [ProducesResponseType(StatusCodes.Status202Accepted)]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> CreateOrder([FromBody] CreateOrderRequest request)
    {
        var orderId = Guid.NewGuid();

        var @event = new OrderRequestedEvent(
            OrderId: orderId,
            EventId: request.EventId,
            UserId: request.UserId,
            SeatIds: request.SeatIds,
            RequestedAt: DateTimeOffset.UtcNow
        );

        await _producer.PublishAsync(
            topic: Topics.OrderRequested,
            key: orderId.ToString(),
            payload: @event
        );

        _logger.LogInformation(
            "Order {OrderId} queued for event {EventId}, seats: {Seats}",
            orderId, request.EventId, string.Join(", ", request.SeatIds));

        return Accepted(new
        {
            orderId,
            message = "Your order is being processed."
        });
    }

    [HttpGet("{orderId:guid}")]
    [ProducesResponseType(StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public IActionResult GetOrder(Guid orderId)
    {
        // TODO: query Order Service once read model is available
        return Ok(new
        {
            orderId,
            status = "PENDING",
            note = "Full order status will be available once the Order Service read model is implemented."
        });
    }
}