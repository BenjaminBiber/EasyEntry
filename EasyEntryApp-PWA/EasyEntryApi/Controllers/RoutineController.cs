using doorOpener.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace EasyEntryApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class RoutineController : ControllerBase
{
    private readonly AppDbContext _context;

    public RoutineController(AppDbContext context)
    {
        _context = context;
    }

    [HttpGet]
    public async Task<ActionResult<IEnumerable<Routine>>> GetAll()
    {
        return await _context.Routines
            .Include(r => r.Steps)
            .ToListAsync();
    }

    [HttpGet("{id}")]
    public async Task<ActionResult<Routine>> GetById(int id)
    {
        var routine = await _context.Routines
            .Include(r => r.Steps)
            .FirstOrDefaultAsync(r => r.Id == id);

        if (routine == null)
            return NotFound();

        return routine;
    }

    [HttpPost]
    public async Task<ActionResult<Routine>> CreateRoutine(Routine routine)
    {
        _context.Routines.Add(routine);
        await _context.SaveChangesAsync();
        return CreatedAtAction(nameof(GetById), new { id = routine.Id }, routine);
    }

    [HttpPut("{id}")]
    public async Task<IActionResult> UpdateRoutine(int id, Routine routine)
    {
        if (id != routine.Id)
            return BadRequest();

        var existing = await _context.Routines
            .Include(r => r.Steps)
            .FirstOrDefaultAsync(r => r.Id == id);
        if (existing == null)
            return NotFound();

        existing.Name = routine.Name;
        _context.RoutineSteps.RemoveRange(existing.Steps);
        existing.Steps = routine.Steps;

        await _context.SaveChangesAsync();
        return NoContent();
    }

    [HttpDelete("{id}")]
    public async Task<IActionResult> DeleteRoutine(int id)
    {
        var routine = await _context.Routines
            .Include(r => r.Steps)
            .FirstOrDefaultAsync(r => r.Id == id);

        if (routine == null)
            return NotFound();

        _context.Routines.Remove(routine);
        await _context.SaveChangesAsync();
        return NoContent();
    }
}

