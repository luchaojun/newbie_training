package com.wistron.generic.gyrosensor;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.wistron.generic.pqaa.R;
import com.wistron.pqaa_common.jar.global.WisCommonConst;
import com.wistron.pqaa_common.jar.global.WisLog;
import com.wistron.pqaa_common.jar.global.WisParseValue;
import com.wistron.pqaa_common.jar.global.WisToolKit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

public class GyroSensor extends Activity implements SensorEventListener {
    private static final String GYROSENSOR_LOG_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "gyro_log.txt";
    // -------------------------MeWIFIssage-----------
    private static final int MSG_START = 0;
    private static final int MSG_STOP = 1;
    private final int MSG_TIME = 2;
    private final int MSG_TIMEOUT = 3;
    // ----------------------result---------------
    private TextView mResultContent;
    private TextView mGyroSensor_X, mGyroSensor_Y, mGyroSensor_Z;
    private Button mResultButton;
    private SeekBar mSBRange;
    // --------------------------------------
    private SensorManager mSensorManager;
    private Sensor mSensor;

    private TextView tvRemainTime, tvTotalTime;
    private TextView mSensorParameters, mSensorValue;
    private Button mPassButton, mFailButton;
    // ---------------------------------------
    private boolean isPass;
    private boolean mComponentMode = true;
    private boolean isPCBATest = true;
    // --------------------parameters---
    private boolean mFirst = true;
    private float mBase = 1000f;
    private float GSensor_X, GSensor_Y, GSensor_Z;

    private int TIMEOUT = 5;
    private int mTimes = 0;
    private Timer mTimer = new Timer();
    private TimerTask mTask;
    private boolean mSensorEnable;

    private float GyroSensor_X, GyroSensor_Y, Gyrosensor_Z;
    private boolean isGyroXFirst = true, isGyroYFirst = true, isGyroZFirst = true;
    private boolean isGyroXPass, isGyroYPass, isGyroZPass;

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
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.gyro_sensor);

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
            Toast.makeText(this, mToolKit.getStringResource(R.string.error), Toast.LENGTH_SHORT).show();
            mFailButton.performClick();
        }
    }

    private void setViewByLanguage() {
        // TODO Auto-generated method stub
        TextView mItemTitle = (TextView) findViewById(R.id.item_title);
        mItemTitle.setText(mToolKit.getStringResource(R.string.gyro_test_title));
        ((TextView) findViewById(R.id.sensor_tips)).setText(mToolKit.getStringResource(R.string.sensor_tips));
        tvTotalTime.setText(mToolKit.getStringResource(R.string.total_time) + mToolKit.formatCountDownTime(TIMEOUT, mTimes));
        tvRemainTime.setText(mToolKit.getStringResource(R.string.remain_time) + mToolKit.formatCountDownTime(TIMEOUT, mTimes));
        mPassButton.setText(mToolKit.getStringResource(R.string.button_pass));
        mFailButton.setText(mToolKit.getStringResource(R.string.button_fail));
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
                WisParseValue mParse = new WisParseValue(this, mToolKit.getCurrentItem(), mToolKit.getCurrentDatabaseAuthorities());
                int mTestTime = Integer.parseInt(mParse.getArg1());
                if (mTestTime > 0) {
                    TIMEOUT = mTestTime;
                }
                float floatValue = Float.parseFloat(mParse.getArg2());
                if (floatValue > 0) {
                    mBase = floatValue;
                }
            }
        }
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
                    tvRemainTime.setText(mToolKit.getStringResource(R.string.remain_time) + mToolKit.formatCountDownTime(TIMEOUT, mTimes));
                    if (!isPCBATest && isGyroXPass && isGyroYPass && isGyroZPass) {
                        mPassButton.performClick();
                    }
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
        mGyroSensor_X = (TextView) findViewById(R.id.sensor_xvalue);
        mGyroSensor_Y = (TextView) findViewById(R.id.sensor_yvalue);
        mGyroSensor_Z = (TextView) findViewById(R.id.sensor_zvalue);

        tvRemainTime = (TextView) findViewById(R.id.remaintime);
        tvTotalTime = (TextView) findViewById(R.id.totaltime);
        mSensorParameters = (TextView) findViewById(R.id.sensor_sensor);
        mSensorValue = (TextView) findViewById(R.id.sensor_data);
        mPassButton = (Button) findViewById(R.id.sensor_pass);
        mFailButton = (Button) findViewById(R.id.sensor_fail);
        mSBRange = (SeekBar) findViewById(R.id.sensor_range);

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
        mSBRange.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub
                if (progress > 9) {
                    progress = 10;
                } else {
                    if (progress < 1) {
                        progress = 0;
                    }
                }
                mBase = (float) progress / seekBar.getMax();
            }
        });
//		getBaseArguments();
        mSBRange.setProgress((int) (mBase * 10));
    }

    private float formatValue(float value) {
        DecimalFormat mFormat = new DecimalFormat("0.0");
        return Float.parseFloat(mFormat.format(value));
    }

    private void getBaseArguments() {
        String mLine;
        String mValue = "";
        try {
            FileReader mFileReader = new FileReader("/mnt/extsd/sensor/gyrosensor.cfg");
            BufferedReader mReader = new BufferedReader(mFileReader);
            while ((mLine = mReader.readLine()) != null) {
                if (!mLine.startsWith("#")) {
                    mValue = mLine.substring(mLine.indexOf("=") + 1);
                    break;
                }
            }
            mBase = formatValue(Integer.parseInt(mValue) / 100f);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    private boolean getSensor() {
        // TODO Auto-generated method stub
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorEnable = mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        return mSensorEnable;
    }

    private void setSensorParameterDisplay() {
        // TODO Auto-generated method stub
        String mParameter = "";
        mParameter = mParameter + (mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Name: " : "感应器:") + mSensor.getName() + "\n";
        mParameter = mParameter + (mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Vendor: " : "厂商:") + mSensor.getVendor() + "\n";
        mParameter = mParameter + (mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Version: " : "版本:") + mSensor.getVersion() + "\n";
        mSensorParameters.setText(mParameter);

        if (isPCBATest) {
            if (mSensor.getName() != null && !mSensor.getName().equals("")) {
                isPass = true;
            }
        }
    }

    private void releaseResourceAndReturn() {
        cancelTimer();
        mSensorManager.unregisterListener(GyroSensor.this);
        if (mComponentMode) {
            displayResult();
        } else {
            backToPQAA();
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
            setGSensorValueDisplay(event);
        }
    }

    private void setGSensorValueDisplay(SensorEvent event) {
        // TODO Auto-generated method stub
        String mValue = "";
        mValue = mValue + "X: " + event.values[0] + "\n";
        mValue = mValue + "Y: " + event.values[1] + "\n";
        mValue = mValue + "Z: " + event.values[2] + "\n";
        mSensorValue.setText(mValue);
        if (mFirst) {
            GSensor_X = event.values[0];
            GSensor_Y = event.values[1];
            GSensor_Z = event.values[2];
            mFirst = false;
        } else {

            float mTempX = (float) (GSensor_X * mBase);
            float mTempY = (float) (GSensor_Y * mBase);
            float mTempZ = (float) (GSensor_Z * mBase);
            if ((event.values[0] > GSensor_X + mTempX || event.values[0] < GSensor_X - mTempX)
                    && (event.values[1] > GSensor_Y + mTempY || event.values[0] < GSensor_Y - mTempY)
                    && (event.values[2] > GSensor_Z + mTempZ || event.values[2] < GSensor_Z - mTempZ)) {
//				mPassButton.performClick();
            }
        }

        GyroSensor_X = (float) Math.toDegrees(event.values[0]);
        GyroSensor_Y = (float) Math.toDegrees(event.values[1]);
        Gyrosensor_Z = (float) Math.toDegrees(event.values[2]);

        if (GyroSensor_X > 5) {
            showXValue(event.values[0]);
        }
        if (GyroSensor_Y > 5) {
            showYValue(event.values[1]);
        }
        if (Gyrosensor_Z > 5) {
            showZValue(event.values[2]);
        }
    }

    private void showXValue(float value) {
        if (isGyroXFirst) {
            mGyroSensor_X.setText("X: " + value);
            try {
                mLogHandler.write("X=: " + value + " rad/s" + "\r\n", true);
                mLogHandler.write("X=: " + GyroSensor_X + " dps" + "\r\n", true);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            isGyroXFirst = false;
            isGyroXPass = true;
        }
    }

    private void showYValue(float value) {
        if (isGyroYFirst) {
            mGyroSensor_Y.setText("Y: " + value);
            try {
                mLogHandler.write("Y=: " + value + " rad/s" + "\r\n", true);
                mLogHandler.write("Y=: " + GyroSensor_Y + " dps" + "\r\n", true);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            isGyroYFirst = false;
            isGyroYPass = true;
        }
    }

    private void showZValue(float value) {
        if (isGyroZFirst) {
            mGyroSensor_Z.setText("Z: " + value);
            try {
                mLogHandler.write("Z=: " + value + " rad/s" + "\r\n", true);
                mLogHandler.write("Z=: " + Gyrosensor_Z + " dps" + "\r\n", true);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            isGyroZFirst = false;
            isGyroZPass = true;
        }
    }
}