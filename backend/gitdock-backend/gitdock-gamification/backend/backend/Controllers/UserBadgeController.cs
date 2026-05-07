using backend.DTOs;
using backend.Services;
using Microsoft.AspNetCore.Mvc;

namespace backend.Controllers;

[ApiController]
[Route("api/UserBadge")]
public class UserBadgeController : ControllerBase
{
    private readonly IUserBadgeService _userBadgeService;

    public UserBadgeController(IUserBadgeService userBadgeService)
    {
        _userBadgeService = userBadgeService;
    }

    // GET /api/UserBadge/my-badges (pour l'utilisateur connecté — à brancher avec JWT)
    [HttpGet("my-badges")]
    public async Task<IActionResult> GetMyBadges([FromQuery] long userId)
    {
        var badges = await _userBadgeService.GetUserBadgesAsync(userId);
        return Ok(badges);
    }

    // GET /api/UserBadge/user/{userId}
    [HttpGet("user/{userId}")]
    public async Task<IActionResult> GetUserBadges(long userId)
    {
        var badges = await _userBadgeService.GetUserBadgesAsync(userId);
        return Ok(badges);
    }

    // POST /api/UserBadge/award
    // Attribution manuelle d'un badge par le manager
    [HttpPost("award")]
    public async Task<IActionResult> AwardBadge([FromBody] AwardBadgeRequest request)
    {
        if (request.UserId == 0 || request.BadgeId == Guid.Empty)
            return BadRequest(new { message = "UserId et BadgeId sont requis." });

        await _userBadgeService.AwardBadgeAsync(request.UserId, request.BadgeId);
        return Ok(new { message = "Badge attribué avec succès." });
    }
}