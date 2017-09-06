package com.wistron.generic.compass;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wistron.generic.pqaa.R;
import com.wistron.pqaa_common.jar.global.WisCommonConst;
import com.wistron.pqaa_common.jar.global.WisParseValue;
import com.wistron.pqaa_common.jar.global.WisToolKit;

import java.util.Timer;
import java.util.TimerTask;

public class Compass_Test extends Activity implements OnClickListener {
    private static final int MSG_REFRESH = 0;

    private TextView mResultContent;
    private Button mResultButton;

    private SensorManager mSensorManager;
    //	private Sensor mSensor;
    private Sensor mAccelerometerSensor, mMageneticFieldSensor;

    private LinearLayout mValueDetectLayout, mModuleDetectLayout;
    private Button mPassBtn, mDetectBtn, mFailBtn;
    private ImageView mPanel, mTop;
    private TextView tvRemainTime, tvTotalTime;
    private TextView mSensorParameters;

    private boolean isPCBATest = false;
    private boolean isPass;
    private boolean mComponentMode = true;
    private int mDeviation = 10;
    private float mDegree;
    private Bitmap mOldBitmap;

    private Timer mTimer;
    private TimerTask mTask;
    private int TIMEOUT = 20;
    private int mTimeCount = 0;

    // sensor variables
    private float[] mGravity;
    private float[] mGeomagnetic;
    private float[] RR = new float[9];
    private float[] I = new float[9];
    private float[] mOrientation = new float[3];

    // common tool kit
    private WisToolKit mToolKit;

    public SensorEventListener mListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            // TODO Auto-generated method stub
            // Log.e("-----------------", ""+event.values[0]);
            // RotateAnimation mAnimation = new RotateAnimation(mOldDegree,
            // -event.values[0], Animation.RELATIVE_TO_SELF, 0.5f,
            // Animation.RELATIVE_TO_SELF, 0.5f);
            // mAnimation.setDuration(300);
            // mTop.startAnimation(mAnimation);
            // mOldDegree = -event.values[0];

            if (isPCBATest) {
                return;
            }
//			mDegree = event.values[0]; //Android2.2 and below
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                mGravity = event.values.clone();
            }
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                mGeomagnetic = event.values.clone();
            }
            if (mGravity == null || mGeomagnetic == null) {
                return;
            }
            SensorManager.getRotationMatrix(RR, I, mGravity, mGeomagnetic);
            SensorManager.getOrientation(RR, mOrientation);
            mDegree = (float) Math.toDegrees(mOrientation[0]);  // convert radian to degree
            Matrix matrix = new Matrix();
            matrix.postRotate(-mDegree);
            Bitmap mBitmap = Bitmap.createBitmap(mOldBitmap, 0, 0, mOldBitmap.getWidth(), mOldBitmap.getHeight(),
                    matrix, false);
            mTop.setImageBitmap(mBitmap);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }
    };

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.ecompass);

        mToolKit = new WisToolKit(this);

        getTestArguments();
        findView();
        setViewByLanguage();
        if (getSensor()) {
            setSensorParameterDisplay();
            initialTimer();
        } else {
            Toast.makeText(this, mToolKit.getStringResource(R.string.compass_error_unavailable), Toast.LENGTH_SHORT).show();
            mFailBtn.performClick();
        }
    }

    private boolean getSensor() {
        // TODO Auto-generated method stub
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION); // Android 2.2 and below
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMageneticFieldSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

//		mSensorManager.registerListener(mListener, mSensor, SensorManager.SENSOR_DELAY_GAME); // Android 2.2 and below
        boolean mAccelerometerSensorEnable = mSensorManager.registerListener(mListener, mAccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        boolean mMageneticFieldSensorEnable = mSensorManager.registerListener(mListener, mMageneticFieldSensor, SensorManager.SENSOR_DELAY_NORMAL);
        return mAccelerometerSensorEnable && mMageneticFieldSensorEnable;
    }

    private void setSensorParameterDisplay() {
        // TODO Auto-generated method stub
        String mParameter = "";
        mParameter = mParameter + (mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Accelerometer sensor:" : "加速度感应器:") + "\n";
        mParameter = mParameter + (mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Name: " : "感应器:") + mAccelerometerSensor.getName() + "\n";
        mParameter = mParameter + (mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Vendor: " : "厂商:") + mAccelerometerSensor.getVendor() + "\n";
        mParameter = mParameter + (mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Version: " : "版本:") + mAccelerometerSensor.getVersion() + "\n";

        mParameter = mParameter + "\n";
        mParameter = mParameter + (mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Magenetic sensor:" : "磁感应器:") + "\n";
        mParameter = mParameter + (mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Name: " : "感应器:") + mMageneticFieldSensor.getName() + "\n";
        mParameter = mParameter + (mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Vendor: " : "厂商:") + mMageneticFieldSensor.getVendor() + "\n";
        mParameter = mParameter + (mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Version: " : "版本:") + mMageneticFieldSensor.getVersion() + "\n";
        mSensorParameters.setText(mParameter);

        if (isPCBATest) {
            if (mAccelerometerSensor.getName() != null && !mAccelerometerSensor.getName().equals("")
                    && mMageneticFieldSensor.getName() != null && !mMageneticFieldSensor.getName().equals("")) {
                isPass = true;
            }
        }
    }

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            if (msg.what == MSG_REFRESH) {
                mTimeCount++;
                if (mTimeCount == TIMEOUT) {
                    if (isPCBATest) {
                        releaseResourceAndReturn();
                    } else {
                        mDetectBtn.performClick();
                    }
                } else {
                    if (isPCBATest) {
                        updateTimeout();
                    } else {
                        updateCompassTips();
                    }
                }
            }
        }

    };

    private void initialTimer() {
        // TODO Auto-generated method stub
        mTimer = new Timer();
        mTask = new TimerTask() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                handler.sendEmptyMessage(MSG_REFRESH);
            }
        };
        mTimer.schedule(mTask, 1000, 1000);
    }

    private void updateTimeout() {
        tvRemainTime.setText(mToolKit.getStringResource(R.string.remain_time) + mToolKit.formatCountDownTime(TIMEOUT, mTimeCount));
    }

    private void updateCompassTips() {
        // TODO Auto-generated method stub
        ((TextView) findViewById(R.id.compass_caution)).setText(String.format(mToolKit.getStringResource(R.string.compass_caution), (TIMEOUT - mTimeCount)));
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

    private void setViewByLanguage() {
        // TODO Auto-generated method stub
        TextView mItemTitle = (TextView) findViewById(R.id.item_title);
        mItemTitle.setText(mToolKit.getStringResource(R.string.ecompass_test_title));
        ((TextView) findViewById(R.id.compass_caution)).setText(mToolKit.getStringResource(R.string.compass_caution));
        mPassBtn.setText(mToolKit.getStringResource(R.string.button_pass));
        mFailBtn.setText(mToolKit.getStringResource(R.string.button_fail));
        mDetectBtn.setText(mToolKit.getStringResource(R.string.button_detect));

        if (isPCBATest) {
            updateTimeout();
            tvTotalTime.setText(mToolKit.getStringResource(R.string.total_time) + mToolKit.formatCountDownTime(TIMEOUT, mTimeCount));
        } else {
            updateCompassTips();
        }
    }

    private void findView() {
        mValueDetectLayout = (LinearLayout) findViewById(R.id.ecompass_value_detect);
        mModuleDetectLayout = (LinearLayout) findViewById(R.id.ecompass_module_detect);

        tvRemainTime = (TextView) findViewById(R.id.remaintime);
        tvTotalTime = (TextView) findViewById(R.id.totaltime);
        mSensorParameters = (TextView) findViewById(R.id.sensor_sensor);

        mPassBtn = (Button) findViewById(R.id.compass_pass_button);
        mDetectBtn = (Button) findViewById(R.id.compass_detect_button);
        mFailBtn = (Button) findViewById(R.id.compass_fail_button);

        mPanel = (ImageView) findViewById(R.id.compass_panel);
        mTop = (ImageView) findViewById(R.id.compass_top);
        mPanel.setBackgroundColor(Color.WHITE);

        mOldBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.top);

        mPassBtn.setOnClickListener(this);
        mFailBtn.setOnClickListener(this);
        mDetectBtn.setOnClickListener(this);

        mDetectBtn.setVisibility(View.INVISIBLE);

        if (isPCBATest) {
            mModuleDetectLayout.setVisibility(View.VISIBLE);
            mValueDetectLayout.setVisibility(View.GONE);
        } else {
            mModuleDetectLayout.setVisibility(View.GONE);
            mValueDetectLayout.setVisibility(View.VISIBLE);
        }
    }

    private void getTestArguments() {
        // TODO Auto-generated method stub
        String mTestStyle = mToolKit.getCurrentTestType();
        if (mTestStyle != null) {
            if (mTestStyle.equals(WisCommonConst.TESTSTYLE_SAVED_CONFIG)) {
                mComponentMode = false;
                isPCBATest = mToolKit.isPCBATestStage();
                WisParseValue mParse = new WisParseValue(this, mToolKit.getCurrentItem(), mToolKit.getCurrentDatabaseAuthorities());
                int mTestDeviation = Integer.parseInt(mParse.getArg1());
                int mTimeout = Integer.parseInt(mParse.getArg2());
                if (mTestDeviation > 0) {
                    mDeviation = mTestDeviation;
                }
                if (mTimeout > 0) {
                    TIMEOUT = mTimeout;
                }
            }
        }
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
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v == mPassBtn) {
            isPass = true;
            releaseResourceAndReturn();
        } else if (v == mFailBtn) {
            isPass = false;
            releaseResourceAndReturn();
        } else if (v == mDetectBtn) {
            if (Math.abs(mDegree) <= mDeviation) {
                mPassBtn.performClick();
            } else {
                mFailBtn.performClick();
            }
        }
    }

    private void releaseResourceAndReturn() {
        cancelTimer();
        mSensorManager.unregisterListener(mListener, mAccelerometerSensor);
        mSensorManager.unregisterListener(mListener, mMageneticFieldSensor);
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
}