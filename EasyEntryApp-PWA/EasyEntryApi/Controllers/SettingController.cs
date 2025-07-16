using System.Text;
using doorOpener.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Newtonsoft.Json;

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
    
    [HttpGet("proxy")]
    public async Task<IActionResult> ProxyToDevice([FromQuery] string url)
    {
        try
        {
            using var client = new HttpClient();
            var response = await client.GetAsync(url);
            var content = await response.Content.ReadAsStringAsync();

            var contentType = response.Content.Headers.ContentType?.ToString() ?? "text/plain";
            return Content(content, contentType);
        }
        catch (Exception ex)
        {
            return StatusCode(500, $"Fehler beim Weiterleiten: {ex.Message}");
        }
    }

    [HttpPut("proxy")]
    public async Task<IActionResult> ProxyPut([FromQuery] string url, [FromBody] object body)
    {
        try
        {
            using var client = new HttpClient();
            var content = new StringContent(JsonConvert.SerializeObject(body), Encoding.UTF8, "application/json");
            var response = await client.PutAsync(url, content);
            var resultContent = await response.Content.ReadAsStringAsync();

            return Content(resultContent, response.Content.Headers.ContentType?.ToString() ?? "application/json");
        }
        catch (Exception ex)
        {
            return StatusCode(500, $"Fehler beim Weiterleiten: {ex.Message}");
        }
    }

}