namespace EncoreApi.Models.Responses;

public record LoginResponse(
    Guid UserId,
    string Name,
    string Email
){}