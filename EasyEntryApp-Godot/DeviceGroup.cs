using Godot;
using System;
using System.Collections.Generic;
using System.Linq;
using doorOpener.Models;

public partial class DeviceGroup : VBoxContainer
{
	[Export]
	public string GroupName { get; set; }

	[Export]
	public Godot.Collections.Array<Device> Devices { get; set; }

	private bool isOpened = false;
	private Button menuButton;
	
	// Called when the node enters the scene tree for the first time.
	public override void _Ready()
	{
		InitilizeDevices();
		InitilizeName();
		menuButton = GetNode<Button>("HBoxContainer/Button");
		menuButton.Pressed += OnButtonClick;
	}

	private void InitilizeName()
	{
		var label = GetNode<Label>("HBoxContainer/GroupName");
		if (label != null)
		{
			label.Text = GroupName;
		}
	}
	
	private void InitilizeDevices()
	{
		var vbox = GetNode<Godot.VBoxContainer>("Panels");
		var packedScene = (PackedScene)GD.Load("res://panel.tscn");
		foreach(var device in Devices.ToList())
		{
			var instance = packedScene.Instantiate();
			if (instance is Panel panelInstance)
			{
				panelInstance.Device = device;
			}
			vbox.AddChild(instance);
		}
	}

	private void OnButtonClick()
	{
		var vbox = GetNode<Godot.VBoxContainer>("Panels");
		if (isOpened)
		{
			menuButton.Icon = GD.Load<Texture2D>("res://stat_minus_1_24dp_000000_FILL0_wght400_GRAD0_opsz24.svg");
			vbox.Visible = true;
		}
		else
		{
			menuButton.Icon = GD.Load<Texture2D>("res://keyboard_control_key_24dp_000000_FILL0_wght400_GRAD0_opsz24.svg");
			vbox.Visible = false;
		}
		isOpened = !isOpened;
	}
}
