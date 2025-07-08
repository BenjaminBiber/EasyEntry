using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.IO;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;
using Godot;
using Newtonsoft.Json;
using HttpClient = System.Net.Http.HttpClient;

namespace doorOpener.Models;

public partial class Device: Resource
{
    [Export]
    public string Name { get; set; }
    
    [Export]
    public DeviceStatus Status { get; set; }
    
    [Export]
    public string DeviceURL { get; set; }
    
    [Export]
    public bool IsOpened { get; set; }

    public static string FileName => ProjectSettings.GlobalizePath("user://Devices.json");
    public Device( string name, DeviceStatus status, string deviceUrl)
    {
        Name = name;
        Status = status;
        DeviceURL = deviceUrl;
    }

    public static void SaveDeviceToJSON(KeyValuePair<string, Device> device)
    {
        Dictionary<string, List<Device>> devices = new Dictionary<string, List<Device>>();
        if (File.Exists(FileName))
        { 
            var existingData = File.ReadAllText(FileName);
            if (!string.IsNullOrEmpty(existingData))
            {
                devices = JsonConvert.DeserializeObject<Dictionary<string, List<Device>>>(existingData);
            }
        }

        if (devices.ContainsKey(device.Key))
        {
            devices[device.Key].Add(device.Value);
        }
        else
        {
            devices.Add(device.Key, new List<Device>(){device.Value});
        }

        var writeData = JsonConvert.SerializeObject(devices, Formatting.Indented);
        File.WriteAllText(Device.FileName, writeData);
    }
    
    public static void SaveDevicesToJSON(Dictionary<string, List<Device>> dic)
    {
        Dictionary<string, List<Device>> devices = new Dictionary<string, List<Device>>();
        var writeData = JsonConvert.SerializeObject(dic, Formatting.Indented);
        File.WriteAllText(Device.FileName, writeData);
    }

    public static Dictionary<string, List<Device>> GetAllDevicesFromJson()
    {
        Dictionary<string, List<Device>> devices = [];
        if (File.Exists(FileName))
        { 
            var existingData = File.ReadAllText(Device.FileName);
            if (!string.IsNullOrEmpty(existingData))
            {
                devices = JsonConvert.DeserializeObject<Dictionary<string, List<Device>>>(existingData);
            }
        }

        return devices;
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
    
    static async Task<bool> SendPostRequest(string url, string jsonContent)
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
                    }

                    return response.IsSuccessStatusCode;
                }
                else
                {
                    return false;
                }
            }
            catch (HttpRequestException e)
            {
                return false;
            }
            catch (Exception ex)
            {
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