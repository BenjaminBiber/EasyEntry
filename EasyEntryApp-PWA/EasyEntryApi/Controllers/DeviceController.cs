using doorOpener.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace EasyEntryApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class DeviceController : ControllerBase
{
    private readonly AppDbContext _context;

    public DeviceController(AppDbContext context)
    {
        _context = context;
    }

    // GET: api/device
    [HttpGet]
    public async Task<ActionResult<IEnumerable<Device>>> GetDevices()
    {
        return await _context.Devices.ToListAsync();
    }

    // GET: api/device/5
    [HttpGet("{id}")]
    public async Task<ActionResult<Device>> GetDevice(int id)
    {
        var device = await _context.Devices.FindAsync(id);

        if (device == null)
            return NotFound();

        return device;
    }

    // POST: api/device
    [HttpPost]
    public async Task<ActionResult<Device>> CreateDevice(Device device)
    {
        _context.Devices.Add(device);
        await _context.SaveChangesAsync();

        return CreatedAtAction(nameof(GetDevice), new { id = device.Id }, device);
    }

    // DELETE: api/device/5
    [HttpDelete("{id}")]
    public async Task<IActionResult> DeleteDevice(int id)
    {
        var device = await _context.Devices.FindAsync(id);
        if (device == null)
            return NotFound();

        _context.Devices.Remove(device);
        await _context.SaveChangesAsync();

        return NoContent();
    }
}