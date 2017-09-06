package com.wistron.pqaa_common.jar.autotest;

public class WisAudioLoopback_Service {
	public static final String ACTION_AUDIOLOOPBACK_SERVICE = "com.wistron.audio.loopback.headset";
	// broadcast
	public static final String ACTION_AUDIOLOOPBACK_STATE_CHANGED = "com.wistron.action.audioloopback.state.changed";
	public static final String EXTRA_AUDIOLOOPBACK_STATE = "state";
	public static final String EXTRA_AUDIOLOOPBACK_START_CYCLE_INDEX = "test_cycle";
	public static final String EXTRA_AUDIOLOOPBACK_DECODE_VALUE = "decode_value";
	public static final String EXTRA_AUDIOLOOPBACK_CYCLE_DONE_RESULT = "cycle_result";
	
	public static final String EXTRA_IS_DTMF = "is_dtmf";
	public static final String EXTRA_IS_WCIS = "is_wcis";
	public static final String EXTRA_TEST_QUEUE = "test_queue";
	public static final String EXTRA_NEED_MATCH_COUNT = "need_match_count";
	public static final String EXTRA_VOLUME = "volume";
	public static final String EXTRA_TEST_CYCLES = "test_cycles";
	
	public static final int AUDIOLOOPBACK_STATE_PLAYER_ERROR = 0;
	public static final int AUDIOLOOPBACK_STATE_ONE_CYCLE_START = 1;
	public static final int AUDIOLOOPBACK_STATE_UPDATE_DECODE_VALUE = 2;
	public static final int AUDIOLOOPBACK_STATE_ONE_CYCLE_DONE = 3;
	public static final int AUDIOLOOPBACK_STATE_TO_EXIT = 4;
	// activity to service do action
	public static final String EXTRA_AUDIOLOOPBACK_SERVICE_DO_ACTION = "action";
	public static final int AUDIOLOOPBACK_SERVICE_START_TEST = 0;
	public static final int AUDIOLOOPBACK_SERVICE_STOP_TEST = 1;
}
