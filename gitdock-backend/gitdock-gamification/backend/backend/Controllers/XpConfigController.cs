using backend.DTOs;
using backend.Services;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;

namespace backend.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class XpConfigController : ControllerBase
    {
        private readonly XpConfigService _service;

        public XpConfigController(XpConfigService service) => _service = service;

        [HttpGet]
        public async Task<IActionResult> Get()
        {
            return Ok(await _service.GetConfigAsync());
        }

        [HttpPut]
        public async Task<IActionResult> Update([FromBody] XpConfigDto dto)
        {
            await _service.UpdateConfigAsync(dto);
            return NoContent();
        }
    }
}
