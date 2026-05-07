using backend.Services;
using backend.DTOs;
using Microsoft.AspNetCore.Mvc;

namespace backend.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class UserProgressController : ControllerBase
    {
        private readonly IUserProgressService _service;

        public UserProgressController(IUserProgressService service) => _service = service;

        [HttpPost("process")]
        public async Task<IActionResult> Process([FromBody] ProcessActivityRequest request)
        {
            try
            {
                // Appelle le service qui gère l'initialisation et la montée de niveau
                var result = await _service.ProcessActivityAsync(request);
                return Ok(result);
            }
            catch (Exception ex)
            {
                // Renvoie un message d'erreur clair pour le débogage dans Scalar
                return BadRequest(new {
                    message = "Erreur lors du traitement de l'activité",
                    detail = ex.Message
                });
            }
        }

        [HttpGet("{userId}")]
        public async Task<IActionResult> Get(long userId)
        {
            var result = await _service.GetUserProgressAsync(userId);
            if (result == null)
            {
                return NotFound(new { message = $"Aucune progression trouvée pour l'utilisateur {userId}" });
            }
            return Ok(result);
        }
    }
}