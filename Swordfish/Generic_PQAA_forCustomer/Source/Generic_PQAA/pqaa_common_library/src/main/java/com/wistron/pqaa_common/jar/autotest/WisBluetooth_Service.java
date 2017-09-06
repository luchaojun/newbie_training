package com.wistron.pqaa_common.jar.autotest;

public class WisBluetooth_Service {
	public static final String ACTION_BLUETOOTH_SERVICE = "com.wistron.bluetooth";
	// broadcast
	public static final String ACTION_BLUETOOTH_STATE_CHANGED = "com.wistron.action.bluetooth.state.changed";
	public static final String EXTRA_BLUETOOTH_STATE = "state";
	public static final String EXTRA_DATA = "data";
	public static final int BLUETOOTH_STATE_ENABLED = -1;
	public static final int BLUETOOTH_STATE_START_DISCOVERY = 0;
	public static final int BLUETOOTH_STATE_START_FIND_PAIRED_DEVICES = 1;
	public static final int BLUETOOTH_STATE_START_FIND_NEW_DEVICES = 2;
	public static final int BLUETOOTH_STATE_FOUND_PAIRED_DEVICES = 3;
	public static final int BLUETOOTH_STATE_FOUND_NEW_DEVICES = 4;
	public static final int BLUETOOTH_STATE_SCAN_FINISH = 5;
	// activity to service do action
	public static final String EXTRA_BLUETOOTH_SERVICE_DO_ACTION = "action";
	public static final int BLUETOOTH_SERVICE_START_TEST = 0;
	public static final int BLUETOOTH_SERVICE_STOP_TEST = 1;
	public static final int BLUETOOTH_SERVICE_OPEN_BT = 2;
	public static final int BLUETOOTH_SERVICE_CLOSE_BT = 3;
}
