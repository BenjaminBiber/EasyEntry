namespace doorOpener.Models;

public enum StepType
{
    DeviceAction = 1,
    Delay = 2
}

public class Routine
{
    public int Id { get; set; }
    public string Name { get; set; } = string.Empty;
    public List<RoutineStep> Steps { get; set; } = new();
}

public class RoutineStep
{
    public int Id { get; set; }
    public int Order { get; set; }
    public StepType StepType { get; set; }
    public int? DeviceId { get; set; }
    public Device? Device { get; set; }
    public DeviceStatus? Action { get; set; }
    public int DurationSeconds { get; set; }
    public int RoutineId { get; set; }
    public Routine? Routine { get; set; }
}

