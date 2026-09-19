// ContributorsController.cs
using backend.Services;
using Microsoft.AspNetCore.Mvc;

namespace backend.Controllers;

[ApiController]
[Route("api/[controller]")]
public class ContributorsController : ControllerBase
{
    private readonly IContributorService _contributorService;

    public ContributorsController(IContributorService contributorService)
    {
        _contributorService = contributorService;
    }

    [HttpGet]
    public async Task<IActionResult> GetAllContributors()
    {
        var result = await _contributorService.GetAllContributorsAsync();
        return Ok(result);
    }

    [HttpGet("by-project")]
    public async Task<IActionResult> GetContributorsByProject()
    {
        var result = await _contributorService.GetContributorsByProjectAsync();
        return Ok(result);
    }
}