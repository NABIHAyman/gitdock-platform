using backend.Domain;
using backend.DTOs;
using backend.Services;
using Microsoft.AspNetCore.Mvc;

namespace backend.Controllers;

[ApiController]
[Route("api/[controller]")]
public class BadgesController : ControllerBase
{
    private readonly IBadgeService _badgeService; // Utilise l'interface ici

    public BadgesController(IBadgeService badgeService)
    {
        _badgeService = badgeService;
    }

    // --- PARTIE CATALOGUE (MANAGER) ---

    [HttpGet]
    public async Task<ActionResult<List<BadgeResponseDto>>> GetAll()
    {
        var badges = await _badgeService.GetAllBadgesAsync();
        return Ok(badges);
    }

    [HttpPost]
    public async Task<ActionResult<BadgeResponseDto>> Create(CreateBadgeDto dto)
    {
        var result = await _badgeService.CreateBadgeAsync(dto);
        // Utilisation du ID du DTO de réponse
        return CreatedAtAction(nameof(GetAll), new { id = result.Id }, result);
    }

    [HttpPut("{id}")]
    public async Task<IActionResult> Update(Guid id, CreateBadgeDto dto)
    {
        var updated = await _badgeService.UpdateBadgeAsync(id, dto);
        if (!updated) return NotFound();
        return NoContent();
    }

    [HttpDelete("{id}")]
    public async Task<IActionResult> Delete(Guid id)
    {
        var deleted = await _badgeService.DeleteBadgeAsync(id);
        if (!deleted) return NotFound();
        return NoContent();
    }
}