using doorOpener.Models;

namespace EasyEntryLib.Services;
using System.Net.Http.Json;
using Microsoft.Extensions.Logging;

public class DeviceService
{
    private readonly HttpClient _http;
    private readonly ILogger<DeviceService> _logger;

    public DeviceService(HttpClient http, ILogger<DeviceService> logger)
    {
        _http = http;
        _logger = logger;
    }

    // GET: Alle Geräte
    public async Task<List<Device>> GetAllAsync()
    {
        try
        {
            return await _http.GetFromJsonAsync<List<Device>>("api/device") ?? new();
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Fehler beim Abrufen der Geräteliste");
            return new();
        }
    }

    // GET: Einzelnes Gerät
    public async Task<Device?> GetByIdAsync(int id)
    {
        try
        {
            var response = await _http.GetAsync($"api/device/{id}");

            if (!response.IsSuccessStatusCode)
            {
                _logger.LogWarning("Gerät mit ID {Id} nicht gefunden. Status: {Status}", id, response.StatusCode);
                return null;
            }

            return await response.Content.ReadFromJsonAsync<Device>();
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Fehler beim Abrufen des Geräts mit ID {Id}", id);
            return null;
        }
    }

    // POST: Neues Gerät hinzufügen
    public async Task<bool> CreateAsync(Device device)
    {
        try
        {
            var response = await _http.PostAsJsonAsync("api/device", device);
            return response.IsSuccessStatusCode;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Fehler beim Erstellen eines neuen Geräts");
            return false;
        }
    }

    // PUT: Bestehendes Gerät aktualisieren
    public async Task<bool> UpdateAsync(Device device)
    {
        try
        {
            var response = await _http.PutAsJsonAsync($"api/device/{device.Id}", device);
            return response.IsSuccessStatusCode;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Fehler beim Aktualisieren des Geräts mit ID {Id}", device.Id);
            return false;
        }
    }

    // DELETE: Gerät löschen
    public async Task<bool> DeleteAsync(int id)
    {
        try
        {
            var response = await _http.DeleteAsync($"api/device/{id}");
            return response.IsSuccessStatusCode;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Fehler beim Löschen des Geräts mit ID {Id}", id);
            return false;
        }
    }
}
