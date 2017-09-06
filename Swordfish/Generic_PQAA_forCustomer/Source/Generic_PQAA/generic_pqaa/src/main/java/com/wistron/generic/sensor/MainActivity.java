package com.wistron.generic.sensor;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wistron.generic.pqaa.R;
import com.wistron.pqaa_common.jar.global.WisCommonConst;
import com.wistron.pqaa_common.jar.global.WisParseValue;
import com.wistron.pqaa_common.jar.global.WisToolKit;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements SensorEventListener, OnClickListener {

    private int mTestItem = 15;
    private int TIMEOUT = 20, times = 0;
    private Timer mTimer;
    private TimerTask mTask;
    private WisToolKit mToolKit;
    private SensorManager mSensorManager;
    private Sensor mLightSensor, mProximitySensor;
    private Sensor mAccelerometerSensor, mGyroSensor;
    private Sensor mMageneticFieldSensor;
    private boolean mLightSensorEnable, mProximitySensorEnable;
    private boolean mAccelerometerSensorEnable, mGyroSensorEnable;
    private boolean mMageneticFieldSensorEnable;
    private LinearLayout mShowSensorInfoLayout, mShowEcompassLayout;
    private LinearLayout mBottomRightLayout, mBottomLeftLayout;
    private TextView mTotalTimeView, mRemainTimeView;
    private TextView mFirstVendorView, mFirstDataView, mFirstPassDataView;
    private TextView mSecondVendorView, mSecondDataView, mSecondPassDataView;
    private TextView mThirdVendorView, mThirdSensorDataView, mThirdPassDataView;
    private TextView mFourthVendorView, mFourthSensorDataView, mFourthPassDataView;
    private TextView mFifthVendorView, mFifthSensorDataView, mFifthPassDataView;

    private CheckBox lightBox, proximityBox, accelerometerBox, gyroBox, ecompassBox;
    private boolean isTestLight, isTestProximity, isTestAcce, isTestGyro, isTestEcompass;
    private int lightNumber = 0, proximityNumber = 0, accNumber = 0, gyroNumber = 0,
            eCompassNumber = 0;
    private boolean isLightSensorPass, isProximityPass, isAccelePass, isGyroPass, iseCompass;
    private boolean isLightPass;
    private boolean isLightFirst = true, isProximityFirst = true, isAcceleFirst = true,
            isGyroFirst = true, iseCompassFirst = true;
    private float lightFirstvalue = 0, proximityFirstvalue = 0;
    private float acceleFirstXvalue = 0, acceleFirstYvalue = 0, acceleFirstZvalue = 0;
    private float gyroFirstXvalue = 0, gyroFirstYvalue = 0, gyroFirstZvalue = 0;
    private float mFirstDegree;
    private float mBase = (float) 0.3;
    private float mDegree;
    private float[] mGravity;
    private float[] mGeomagnetic;
    private float[] RR = new float[9];
    private float[] I = new float[9];
    private float[] mOrientation = new float[3];
    private boolean isPass;
    private Button btnStart;
    private String mNGItemName = "";
    private boolean isSensorAvailable = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main_sensor);
        mToolKit = new WisToolKit(this);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        initView();
        getTestArguments();
        setViewByLanguage();

        btnStart.setVisibility(View.INVISIBLE);

        new Thread(waitStart).start();
    }

    private Runnable waitStart = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            try {
                Thread.sleep(500);
            } catch (Exception e) {
                // TODO: handle exception
            }
            handler.sendEmptyMessage(2);
        }
    };

    private void initView() {
        mShowSensorInfoLayout = (LinearLayout) findViewById(R.id.show_sensor_info);
        mShowEcompassLayout = (LinearLayout) findViewById(R.id.show_ecompass_layout);
        mBottomRightLayout = (LinearLayout) findViewById(R.id.sensor_bottom_right_layout);
        mBottomLeftLayout = (LinearLayout) findViewById(R.id.sensor_bottom_left_layout);

        mTotalTimeView = (TextView) findViewById(R.id.totaltime);
        mRemainTimeView = (TextView) findViewById(R.id.remaintime);

        mFirstPassDataView = (TextView) findViewById(R.id.first_layout_pass_value);
        mFirstDataView = (TextView) findViewById(R.id.first_layout_sensor_data);
        mFirstVendorView = (TextView) findViewById(R.id.first_layout_vendor_info);

        mSecondPassDataView = (TextView) findViewById(R.id.second_layout_pass_value);
        mSecondDataView = (TextView) findViewById(R.id.second_layout_sensor_data);
        mSecondVendorView = (TextView) findViewById(R.id.second_layout_vendor_info);

        mThirdPassDataView = (TextView) findViewById(R.id.third_layout_pass_value);
        mThirdSensorDataView = (TextView) findViewById(R.id.third_layout_sensor_data);
        mThirdVendorView = (TextView) findViewById(R.id.third_layout_vendor_info);

        mFourthPassDataView = (TextView) findViewById(R.id.fourth_layout_pass_value);
        mFourthSensorDataView = (TextView) findViewById(R.id.fourth_layout_sensor_data);
        mFourthVendorView = (TextView) findViewById(R.id.fourth_layout_vendor_info);

        mFifthPassDataView = (TextView) findViewById(R.id.fifth_layout_pass_value);
        mFifthSensorDataView = (TextView) findViewById(R.id.fifth_layout_sensor_data);
        mFifthVendorView = (TextView) findViewById(R.id.fifth_layout_vendor_info);

        lightBox = (CheckBox) findViewById(R.id.light_sensor_box);
        proximityBox = (CheckBox) findViewById(R.id.proximity_sensor_box);
        accelerometerBox = (CheckBox) findViewById(R.id.accelerometer_sensor_box);
        gyroBox = (CheckBox) findViewById(R.id.gyro_sensor_box);
        ecompassBox = (CheckBox) findViewById(R.id.ecompass_box);

        lightBox.setOnCheckedChangeListener(new boxCheckChangeListener());
        proximityBox.setOnCheckedChangeListener(new boxCheckChangeListener());
        accelerometerBox.setOnCheckedChangeListener(new boxCheckChangeListener());
        gyroBox.setOnCheckedChangeListener(new boxCheckChangeListener());
        ecompassBox.setOnCheckedChangeListener(new boxCheckChangeListener());

        btnStart = (Button) findViewById(R.id.btn_start);
        btnStart.setOnClickListener(this);
    }

    private void getTestArguments() {
        // TODO Auto-generated method stub
        String mTestStyle = mToolKit.getCurrentTestType();
        if (mTestStyle != null) {
            if (mTestStyle.equals(WisCommonConst.TESTSTYLE_SAVED_CONFIG)) {
                WisParseValue mParse = new WisParseValue(this, mToolKit.getCurrentItem(),
                        mToolKit.getCurrentDatabaseAuthorities());
                int mTestTime = Integer.parseInt(mParse.getArg1());
                if (mTestTime > 0) {
                    TIMEOUT = mTestTime;
                }
                int subitem = Integer.parseInt(mParse.getArg2());
                if (subitem > 0) {
                    mTestItem = subitem;
                }
            }
        }
        mTotalTimeView.setText(mToolKit.getStringResource(R.string.total_time)
                + mToolKit.formatCountDownTime(TIMEOUT, times));
        mRemainTimeView.setText(mToolKit.getStringResource(R.string.remain_time)
                + mToolKit.formatCountDownTime(TIMEOUT, times));
        if ((mTestItem & (int) Math.pow((double) 2, (double) 0)) != 0) {
            lightBox.setChecked(true);
        }
        if ((mTestItem & (int) Math.pow((double) 2, (double) 1)) != 0) {
            proximityBox.setChecked(true);
        }
        if ((mTestItem & (int) Math.pow((double) 2, (double) 2)) != 0) {
            accelerometerBox.setChecked(true);
        }
        if ((mTestItem & (int) Math.pow((double) 2, (double) 3)) != 0) {
            gyroBox.setChecked(true);
        }
        if ((mTestItem & (int) Math.pow((double) 2, (double) 4)) != 0) {
            ecompassBox.setChecked(true);
        }
    }

    class boxCheckChangeListener implements OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            // TODO Auto-generated method stub
            switch (buttonView.getId()) {
                case R.id.light_sensor_box:
                    isTestLight = isChecked;
                    break;
                case R.id.proximity_sensor_box:
                    isTestProximity = isChecked;
                    break;
                case R.id.accelerometer_sensor_box:
                    isTestAcce = isChecked;
                    break;
                case R.id.gyro_sensor_box:
                    isTestGyro = isChecked;
                    break;
                case R.id.ecompass_box:
                    isTestEcompass = isChecked;
                    break;
            }
        }
    }

    private void setViewByLanguage() {
        ((TextView) findViewById(R.id.item_title)).setText(mToolKit
                .getStringResource(R.string.sensor_title));
        ((TextView) findViewById(R.id.sensor_select_prompt)).setText(mToolKit
                .getStringResource(R.string.sensor_select));
        lightBox.setText(mToolKit.getStringResource(R.string.sensor_checkbox_light));
        proximityBox.setText(mToolKit.getStringResource(R.string.sensor_checkbox_pro));
        accelerometerBox.setText(mToolKit.getStringResource(R.string.sensor_checkbox_acce));
        gyroBox.setText(mToolKit.getStringResource(R.string.sensor_checkbox_gyro));
        ecompassBox.setText(mToolKit.getStringResource(R.string.sensor_checkbox_ecompass));
    }

    private boolean getLightSensor() {
        // TODO Auto-generated method stub
        mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mLightSensorEnable = mSensorManager.registerListener(this, mLightSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
        return mLightSensorEnable;
    }

    private boolean getProximitySensor() {
        // TODO Auto-generated method stub
        mProximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mProximitySensorEnable = mSensorManager.registerListener(this, mProximitySensor,
                SensorManager.SENSOR_DELAY_NORMAL);
        return mProximitySensorEnable;
    }

    private boolean getAccelerometerSensor() {
        // TODO Auto-generated method stub
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mAccelerometerSensorEnable = mSensorManager.registerListener(this, mAccelerometerSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
        return mAccelerometerSensorEnable;
    }

    private boolean getGyroSensor() {
        // TODO Auto-generated method stub
        mGyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mGyroSensorEnable = mSensorManager.registerListener(this, mGyroSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
        return mGyroSensorEnable;
    }

    private boolean getMageneticFieldSensor() {
        // TODO Auto-generated method stub
        mMageneticFieldSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mMageneticFieldSensorEnable = mSensorManager.registerListener(this, mMageneticFieldSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
        return mMageneticFieldSensorEnable;
    }

    private void setLightSensorParameterDisplay(int layout_id, String title) {
        // TODO Auto-generated method stub
        String mParameter = "";
        mParameter = mParameter
                + (mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Name: "
                : "感应器： ") + mLightSensor.getName() + "\n";
        mParameter = mParameter
                + (mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Vendor: "
                : "厂商： ") + mLightSensor.getVendor() + "\n";
        mParameter = mParameter
                + (mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Version: "
                : "版本： ") + mLightSensor.getVersion() + "\n";
        // mLightVendorView.setText(mParameter);
        showLayouArea(mParameter, title, layout_id, 1);
    }

    private void setProximitySensorParameterDisplay(int layout_id, String title) {
        // TODO Auto-generated method stub
        String mParameter = "";
        mParameter = mParameter
                + (mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Name: "
                : "感应器： ") + mProximitySensor.getName() + "\n";
        mParameter = mParameter
                + (mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Vendor: "
                : "厂商： ") + mProximitySensor.getVendor() + "\n";
        mParameter = mParameter
                + (mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Version: "
                : "版本： ") + mProximitySensor.getVersion() + "\n";
        // mProximityVendorView.setText(mParameter);
        showLayouArea(mParameter, title, layout_id, 1);
    }

    private void setAccelerometerSensorParameterDisplay(int layout_id, String title) {
        // TODO Auto-generated method stub
        String mParameter = "";
        mParameter = mParameter
                + (mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Name: "
                : "感应器： ") + mAccelerometerSensor.getName() + "\n";
        mParameter = mParameter
                + (mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Vendor: "
                : "厂商： ") + mAccelerometerSensor.getVendor() + "\n";
        mParameter = mParameter
                + (mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Version: "
                : "版本： ") + mAccelerometerSensor.getVersion() + "\n";
        // mAccelerometerVendorView.setText(mParameter);
        showLayouArea(mParameter, title, layout_id, 1);
    }

    private void setGyroSensorParameterDisplay(int layout_id, String title) {
        // TODO Auto-generated method stub
        String mParameter = "";
        mParameter = mParameter
                + (mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Name: "
                : "感应器： ") + mGyroSensor.getName() + "\n";
        mParameter = mParameter
                + (mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Vendor: "
                : "厂商： ") + mGyroSensor.getVendor() + "\n";
        mParameter = mParameter
                + (mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Version: "
                : "版本： ") + mGyroSensor.getVersion() + "\n";
        // mGyroVendorView.setText(mParameter);
        showLayouArea(mParameter, title, layout_id, 1);
    }

    private void setMageneticFieldSensorParameterDisplay(int layout_id, String title) {
        // TODO Auto-generated method stub
        String mParameter = "";
        mParameter = mParameter
                + (mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Name: "
                : "感应器： ") + mMageneticFieldSensor.getName() + "\n";
        mParameter = mParameter
                + (mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Vendor: "
                : "厂商： ") + mMageneticFieldSensor.getVendor() + "\n";
        mParameter = mParameter
                + (mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Version: "
                : "版本： ") + mMageneticFieldSensor.getVersion() + "\n";
        // mGyroVendorView.setText(mParameter);
        showLayouArea(getAccelerometerSensorParameterDisplay() + "\n" + mParameter, title,
                layout_id, 1);
    }

    private String getAccelerometerSensorParameterDisplay() {
        // TODO Auto-generated method stub
        String mParameter = "";
        mParameter = mParameter
                + (mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Name: "
                : "感应器： ") + mAccelerometerSensor.getName() + "\n";
        mParameter = mParameter
                + (mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Vendor: "
                : "厂商： ") + mAccelerometerSensor.getVendor() + "\n";
        mParameter = mParameter
                + (mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Version: "
                : "版本： ") + mAccelerometerSensor.getVersion() + "\n";
        // mAccelerometerVendorView.setText(mParameter);
        return mParameter;
    }

    private void showLayouArea(String content, String title, int layout_id, int info_id) {
        switch (layout_id) {
            case 1:
                switch (info_id) {
                    case 1:
                        ((TextView) findViewById(R.id.first_layout_title)).setText(title);
                        ((TextView) findViewById(R.id.first_layout_title)).getPaint().setFakeBoldText(true);
                        mFirstVendorView.setText(content);
                        break;
                    case 2:
                        mFirstDataView.setText(content);
                        break;
                    case 3:
                        mFirstPassDataView.setText(content);
                        break;
                }
                break;
            case 2:
                switch (info_id) {
                    case 1:
                        ((TextView) findViewById(R.id.second_layout_title)).setText(title);
                        ((TextView) findViewById(R.id.second_layout_title)).getPaint()
                                .setFakeBoldText(true);
                        mSecondVendorView.setText(content);
                        break;
                    case 2:
                        mSecondDataView.setText(content);
                        break;
                    case 3:
                        mSecondPassDataView.setText(content);
                        break;
                }
                break;
            case 3:
                switch (info_id) {
                    case 1:
                        ((TextView) findViewById(R.id.third_layout_title)).setText(title);
                        ((TextView) findViewById(R.id.third_layout_title)).getPaint().setFakeBoldText(true);
                        mThirdVendorView.setText(content);
                        break;
                    case 2:
                        mThirdSensorDataView.setText(content);
                        break;
                    case 3:
                        mThirdPassDataView.setText(content);
                        break;
                }
                break;
            case 4:
                switch (info_id) {
                    case 1:
                        ((TextView) findViewById(R.id.fourth_layout_title)).setText(title);
                        ((TextView) findViewById(R.id.fourth_layout_title)).getPaint()
                                .setFakeBoldText(true);
                        mFourthVendorView.setText(content);
                        break;
                    case 2:
                        mFourthSensorDataView.setText(content);
                        break;
                    case 3:
                        mFourthPassDataView.setText(content);
                        break;
                }
                break;
            case 5:
                switch (info_id) {
                    case 1:
                        ((TextView) findViewById(R.id.fifth_layout_title)).setText(title);
                        ((TextView) findViewById(R.id.fifth_layout_title)).getPaint().setFakeBoldText(true);
                        mFifthVendorView.setText(content);
                        break;
                    case 2:
                        mFifthSensorDataView.setText(content);
                        break;
                    case 3:
                        mFifthPassDataView.setText(content);
                        break;
                }
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        displaySensorValue(event);
    }

    private void displaySensorValue(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            String mValue = "";
            mValue = mValue
                    + (mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Light level: "
                    : "亮度阶：") + event.values[0];
            // mLightSensorDataView.setText(mValue);
            showLayouArea(mValue, "", lightNumber, 2);
            if (isLightFirst) {
                isLightFirst = false;
                lightFirstvalue = event.values[0];
                Log.i("Payne",lightFirstvalue+"=LightSensor");
                if ((int) lightFirstvalue == 0) {
                    isLightFirst = true;
                }
            } else {
                if (event.values[0] > (lightFirstvalue + lightFirstvalue * mBase)
                        || event.values[0] < (lightFirstvalue - lightFirstvalue * mBase)) {
                    isLightSensorPass = true;
                    // mLightPassDataView
                    // .setText((mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Pass Value: "
                    // : "通过值：")
                    // + event.values[0] + "\n");
                    showLayouArea(
                            (mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Pass Value: "
                                    : "通过值：")
                                    + event.values[0] + "\n", "", lightNumber, 3);
                }
            }
        } else if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            String mValue = "";
            mValue = mValue
                    + (mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Proximity distance: "
                    : "距离：") + event.values[0];
            // mProximitySensorDataView.setText(mValue);
            showLayouArea(mValue, "", proximityNumber, 2);
            if (isProximityFirst) {
                isProximityFirst = false;
                proximityFirstvalue = event.values[0];
                Log.i("Payne",proximityFirstvalue+"=PSensor");
            } else {
                if (event.values[0] > (proximityFirstvalue + proximityFirstvalue * mBase)
                        || event.values[0] < (proximityFirstvalue - proximityFirstvalue * mBase)) {
                    isProximityPass = true;
                    // mProximityPassDataView
                    // .setText((mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Pass Value: "
                    // : "通过值：")
                    // + event.values[0] + "\n");
                    showLayouArea(
                            (mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Pass Value: "
                                    : "通过值：")
                                    + event.values[0] + "\n", "", proximityNumber, 3);
                }
            }
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            String mValue = "";
            mValue = mValue + "X: " + event.values[0] + "\n";
            mValue = mValue + "Y: " + event.values[1] + "\n";
            mValue = mValue + "Z: " + event.values[2];
            // mGyroSensorDataView.setText(mValue);
            showLayouArea(mValue, "", gyroNumber, 2);
            if (isGyroFirst) {
                isGyroFirst = false;
                gyroFirstXvalue = event.values[0];
                gyroFirstYvalue = event.values[1];
                gyroFirstZvalue = event.values[2];
            } else {
                float tempx = gyroFirstXvalue * mBase;
                float tempy = gyroFirstYvalue * mBase;
                float tempz = gyroFirstZvalue * mBase;
                if ((event.values[0] > gyroFirstXvalue + tempx || event.values[0] < gyroFirstXvalue
                        - tempx)
                        && (event.values[1] > gyroFirstYvalue + tempy || event.values[1] < gyroFirstYvalue
                        - tempy)
                        && (event.values[2] > gyroFirstZvalue + tempz || event.values[2] < gyroFirstZvalue
                        - tempz)) {
                    isGyroPass = true;
                    // mGyroPassDataView
                    // .setText((mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Pass Value: "
                    // : "通过值：")
                    // + "\n"
                    // + "X: "
                    // + event.values[0]
                    // + "\n"
                    // + "Y: "
                    // + event.values[1] + "\n" + "Z: " + event.values[2] + "\n");
                    showLayouArea(
                            (mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Pass Value: "
                                    : "通过值：")
                                    + "\n"
                                    + "X: "
                                    + event.values[0]
                                    + "\n"
                                    + "Y: "
                                    + event.values[1] + "\n" + "Z: " + event.values[2] + "\n", "",
                            gyroNumber, 3);
                }
            }
        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            String mValue = "";
            mValue = mValue + "X: " + event.values[0] + "\n";
            mValue = mValue + "Y: " + event.values[1] + "\n";
            mValue = mValue + "Z: " + event.values[2];
            // mAccelerometerSensorDataView.setText(mValue);
            showLayouArea(mValue, "", accNumber, 2);
            mGravity = event.values.clone();
            if (isAcceleFirst) {
                isAcceleFirst = false;
                acceleFirstXvalue = event.values[0];
                acceleFirstYvalue = event.values[1];
                acceleFirstZvalue = event.values[2];
            } else {
                float tempx = acceleFirstXvalue * mBase;
                float tempy = acceleFirstYvalue * mBase;
                float tempz = acceleFirstZvalue * mBase;
                if ((event.values[0] > acceleFirstXvalue + tempx || event.values[0] < acceleFirstXvalue
                        - tempx)
                        && (event.values[1] > acceleFirstYvalue + tempy || event.values[1] < acceleFirstYvalue
                        - tempy)
                        && (event.values[2] > acceleFirstZvalue + tempz || event.values[2] < acceleFirstZvalue
                        - tempz)) {
                    isAccelePass = true;
                    // mAccelerometerPassDataView
                    // .setText((mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Pass Value: "
                    // : "通过值：")
                    // + "\n"
                    // + "X: "
                    // + event.values[0]
                    // + "\n"
                    // + "Y: "
                    // + event.values[1] + "\n" + "Z: " + event.values[2] + "\n");
                    showLayouArea(
                            (mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Pass Value: "
                                    : "通过值：")
                                    + "\n"
                                    + "X: "
                                    + event.values[0]
                                    + "\n"
                                    + "Y: "
                                    + event.values[1] + "\n" + "Z: " + event.values[2] + "\n", "",
                            accNumber, 3);
                }
            }
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mGeomagnetic = event.values.clone();
            if (mGravity == null || mGeomagnetic == null) {
                return;
            }
            SensorManager.getRotationMatrix(RR, I, mGravity, mGeomagnetic);
            SensorManager.getOrientation(RR, mOrientation);
            mDegree = (float) Math.toDegrees(mOrientation[0]);
            showLayouArea(
                    (mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Degree: "
                            : "度数：") + mDegree + "", "", eCompassNumber, 2);
            if (iseCompassFirst) {
                iseCompassFirst = false;
                mFirstDegree = mDegree;
            } else {
                if (mDegree >= (mFirstDegree * mBase + mFirstDegree)
                        || mDegree <= (mFirstDegree - mFirstDegree * mBase)) {
                    iseCompass = true;
                    showLayouArea(
                            (mToolKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? "Pass Value: "
                                    : "通过值：")
                                    + mDegree + "", "", eCompassNumber, 3);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v == btnStart) {

            if (detectTestItem()) {
                mOriginalBrightness = getOriginalBrightness();
                showLayout();
                mShowSensorInfoLayout.setVisibility(View.VISIBLE);
                initTimer();
            } else {
                backToPQAA();
            }
        }
    }

    private void showLayout() {
        int number = 0;
        if (lightBox.isChecked()) {
            number++;
        }
        if (proximityBox.isChecked()) {
            number++;
        }
        if (accelerometerBox.isChecked()) {
            number++;
        }
        if (gyroBox.isChecked()) {
            number++;
        }
        if (ecompassBox.isChecked()) {
            number++;
        }
        if (number < 5) {
            mShowEcompassLayout.setVisibility(View.GONE);
            mBottomLeftLayout.setVisibility(View.GONE);
            mBottomRightLayout.setVisibility(View.GONE);
        }
    }


    private boolean detectTestItem() {
        if (!isTestLight && !isTestProximity && !isTestAcce && !isTestGyro && !isTestEcompass) {
            Toast.makeText(MainActivity.this, "Please select one sensoe test", Toast.LENGTH_SHORT)
                    .show();
            return false;
        }
        boolean result = true;
        int number = 0;
        if (isTestLight) {
            if (getLightSensor()) {
                number++;
                lightNumber = number;
                setLightSensorParameterDisplay(lightNumber, "LightSensor");

                AutoBrightness autoBrightness = new AutoBrightness(mHandler);
                new Thread(autoBrightness).start();
            } else {
                Toast.makeText(MainActivity.this, "Light sensor is unavailable", Toast.LENGTH_SHORT)
                        .show();
                result = false;
                mNGItemName += "LS:";
                isSensorAvailable = false;
            }
        } else {
            isLightPass = true;
        }
        if (isTestProximity) {
            if (getProximitySensor()) {
                number++;
                proximityNumber = number;
                setProximitySensorParameterDisplay(proximityNumber, "ProximitySensor");
            } else {
                Toast.makeText(MainActivity.this, "Proximity sensor is unavailable",
                        Toast.LENGTH_SHORT).show();
                result = false;
                mNGItemName += "PS:";
                isSensorAvailable = false;
            }
        } else {
            isProximityPass = true;
        }
        if (isTestAcce) {
            if (getAccelerometerSensor()) {
                number++;
                accNumber = number;
                setAccelerometerSensorParameterDisplay(accNumber, "AccelerometerSensor");
            } else {
                Toast.makeText(MainActivity.this, "Accelerometer sensor is unavailable",
                        Toast.LENGTH_SHORT).show();
                result = false;
                mNGItemName += "GS:";
                isSensorAvailable = false;
            }
        } else {
            isAccelePass = true;
        }
        if (isTestGyro) {
            if (getGyroSensor()) {
                number++;
                gyroNumber = number;
                setGyroSensorParameterDisplay(gyroNumber, "GyroSensor");
            } else {
                Toast.makeText(MainActivity.this, "Gyro sensor is unavailable", Toast.LENGTH_SHORT)
                        .show();
                result = false;
                mNGItemName += "GyS:";
                isSensorAvailable = false;
            }
        } else {
            isGyroPass = true;
        }
        if (isTestEcompass) {
            if (isTestAcce) {
                if (getMageneticFieldSensor()) {
                    number++;
                    eCompassNumber = number;
                    setMageneticFieldSensorParameterDisplay(eCompassNumber, "eCompass");
                } else {
                    Toast.makeText(MainActivity.this, "ECompass is unavailable!",
                            Toast.LENGTH_SHORT).show();
                    result = false;
                    mNGItemName += "EC:";
                    isSensorAvailable = false;
                }
            } else {
                if (getAccelerometerSensor() && getMageneticFieldSensor()) {
                    number++;
                    eCompassNumber = number;
                    setMageneticFieldSensorParameterDisplay(eCompassNumber, "eCompass");
                } else {
                    Toast.makeText(MainActivity.this, "ECompass is unavailable!",
                            Toast.LENGTH_SHORT).show();
                    result = false;
                    mNGItemName += "EC:";
                    isSensorAvailable = false;
                }
            }
        } else {
            iseCompass = true;
        }
        return result;
    }

    private void initTimer() {
        mTimer = new Timer();
        mTask = new TimerTask() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                times++;
                if (times >= TIMEOUT) {
                    handler.sendEmptyMessage(1);
                } else {
                    handler.sendEmptyMessage(0);
                }
            }
        };
        mTimer.schedule(mTask, 1000, 1000);
    }

    private void cancelTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTask != null) {
            mTask.cancel();
            mTask = null;
        }
    }

    private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case 0:
                    mRemainTimeView.setText(mToolKit.getStringResource(R.string.remain_time)
                            + mToolKit.formatCountDownTime(TIMEOUT, times));
                    detectPass();
                    break;
                case 1:
                    backToPQAA();
                    break;
                case 2:
                    btnStart.performClick();
                    break;
            }
            return false;
        }
    });

    private void backToPQAA() {
        cancelTimer();
        Toast.makeText(MainActivity.this, "Test result: " + isPass, Toast.LENGTH_SHORT).show();
        // mToolKit.returnWithResult(isPass);

        Intent intent = new Intent();
        intent.putExtra(WisCommonConst.EXTRA_PASS, isPass);
        if (!isPass) {
            if (!isSensorAvailable) {
                intent.putExtra("ng", mNGItemName);
            } else {
                String content = "";
                if (!isLightPass) {
                    content += "LS:";
                }
                if (!isProximityPass) {
                    content += "PS:";
                }
                if (!isAccelePass) {
                    content += "GS:";
                }
                if (!isGyroPass) {
                    content += "GyS:";
                }
                if (!iseCompass) {
                    content += "EC:";
                }
                intent.putExtra("ng", content);
            }
        }
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void detectPass() {
        if (isTestLight) {
            isLightPass = isLightSensorPass && isBlackPass && isWhitePass;
            Log.i("Bob", "light sensor paa");
        }
        if (isLightPass && isProximityPass && isAccelePass && isGyroPass && iseCompass) {
            isPass = true;
            handler.sendEmptyMessage(1);
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        mSensorManager.unregisterListener(MainActivity.this);

        setBrightness(mOriginalBrightness);
    }

    private final static int MESSAGE_BRIGHTNESS_BLACK = 0;
    private final static int MESSAGE_BRIGHTNESS_WHITE = 1;
    private final static int MESSAGE_FINISH = 2;
    private int mBrightnessInterval = 1000;
    private boolean isBlackPass, isWhitePass;
    private int mOriginalBrightness = 120, mBrightnessNumber = 0;

    private class AutoBrightness implements Runnable {
        private Handler handler;

        public AutoBrightness(Handler handler) {
            this.handler = handler;
            // TODO Auto-generated constructor stub
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            // Add brightness test.
            for (int i = 0; i < 2; i++) {
                if (mBrightnessNumber == 0) {
                    mBrightnessNumber++;
                    handler.obtainMessage(MESSAGE_BRIGHTNESS_BLACK).sendToTarget();
                    sleepTime(mBrightnessInterval);
                } else if (mBrightnessNumber == 1) {
                    mBrightnessNumber++;
                    handler.obtainMessage(MESSAGE_BRIGHTNESS_WHITE).sendToTarget();
                    sleepTime(mBrightnessInterval);
                }
            }

            handler.sendEmptyMessage(MESSAGE_FINISH);
        }

        private void sleepTime(int time) {
            try {
                Thread.sleep(time);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_BRIGHTNESS_BLACK:
                    setBrightness(10);
                    if (getOriginalBrightness() == 10) {
                        isBlackPass = true;
                    }
                    break;
                case MESSAGE_BRIGHTNESS_WHITE:
                    setBrightness(255);
                    if (getOriginalBrightness() == 255) {
                        isWhitePass = true;
                    }
                    break;
                case MESSAGE_FINISH:

                    break;
            }
            return false;
        }
    });

    private void setBrightness(int iValue) {
        Settings.System.putString(this.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,
                String.valueOf(iValue));
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        Float tmpFloat = (float) iValue / 255;
        if (tmpFloat < 0.1f) {
            tmpFloat = 0.1f;
        }
        lp.screenBrightness = tmpFloat;
        getWindow().setAttributes(lp);
    }

    private int getOriginalBrightness() {
        // TODO Auto-generated method stub
        int tempBrightness = 0;
        try {
            tempBrightness = Settings.System.getInt(this.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS);
            Log.i("W", ": " + tempBrightness);
        } catch (Settings.SettingNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return tempBrightness;
    }


}
