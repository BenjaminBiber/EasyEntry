using doorOpener.Models;
using Microsoft.AspNetCore.Mvc;

namespace EasyEntryApi.Controllers;

using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

[ApiController]
[Route("api/[controller]")]
public class DeviceGroupController : ControllerBase
{
    private readonly AppDbContext _context;

    public DeviceGroupController(AppDbContext context)
    {
        _context = context;
    }

    // GET: api/devicegroup
    [HttpGet]
    public async Task<ActionResult<IEnumerable<DeviceGroup>>> GetAllGroups()
    {
        return await _context.DeviceGroups
            .Include(g => g.Devices)
            .ToListAsync();
    }

    // GET: api/devicegroup/5
    [HttpGet("{id}")]
    public async Task<ActionResult<DeviceGroup>> GetGroupById(int id)
    {
        var group = await _context.DeviceGroups
            .Include(g => g.Devices)
            .FirstOrDefaultAsync(g => g.Id == id);

        if (group == null)
            return NotFound();

        return group;
    }

    // POST: api/devicegroup
    [HttpPost]
    public async Task<ActionResult<DeviceGroup>> CreateGroup(DeviceGroup group)
    {
        _context.DeviceGroups.Add(group);
        await _context.SaveChangesAsync();

        return CreatedAtAction(nameof(GetGroupById), new { id = group.Id }, group);
    }

    // PUT: api/devicegroup/5
    [HttpPut("{id}")]
    public async Task<IActionResult> UpdateGroup(int id, DeviceGroup updatedGroup)
    {
        if (id != updatedGroup.Id)
            return BadRequest();

        var existingGroup = await _context.DeviceGroups
            .Include(g => g.Devices)
            .FirstOrDefaultAsync(g => g.Id == id);

        if (existingGroup == null)
            return NotFound();

        existingGroup.GroupName = updatedGroup.GroupName;

        // Option: Geräte aktualisieren
        // → hier: alle alten löschen und neu setzen
        _context.Devices.RemoveRange(existingGroup.Devices);
        existingGroup.Devices = updatedGroup.Devices;

        await _context.SaveChangesAsync();
        return NoContent();
    }

    // DELETE: api/devicegroup/5
    [HttpDelete("{id}")]
    public async Task<IActionResult> DeleteGroup(int id)
    {
        var group = await _context.DeviceGroups
            .Include(g => g.Devices)
            .FirstOrDefaultAsync(g => g.Id == id);

        if (group == null)
            return NotFound();

        _context.DeviceGroups.Remove(group);
        await _context.SaveChangesAsync();

        return NoContent();
    }
}
