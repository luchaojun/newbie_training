package com.wistron.generic.pqaa.utility;

import android.os.Environment;

import java.io.File;

public class CommonParams {
    //	private static String STORAGE_ROOT_PATH="/mnt/sdcard/"+File.separator;
    public static String STORAGE_ROOT_PATH = Environment.getExternalStorageDirectory().getPath() + File.separator;

    public static String GENERIC_PATH = "/mnt/sdcard/Android/data/com.wistron.generic.pqaa/files/";
    public static String CONFIG_FILE_PATH = GENERIC_PATH + "pqaa_config/pqaa.cfg";
    public static String AUTOMATIC_FILE_PATH = GENERIC_PATH + "pqaa_config/automatic.cfg";
    public static String PQAA_START_PATH = GENERIC_PATH + "pqaa_config/start.cfg";

    public static String PQAA_LOG_FILE_PATH = GENERIC_PATH + "generic_pqaa.txt";
    public static String PQAA_SYSTEMINFO_FILE_PATH = GENERIC_PATH + "generic_sysinfo.txt";
    public static String PQAA_RESULT_PASS_FILE_PATH = GENERIC_PATH + "GENERIC_PQAA.PASS";
    public static String PQAA_RESULT_FALI_FILE_PATH = GENERIC_PATH + "GENERIC_PQAA.FAIL";

    public static String PQAA_GENERIC_LOG_FILE_PATH = GENERIC_PATH + "TestLog.ini";
    public static String PQAA_GENERIC_LOG_DATA = GENERIC_PATH + "data.txt";
}
