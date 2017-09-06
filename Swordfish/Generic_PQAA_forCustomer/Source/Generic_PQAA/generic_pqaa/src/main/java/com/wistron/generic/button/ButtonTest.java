package com.wistron.generic.button;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.wistron.generic.pqaa.R;
import com.wistron.pqaa_common.jar.global.WisCommonConst;
import com.wistron.pqaa_common.jar.global.WisParseValue;
import com.wistron.pqaa_common.jar.global.WisShellCommandHelper;
import com.wistron.pqaa_common.jar.global.WisToolKit;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ButtonTest extends Activity {
    // ----------------------result---------------
    private TextView mResultContent;
    private Button mResultButton;
    // --------------------------------------
    private final int MSG_LOCKR = 0;
    private final int MSG_LOCKL = 1;
    private final int MSG_PASS = 2;
    private final int MSG_FAIL = 3;
    private final int MSG_REFRESH = 4;
    private TextView mRemainTime, mTotalTime;
    private ImageButton mPowerButton, mVolumeDownButton, mVolumeUpButton;
    private boolean isVolumeUp = false, isVolumeDown = false, isPowerPass = true;
    private Button mPassButton, mFailButton;
    private KeyguardLock mKeyguardLock;

    private boolean isPass;
    private boolean mComponentMode = true;
    ;
    private boolean isRegisterBroadcast = false;

    private WakeLock mWakeLock;

    private int mOriginalKey = -1;
    private int mTimes = 0;
    private Timer mTimer = new Timer();
    private TimerTask mTimeOutTask, mRotationTask;
    private int TIMEOUT = 30; // seconds

    // common tool kit
    private WisToolKit mToolKit;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.button);

        mToolKit = new WisToolKit(this);

        findView();
        getTestArguments();
        setViewByLanguage();
        initializeTimerTask();
        shieldPowerAndEndCallKey();

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        registerReceiver(screenOnReceiver, filter);
        isRegisterBroadcast = true;
    }

    private void setViewByLanguage() {
        // TODO Auto-generated method stub
        TextView mItemTitle = (TextView) findViewById(R.id.item_title);
        mItemTitle.setText(mToolKit.getStringResource(R.string.keypad_test_title));
        mTotalTime.setText(mToolKit.getStringResource(R.string.total_time)
                + mToolKit.formatCountDownTime(TIMEOUT, mTimes));
        mRemainTime.setText(mToolKit.getStringResource(R.string.remain_time)
                + mToolKit.formatCountDownTime(TIMEOUT, mTimes));
        mPassButton.setText(mToolKit.getStringResource(R.string.button_pass));
        mFailButton.setText(mToolKit.getStringResource(R.string.button_fail));

        ((TextView) findViewById(R.id.text_power)).setText(mToolKit
                .getStringResource(R.string.keypad_title_power));
        ((TextView) findViewById(R.id.text_volumedown)).setText(mToolKit
                .getStringResource(R.string.keypad_title_volumedown));
        ((TextView) findViewById(R.id.text_volumeup)).setText(mToolKit
                .getStringResource(R.string.keypad_title_volumeup));
    }

    private void getTestArguments() {
        // TODO Auto-generated method stub
        String mTestStyle = mToolKit.getCurrentTestType();
        if (mTestStyle != null) {
            if (mTestStyle.equals(WisCommonConst.TESTSTYLE_SAVED_CONFIG)) {
                mComponentMode = false;
                WisParseValue mParse = new WisParseValue(this, mToolKit.getCurrentItem(),
                        mToolKit.getCurrentDatabaseAuthorities());
                int mTestTime = Integer.parseInt(mParse.getArg1());
                if (mTestTime > 0) {
                    TIMEOUT = mTestTime;
                }
            }
        }
    }

    private void initializeTimerTask() {
        // TODO Auto-generated method stub
        mTimeOutTask = new TimerTask() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                mTimes++;
                handler.sendEmptyMessage(MSG_REFRESH);
                detectResult();
            }
        };
        mRotationTask = new TimerTask() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                detectRotationKey();
            }
        };
        mTimer.schedule(mTimeOutTask, 1000, 1000);
//		mTimer.schedule(mRotationTask, 300, 300);
    }

    protected void detectResult() {
        // TODO Auto-generated method stub
        if (isPowerPass && isVolumeDown && isVolumeUp) {
            cancelTimer();
            handler.sendEmptyMessage(MSG_PASS);
        } else {
            if (mTimes >= TIMEOUT) {
                cancelTimer();
                handler.sendEmptyMessage(MSG_FAIL);
            }
        }
    }

    private void cancelTimer() {
        // TODO Auto-generated method stub
        if (mTimeOutTask != null) {
            mTimeOutTask.cancel();
        }
        if (mRotationTask != null) {
            mRotationTask.cancel();
        }
        if (mTimer != null) {
            mTimer.cancel();
        }
        mTimeOutTask = null;
        mRotationTask = null;
        mTimer = null;
    }

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_PASS:
                    mPassButton.performClick();
                    break;
                case MSG_FAIL:
                    mFailButton.performClick();
                    break;
                case MSG_REFRESH:
                    Log.i("Time", "----------" + mToolKit.formatCountDownTime(TIMEOUT, mTimes));
                    mRemainTime.setText(mToolKit.getStringResource(R.string.remain_time)
                            + mToolKit.formatCountDownTime(TIMEOUT, mTimes));
                    break;
                default:
                    break;
            }
        }

    };

    protected void detectRotationKey() {
        // TODO Auto-generated method stub
        int mKeyStatus = -1;
        WisShellCommandHelper mShellCommandHelper = new WisShellCommandHelper();
        ArrayList<String> result = mShellCommandHelper.exec("cat /sys/class/switch/rotation-lock/state");
        if (result != null && result.size() > 0) {
            String state = result.get(0);
            if (state != null) {
                try {
                    mKeyStatus = Integer.parseInt(state);
                } catch (NumberFormatException e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }
            }
        }
        System.out.println("----------***->" + mKeyStatus);
        if (mOriginalKey == -1) {
            mOriginalKey = mKeyStatus;
        }
    }

    private void findView() {
        // TODO Auto-generated method stub
        mRemainTime = (TextView) findViewById(R.id.remaintime);
        mTotalTime = (TextView) findViewById(R.id.totaltime);
        mPowerButton = (ImageButton) findViewById(R.id.button_power);
        mVolumeDownButton = (ImageButton) findViewById(R.id.button_volumedown);
        mVolumeUpButton = (ImageButton) findViewById(R.id.button_volumeup);
        mPassButton = (Button) findViewById(R.id.button_pass);
        mFailButton = (Button) findViewById(R.id.button_fail);

        mPowerButton.setClickable(false);
        mVolumeDownButton.setClickable(false);
        mVolumeUpButton.setClickable(false);

        mPassButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                isPass = true;
                if (mComponentMode) {
                    // displayResult();
                    Toast.makeText(ButtonTest.this, "Pass", Toast.LENGTH_SHORT).show();
                    mToolKit.returnWithResult(isPass);
                } else {
                    mToolKit.returnWithResult(isPass);
                }
            }
        });
        mFailButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                isPass = false;
                if (mComponentMode) {
                    // displayResult();
                    Toast.makeText(ButtonTest.this, "Fail", Toast.LENGTH_SHORT).show();
                    mToolKit.returnWithResult(isPass);
                } else {
                    mToolKit.returnWithResult(isPass);
                }
            }
        });

//		mVolumeDownButton.setVisibility(View.INVISIBLE);
//		mVolumeUpButton.setVisibility(View.INVISIBLE);
//		((TextView) findViewById(R.id.text_volumedown)).setVisibility(View.INVISIBLE);
//		((TextView) findViewById(R.id.text_volumeup)).setVisibility(View.INVISIBLE);
    }

    private void shieldPowerAndEndCallKey() {
        // TODO Auto-generated method stub
        KeyguardManager mManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        mKeyguardLock = mManager.newKeyguardLock("wistron");
        mKeyguardLock.disableKeyguard();

        PowerManager mPManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = mPManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "wistron");
        mWakeLock.acquire();
    }

    @Override
    public void onAttachedToWindow() {
        // TODO Auto-generated method stub
        super.onAttachedToWindow();
//		getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD); // �̽�Home��
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                isVolumeUp = true;
                mVolumeUpButton.setImageResource(R.drawable.button_ok);
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                isVolumeDown = true;
                mVolumeDownButton.setImageResource(R.drawable.button_ok);
                break;
            case KeyEvent.KEYCODE_POWER:
                isPowerPass = true;
                mPowerButton.setImageResource(R.drawable.button_ok);
                break;
            default:
                break;
        }
        Log.i("------------->", keyCode + "");
        return true;
    }

    private void restoreDefaultKey() {
        // TODO Auto-generated method stub
        getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION);
        mKeyguardLock.reenableKeyguard();
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    private void displayResult() {
        // TODO Auto-generated method stub
        restoreDefaultKey();
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
                mToolKit.returnWithResult(isPass);
            }
        });
    }

    private BroadcastReceiver screenOnReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            isPowerPass = true;
            mPowerButton.setImageResource(R.drawable.button_ok);
        }
    };

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (isRegisterBroadcast) {
            unregisterReceiver(screenOnReceiver);
            isRegisterBroadcast = false;
        }
    }
}