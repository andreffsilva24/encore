using System.ComponentModel.DataAnnotations;

namespace EncoreApi.Models.Requests;

public record CreateOrderRequest(
    [Required] Guid EventId,
    [Required] Guid UserId,
    [Required, MinLength(1)] List<string> SeatIds
);