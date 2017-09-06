package com.wistron.generic.autotest.internal.bluetooth;

interface IBluetoothService {
	boolean isBluetoothCanUse();
	boolean isBluetoothEnable();
	String  getBluetoothAddress();
	boolean isDiscovering();
}