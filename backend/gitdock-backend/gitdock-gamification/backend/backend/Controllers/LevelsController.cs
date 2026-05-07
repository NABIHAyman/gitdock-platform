using backend.DTOs;
using backend.Services;
using Microsoft.AspNetCore.Mvc;

namespace backend.Controllers;

[ApiController]
[Route("api/levels")]
public class LevelsController : ControllerBase
{
    private readonly ILevelService _levelService;

    public LevelsController(ILevelService levelService) => _levelService = levelService;

    [HttpGet]
    public async Task<IActionResult> GetAll() => Ok(await _levelService.GetAllWithRequirementsAsync());

    [HttpGet("{id}")]
    public async Task<IActionResult> GetById(Guid id)
    {
        var level = await _levelService.GetByIdAsync(id);
        if (level == null)
            return NotFound(new { message = $"Le niveau avec l'ID {id} n'existe pas." });
        return Ok(level);
    }

    [HttpPost]
    public async Task<IActionResult> Create([FromBody] CreateLevelDto dto)
    {
        var result = await _levelService.CreateLevelAsync(dto);
        return CreatedAtAction(nameof(GetById), new { id = result.Id }, result);
    }

    [HttpPut("{id}")]
    public async Task<IActionResult> Update(Guid id, [FromBody] CreateLevelDto dto)
    {
        var success = await _levelService.UpdateLevelAsync(id, dto);
        return success ? NoContent() : NotFound();
    }

    [HttpDelete("{id}")]
    public async Task<IActionResult> Delete(Guid id)
    {
        var success = await _levelService.DeleteLevelAsync(id);
        return success ? NoContent() : NotFound();
    }
}