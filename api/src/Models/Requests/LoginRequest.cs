using System.ComponentModel.DataAnnotations;

namespace EncoreApi.Models.Requests;

public record LoginRequest(
    [Required, EmailAddress] string Email,
    [Required] string Password
);