using backend.Services;
using backend.DTOs;
using Microsoft.AspNetCore.Mvc;

namespace backend.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class UserTagProgressController : ControllerBase
    {
        private readonly IUserTagProgressService _service;

        public UserTagProgressController(IUserTagProgressService service)
        {
            _service = service;
        }

        // POST: api/UserTagProgress/increment
        [HttpPost("increment")]
        public async Task<IActionResult> Increment([FromBody] TagActivityRequest request)
        {
            if (!ModelState.IsValid) return BadRequest(ModelState);

            await _service.IncrementTagOccurrenceAsync(request);
            return Ok(new { message = "Compteur technologique mis à jour avec succès." });
        }

        // GET: api/UserTagProgress/{userId}
        [HttpGet("{userId}")]
        public async Task<IActionResult> GetUserTags(long userId)
        {
            var result = await _service.GetUserTagsAsync(userId);
            return Ok(result);
        }
    }
}