using System.ComponentModel.DataAnnotations;

namespace EncoreApi.Models.Requests;

public record CreateUserRequest(
    [Required] string Name,
    [Required, EmailAddress] string Email
);