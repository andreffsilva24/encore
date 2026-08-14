using System.Text.Json;
using EncoreApi.Auth;
using EncoreApi.Models.Requests;
using EncoreApi.Models.Responses;
using Microsoft.AspNetCore.Mvc;

namespace EncoreApi.Controllers;

[ApiController]
[Route("api/[controller]")]
[Produces("application/json")]
public class AuthController: ControllerBase
{
    private readonly ILogger<AuthController> _logger;
    private readonly IHttpClientFactory _httpClientFactory;
    private readonly JwtService _jwtService;

    public AuthController(ILogger<AuthController> logger, IHttpClientFactory httpClientFactory, JwtService jwtService)
    {
        _logger = logger;
        _httpClientFactory = httpClientFactory;
        _jwtService = jwtService;
    }

    [HttpPost("register")]
    [ProducesResponseType(StatusCodes.Status202Accepted)]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> Register([FromBody] CreateUserRequest request)
    {
        var client = _httpClientFactory.CreateClient("UserService");
        var response = await client.PostAsJsonAsync("/api/users", request);
        var content = await response.Content.ReadAsStringAsync();
        
        return StatusCode((int)response.StatusCode, content);
    }
    
    [HttpPost("login")]
    [ProducesResponseType(StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status401Unauthorized)]
    public async Task<IActionResult> Login([FromBody] LoginRequest loginRequest)
    {
        var client = _httpClientFactory.CreateClient("UserService");
        var response = await client.PostAsJsonAsync($"/api/users/login", loginRequest);

        if (response.StatusCode == System.Net.HttpStatusCode.Unauthorized)
            return Unauthorized(new { message = "Invalid email or password." });

        if (!response.IsSuccessStatusCode)
            return StatusCode(502, new { message = "User Service unavailable." });
        
        var content = await response.Content.ReadAsStringAsync();
        var loginResponse = JsonSerializer.Deserialize<LoginResponse>(content, new JsonSerializerOptions
        {
            PropertyNameCaseInsensitive = true
        });

        if (loginResponse == null)
            return StatusCode(502, new { message = "User Service unavailable." });
            
        var token = _jwtService.GenerateToken(loginResponse.UserId, loginResponse.Email);
        return Ok(new { token });
    }
}