namespace doorOpener.Models;

public class DeviceGroup
{
    public int Id { get; set; }
    public string GroupName { get; set; } = string.Empty;
    public List<Device> Devices { get; set; } = new();
}