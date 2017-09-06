package com.wistron.generic.monipower;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.wistron.generic.pqaa.R;
import com.wistron.pqaa_common.jar.global.WisCommonConst;
import com.wistron.pqaa_common.jar.global.WisParseValue;
import com.wistron.pqaa_common.jar.global.WisToolKit;

import java.sql.Time;
import java.text.DecimalFormat;
import java.util.Map;

public class MoniPower extends Activity implements OnClickListener {
    private static final String MONIPOWER_CONFIG_FILE_PATH = "/mnt/sdcard/pqaa_config/monipower.cfg";
    private static final int TIMEOUT = 3;
    // ----------------------result---------------
    private TextView mResultContent;
    private Button mResultButton;
    private boolean isResultPage = false;
    // --------------------------------------
    private float MIN_VOLTAGE = 3.5f;
    private float MAX_VOLTAGE = 4.2f;
    private Button btnExit;
    private PowerReceiver mReceiver;
    private TextView mBatteryStatus, mBatteryPresent, mBatteryPlug, mBatteryLevel, mBatteryScale, mBatteryHealth, mBatteryVoltage,
            mBatteryTemperature, mBatteryTechnology, mTime;

    private boolean isPass = true;
    private boolean mComponentMode = true;

    // common tool kit
    private WisToolKit mToolKit;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.monipower);

        mToolKit = new WisToolKit(this);

        getTestArguments();
        findView();
        setViewByLanguage();

        mReceiver = new PowerReceiver();
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        mFilter.addAction(Intent.ACTION_BOOT_COMPLETED);
        registerReceiver(mReceiver, mFilter);

        new Thread(mExitRunnable).start();
    }

    private void getConfigParameters() {
        // TODO Auto-generated method stub
        Map<String, String> mParametersList = mToolKit.getSingleParameters(MONIPOWER_CONFIG_FILE_PATH);
        if (mParametersList != null && mParametersList.size() > 0) {
            for (String key : mParametersList.keySet()) {
                if (key.equals("min_voltage")) {
                    MIN_VOLTAGE = Float.parseFloat(mParametersList.get(key));
                } else if (key.equals("max_voltage")) {
                    MAX_VOLTAGE = Float.parseFloat(mParametersList.get(key));
                }
            }
        } else {
            Toast.makeText(this, mToolKit.getStringResource(R.string.noconfigfile_msg), Toast.LENGTH_SHORT).show();
        }
    }

    private void setViewByLanguage() {
        // TODO Auto-generated method stub
        TextView mItemTitle = (TextView) findViewById(R.id.item_title);
        mItemTitle.setText(mToolKit.getStringResource(R.string.monipower_test_title));
        ((TextView) findViewById(R.id.monipower_status_title)).setText(mToolKit.getStringResource(R.string.monipower_status));
        ((TextView) findViewById(R.id.monipower_present_title)).setText(mToolKit.getStringResource(R.string.monipower_present));
        ((TextView) findViewById(R.id.monipower_plug_title)).setText(mToolKit.getStringResource(R.string.monipower_plug));
        ((TextView) findViewById(R.id.monipower_level_title)).setText(mToolKit.getStringResource(R.string.monipower_level));
        ((TextView) findViewById(R.id.monipower_scale_title)).setText(mToolKit.getStringResource(R.string.monipower_scale));
        ((TextView) findViewById(R.id.monipower_health_title)).setText(mToolKit.getStringResource(R.string.monipower_health));
        ((TextView) findViewById(R.id.monipower_voltage_title)).setText(mToolKit.getStringResource(R.string.monipower_voltage));
        ((TextView) findViewById(R.id.monipower_temperature_title)).setText(mToolKit.getStringResource(R.string.monipower_temperature));
        ((TextView) findViewById(R.id.monipower_technology_title)).setText(mToolKit.getStringResource(R.string.monipower_technology));
        ((TextView) findViewById(R.id.monipower_time_title)).setText(mToolKit.getStringResource(R.string.monipower_time));
        btnExit.setText(mToolKit.getStringResource(R.string.button_exit));
    }

    private void findView() {
        // TODO Auto-generated method stub
        mBatteryStatus = (TextView) findViewById(R.id.monipower_status);
        mBatteryPresent = (TextView) findViewById(R.id.monipower_present);
        mBatteryPlug = (TextView) findViewById(R.id.monipower_plug);
        mBatteryLevel = (TextView) findViewById(R.id.monipower_level);
        mBatteryScale = (TextView) findViewById(R.id.monipower_scale);
        mBatteryHealth = (TextView) findViewById(R.id.monipower_health);
        mBatteryVoltage = (TextView) findViewById(R.id.monipower_voltage);
        mBatteryTemperature = (TextView) findViewById(R.id.monipower_temperature);
        mBatteryTechnology = (TextView) findViewById(R.id.monipower_technology);
        mTime = (TextView) findViewById(R.id.monipower_time);
        btnExit = (Button) findViewById(R.id.monipower_exit);
        btnExit.setOnClickListener(this);
        btnExit.setVisibility(View.INVISIBLE);
    }

    private void getTestArguments() {
        // TODO Auto-generated method stub
        String mTestStyle = mToolKit.getCurrentTestType();
        if (mTestStyle != null) {
            if (mTestStyle.equals(WisCommonConst.TESTSTYLE_SAVED_CONFIG)) {
                mComponentMode = false;
                WisParseValue mParse = new WisParseValue(this, mToolKit.getCurrentItem(), mToolKit.getCurrentDatabaseAuthorities());
                String arg = mParse.getArg1();
                if (arg != null && arg.length() > 0) {
                    float minVol = Float.parseFloat(arg);
                    if (minVol > 0) {
                        MIN_VOLTAGE = minVol;
                    }
                }
                arg = mParse.getArg2();
                if (arg != null && arg.length() > 0) {
                    float maxVol = Float.parseFloat(arg);
                    if (maxVol > 0) {
                        MAX_VOLTAGE = maxVol;
                    }
                }
            }
        }
    }

    private final Handler mExitHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            btnExit.performClick();
        }

    };
    private final Runnable mExitRunnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            try {
                Thread.sleep(TIMEOUT * 1000);
                mExitHandler.sendEmptyMessage(1);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
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

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }

    public class PowerReceiver extends BroadcastReceiver {
        private int mStatus;
        private int mPlug;
        private int mLevel, mScale;
        private int mHealth;
        private float mVoltage;
        private int mTemperature;
        private String mTechnology;

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                isPass = true;
                Log.i("Tag", "battery changed");
                mStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
                mPlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
                mLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                mScale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
                mHealth = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
                mVoltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) / 1000f;
                mTemperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
                mTechnology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);

                boolean present = intent.getBooleanExtra(BatteryManager.EXTRA_PRESENT, false);
                mBatteryPresent.setTextColor(Color.BLACK);
                if (present) {
                    mBatteryPresent.setText(mToolKit.getStringResource(R.string.monipower_present_true));
                } else {
                    mBatteryPresent.setText(mToolKit.getStringResource(R.string.monipower_present_false));
                    mBatteryPresent.setTextColor(Color.RED);
                }
                Log.i("Tag", String.valueOf(present));

                DecimalFormat mFormat = new DecimalFormat("0.0");
                mBatteryLevel.setText(String.valueOf(mLevel));
                mBatteryScale.setText(String.valueOf(mScale));
                mBatteryVoltage.setText(String.valueOf(mVoltage) + " V");
                mBatteryTemperature.setText(mFormat.format(mTemperature * 0.1) + " \u00b0C");
                mBatteryTechnology.setText(String.valueOf(mTechnology));

                mBatteryVoltage.setTextColor(Color.BLACK);
                if (mVoltage < MIN_VOLTAGE || mVoltage > MAX_VOLTAGE) {
                    isPass = false;
                    mBatteryVoltage.setTextColor(Color.RED);
                }

                mBatteryStatus.setTextColor(Color.BLACK);
                switch (mStatus) {
                    case BatteryManager.BATTERY_STATUS_CHARGING:
                        mBatteryStatus.setText("Charging");
                        break;
                    case BatteryManager.BATTERY_STATUS_DISCHARGING:
                        mBatteryStatus.setText("Discharging");
                        break;
                    case BatteryManager.BATTERY_STATUS_FULL:
                        mBatteryStatus.setText("Full");
                        break;
                    case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                        isPass = false;
                        mBatteryStatus.setTextColor(Color.RED);
                        mBatteryStatus.setText("Not charging");
                        break;
                    case BatteryManager.BATTERY_STATUS_UNKNOWN:
                        isPass = false;
                        mBatteryStatus.setTextColor(Color.RED);
                        mBatteryStatus.setText("Unknown");
                        break;
                    default:
                        isPass = false;
                        mBatteryStatus.setTextColor(Color.RED);
                        mBatteryStatus.setText("Unknown");
                        break;
                }

                mBatteryPlug.setTextColor(Color.BLACK);
                switch (mPlug) {
                    case BatteryManager.BATTERY_PLUGGED_AC:
                        mBatteryPlug.setText("AC");
                        break;
                    case BatteryManager.BATTERY_PLUGGED_USB:
                        mBatteryPlug.setText("USB");
                        break;
                    default:
                        mBatteryPlug.setText("On battery");
                        mBatteryPlug.setTextColor(Color.RED);
                        isPass = false;
                        break;
                }

                mBatteryHealth.setTextColor(Color.RED);
                switch (mHealth) {
                    case BatteryManager.BATTERY_HEALTH_DEAD:
                        mBatteryHealth.setText("dead");
                       // isPass = false;
                        break;
                    case BatteryManager.BATTERY_HEALTH_GOOD:
                        mBatteryHealth.setTextColor(Color.BLACK);
                        mBatteryHealth.setText("good");
                        break;
                    case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                        mBatteryHealth.setText("over voltage");
                        //isPass = false;
                        break;
                    case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                        mBatteryHealth.setText("over heat");
                        //isPass = false;
                        break;
                    case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                        mBatteryHealth.setText("unknown");
                       // isPass = false;
                        break;
                    case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                        mBatteryHealth.setText("unspecified failure");
                        //isPass = false;
                        break;
                    default:
                        mBatteryHealth.setText("unknown");
                        //isPass = false;
                        break;
                }
                mTime.setText(new Time(SystemClock.elapsedRealtime()).toString());
            } else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
                Log.i("Tag", intent.getExtras().toString());
            }
        }

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v == btnExit) {
            if (mComponentMode) {
                isResultPage = true;
                displayResult();
            } else {
                mToolKit.returnWithResult(isPass);
            }
        } else if (v == mResultButton) {
            mToolKit.returnWithResult(isPass);
        }
    }

    private void displayResult() {
        // TODO Auto-generated method stub
        setContentView(R.layout.result);
        mResultContent = (TextView) findViewById(R.id.result_result);
        mResultButton = (Button) findViewById(R.id.result_back);
        mResultButton.setText(mToolKit.getStringResource(R.string.ok));
        if (isPass) {
            mResultContent.setText(mToolKit.getStringResource(R.string.pass));
        } else {
            mResultContent.setText(mToolKit.getStringResource(R.string.fail));
        }
        mResultButton.setOnClickListener(this);
    }
}