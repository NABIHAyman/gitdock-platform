using backend.Domain;
using backend.DTOs;
using backend.Services;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace backend.Controllers;

[ApiController]
[Route("api/[controller]")] // L'URL sera : /api/badges
public class BadgesController : ControllerBase
{
    private readonly BadgeService _badgeService;

    // Injection de dépendance (SOLID : D - Dependency Inversion)
    public BadgesController(BadgeService badgeService)
    {
        _badgeService = badgeService;
    }

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
        return CreatedAtAction(nameof(GetAll), new { id = result.Id }, result);
    }

    // PUT: api/badges/5
    [HttpPut("{id}")]
    public async Task<IActionResult> Update(Guid id, CreateBadgeDto dto) // On utilise le DTO pour recevoir les données
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