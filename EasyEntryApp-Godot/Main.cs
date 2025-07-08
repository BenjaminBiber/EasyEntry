using Godot;
using System;
using doorOpener.Models;

public partial class Main : Godot.PanelContainer
{
	// Called when the node enters the scene tree for the first time.
	public override void _Ready()
	{
		InitializeDevices();
		InitializeNavigation();
	}

	private void InitializeNavigation()
	{
		var myButton = GetNode<Button>("Layout/NavBar/HBoxContainer/VBoxContainer/OpenDialogButton");
		myButton.Pressed += OpenNewDeviceDialog;
	}

	private void OpenNewDeviceDialog()
	{
		var packedDialog = (PackedScene)GD.Load("res://CreateDeviceDialog.tscn");
		var dialogInstance = packedDialog.Instantiate();
		AddChild(dialogInstance);

		if (dialogInstance is CreateDeviceDialog myDialog)
		{
			myDialog.PopupCentered();
			var okButton = myDialog.GetOkButton();
			okButton.Pressed += InitializeDevices;
		}
	}
	
	private void InitializeDevices()
	{
		var devices = Device.GetAllDevicesFromJson();
		var vbox = GetNode<Godot.VBoxContainer>("Layout/MainBox");

		// ✅ 1. Bestehende Children entfernen
		foreach (Node child in vbox.GetChildren())
		{
			child.QueueFree();
		}

		// ✅ 2. Neue DeviceGroups hinzufügen
		var packedScene = (PackedScene)GD.Load("res://device_group.tscn");
		foreach (var device in devices)
		{
			var instance = packedScene.Instantiate();
			if (instance is DeviceGroup panelInstance)
			{
				panelInstance.GroupName = device.Key;
				panelInstance.Devices = new Godot.Collections.Array<doorOpener.Models.Device>(device.Value);
			}
			vbox.AddChild(instance);
		}
	}


	// Called every frame. 'delta' is the elapsed time since the previous frame.
	public override void _Process(double delta)
	{
	}
}
