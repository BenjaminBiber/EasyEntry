using doorOpener.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace EasyEntryApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class SettingController : ControllerBase
{
    private readonly AppDbContext _context;

    public SettingController(AppDbContext context)
    {
        _context = context;
    }

    // GET: api/setting
    [HttpGet]
    public async Task<ActionResult<IEnumerable<Setting>>> GetSettings()
    {
        return await _context.Settings.ToListAsync();
    }

    // GET: api/setting/1
    [HttpGet("{id}")]
    public async Task<ActionResult<Setting>> GetSetting(int id)
    {
        var setting = await _context.Settings.FindAsync(id);

        if (setting == null)
            return NotFound();

        return setting;
    }

    // POST: api/setting
    [HttpPost]
    public async Task<ActionResult<Setting>> CreateSetting(Setting setting)
    {
        _context.Settings.Add(setting);
        await _context.SaveChangesAsync();

        return CreatedAtAction(nameof(GetSetting), new { id = setting.Id }, setting);
    }

    // PUT: api/setting/1
    [HttpPut("{id}")]
    public async Task<IActionResult> UpdateSetting(int id, Setting setting)
    {
        if (id != setting.Id)
            return BadRequest();

        _context.Entry(setting).State = EntityState.Modified;

        try
        {
            await _context.SaveChangesAsync();
        }
        catch (DbUpdateConcurrencyException)
        {
            if (!_context.Settings.Any(e => e.Id == id))
                return NotFound();

            throw;
        }

        return NoContent();
    }

    // DELETE: api/setting/1
    [HttpDelete("{id}")]
    public async Task<IActionResult> DeleteSetting(int id)
    {
        var setting = await _context.Settings.FindAsync(id);
        if (setting == null)
            return NotFound();

        _context.Settings.Remove(setting);
        await _context.SaveChangesAsync();

        return NoContent();
    }
}