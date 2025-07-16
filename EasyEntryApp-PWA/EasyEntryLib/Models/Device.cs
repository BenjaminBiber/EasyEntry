using System.ComponentModel;
using System.Net;
using System.Net.Http.Json;
using System.Text;
using MudBlazor;
using Newtonsoft.Json;

namespace doorOpener.Models;

public class Device
{
    private static HttpClient? _httpClient;
    public static void SetHttpClient(HttpClient httpClient) => _httpClient = httpClient;

    public int Id { get; set; }
    public string Name { get; set; }
    public DeviceStatus Status { get; set; }
    public string DeviceURL { get; set; }

    public bool IsOpened { get; set; }

    public int DeviceGroupId { get; set; }
    public DeviceGroup? DeviceGroup { get; set; }

    public Device() { }

    public Device(string name, DeviceStatus status, string deviceUrl)
    {
        Name = name;
        Status = status;
        DeviceURL = deviceUrl;
    }

    public async Task<bool> UpdateStatus(DeviceStatus status, string url)
    {
        var jsonData = new { Status = (int)status };
        string jsonString = JsonConvert.SerializeObject(jsonData);
        return await SendPutRequest(url, jsonString);
    }

    public static async Task<DeviceResponse> TestConnection(string url)
    {
        if (_httpClient is null)
            throw new InvalidOperationException("HttpClient not set.");

        var responseObj = new DeviceResponse();
        var fullUrl = $"http://{url}";
        var proxyUrl = $"/api/Setting/proxy?url={WebUtility.UrlEncode(fullUrl)}";

        try
        {
            var response = await _httpClient.GetAsync(proxyUrl);
            if (response.IsSuccessStatusCode)
            {
                var stringResponse = await response.Content.ReadAsStringAsync();
                var jsonResponse = JsonConvert.DeserializeObject<returnDevice>(stringResponse);
                if (jsonResponse != null)
                {
                    responseObj.IsOnline = true;
                    responseObj.IsOpen = jsonResponse.IsOpen;
                    responseObj.Name = jsonResponse.name;
                }
            }
            else
            {
                Console.WriteLine("Response was not successful.");
            }
        }
        catch (HttpRequestException ex)
        {
            Console.WriteLine("Request failed. The device is probably offline: " + ex.Message);
        }

        return responseObj;
    }

    static async Task<bool> SendPostRequest(string url, string jsonContent, ISnackbar snackbar, bool showSnackBar = true)
    {
        if (_httpClient is null)
            throw new InvalidOperationException("HttpClient not set.");

        var fullUrl = $"http://{url}";
        var content = new StringContent(jsonContent, Encoding.UTF8, "application/json");

        try
        {
            var response = await _httpClient.PostAsync(fullUrl, content);

            if (response != null)
            {
                if (response.IsSuccessStatusCode && showSnackBar)
                {
                    var responseContent = await response.Content.ReadAsStringAsync();
                    snackbar.Add($"Response Content: {responseContent}", Severity.Info);
                }
                return response.IsSuccessStatusCode;
            }
            else
            {
                if (showSnackBar) snackbar.Add("No response from server", Severity.Error);
                return false;
            }
        }
        catch (Exception ex)
        {
            if (showSnackBar) snackbar.Add($"Request error: {ex.Message}", Severity.Error);
            return false;
        }
    }

    static async Task<bool> SendPutRequest(string url, string jsonContent)
    {
        if (_httpClient is null)
            throw new InvalidOperationException("HttpClient not set.");

        var fullUrl = $"http://{url}";
        var proxyUrl = $"/api/proxy?url={WebUtility.UrlEncode(fullUrl)}";
        var content = new StringContent(jsonContent, Encoding.UTF8, "application/json");

        try
        {
            var response = await _httpClient.PutAsync(proxyUrl, content);
            return response.IsSuccessStatusCode;
        }
        catch
        {
            return false;
        }
    }
}

public enum DeviceStatus
{
    [Description("geöffnet")]
    Opened = 1,
    [Description("geschlossen")]
    Closed = 2,
    [Description("Stopp")]
    Neutral = 3,
}
