package com.wistron.pqaa_common.jar.autotest;

public class WisSDCard_Service {
	public static final String ACTION_SDCARD_SERVICE = "com.wistron.sdcard";
	// broadcast
	public static final String ACTION_SDCARD_STATE_CHANGED = "com.wistron.action.sdcard.state.changed";
	public static final String EXTRA_SDCARD_STATE = "state";
	public static final String EXTRA_SDCARD_PATH = "sdcard_path";
	public static final String EXTRA_FLAG = "flag";
	public static final String EXTRA_PROGRESS = "progress";
	public static final String EXTRA_EXCEPTION = "exception";
	public static final int SDCARD_STATE_TEST_START = 0;
	public static final int SDCARD_STATE_TEST_ABORT = 1;
	public static final int SDCARD_STATE_TEST_PROGRESS_CHANGE = 2;
	public static final int SDCARD_STATE_DELETE_START = 3;
	public static final int SDCARD_STATE_DELETE_PROGRESS_CHANGE = 4;
	public static final int SDCARD_STATE_RESULT_FAIL = 5;
	public static final int SDCARD_STATE_TEST_DONE = 6;
	// activity to service do action
	public static final String EXTRA_SDCARD_SERVICE_DO_ACTION = "action";
	public static final int SDCARD_SERVICE_START_TEST = 0;
	public static final int SDCARD_SERVICE_STOP_TEST = 1;
}
