using EncoreApi.Models.Requests;
using Microsoft.AspNetCore.Mvc;

namespace EncoreApi.Controllers;

[ApiController]
[Route("api/[controller]")]
[Produces("application/json")]
public class UsersController: ControllerBase
{
    private readonly ILogger<UsersController> _logger;
    private readonly IHttpClientFactory _httpClientFactory;

    public UsersController(
        ILogger<UsersController> logger,
        IHttpClientFactory httpClientFactory)
    {
        _logger = logger;
        _httpClientFactory = httpClientFactory;
    }
    
    [HttpPost]
    [ProducesResponseType(StatusCodes.Status202Accepted)]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> CreateUser([FromBody] CreateUserRequest request)
    {
        var client = _httpClientFactory.CreateClient("UserService");
        var response = await client.PostAsJsonAsync("/api/users", request);
        var content = await response.Content.ReadAsStringAsync();
        
        return StatusCode((int)response.StatusCode, content);
    }
    
    [HttpGet("{userId:guid}")]
    [ProducesResponseType(StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> GetUser(Guid userId)
    {
        var client = _httpClientFactory.CreateClient("UserService");
        var response = await client.GetAsync($"/api/users/{userId}");

        if (response.IsSuccessStatusCode)
        {
            var content = await response.Content.ReadAsStringAsync();
            return Content(content, "application/json");
        }

        if (response.StatusCode == System.Net.HttpStatusCode.NotFound)
            return NotFound(new { message = $"User {userId} not found." });

        return StatusCode(502, new { message = "User Service unavailable." });
    }
}