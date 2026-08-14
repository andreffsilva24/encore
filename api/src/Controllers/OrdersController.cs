using EncoreApi.Kafka;
using EncoreApi.Models.Events;
using EncoreApi.Models.Requests;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace EncoreApi.Controllers;

[Authorize]
[ApiController]
[Route("api/[controller]")]
[Produces("application/json")]
public class OrdersController: ControllerBase
{
    private readonly KafkaProducerService _producer;
    private readonly ILogger<OrdersController> _logger;
    private readonly IHttpClientFactory _httpClientFactory;

    public OrdersController(
        KafkaProducerService producer,
        ILogger<OrdersController> logger,
        IHttpClientFactory httpClientFactory)
    {
        _producer = producer;
        _logger = logger;
        _httpClientFactory = httpClientFactory;
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
    public async Task<IActionResult> GetOrder(Guid orderId)
    {
        var client = _httpClientFactory.CreateClient("OrderService");
        var response = await client.GetAsync($"/api/orders/{orderId}");

        if (response.IsSuccessStatusCode)
        {
            var content = await response.Content.ReadAsStringAsync();
            return Content(content, "application/json");
        }

        if (response.StatusCode == System.Net.HttpStatusCode.NotFound)
            return NotFound(new { message = $"Order {orderId} not found." });

        _logger.LogError("Order Service returned {StatusCode} for order {OrderId}", response.StatusCode, orderId);

        return StatusCode(502, new { message = "Order Service unavailable." });
    }
}