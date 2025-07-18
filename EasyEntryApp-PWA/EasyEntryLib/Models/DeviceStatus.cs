namespace doorOpener.Models;

public class DeviceResponse
{
    public bool IsOnline { get; set; }
    public bool IsOpen { get; set; }
    public string Name { get; set; } = string.Empty;
    public string? ErrorMessage { get; set; }
}