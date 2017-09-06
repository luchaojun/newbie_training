package com.wistron.generic.configchk;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.wistron.generic.pqaa.R;
import com.wistron.pqaa_common.jar.global.WisCommonConst;
import com.wistron.pqaa_common.jar.global.WisLog;
import com.wistron.pqaa_common.jar.global.WisParseValue;
import com.wistron.pqaa_common.jar.global.WisShellCommandHelper;
import com.wistron.pqaa_common.jar.global.WisToolKit;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class ConfigChk extends Activity {
    private static final int TIMEOUT = 2;
    private static final String CONFIGCHK_CONFIG_PATH = "/mnt/sdcard/pqaa_config/sysinfo.cfg";
    private static final String CONFIGCHK_LOG_PATH = "/mnt/sdcard/sysinfo.txt";

    private int MIN_FREQUENCY = 100, MAX_FREQUENCY = 2500;
    private int MIN_FLASH = 2, MAX_FLASH = 30;
    private int MIN_MEMORY = 256, MAX_MEMORY = 2048;
    private String DEFAULT_OS_VERSION = "2013", DEFAULT_PROCESSOR = "ARMv7", DEFAULT_ARCHITECTURE = "4", DEFAULT_REVISION = "4";

    private TextView tvFlashSizeSet, tvMemorySizeSet, tvOSVersionSet, tvUbootVersionSet, tvCPUProcessorSet, tvCPUarchitectureSet, tvCPUrevisionSet,
            tvCPUFrequencySet;
    private TextView tvFlashSizeSys, tvMemorySizeSys, tvOSVersionSys, tvUbootVersionSys, tvCPUProcessorSys, tvCPUarchitectureSys, tvCPUrevisionSys,
            tvCPUFrequencySys;
    private ImageView ivFlashSizeResult, ivMemorySizeResult, ivOSVersionResult, ivUbootVersionResult, ivCPUProcessorResult, ivCPUarchitectureResult,
            ivCPUrevisionResult, ivCPUFrequencyResult;
    private boolean isPass;

    private int mFlashSizeSys, mMemorySizeSys;
    private float mCPUFrequencySys;
    private String mOSVersionSet = DEFAULT_OS_VERSION, mUbootVersionSet = "", mCPUProcessorSet = DEFAULT_PROCESSOR, mCPUArchitectureSet = DEFAULT_ARCHITECTURE, mCPURevisionSet = DEFAULT_REVISION;
    private String mOSVersionSys = "", mUbootVersionSys = "", mCPUProcessorSys = "", mCPUArchitectureSys = "", mCPURevisionSys = "";

    // common tool kit
    private WisToolKit mToolKit;
    private WisShellCommandHelper mWisShellCommandHelper;

    // Log class
    private WisLog mLogHandler;

    //IMEI and LAN MAC
    private TextView tvIMEI, tvLanMac;
    private String IMEI, LANMAC;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.configchk);

        mToolKit = new WisToolKit(this);
        mWisShellCommandHelper = new WisShellCommandHelper();

        try {
            mLogHandler = new WisLog(CONFIGCHK_LOG_PATH);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        findView();
        setViewByLanguage();
        getTestArguments();
        getFlashSize();
        getMemorySize();
        getOSVersion();
        getCPUInfo();
        getLanMac();
        IMEI = getIMEI();

        tvFlashSizeSet.setText(MIN_FLASH + "~" + MAX_FLASH + " GB");
        tvMemorySizeSet.setText(MIN_MEMORY + "~" + MAX_MEMORY + " MB");
        tvOSVersionSet.setText(mOSVersionSet);
        tvCPUProcessorSet.setText(mCPUProcessorSet);
        tvCPUarchitectureSet.setText(mCPUArchitectureSet);
        tvCPUrevisionSet.setText(mCPURevisionSet);
        tvCPUFrequencySet.setText(MIN_FREQUENCY + "~" + MAX_FREQUENCY + " HZ");
        tvIMEI.setText(IMEI);
        tvLanMac.setText(LANMAC.toUpperCase());
        compareResult();
        new Thread(exitRunnable).start();

        try {
            mLogHandler.write("Flash Size:" + mFlashSizeSys + " GB", true);
            mLogHandler.write("Memory Size:" + mMemorySizeSys + " MB", true);
            mLogHandler.write("OS Version:" + mOSVersionSys, true);
            mLogHandler.write("CPU Processor:" + mCPUProcessorSys, true);
            mLogHandler.write("CPU Architecture:" + mCPUArchitectureSys, true);
            mLogHandler.write("CPU Revision:" + mCPURevisionSys, true);
            mLogHandler.write("CPU Frequency:" + mCPUFrequencySys, true);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }
    private void getLanMac(){
        ArrayList<String> mResultList = mWisShellCommandHelper.exec("cat /sys/class/net/wlan0/address");
        if (!mResultList.isEmpty()){
            LANMAC = mResultList.get(0);
        }else {
            LANMAC = "NOT GET";
        }
    }
    private String getIMEI(){
        TelephonyManager telephonyManager = (TelephonyManager) ConfigChk.this
                .getSystemService(TELEPHONY_SERVICE);

        String IMEI = "";
        String imei1= "";
        String[] imei;

        /*WisShellCommandHelper mWisShellCommandHelper = new WisShellCommandHelper();
        ArrayList<String> mResultList = mWisShellCommandHelper.exec("getprop telephony.imei_1");

        if (mResultList != null) {
            imei = mResultList.get(0).trim();
            if (imei.isEmpty()){
                imei = "N/A";
            }
        }else {
            imei = "N/A";
        }*/
        imei1=telephonyManager.getDeviceId();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            int slot = 1;
            try {
                Class  phoneManager = Class.forName("android.telephony.TelephonyManager");
                // 通过getMethod来获取到隐藏类的方法
                Method expand = phoneManager.getDeclaredMethod("getPhoneCount");
                Object object = expand.invoke(telephonyManager);
                //   通过invoke来执行该函数
                slot = Integer.parseInt(object.toString());

                // slot = telephonyManager.getPhoneCount();
                Log.i("Payne","SiMslot="+slot);
                imei =new String[slot];
                for(int i=0;i<slot;i++) {
                    try {
                        Method getDeviceId = phoneManager.getDeclaredMethod("getDeviceId",int.class);
                        //   通过invoke来执行该函数


                        imei[i] = getDeviceId.invoke(telephonyManager,i).toString();
                        // imei[i] = telephonyManager.getDeviceId(i);
                        IMEI=IMEI+imei[i]+";";
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }catch (IllegalAccessException e){
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

        }else {
            IMEI=imei1;
        }

        return IMEI;
    }
    private void getTestArguments() {
        // TODO Auto-generated method stub
        String mTestStyle = mToolKit.getCurrentTestType();
        if (mTestStyle != null) {
            if (mTestStyle.equals(WisCommonConst.TESTSTYLE_SAVED_CONFIG)) {
               // isComponentMode = false;
                WisParseValue mParse=new WisParseValue(this, mToolKit.getCurrentItem(),mToolKit.getCurrentDatabaseAuthorities());
                String flash = mParse.getArg1();
                String memory = mParse.getArg2();
                String os = mParse.getArg3();
                String processor = mParse.getArg4();
                String revision = mParse.getArg5();
                String frequency = mParse.getArg6();
                if (flash != null && flash.length() > 0) {
                    MIN_FLASH = Integer.parseInt(flash.substring(0, flash.indexOf("~")));
                    MAX_FLASH = Integer.parseInt(flash.substring(flash.indexOf("~") + 1));
                }
                if (memory != null && memory.length() > 0) {
                    MIN_MEMORY = Integer.parseInt(memory.substring(0, memory.indexOf("~")));
                    MAX_MEMORY = Integer.parseInt(memory.substring(memory.indexOf("~") + 1));
                }
                if (os != null && os.length() > 0) {
                    mOSVersionSet = os;
                }
                if (processor != null && processor.length() > 0) {
                    mCPUProcessorSet=processor;
                }
                if (revision != null && revision.length() > 0) {
                    mCPURevisionSet=revision;
                }
                if (frequency != null && frequency.length() > 0) {
                    MIN_FREQUENCY = Integer.parseInt(frequency.substring(0, frequency.indexOf("~")));
                    MAX_FREQUENCY = Integer.parseInt(frequency.substring(frequency.indexOf("~") + 1));
                }

            }
        }
    }
    private void setViewByLanguage() {
        // TODO Auto-generated method stub
        TextView mItemTitle = (TextView) findViewById(R.id.item_title);
        mItemTitle.setText(mToolKit.getStringResource(R.string.configchk_test_title));
        ((TextView) findViewById(R.id.configchk_content)).setText(mToolKit.getStringResource(R.string.configchk_title_content));
        ((TextView) findViewById(R.id.configchk_range)).setText(mToolKit.getStringResource(R.string.configchk_title_range));
        ((TextView) findViewById(R.id.configchk_value)).setText(mToolKit.getStringResource(R.string.configchk_title_value));
        ((TextView) findViewById(R.id.configchk_result)).setText(mToolKit.getStringResource(R.string.configchk_title_result));
        ((TextView) findViewById(R.id.configchk_item_flash)).setText(mToolKit.getStringResource(R.string.configchk_item_flash));
        ((TextView) findViewById(R.id.configchk_item_memory)).setText(mToolKit.getStringResource(R.string.configchk_item_memory));
        ((TextView) findViewById(R.id.configchk_item_os)).setText(mToolKit.getStringResource(R.string.configchk_item_os));
        ((TextView) findViewById(R.id.configchk_item_uboot)).setText(mToolKit.getStringResource(R.string.configchk_item_uboot));
        ((TextView) findViewById(R.id.configchk_item_cpu_processor)).setText(mToolKit.getStringResource(R.string.configchk_item_cpu_processor));
        ((TextView) findViewById(R.id.configchk_item_cpu_architecture)).setText(mToolKit.getStringResource(R.string.configchk_item_cpu_architecture));
        ((TextView) findViewById(R.id.configchk_item_cpu_revision)).setText(mToolKit.getStringResource(R.string.configchk_item_cpu_revision));
        ((TextView) findViewById(R.id.configchk_item_cpu_frequency)).setText(mToolKit.getStringResource(R.string.configchk_item_cpu_frequency));
    }

    private void compareResult() {
        // TODO Auto-generated method stub
        isPass = true;
        if (mFlashSizeSys >= MIN_FLASH && mFlashSizeSys <= MAX_FLASH) {
            ivFlashSizeResult.setImageResource(R.drawable.pass);
        } else {
            isPass = false;
        }
        if (mMemorySizeSys >= MIN_MEMORY && mMemorySizeSys <= MAX_MEMORY) {
            ivMemorySizeResult.setImageResource(R.drawable.pass);
        } else {
            isPass = false;
        }
        if (mOSVersionSet.equals(mOSVersionSys)) {
            ivOSVersionResult.setImageResource(R.drawable.pass);
        } else {
            isPass = false;
        }
        /*if (mUbootVersionSet.equals(mUbootVersionSys)) {
			ivUbootVersionResult.setImageResource(R.drawable.pass);
		} else {
			isPass = false;
		}*/
        if (mCPUProcessorSet.equals(mCPUProcessorSys)) {
            ivCPUProcessorResult.setImageResource(R.drawable.pass);
        } else {
            isPass = false;
        }

        if (mCPUFrequencySys >= MIN_FREQUENCY && mCPUFrequencySys <= MAX_FREQUENCY) {
            ivCPUFrequencyResult.setImageResource(R.drawable.pass);
        } else {
            isPass = false;
        }
        if (mCPURevisionSet.equals(mCPURevisionSys)) {
            ivCPUrevisionResult.setImageResource(R.drawable.pass);
        } else {
            isPass = false;
        }
    }

    private void findView() {
        // TODO Auto-generated method stub
        tvFlashSizeSet = (TextView) findViewById(R.id.configchk_flash_set);
        tvMemorySizeSet = (TextView) findViewById(R.id.configchk_memory_set);
        tvOSVersionSet = (TextView) findViewById(R.id.configchk_os_set);
        tvUbootVersionSet = (TextView) findViewById(R.id.configchk_uboot_set);
        tvCPUProcessorSet = (TextView) findViewById(R.id.configchk_processor_set);
        tvCPUarchitectureSet = (TextView) findViewById(R.id.configchk_architecture_set);
        tvCPUrevisionSet = (TextView) findViewById(R.id.configchk_revision_set);
        tvCPUFrequencySet = (TextView) findViewById(R.id.configchk_frequency_set);

        tvFlashSizeSys = (TextView) findViewById(R.id.configchk_flash_sys);
        tvMemorySizeSys = (TextView) findViewById(R.id.configchk_memory_sys);
        tvOSVersionSys = (TextView) findViewById(R.id.configchk_os_sys);
        tvUbootVersionSys = (TextView) findViewById(R.id.configchk_uboot_sys);
        tvCPUProcessorSys = (TextView) findViewById(R.id.configchk_processor_sys);
        tvCPUarchitectureSys = (TextView) findViewById(R.id.configchk_architecture_sys);
        tvCPUrevisionSys = (TextView) findViewById(R.id.configchk_revision_sys);
        tvCPUFrequencySys = (TextView) findViewById(R.id.configchk_frequency_sys);

        ivFlashSizeResult = (ImageView) findViewById(R.id.configchk_flash_result);
        ivMemorySizeResult = (ImageView) findViewById(R.id.configchk_memory_result);
        ivOSVersionResult = (ImageView) findViewById(R.id.configchk_os_result);
        ivUbootVersionResult = (ImageView) findViewById(R.id.configchk_uboot_result);
        ivCPUarchitectureResult = (ImageView) findViewById(R.id.configchk_architecture_result);
        ivCPUProcessorResult = (ImageView) findViewById(R.id.configchk_processor_result);
        ivCPUrevisionResult = (ImageView) findViewById(R.id.configchk_revision_result);
        ivCPUFrequencyResult = (ImageView) findViewById(R.id.configchk_frequency_result);

        tvIMEI = (TextView) findViewById(R.id.configchk_IMEI_set);
        tvLanMac = (TextView) findViewById(R.id.configchk_LanMac_sys);
    }

    private void getFlashSize() {
        // TODO Auto-generated method stub
        String result = "";
        ArrayList<String> mResultList = mWisShellCommandHelper.exec("cat /proc/partitions");
        for (String temp : mResultList) {
            if (temp.contains("mmcblk0")) {
                System.out.println(temp + "-------");
                int i = 0;
                StringTokenizer mTokenizer = new StringTokenizer(temp, " ");
                while (mTokenizer.hasMoreElements()) {
                    result = mTokenizer.nextToken().trim();
                    i++;
                    if (i == 3) {
                        break;
                    }
                }
                break;
            }
        }
        if (result.length() <= 0) {
            result = "0";
        }

        mFlashSizeSys = (int) (Long.parseLong(result) / (1024 * 1024));
        tvFlashSizeSys.setText(String.valueOf(mFlashSizeSys) + " GB");
    }

    private void getMemorySize() {
        // TODO Auto-generated method stub
        ArrayList<String> mResultList = mWisShellCommandHelper.exec("cat /proc/meminfo");
        for (String temp : mResultList) {
            if (temp.startsWith("MemTotal")) {
                temp = temp.substring(temp.indexOf(":") + 1).trim();
                temp = temp.substring(0, temp.indexOf(" ")).trim();
                mMemorySizeSys = (int) (Long.parseLong(temp) / 1024);
                break;
            }
        }
        tvMemorySizeSys.setText(String.valueOf(mMemorySizeSys) + " MB");
    }

    private void getOSVersion() {
        // TODO Auto-generated method stub
        mOSVersionSys = "Android" + Build.VERSION.RELEASE;
        tvOSVersionSys.setText(mOSVersionSys);
    }

    private void getUBootVersion() {
        // TODO Auto-generated method stub
        ArrayList<String> mResultList = mWisShellCommandHelper.exec("cat /sys/sysinfo/bootloader_ver");
        mUbootVersionSys = mResultList.get(0).trim();
        tvUbootVersionSys.setText(mUbootVersionSys);
    }

    private void getCPUInfo() {
        ArrayList<String> mResultList = mWisShellCommandHelper.exec("cat /proc/cpuinfo");
        for (String temp : mResultList) {
            if (temp.startsWith("vendor_id")) {
                temp = temp.substring(temp.indexOf(":") + 1).trim();
                mCPUProcessorSys = temp;
            } else if (temp.startsWith("bogomips")) {
                temp = temp.substring(temp.indexOf(":") + 1).trim();
                mCPUFrequencySys = Float.parseFloat(temp.substring(0).trim());
            } else if (temp.startsWith("cpu cores")) {
                temp = temp.substring(temp.indexOf(":") + 1).trim();
                mCPURevisionSys = temp.substring(0).trim();
            }
        }
        tvCPUProcessorSys.setText(mCPUProcessorSys);
        tvCPUFrequencySys.setText(String.valueOf(mCPUFrequencySys));
        tvCPUarchitectureSys.setText(mCPUArchitectureSys);
        tvCPUrevisionSys.setText(mCPURevisionSys);
    }
    private Handler exitHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            mToolKit.returnWithResult(isPass);
        }

    };
    private Runnable exitRunnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            try {
                if (isPass) {
                    Thread.sleep(500);
                } else {
                    Thread.sleep(TIMEOUT * 1000);
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            exitHandler.sendEmptyMessage(0);
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}