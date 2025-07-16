using System.ComponentModel;
using System.Text;
using MudBlazor;
using Newtonsoft.Json;
namespace doorOpener.Models;

public class Device
{
    public int Id { get; set; }
    public string Name { get; set; }
    public DeviceStatus Status { get; set; }
    public string DeviceURL { get; set; }
    
    public bool IsOpened { get; set; }
    
    public int DeviceGroupId { get; set; } // FK
    public DeviceGroup? DeviceGroup { get; set; } // optional für Zugriff
    
    public Device() { }
    
    public Device( string name, DeviceStatus status, string deviceUrl)
    {
        Name = name;
        Status = status;
        DeviceURL = deviceUrl;
    }
    public async Task<bool> UpdateStatus(DeviceStatus status, string url)
    {
        var jsonData = new
        {
            Status = (int)status
        };

        string jsonString = JsonConvert.SerializeObject(jsonData);

        return await SendPutRequest(url, jsonString);
    }
  
    public static async Task<DeviceResponse> TestConnection(string url)
    {
        using (HttpClient client = new HttpClient())
        {
            var responseObj = new DeviceResponse();
            url = $"http://{url}";
            try
            {
                HttpResponseMessage response = await client.GetAsync(url);
                if(response.IsSuccessStatusCode)
                {
                    var stringResponse = await response.Content.ReadAsStringAsync();
                    var jsonResponse = JsonConvert.DeserializeObject<returnDevice>(stringResponse);
                    if (jsonResponse != null)
                    {
                        responseObj.IsOnline = true;
                        responseObj.IsOpen = jsonResponse.IsOpen;
                        responseObj.Name = jsonResponse.name;
                        return responseObj;
                    }

                    return responseObj;
                }
                else
                {
                    return responseObj;
                }
            }
            catch (HttpRequestException)
            {
                return responseObj;
            }
        }
    }
    
    static async Task<bool> SendPostRequest(string url, string jsonContent, ISnackbar snackbar, bool showSnackBar = true)
    {
        using (HttpClient client = new HttpClient())
        {
            url = $"http://{url}";
            try
            {
                var content = new StringContent(jsonContent, Encoding.UTF8, "application/json");

                HttpResponseMessage response = await client.PostAsync(url, content);

                if (response != null)
                {
                    Console.WriteLine($"Status Code: {response.StatusCode}");

                    if (response.IsSuccessStatusCode)
                    {
                        string responseContent = await response.Content.ReadAsStringAsync();
                        if (showSnackBar)
                        {
                            snackbar.Add($"Response Content: {responseContent}", Severity.Info);
                        }
                    }

                    return response.IsSuccessStatusCode;
                }
                else
                {
                    if (showSnackBar)
                    {
                        snackbar.Add("No response from server", Severity.Error);
                    }

                    return false;
                }
            }
            catch (HttpRequestException e)
            {
                if (showSnackBar)
                {
                    snackbar.Add($"Request error: {e.Message}",Severity.Error);
                }
                return false;
            }
            catch (Exception ex)
            {
                if (showSnackBar)
                {
                    snackbar.Add($"Error: {ex.Message}", Severity.Error);;
                }
                return false;
            }
        }
    }

    
    static async Task<bool> SendPutRequest(string url, string jsonContent)
    {
        using (HttpClient client = new HttpClient())
        {
            url = $"http://{url}";
            var content = new StringContent(jsonContent, Encoding.UTF8, "application/json");

            HttpResponseMessage response = await client.PutAsync(url, content);
            return response.IsSuccessStatusCode;
        }
    }
}

public enum DeviceStatus
{
    [Description ("geöffnet")]
    Opened = 1,
    [Description ("geschlossen")]
    Closed = 2,
    [Description ("Stopp")]
    Neutral = 3,
}