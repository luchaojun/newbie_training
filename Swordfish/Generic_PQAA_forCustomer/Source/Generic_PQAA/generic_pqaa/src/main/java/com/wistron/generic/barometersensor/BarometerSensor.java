package com.wistron.generic.barometersensor;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.wistron.generic.pqaa.utility.CommonParams;
import com.wistron.pqaa_common.jar.global.WisCommonConst;
import com.wistron.pqaa_common.jar.global.WisLog;
import com.wistron.pqaa_common.jar.global.WisParseValue;
import com.wistron.pqaa_common.jar.global.WisToolKit;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class BarometerSensor extends Activity implements SensorEventListener {
    private static final String GYROSENSOR_LOG_PATH = CommonParams.GENERIC_PATH
            + "barometer_log.txt";
    // -------------------------MeWIFIssage-----------
    private static final int MSG_START = 0;
    private static final int MSG_STOP = 1;
    private final int MSG_TIME = 2;
    private final int MSG_TIMEOUT = 3;
    // ----------------------result---------------
    private TextView mResultContent;
    private Button mResultButton;
    // --------------------------------------
    private SensorManager mSensorManager;
    private Sensor mSensor;

    private TextView tvRemainTime, tvTotalTime;
    private TextView mSensorParameters, mSensorValue;
    private TextView mRangeVauleView;
    private Button mPassButton, mFailButton;
    // ---------------------------------------
    private boolean isPass;
    private boolean mComponentMode = true;
    private boolean isPCBATest = true;
    // --------------------parameters---
    private float mBase = 0.3f;
    private int minValue = 1000, maxValue = 1500;

    private int TIMEOUT = 5;
    private int mTimes = 0;
    private Timer mTimer = new Timer();
    private TimerTask mTask;
    private boolean mSensorEnable;

    // common tool kit
    private WisToolKit mToolKit;

    // Log class
    private WisLog mLogHandler;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.barometersensor);

        mToolKit = new WisToolKit(this);

        try {
            mLogHandler = new WisLog(GYROSENSOR_LOG_PATH);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        findView();
        getTestArguments();
        setViewByLanguage();
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (getSensor()) {
            setSensorParameterDisplay();
            initialTimer();
        } else {
            Toast.makeText(this, mToolKit.getStringResource(R.string.barometererror), Toast.LENGTH_SHORT)
                    .show();
            mFailButton.performClick();
        }
    }

    private void setViewByLanguage() {
        // TODO Auto-generated method stub
        TextView mItemTitle = (TextView) findViewById(R.id.item_title);
        mItemTitle.setText(mToolKit.getStringResource(R.string.barometer_test_title));

        tvTotalTime.setText(mToolKit.getStringResource(R.string.total_time)
                + mToolKit.formatCountDownTime(TIMEOUT, mTimes));
        tvRemainTime.setText(mToolKit.getStringResource(R.string.remain_time)
                + mToolKit.formatCountDownTime(TIMEOUT, mTimes));
        mPassButton.setText(mToolKit.getStringResource(R.string.button_pass));
        mFailButton.setText(mToolKit.getStringResource(R.string.button_fail));

        mRangeVauleView.setText(String.format(mToolKit.getStringResource(R.string.barometer_range),
                minValue, maxValue));
    }

    private void initialTimer() {
        // TODO Auto-generated method stub
        mTask = new TimerTask() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                mTimes++;
                if (mTimes >= TIMEOUT) {
                    handler.sendEmptyMessage(MSG_TIMEOUT);
                } else {
                    handler.sendEmptyMessage(MSG_TIME);
                }
            }
        };
        mTimer.schedule(mTask, 1000, 1000);
    }

    private void getTestArguments() {
        // TODO Auto-generated method stub
        String mTestStyle = mToolKit.getCurrentTestType();
        if (mTestStyle != null) {
            if (mTestStyle.equals(WisCommonConst.TESTSTYLE_SAVED_CONFIG)) {
                mComponentMode = false;
                isPCBATest = mToolKit.isPCBATestStage();
                WisParseValue mParse = new WisParseValue(this, mToolKit.getCurrentItem(),
                        mToolKit.getCurrentDatabaseAuthorities());
                int mTestTime = Integer.parseInt(mParse.getArg1());
                if (mTestTime > 0) {
                    TIMEOUT = mTestTime;
                }

                int min = Integer.parseInt(mParse.getArg2());
                if (min > 0) {
                    minValue = min;
                }
                int max = Integer.parseInt(mParse.getArg3());
                if (max > 0) {
                    maxValue = max;
                }
            }
        }
        Log.i("W", String.format("The range value %1$s ~ %2$s", minValue, maxValue));
    }

    private void cancelTimer() {
        if (mTask != null) {
            mTask.cancel();
            mTask = null;
        }
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_TIME:
                    tvRemainTime.setText(mToolKit.getStringResource(R.string.remain_time)
                            + mToolKit.formatCountDownTime(TIMEOUT, mTimes));
                    break;
                case MSG_TIMEOUT:
                    releaseResourceAndReturn();
                    break;
                default:
                    break;
            }
        }

    };

    private void findView() {
        // TODO Auto-generated method stub
        tvRemainTime = (TextView) findViewById(R.id.remaintime);
        tvTotalTime = (TextView) findViewById(R.id.totaltime);
        mSensorParameters = (TextView) findViewById(R.id.sensor_sensor);
        mSensorValue = (TextView) findViewById(R.id.sensor_data);
        mPassButton = (Button) findViewById(R.id.sensor_pass);
        mFailButton = (Button) findViewById(R.id.sensor_fail);

        mPassButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                isPass = true;
                releaseResourceAndReturn();
            }
        });
        mFailButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                isPass = false;
                releaseResourceAndReturn();
            }
        });

        mRangeVauleView = (TextView) findViewById(R.id.sensor_range);
    }

    private boolean getSensor() {
        // TODO Auto-generated method stub
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        mSensorEnable = mSensorManager.registerListener(this, mSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
        return mSensorEnable;
    }

    private void setSensorParameterDisplay() {
        // TODO Auto-generated method stub
        String mParameter = "";
        mParameter = mParameter
                + (mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Name: "
                : "感应器:") + mSensor.getName() + "\n";
        mParameter = mParameter
                + (mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Vendor: "
                : "厂商:") + mSensor.getVendor() + "\n";
        mParameter = mParameter
                + (mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Version: "
                : "版本:") + mSensor.getVersion() + "\n";
        mSensorParameters.setText(mParameter);

        if (isPCBATest) {
            if (mSensor.getName() != null && !mSensor.getName().equals("")) {
                isPass = true;
            }
        }
    }

    private void releaseResourceAndReturn() {
        cancelTimer();
        mSensorManager.unregisterListener(BarometerSensor.this);

            backToPQAA();

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

        mResultButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                backToPQAA();
            }
        });
    }

    private void backToPQAA() {
        mToolKit.returnWithResult(isPass);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        // Process.killProcess(Process.myPid());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        if (!isPCBATest) {
            setLightSensorValueDisplay(event);
        }
    }

    private void setLightSensorValueDisplay(SensorEvent event) {
        // TODO Auto-generated method stub
        String mValue = "";
        mValue = mValue
                + (mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Barometer Sensor Value: "
                : "压力传感器值：") + event.values[0] + "\n";
        mSensorValue.setText(mValue);

        if (event.values[0] >= minValue && event.values[0] <= maxValue) {
            mPassButton.performClick();
        }
    }

}