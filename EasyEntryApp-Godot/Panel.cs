using Godot;
using System;
using System.Threading.Tasks;
using doorOpener.Models;

public partial class Panel : HBoxContainer
{
	[Export] public Device Device { get; set; } = null;

	private Button _upButton;
	private Button _downButton;
	private Button _stopButton;
	private Godot.Panel _statusPanel;
	
	public override void _Ready()
	{
		InitilizeName();
		GetButtons();
		_statusPanel = GetNode<Godot.Panel>("PanelContainer/LayoutBox/NamingBox/HBoxContainer/Status");
		InitilizeStatus();
	}

	private async void InitilizeStatus()
	{
		SetStatusStyles(false);
		if (Device != null)
		{
			var result = await Device.TestConnection(Device.DeviceURL);
			SetStatusStyles(result.IsOnline);
			if (result.IsOnline)
			{
				ActivateButtons();
			}
		}
	}

	private void ActivateButtons()
	{
		_upButton.Disabled = false;
		_downButton.Disabled = false;
		_stopButton.Disabled = false;
	}
	
	private void SetStatusStyles(bool isOnline)
	{
		var styleBox = new StyleBoxFlat();
		styleBox.CornerRadiusTopRight = 100;
		styleBox.CornerRadiusBottomLeft = 100;
		styleBox.CornerRadiusBottomRight = 100;
		styleBox.CornerRadiusTopLeft = 100;
		if (isOnline)
		{
			styleBox.BgColor = new Color(0, 176, 62);
		}
		else
		{
			styleBox.BgColor = new Color(176, 0, 23);
		}

		_statusPanel.AddThemeStyleboxOverride("panel", styleBox);
	}
	
	private void GetButtons()
	{
		_downButton = GetNode<Button>("PanelContainer/LayoutBox/ButtonBox/upButton");
		_upButton = GetNode<Button>("PanelContainer/LayoutBox/ButtonBox/downButton");
		_stopButton = GetNode<Button>("PanelContainer/LayoutBox/ButtonBox/stopButton");

		_upButton.Pressed += OpenDoor;
		_downButton.Pressed += CloseDoor;
		_stopButton.Pressed += StopDoor;
	}
	
	public async void StopDoor()
	{
		var result= await Device.UpdateStatus(DeviceStatus.Neutral, Device.DeviceURL);
	}

	public async void OpenDoor()
	{
		var result= await Device.UpdateStatus(DeviceStatus.Opened, Device.DeviceURL);
	}

	public async void CloseDoor()
	{
		var result = await Device.UpdateStatus(DeviceStatus.Closed, Device.DeviceURL);
	}
	
	private void InitilizeName()
	{
		if (Device != null)
		{
			var namebox = GetNode<Godot.Label>("PanelContainer/LayoutBox/NamingBox/HBoxContainer/Name");
			var ipbox = GetNode<Godot.Label>("PanelContainer/LayoutBox/NamingBox/IP");
			namebox.Text = Device.Name;
			ipbox.Text = Device.DeviceURL;
		}
	}
}
