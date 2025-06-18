using Newtonsoft.Json;
namespace doorOpener.Models;

public class Routine
{
    public string Name { get; set; } = string.Empty;
    public List<RoutineStep> Steps { get; set; } = [];

    public static readonly string FileName = FileSystem.AppDataDirectory + "/routines.json";

    public static Dictionary<string, Routine> GetRoutinesFromJson()
    {
        var routines = new Dictionary<string, Routine>();
        if (File.Exists(FileName))
        {
            var json = File.ReadAllText(FileName);
            if (!string.IsNullOrEmpty(json))
            {
                routines = JsonConvert.DeserializeObject<Dictionary<string, Routine>>(json);
            }
        }
        return routines;
    }

    public static void SaveRoutinesToJson(Dictionary<string, Routine> routines)
    {
        var json = JsonConvert.SerializeObject(routines, Formatting.Indented);
        File.WriteAllText(FileName, json);
    }
}

public class RoutineStep
{
    public StepType StepType { get; set; }
    public string DeviceName { get; set; } = string.Empty;
    public DeviceStatus Status { get; set; }
    public int DelaySeconds { get; set; }
}

public enum StepType
{
    Action,
    Wait
}
