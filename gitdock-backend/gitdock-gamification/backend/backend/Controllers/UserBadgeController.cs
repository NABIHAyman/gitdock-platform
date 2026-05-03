using backend.Services;
using backend.DTOs;
using Microsoft.AspNetCore.Mvc;

namespace backend.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class UserBadgeController : ControllerBase
    {
        // Utilise le service spécialisé pour les relations Utilisateur <-> Badge
        private readonly IUserBadgeService _userBadgeService;

        public UserBadgeController(IUserBadgeService userBadgeService)
        {
            _userBadgeService = userBadgeService;
        }

        // GET: api/UserBadge/user/Rihab_Engineering
        // Controllers/UserBadgeController.cs
        [HttpGet("{userId}")]
        public async Task<IActionResult> GetUserBadges(long userId) // Change string -> long ici
        {
            var badges = await _userBadgeService.GetUserBadgesAsync(userId);
            return Ok(badges);
        }

        // POST: api/UserBadge/award
        [HttpPost("award")]
        public async Task<IActionResult> AwardBadge([FromBody] AwardBadgeRequest request)
        {
            if (!ModelState.IsValid) return BadRequest(ModelState);

            await _userBadgeService.AwardBadgeAsync(request.UserId, request.BadgeId);
            return Ok(new { message = "Badge attribué avec succès par le système ou le manager." });
        }
    }
}