using doorOpener.Models;
using System.Net.Http.Json;
using Microsoft.Extensions.Logging;

namespace EasyEntryLib.Services;

public class RoutineService
{
    private readonly HttpClient _http;
    private readonly ILogger<RoutineService> _logger;

    public RoutineService(HttpClient http, ILogger<RoutineService> logger)
    {
        _http = http;
        _logger = logger;
    }

    public async Task<List<Routine>> GetAllAsync()
    {
        try
        {
            return await _http.GetFromJsonAsync<List<Routine>>("api/routine") ?? new();
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Fehler beim Abrufen aller Routinen");
            return new();
        }
    }

    public async Task<Routine?> GetByIdAsync(int id)
    {
        try
        {
            var response = await _http.GetAsync($"api/routine/{id}");
            if (!response.IsSuccessStatusCode)
            {
                _logger.LogWarning("Routine mit ID {Id} nicht gefunden. Status: {Status}", id, response.StatusCode);
                return null;
            }
            return await response.Content.ReadFromJsonAsync<Routine>();
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Fehler beim Abrufen der Routine ID {Id}", id);
            return null;
        }
    }

    public async Task<bool> CreateAsync(Routine routine)
    {
        try
        {
            var response = await _http.PostAsJsonAsync("api/routine", routine);
            return response.IsSuccessStatusCode;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Fehler beim Erstellen einer Routine");
            return false;
        }
    }

    public async Task<bool> UpdateAsync(Routine routine)
    {
        try
        {
            var response = await _http.PutAsJsonAsync($"api/routine/{routine.Id}", routine);
            return response.IsSuccessStatusCode;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Fehler beim Aktualisieren der Routine ID {Id}", routine.Id);
            return false;
        }
    }

    public async Task<bool> DeleteAsync(int id)
    {
        try
        {
            var response = await _http.DeleteAsync($"api/routine/{id}");
            return response.IsSuccessStatusCode;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Fehler beim Löschen der Routine ID {Id}", id);
            return false;
        }
    }
}

