using Godot;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text.RegularExpressions;
using System.Threading.Tasks;
using doorOpener.Models;

public partial class CreateDeviceDialog : AcceptDialog
{
	public override void _Ready()
	{
		var okButton = GetOkButton();
		if (okButton.IsConnected("pressed", new Callable(this, nameof(OnConfirmed))))
		{
			okButton.Disconnect("pressed", new Callable(this, nameof(OnConfirmed)));
		}
		else
		{
			okButton.Pressed += OnConfirmed;
		}
	}

	private async void OnConfirmed()
	{
		//await SaveDevice();
		await SaveTestDevice();
	}

	private async Task SaveTestDevice()
	{
		var deviceToSaveUrl = GetNode<LineEdit>("VBoxContainer/NameInput").Text.Trim();

		if (!String.IsNullOrEmpty(deviceToSaveUrl))
		{
			var kvp = new KeyValuePair<string, Device>("Test", new Device("TestDevice", DeviceStatus.Neutral, deviceToSaveUrl));
			Device.SaveDeviceToJSON(kvp);
		}
		Hide();
	}
	
	private async Task SaveDevice()
	{
		var deviceToSaveUrl = GetNode<LineEdit>("VBoxContainer/NameInput").Text.Trim();

		try
		{
			if (!string.IsNullOrEmpty(deviceToSaveUrl))
			{
				var devices = Device.GetAllDevicesFromJson();
				if(devices.Values.Any(x => x.Any(y => y.DeviceURL == deviceToSaveUrl)))
				{
					return;
				}
				var response = await Device.TestConnection(deviceToSaveUrl);
				if (response.IsOnline)
				{
					if (!String.IsNullOrEmpty(response.Name))
					{

						if (Regex.IsMatch(deviceToSaveUrl, "^(?:(?:25[0-5]|2[0-4]\\d|1?\\d{1,2})(?:\\.(?!$)|$)){4}$"))
						{
							var deviceToSave = new Device(response.Name, DeviceStatus.Neutral, deviceToSaveUrl);
							Device.SaveDeviceToJSON(new KeyValuePair<string, Device>("ungruppiert", deviceToSave));
						}
						else
						{
							return;
						}

					}
					else if (String.IsNullOrEmpty(response.Name))
					{
						if (Regex.IsMatch(deviceToSaveUrl, "^(?:(?:25[0-5]|2[0-4]\\d|1?\\d{1,2})(?:\\.(?!$)|$)){4}$"))
						{
							var deviceToSave = new Device(response.Name, DeviceStatus.Neutral, deviceToSaveUrl);
							Device.SaveDeviceToJSON(new KeyValuePair<string, Device>("ungruppiert", deviceToSave));
						}
						else
						{
							return;
						}
					}
					else
					{
						return;
					}

				}
				else
				{
					return;
				}
			}
			else
			{
				return;
			}
			Hide();
		}
		catch (Exception exception)
		{
			GD.Print(exception.Message);	
		}
		
	}
}
