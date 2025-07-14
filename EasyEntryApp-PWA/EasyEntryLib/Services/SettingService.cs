using doorOpener.Models;

namespace EasyEntryLib.Services;
using System.Net.Http.Json;
using Microsoft.Extensions.Logging;

public class SettingService
{
    private readonly HttpClient _http;
    private readonly ILogger<SettingService> _logger;

    public SettingService(HttpClient http, ILogger<SettingService> logger)
    {
        _http = http;
        _logger = logger;
    }

    // GET: Alle Settings
    public async Task<List<Setting>> GetAllAsync()
    {
        try
        {
            return await _http.GetFromJsonAsync<List<Setting>>("api/setting") ?? new();
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Fehler beim Abrufen der Einstellungen");
            return new();
        }
    }

    // GET: Einzelnes Setting
    public async Task<Setting?> GetByIdAsync(int id)
    {
        try
        {
            var response = await _http.GetAsync($"api/setting/{id}");

            if (!response.IsSuccessStatusCode)
            {
                _logger.LogWarning("Setting mit ID {Id} nicht gefunden. Status: {Status}", id, response.StatusCode);
                return null;
            }

            return await response.Content.ReadFromJsonAsync<Setting>();
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Fehler beim Abrufen von Setting ID {Id}", id);
            return null;
        }
    }

    // POST: Neue Einstellung hinzufügen
    public async Task<bool> CreateAsync(Setting setting)
    {
        try
        {
            var response = await _http.PostAsJsonAsync("api/setting", setting);
            return response.IsSuccessStatusCode;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Fehler beim Erstellen eines Settings");
            return false;
        }
    }

    // PUT: Setting aktualisieren
    public async Task<bool> UpdateAsync(Setting setting)
    {
        try
        {
            var response = await _http.PutAsJsonAsync($"api/setting/{setting.Id}", setting);
            return response.IsSuccessStatusCode;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Fehler beim Aktualisieren von Setting ID {Id}", setting.Id);
            return false;
        }
    }

    // DELETE: Setting löschen
    public async Task<bool> DeleteAsync(int id)
    {
        try
        {
            var response = await _http.DeleteAsync($"api/setting/{id}");
            return response.IsSuccessStatusCode;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Fehler beim Löschen von Setting ID {Id}", id);
            return false;
        }
    }
}

