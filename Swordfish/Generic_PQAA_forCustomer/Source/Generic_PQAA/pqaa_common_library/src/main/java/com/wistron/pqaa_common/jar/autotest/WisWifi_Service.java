package com.wistron.pqaa_common.jar.autotest;

public class WisWifi_Service {
	public static final String ACTION_WIFI_SERVICE = "com.wistron.wifi";
	// broadcast
	public static final String ACTION_WIFI_CONNECT_STATE_CHANGED = "com.wistron.action.wifi.connect.changed";
	public static final String ACTION_WIFI_ENABLE_STATE_CHANGED = "com.wistron.action.wifi.enable.changed";
	public static final String ACTION_WIFI_PING_STATE_CHANGED = "com.wistron.action.wifi.ping.changed";
	
	public static final String EXTRA_WIFI_PING_STATE = "ping_state";
	public static final String EXTRA_PING_DATA = "data";
	public static final String EXTRA_PING_RESULT = "result";
	public static final int WIFI_PING_STATE_START = 0;
	public static final int WIFI_PING_STATE_DATA_CHANGED = 1;
	public static final int WIFI_PING_STATE_FINISH = 2;
	
	public static final int WIFI_CONNECT_STATE_UNKNOWN = 0;
	public static final int WIFI_CONNECT_STATE_SUSPENDED = 1;
	public static final int WIFI_CONNECT_STATE_DISCONNECTING = 2;
	public static final int WIFI_CONNECT_STATE_DISCONNECTED = 3;
	public static final int WIFI_CONNECT_STATE_CONNECTING = 4;
	public static final int WIFI_CONNECT_STATE_CONNECTED = 5;
}
