using backend.DTOs;
using backend.Services;
using Microsoft.AspNetCore.Mvc;

namespace backend.Controllers;

[ApiController]
[Route("api/[controller]")]
public class TagsController : ControllerBase
{
    private readonly TagService _tagService;

    public TagsController(TagService tagService) => _tagService = tagService;

    [HttpGet]
    public async Task<ActionResult<List<TagResponseDto>>> GetAll()
    {
        // C'est _tagService qu'il faut utiliser ici !
        var result = await _tagService.GetAllTagsAsync();
        return Ok(result);
    }
    [HttpPost]
    public async Task<IActionResult> Create(CreateTagDto dto)
    {
        var tag = await _tagService.CreateTagAsync(dto);
        // Utilise CreatedAtAction pour être cohérent avec Levels
        return CreatedAtAction(nameof(GetAll), new { id = tag.Id }, tag);
    }
    // PUT: api/Tags/{id}
    [HttpPut("{id}")]
    public async Task<ActionResult<TagResponseDto>> Update(Guid id, CreateTagDto dto)
    {
        var result = await _tagService.UpdateTagAsync(id, dto);
        if (result == null) return NotFound();
        return Ok(result);
    }

    // DELETE: api/Tags/{id}
    [HttpDelete("{id}")]
    public async Task<IActionResult> Delete(Guid id)
    {
        var deleted = await _tagService.DeleteTagAsync(id);
        if (!deleted) return NotFound();

        return NoContent(); // Succès 204 (Pas de contenu à renvoyer)
    }
}