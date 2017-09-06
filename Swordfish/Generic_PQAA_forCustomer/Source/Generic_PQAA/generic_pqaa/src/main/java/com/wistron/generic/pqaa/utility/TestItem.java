package com.wistron.generic.pqaa.utility;

import android.content.Context;

import com.wistron.generic.pqaa.R;

import java.io.Serializable;

public class TestItem implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Context mContext;
    public static final int RESULT_DEFAULT = 0;
    public static final int RESULT_PASS = 1;
    public static final int RESULT_FAIL = 2;
    private String mTestItemName;
    private String mTestItemCmdLine;
    private String mTestItemPackageName;
    private String mTestItemActivityName;
    private String mTestItemCNName;
    private String mTestItemVersion;
    private boolean isInstalled;
    private boolean isChecked;
    private boolean isPCBATestStage;
    private int mTestItemResult;

    public TestItem(Context context, String mTestItemName) {
        super();
        this.mContext = context;
        this.mTestItemName = mTestItemName;
        this.mTestItemCmdLine = "";
        this.mTestItemResult = RESULT_DEFAULT;
        setPackageAndActivityName();
        this.isChecked = true;
        this.isInstalled = true;
    }

    private void setPackageAndActivityName() {
        // TODO Auto-generated method stub
        if (mTestItemName.equals("TouchPanel")) {
            this.mTestItemActivityName = "com.wistron.generic.touchpanel.TouchPanel";
            this.mTestItemCNName = mContext.getResources().getString(R.string.touchpanel_cn);
        } else if (mTestItemName.equals("Display")) {
            this.mTestItemActivityName = "com.wistron.generic.display.Display";
            this.mTestItemCNName = mContext.getResources().getString(R.string.display_cn);
        } else if (mTestItemName.equals("Audio")) {
            this.mTestItemActivityName = "com.wistron.generic.audio.Audio";
            this.mTestItemCNName = mContext.getResources().getString(R.string.audio_cn);
        } else if (mTestItemName.equals("AudioLoopback")) {
            this.mTestItemActivityName = "com.wistron.generic.audio.loopback.AudioLoopback";
            this.mTestItemCNName = mContext.getResources().getString(R.string.audio_loopback_cn);
        } else if (mTestItemName.equals("RTC")) {
            this.mTestItemActivityName = "com.wistron.generic.rtc.RTC";
            this.mTestItemCNName = mContext.getResources().getString(R.string.rtc_cn);
        } else if (mTestItemName.equals("SDCard")) {
            this.mTestItemActivityName = "com.wistron.generic.sdcard.SDCard";
            this.mTestItemCNName = mContext.getResources().getString(R.string.sdcard_cn);
        } else if (mTestItemName.equals("MoniPower")) {
            this.mTestItemActivityName = "com.wistron.generic.monipower.MoniPower";
            this.mTestItemCNName = mContext.getResources().getString(R.string.monipower_cn);
        } else if (mTestItemName.equals("MaxLoad")) {
            this.mTestItemActivityName = "com.wistron.generic.maxload.MaxLoad";
            this.mTestItemCNName = mContext.getResources().getString(R.string.maxload_cn);
        } else if (mTestItemName.equals("2D")) {
            this.mTestItemActivityName = "com.wistron.generic.maxload.MaxLoad";
            this.mTestItemCNName = mContext.getResources().getString(R.string.maxload_cn);
        } else if (mTestItemName.equals("RamTest")) {
            this.mTestItemActivityName = "com.wistron.generic.maxload.MaxLoad";
            this.mTestItemCNName = mContext.getResources().getString(R.string.maxload_cn);
        } else if (mTestItemName.equals("DiskTest")) {
            this.mTestItemActivityName = "com.wistron.generic.maxload.MaxLoad";
            this.mTestItemCNName = mContext.getResources().getString(R.string.maxload_cn);
        } else if (mTestItemName.equals("AudioTest")) {
            this.mTestItemActivityName = "com.wistron.generic.maxload.MaxLoad";
            this.mTestItemCNName = mContext.getResources().getString(R.string.maxload_cn);
        } else if (mTestItemName.equals("Brightness")) {
            this.mTestItemActivityName = "com.wistron.generic.brightness.Brightness";
            this.mTestItemCNName = mContext.getResources().getString(R.string.brightness_cn);
        } else if (mTestItemName.equals("ConfigChk")) {
            this.mTestItemActivityName = "com.wistron.generic.configchk.ConfigChk";
            this.mTestItemCNName = mContext.getResources().getString(R.string.configchk_cn);
        } else if (mTestItemName.equals("Wifi")) {
            this.mTestItemActivityName = "com.wistron.generic.wifi.Wireless";
            this.mTestItemCNName = mContext.getResources().getString(R.string.wifi_cn);
        } else if (mTestItemName.equals("Vibration")) {
            this.mTestItemActivityName = "com.wistron.generic.vibration.Vibration";
            this.mTestItemCNName = mContext.getResources().getString(R.string.vibration_cn);
        } else if (mTestItemName.equals("BlueTooth")) {
            this.mTestItemActivityName = "com.wistron.generic.bluetooth.BlueTooth";
            this.mTestItemCNName = mContext.getResources().getString(R.string.bluetooth_cn);
        } else if (mTestItemName.equals("Camera")) {
            this.mTestItemActivityName = "com.wistron.generic.camera.WisCamera";
            this.mTestItemCNName = mContext.getResources().getString(R.string.camera_cn);
        } else if (mTestItemName.equals("FrontCamera")) {
            this.mTestItemActivityName = "com.wistron.generic.camera.WisCamera";
            this.mTestItemCNName = mContext.getResources().getString(R.string.camera_front_cn);
        } else if (mTestItemName.equals("BackCamera")) {
            this.mTestItemActivityName = "com.wistron.generic.camera.WisCamera";
            this.mTestItemCNName = mContext.getResources().getString(R.string.camera_back_cn);
        } else if (mTestItemName.equals("CameraFlash")) {
            this.mTestItemActivityName = "com.wistron.generic.camera.WisCamera";
            this.mTestItemCNName = mContext.getResources().getString(R.string.camera_flash_cn);
        } else if (mTestItemName.equals("USBClient")) {
            this.mTestItemActivityName = "com.wistron.generic.usbclient.USB";
            this.mTestItemCNName = mContext.getResources().getString(R.string.usbclient_cn);
        } else if (mTestItemName.equals("USBHost")) {
            this.mTestItemActivityName = "com.wistron.generic.usbhost.USB";
            this.mTestItemCNName = mContext.getResources().getString(R.string.usbhost_cn);
        } else if (mTestItemName.equals("LED")) {
            this.mTestItemActivityName = "com.wistron.generic.LED.LED";
            this.mTestItemCNName = mContext.getResources().getString(R.string.led_cn);
        } else if (mTestItemName.equals("Button")) {
            this.mTestItemActivityName = "com.wistron.generic.button.ButtonTest";
            this.mTestItemCNName = mContext.getResources().getString(R.string.button_cn);
        } else if (mTestItemName.equals("GSensor")) {
            this.mTestItemActivityName = "com.wistron.generic.gsensor.GSensorTest";
            this.mTestItemCNName = mContext.getResources().getString(R.string.gsensor_cn);
        } else if (mTestItemName.equals("GyroSensor")) {
            this.mTestItemActivityName = "com.wistron.generic.gyrosensor.GyroSensor";
            this.mTestItemCNName = mContext.getResources().getString(R.string.gyrosensor_cn);
        } else if (mTestItemName.equals("LightSensor")) {
            this.mTestItemActivityName = "com.wistron.generic.lightsensor.LightSensorTest";
            this.mTestItemCNName = mContext.getResources().getString(R.string.lightsensor_cn);
        } else if (mTestItemName.equals("ProximitySensor")) {
            this.mTestItemActivityName = "com.wistron.generic.proximitysensor.ProximitySensorTest";
            this.mTestItemCNName = mContext.getResources().getString(R.string.proximitysensor_cn);
        } else if (mTestItemName.equals("3G")) {
            this.mTestItemActivityName = "com.wistron.generic.sim.PQAA_3G";
            this.mTestItemCNName = mContext.getResources().getString(R.string.sim_cn);
        } else if (mTestItemName.equals("IMEI")) {
            this.mTestItemActivityName = "com.wistron.generic.imei.IMEI";
            this.mTestItemCNName = mContext.getResources().getString(R.string.imei_cn);
        } else if (mTestItemName.equals("ECompass")) {
            this.mTestItemActivityName = "com.wistron.generic.compass.Compass_Test";
            this.mTestItemCNName = mContext.getResources().getString(R.string.ecompass_cn);
        } else if (mTestItemName.equals("FingerPrinter")) {
            this.mTestItemActivityName = "egistec.es603.mptool.Main";
            this.mTestItemCNName = mContext.getResources().getString(R.string.fingerprinter_cn);
        } else if (mTestItemName.equals("PCB")) {
            this.mTestItemActivityName = "com.wistron.generic.pcb.PCB";
            this.mTestItemCNName = mContext.getResources().getString(R.string.pcb_cn);
        } else if (mTestItemName.equals("TouchFW")) {
            this.mTestItemActivityName = "com.wistron.generic.touchfw.TouchFW";
            this.mTestItemCNName = mContext.getResources().getString(R.string.touchfw_cn);
        } else if (mTestItemName.equals("HDMI")) {
            this.mTestItemActivityName = "com.wistron.generic.hdmi.HDMI";
            this.mTestItemCNName = mContext.getResources().getString(R.string.hdmi_cn);
        } else if (mTestItemName.equals("Thermal")) {
            this.mTestItemActivityName = "com.wistron.generic.thermal.Thermal";
            this.mTestItemCNName = mContext.getResources().getString(R.string.thermal_cn);
        } else if (mTestItemName.equals("Headset")) {
            this.mTestItemActivityName = "com.wistron.generic.audio.Audio";
            this.mTestItemCNName = mContext.getResources().getString(R.string.earphone_cn);
        } else if (mTestItemName.equals("HeadsetLoopback")) {
            this.mTestItemActivityName = "com.wistron.generic.headset.loopback.AudioLoopback";
            this.mTestItemCNName = mContext.getResources().getString(R.string.earphone_auto_cn);
        } else if (mTestItemName.equals("GPS")) {
            this.mTestItemActivityName = "com.wistron.generic.gps.GPS";
            this.mTestItemCNName = mContext.getResources().getString(R.string.gps_cn);
        } else if (mTestItemName.equals("NFC")) {
            this.mTestItemActivityName = "com.wistron.generic.nfc.NFC";
            this.mTestItemCNName = mContext.getResources().getString(R.string.nfc_cn);
        } else if (mTestItemName.equals("SuspendResume")) {
            this.mTestItemActivityName = "com.wistron.generic.suspendresume.SuspendResume";
            this.mTestItemCNName = mContext.getResources().getString(R.string.suspendresume_cn);
        } else if (mTestItemName.equals("Charging")) {
            this.mTestItemActivityName = "com.wistron.generic.charging.Charging";
            this.mTestItemCNName = mContext.getResources().getString(R.string.charging_cn);
        } else if (mTestItemName.equals("EDID")) {
            this.mTestItemActivityName = "com.wistron.generic.edid.EDID";
            this.mTestItemCNName = mContext.getResources().getString(R.string.edid_cn);
        } else if (mTestItemName.equals("NTPSync")) {
            this.mTestItemActivityName = "com.wistron.generic.ntp.NTP";
            this.mTestItemCNName = mContext.getResources().getString(R.string.ntpsync_cn);
        } else if (mTestItemName.equals("Automatic")) {
            this.mTestItemActivityName = "com.wistron.generic.autotest.AutoTest";
            this.mTestItemCNName = mContext.getResources().getString(R.string.automatic_cn);
        } else if (mTestItemName.equals("FlashLight")) {
            this.mTestItemActivityName = "com.wistron.generic.camera.Flash";
            this.mTestItemCNName = mContext.getResources().getString(R.string.camera_flash_cn);
        } else if (mTestItemName.equals("MultiTouch")) {
            this.mTestItemActivityName = "com.wistron.generic.multitouch.MultiTouch";
            this.mTestItemCNName = mContext.getResources().getString(R.string.multitouch_cn);
        } else if (mTestItemName.equals("RAM")) {
            this.mTestItemActivityName = "com.wistron.generic.ram.RAM";
            this.mTestItemCNName = mContext.getResources().getString(R.string.ram_cn);
        } else if (mTestItemName.equals("BarometerSensor")) {
            this.mTestItemActivityName = "com.wistron.generic.barometersensor.BarometerSensor";
            this.mTestItemCNName = mContext.getResources().getString(R.string.barometer_sensor_cn);
        }else if(mTestItemName.equals("SensorTest")){
            this.mTestItemActivityName = "com.wistron.generic.sensor.MainActivity";
            this.mTestItemCNName = mContext.getResources().getString(R.string.sensor_cn);
        }else if(mTestItemName.equals("Camera_LED")){
            this.mTestItemActivityName = "com.wistron.generic.cameraled.Camera_LED";
            this.mTestItemCNName = mContext.getResources().getString(R.string.sensor_cn);
        }
    }

    public String getTestItemName() {
        return mTestItemName;
    }

    public void setTestItemName(String mTestItemName) {
        this.mTestItemName = mTestItemName;
    }

    public String getTestItemCmdLine() {
        return mTestItemCmdLine;
    }

    public void setTestItemCmdLine(String mTestItemCmdLine) {
        this.mTestItemCmdLine = mTestItemCmdLine;
    }

    public int getTestItemResult() {
        return mTestItemResult;
    }

    public void setTestItemResult(int mTestItemResult) {
        this.mTestItemResult = mTestItemResult;
    }

    public String getTestItemPackageName() {
        return mTestItemPackageName;
    }

    public void setTestItemPackageName(String mTestItemPackageName) {
        this.mTestItemPackageName = mTestItemPackageName;
    }

    public String getTestItemActivityName() {
        return mTestItemActivityName;
    }

    public void setTestItemActivityName(String mTestItemActivityName) {
        this.mTestItemActivityName = mTestItemActivityName;
    }

    public String getTestItemVersion() {
        return mTestItemVersion;
    }

    public void setTestItemVersion(String mTestItemVersion) {
        this.mTestItemVersion = mTestItemVersion;
    }

    public boolean isInstalled() {
        return isInstalled;
    }

    public void setInstalled(boolean isInstalled) {
        this.isInstalled = isInstalled;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public String getTestItemCNName() {
        return mTestItemCNName;
    }

    public boolean isPCBATestStage() {
        return isPCBATestStage;
    }

    public void setPCBATestStage(boolean isPCBATestStage) {
        this.isPCBATestStage = isPCBATestStage;
    }
}
