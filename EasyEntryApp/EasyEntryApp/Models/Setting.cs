using Newtonsoft.Json;

namespace doorOpener.Models;

public class Setting
{
    public static readonly string FileName = FileSystem.AppDataDirectory + "/settings.json";

    public static void SaveSettingToJSON(bool status)
    {
        // var thingToSave = new return_Setting();
        // thingToSave.ShowSnackBar = status;
            var writeData = JsonConvert.SerializeObject(status, Formatting.Indented);
            File.WriteAllText(Setting.FileName, writeData);
    }

    public static bool GetSettingFromJSON()
    {
        var returnBool = true;
        if (File.Exists(Setting.FileName))
        { 
            var existingData = File.ReadAllText(Setting.FileName);
            if (!string.IsNullOrEmpty(existingData))
            {
                returnBool = JsonConvert.DeserializeObject<bool>(existingData);
            }
        }

        return returnBool;
    }
}