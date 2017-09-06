package com.wistron.pqaa_common.jar.autotest;

public class WisGPS_Service {
	public static final String ACTION_GPS_SERVICE = "com.wistron.gps";
	// broadcast
	public static final String ACTION_GPS_STATE_CHANGED = "com.wistron.action.gps.state.changed";
	public static final String EXTRA_GPS_STATE = "state";
	public static final String EXTRA_GPS_SATELLITE_LIST = "satellite_list";
	public static final String EXTRA_GPS_LOCATION_DATA = "location_data";
	public static final int GPS_STATE_SATELLITE_CHANGED = 0;
	public static final int GPS_STATE_LOCATION_CHANGED = 1;
	// activity to service do action
	public static final String EXTRA_GPS_SERVICE_DO_ACTION = "action";
	public static final int GPS_SERVICE_OPEN_GPS = 0;
	public static final int GPS_SERVICE_SEARCH_SATELLITE = 1;
	public static final int GPS_SERVICE_SEARCH_LOCATION = 2;
	public static final int GPS_SERVICE_CLOSE_GPS = 3;
}
