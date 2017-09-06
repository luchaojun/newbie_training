package com.wistron.pqaa_common.jar.global;

public class WisCommonConst {
	/**
	 * Language arguments
	 */
	public static final int LANGUAGE_ENGLISH = 0;
	/**
	 * Language arguments
	 */
	public static final int LANGUAGE_CHINESE_SIMPLE = 1;

	/**
	 * default language
	 */
	public static final int DEFAULT_LANGUAGE = LANGUAGE_CHINESE_SIMPLE;

	// Activity extra keys
	public static final String EXTRA_TESTSTYLE = "teststyle";
	public static final String EXTRA_STAGE = "stage";
	public static final String EXTRA_ITEM = "item";
	public static final String EXTRA_LANGUAGE = "language";
	public static final String EXTRA_AUTHORITIES = "authorities";
	public static final String EXTRA_PASS = "pass";
	public static final String EXTRA_REBOOT = "reboot";
	public static final String EXTRA_REMARK = "remark";
	public static final String EXTRA_IS_PQAA_TEST="is_pqaa_test";
	public static final String EXTRA_IS_RUNIN_TEST="is_runin_test";
	public static final String EXTRA_IS_WCIS_TEST="is_wcis_test";
	
	/**
	 * Test style: Component
	 */
	public static final String TESTSTYLE_COMPONENT = "Component";
	/**
	 * Test style: Saved Config
	 */
	public static final String TESTSTYLE_SAVED_CONFIG = "Saved Config";
	
	/**
	 * NFC change status broadcast action
	 */
	public static final String ACTION_NFC_CHANGE_STATUS="com.wistron.nfc.enabler.utility";
	
	/**
	 * the intent extra data for NFC broadcast
	 */
	public static final String EXTRA_NFC_STATUS="enable";
	
	/**
	 * the Wistron lock flag, need BSP support.
	 */
	public static final String INTERFACE_LOCK_FLAG = "cat /proc/sysinfo/manufacturer";
	/**
	 * kernel version flag
	 */
	public static final String INTERFACE_KERNEL_VERSION = "cat /proc/version";
	/**
	 * KBC version flag
	 */
	public static final String INTERFACE_KBC_VERSION = "cat /proc/sysinfo/kbc_ver";
	/**
	 * PCB version flag
	 */
	public static final String INTERFACE_PCB_VERSION = "cat /proc/sysinfo/pcb_ver";
	/**
	 * USB plug status detect, Android API
	 */
	public static final String INTERFACE_USB_PLUG = "cat /sys/devices/virtual/android_usb/android0/state";
	
	// ----------------- Database select----------------------------
	/**
	 * For database select: PQAA
	 */
	public static final int TEST_FLAG_PQAA = 0;
	/**
	 * For database select: Single apk Genenric PQAA
	 */
	public static final int TEST_FLAG_GENERIC_PQAA = TEST_FLAG_PQAA + 1;
	/**
	 * For database select: RunIn
	 */
	public static final int TEST_FLAG_RUNIN = TEST_FLAG_GENERIC_PQAA + 1;
	/**
	 * For database select: WCIS
	 */
	public static final int TEST_FLAG_WCIS = TEST_FLAG_RUNIN + 1;
	
}
