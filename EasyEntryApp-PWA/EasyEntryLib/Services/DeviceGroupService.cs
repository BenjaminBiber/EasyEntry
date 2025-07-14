using System.Net.Http.Json;
using doorOpener.Models;
using Microsoft.Extensions.Logging;

namespace EasyEntryLib.Services;

public class DeviceGroupService
{
    private readonly HttpClient _http;
    private readonly ILogger<DeviceGroupService> _logger;

    public DeviceGroupService(HttpClient http, ILogger<DeviceGroupService> logger)
    {
        _http = http;
        _logger = logger;
    }

    // GET: Alle Gruppen inkl. Devices
    public async Task<List<DeviceGroup>> GetAllAsync()
    {
        try
        {
            return await _http.GetFromJsonAsync<List<DeviceGroup>>("api/devicegroup") ?? new();
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Fehler beim Abrufen aller DeviceGroups");
            return new();
        }
    }

    // GET: Einzelne Gruppe nach ID
    public async Task<DeviceGroup?> GetByIdAsync(int id)
    {
        try
        {
            var response = await _http.GetAsync($"api/devicegroup/{id}");

            if (!response.IsSuccessStatusCode)
            {
                _logger.LogWarning("DeviceGroup mit ID {Id} nicht gefunden (Status {StatusCode})", id, response.StatusCode);
                return null;
            }

            return await response.Content.ReadFromJsonAsync<DeviceGroup>();
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Fehler beim Abrufen der DeviceGroup mit ID {Id}", id);
            return null;
        }
    }

    // POST: Neue Gruppe anlegen
    public async Task<bool> CreateAsync(DeviceGroup group)
    {
        try
        {
            var response = await _http.PostAsJsonAsync("api/devicegroup", group);
            return response.IsSuccessStatusCode;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Fehler beim Erstellen einer DeviceGroup");
            return false;
        }
    }

    // PUT: Gruppe inkl. Geräte aktualisieren
    public async Task<bool> UpdateAsync(DeviceGroup group)
    {
        try
        {
            var response = await _http.PutAsJsonAsync($"api/devicegroup/{group.Id}", group);
            return response.IsSuccessStatusCode;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Fehler beim Aktualisieren der DeviceGroup ID {Id}", group.Id);
            return false;
        }
    }

    // DELETE: Gruppe löschen
    public async Task<bool> DeleteAsync(int id)
    {
        try
        {
            var response = await _http.DeleteAsync($"api/devicegroup/{id}");
            return response.IsSuccessStatusCode;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Fehler beim Löschen der DeviceGroup ID {Id}", id);
            return false;
        }
    }
}