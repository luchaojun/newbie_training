package com.wistron.pqaa_common.jar.wcis;


public class WisWCISCommonConst {
	/**
	 * WCIS read config action, if wcis item need read config,you should send broadcast with this action.
	 */
	public static final String ACTION_WCIS_READ_CONFIG = "com.wistron.wcis.transform.station.read.config";
	/**
	 * WCIS feedback config action, this action send by WCIS,do not send the action in wcis sub item.
	 * usage:
	 * (Map<String, String>) intent.getSerializableExtra(WisWCISCommonConst.EXTRA_CONFIG_CONTENT);
	 */
	public static final String ACTION_WCIS_FEEDBACK_CONFIG = "com.wistron.wcis.transform.station.feedback.config";
	/**
	 * WCIS write log action, to generate the log file.
	 */
	public static final String ACTION_WCIS_WRITE_LOG = "com.wistron.wcis.transform.station.write.log";
	/**
	 * WCIS delete log action, to delete the log file
	 */
	public static final String ACTION_WCIS_DELETE_LOG = "com.wistron.wcis.transform.station.delete.log";
	/**
	 * WCIS erase SDCard action, will delete all of files on SDCard. you should specified the sdcard root path with:
	 * WisWCISCommonConst.EXTRA_SDCARD_ROOT_PATH
	 */
	public static final String ACTION_WCIS_ERASE_SDCARD = "com.wistron.wcis.transform.station.erase.sdcard";
	/**
	 * WCIS erase SDCard done action.
	 */
	public static final String ACTION_WCIS_ERASE_SDCARD_DONE = "com.wistron.wcis.transform.station.erase.done";
	
	public static final String EXTRA_CONFIG_PATH = "config_path";
	public static final String EXTRA_CONFIG_CONTENT = "config_content";
	public static final String EXTRA_LOG_FILE_NAME = "log_file_name";
	public static final String EXTRA_LOG_FILE_CONTENT = "log_file_content";
	public static final String EXTRA_LOG_TYPE = "log_flag";
	public static final String EXTRA_CONFIG_FOLDER = "config_folder";
	public static final String EXTRA_LOG_FOLDER = "log_folder";
	public static final String EXTRA_SDCARD_ROOT_PATH = "sdcard_root_path";
	
	/**
	 * WCIS utility service to R/W sdcard for sub items.
	 */
	public static final String ACTION_WCIS_UTILITY_SERVICE = "com.wistron.wcis.utility.UtilityService";
	
	/**
	 * To generate the log during testing.
	 */
	public static final String ACTION_WRITE_TEMP_HTML_LOG = "com.wistron.wcis.write.temp.html.log";
	
	/**
	 * generate general log file, contains date and time tags
	 */
	public static final int LOG_TYPE_GENERAL_LOG = 0;
	/**
	 * generate HTML log file
	 */
	public static final int LOG_TYPE_HTML_LOG = 1;
	/**
	 * generate XML log file
	 */
	public static final int LOG_TYPE_XML_LOG = 2;
	
	/**
	 * For WCIS sense test item: LightSensor. request command code is 0X01.
	 */
	public static final int SENSE_CMD_REQUEST_LIGHTSENSOR = 0x01;
	/**
	 * For WCIS sense test item: Brightness. request command code is 0X02.
	 */
	public static final int SENSE_CMD_REQUEST_BRIGHTNESS = 0x02;
	/**
	 * For WCIS sense test item: FlashLight. request command code is 0X03.
	 */
	public static final int SENSE_CMD_REQUEST_FLASHLIGHT = 0x03;
	/**
	 * For WCIS sense test item: Display. request command code is 0X04.
	 */
	public static final int SENSE_CMD_REQUEST_DISPLAY = 0x04;
	/**
	 * For WCIS sense test item: VideoCodec. request command code is 0X08.
	 */
	public static final int SENSE_CMD_REQUEST_VIDEO_CODEC = 0x08;
	/**
	 * For WCIS sense test item: Browser. request command code is 0X09.
	 */
	public static final int SENSE_CMD_REQUEST_BROWSER = 0x09;
	/**
	 * For WCIS sense test item: LED. request command code is 0X0a.
	 */
	public static final int SENSE_CMD_REQUEST_LED = 0x0a;
	/**
	 * For WCIS sense test item: AudioCodec. request command code is 0X0b.
	 */
	public static final int SENSE_CMD_REQUEST_AUDIO_CODEC = 0x0b;
	/**
	 * For WCIS sense test item: SMS. request command code is 0X0c.
	 */
	public static final int SENSE_CMD_REQUEST_SMS = 0x0c;
	/**
	 * For WCIS sense test item: Alarm. request command code is 0X0d.
	 */
	public static final int SENSE_CMD_REQUEST_ALARM = 0x0d;
	/**
	 * For WCIS sense test item: Phone. request command code is 0X0e.
	 */
	public static final int SENSE_CMD_REQUEST_PHONE = 0x0e;
	/**
	 * For WCIS sense all test items,to request test result from server. request command code is 0X50.
	 */
	public static final int SENSE_CMD_REQUEST_TEST_RESULT = 0x50;
	/**
	 * For WCIS sense all test items, to request finish the server app if need. request command code is 0x51.
	 */
	public static final int SENSE_CMD_REQUEST_FINISH_SERVER_APP = 0x51;
	/**
	 * For WCIS sense all test items, to request send next action. request command code is 0x52.
	 */
	public static final int SENSE_CMD_REQUEST_START_NEXT_ACTION = 0x52;
	/**
	 * Response to request command, response command code is 0XFF00.
	 */
	public static final int SENSE_RESPONSE_TO_REQUEST = 0xff00;
	/**
	 * Response to current sense test item with PASS result, response command code is 0XFF01.
	 */
	public static final int SENSE_RESPONSE_WITH_PASS_RESULT = 0xff01;
	/**
	 * Response to current sense test item with FAIL result, response command code is 0XFF02;
	 */
	public static final int SENSE_RESPONSE_WITH_FAIL_RESULT = 0xff02;
	/**
	 * Response to current sense test item due to Exception issue, response command code is 0XFF03;
	 */
	public static final int SENSE_RESPONSE_WITH_EXCEPTION = 0xff03;
	/**
	 * The WCIS Sense extra: parameters
	 */
	public static final String SENSE_EXTRA_PARAMETERS = "parameters";
}
