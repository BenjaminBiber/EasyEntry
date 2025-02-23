using Microsoft.AspNetCore.Components;
using MudBlazor;
using Newtonsoft.Json;
namespace doorOpener.Models;

public class Setting
{
    public static readonly string FileName = FileSystem.AppDataDirectory + "/settings.json";
    
    public static void SaveSettingToJSON(bool status)
    {
            var writeData = JsonConvert.SerializeObject(status, Formatting.Indented);
            File.WriteAllText(Setting.FileName, writeData);
    }

    public static bool GetSettingFromJSON(ISnackbar snackbar = null)
    {
        var returnBool = true;
        if (File.Exists(Setting.FileName))
        {
            try
            {
                var existingData = File.ReadAllText(Setting.FileName);
                if (!string.IsNullOrEmpty(existingData))
                {
                    returnBool = JsonConvert.DeserializeObject<bool>(existingData);
                }
            }
            catch (Exception e)
            {
                if (snackbar != null)
                {
                    snackbar.Add($"Fehler beim auslesen der Einstellungen: {e}", Severity.Error);
                }
            }
           
        }

        return returnBool;
    }
}