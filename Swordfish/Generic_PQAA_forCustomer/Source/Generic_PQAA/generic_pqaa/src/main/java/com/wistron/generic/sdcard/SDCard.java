package com.wistron.generic.sdcard;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wistron.generic.pqaa.R;
import com.wistron.pqaa_common.jar.global.WisCommonConst;
import com.wistron.pqaa_common.jar.global.WisParseValue;
import com.wistron.pqaa_common.jar.global.WisStorageRW;
import com.wistron.pqaa_common.jar.global.WisStorageRW.OnSDCardTestStateChangedListener;
import com.wistron.pqaa_common.jar.global.WisToolKit;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class SDCard extends Activity implements OnClickListener, OnSDCardTestStateChangedListener {
    private static final String PREFERRENCES_NAME = "sdcard";
    private static final String KEY_PATH = "path";

    //	private String mSDPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    private String mSDPath = "/mnt/sdcard/";
    private String mProtectFilePath = mSDPath + File.separator + "protect.txt";
    private final int SDCARD_WR_TEST = 1;
    private final int SDCARD_PROTECT_TEST = 2;
    // ----------------------result---------------
    private TextView mResultContent;
    private Button mResultButton;
    // --------------------------------------
    private static final int MESSAGE_UPDATE_TIME = 1;
    private static final int MESSAGE_BACK_RESULT = 2;

    private CheckBox cb_wr;
    private CheckBox cb_protect;
    private Button btn_start, btn_exit;
    private EditText mCardPath;
    private ImageView mTestResult;
    private LinearLayout mProgressLayout;
    private ProgressBar mTestProgress;
    private TextView mTestStatus;

    private boolean isPass = false;
    private boolean mComponentMode = true;
    private boolean isProtectTest = false;
    private boolean isRegisterBroadcast = false;
    private ProgressDialog mPromptDialog;
    private int mTimes = 0;
    private Timer mTimer = new Timer();
    private TimerTask mTimeOutTask;
    private int TIMEOUT = 20; // seconds

    // common tool kit
    private WisToolKit mToolKit;

    private WisStorageRW mSDCardRWHandler;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.sdcard);

        mToolKit = new WisToolKit(this);

        getViews();
        getTestArguments();
        setBroadcast();
        createAlertDialog();
        getExtSDPath();
        setViewByLanguage();
        btn_start.setVisibility(View.VISIBLE);
        btn_start.performClick();
    }

    private void setViewByLanguage() {
        // TODO Auto-generated method stub
        TextView mItemTitle = (TextView) findViewById(R.id.item_title);
        mItemTitle.setText(mToolKit.getStringResource(R.string.sdcard_test_title));
        cb_wr.setText(mToolKit.getStringResource(R.string.sdcard_test));
        ((TextView) findViewById(R.id.sdcard_path_title)).setText(mToolKit.getStringResource(R.string.sdcard_path));
        ((TextView) findViewById(R.id.sdcard_status_title)).setText(mToolKit.getStringResource(R.string.sdcard_status));
        mCardPath.setText(mSDPath);
        cb_protect.setText(mToolKit.getStringResource(R.string.sdcard_protect));
    }

    private void getTestArguments() {
        // TODO Auto-generated method stub
        String mTestStyle = mToolKit.getCurrentTestType();
        if (mTestStyle != null) {
            if (mTestStyle.equals(WisCommonConst.TESTSTYLE_SAVED_CONFIG)) {
                mComponentMode = false;
                WisParseValue mParse = new WisParseValue(this, mToolKit.getCurrentItem(), mToolKit.getCurrentDatabaseAuthorities());
                String mExtSDPath = mParse.getArg1();
                int mTestItem = Integer.parseInt(mParse.getArg2());
                if ((mTestItem & SDCARD_WR_TEST) != 0) {
                    cb_wr.setChecked(true);
                } else {
                    cb_wr.setChecked(false);
                }
                if ((mTestItem & SDCARD_PROTECT_TEST) != 0) {
                    cb_protect.setChecked(true);
                } else {
                    cb_protect.setChecked(false);
                }
                setExtSDPath(mExtSDPath);
            }
        }
    }

    private void setExtSDPath(String path) {
        // TODO Auto-generated method stub
        Editor mEditor = getSharedPreferences(PREFERRENCES_NAME, MODE_PRIVATE).edit();
        mEditor.putString(KEY_PATH, path);
        mEditor.apply();
    }

    private void getExtSDPath() {
        SharedPreferences mPreferences = getSharedPreferences(PREFERRENCES_NAME, MODE_PRIVATE);
        mSDPath = mPreferences.getString(KEY_PATH, mSDPath);
    }

    private void setBroadcast() {
        // TODO Auto-generated method stub
        IntentFilter mFilter = new IntentFilter();
        mFilter = new IntentFilter();
        mFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        mFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        mFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        mFilter.addDataScheme("file"); // need to match with data's scheme,
        registerReceiver(protectReciver, mFilter);
        isRegisterBroadcast = true;
    }

    private void createAlertDialog() {
        mPromptDialog = new ProgressDialog(this);
        mPromptDialog.setTitle(mToolKit.getStringResource(R.string.sdcard_dialog_title));
        mPromptDialog.setMessage(mToolKit.getStringResource(R.string.sdcard_dialog_msg_plug) + TIMEOUT);
        mPromptDialog.setCancelable(false);
        mPromptDialog.setCanceledOnTouchOutside(false);
    }

    private BroadcastReceiver protectReciver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub
            if (isProtectTest) {
                if (arg1.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {
                    isProtectTest = false;
                    cancelTimer();
                    if (mPromptDialog.isShowing()) {
                        mPromptDialog.dismiss();
                    }
                    testProtectMode();
                }
            }
        }

    };

    private void disableViews() {
        // TODO Auto-generated method stub
        btn_start.setEnabled(false);
        mTestResult.setVisibility(ImageView.INVISIBLE);
        mProgressLayout.setVisibility(View.VISIBLE);
        mTestProgress.setProgress(0);
        mTestStatus.setText(mToolKit.getStringResource(R.string.sdcard_testing));

        cb_wr.setEnabled(false);
        mCardPath.setEnabled(false);
    }

    private void getViews() {
        // TODO Auto-generated method stub
        cb_wr = (CheckBox) findViewById(R.id.sdcard_test);
        cb_protect = (CheckBox) findViewById(R.id.sdcard_protect);
        btn_start = (Button) findViewById(R.id.sdcard_start);
        btn_exit = (Button) findViewById(R.id.sdcard_exit);
        mCardPath = (EditText) findViewById(R.id.sdcard_path);
        mTestResult = (ImageView) findViewById(R.id.sdcard_result);
        mProgressLayout = (LinearLayout) findViewById(R.id.sdcard_progress_layout);
        mTestProgress = (ProgressBar) findViewById(R.id.sdcard_progress);
        mTestStatus = (TextView) findViewById(R.id.sdcard_status);

        btn_start.setOnClickListener(this);
        btn_exit.setOnClickListener(this);

        mCardPath.setInputType(InputType.TYPE_NULL);
        mCardPath.setEnabled(false);
    }

    protected void testProtectMode() {
        // TODO Auto-generated method stub
        File mProtectTestFile = new File(mProtectFilePath);
        boolean mDeleteStatus = false;
        if (mProtectTestFile.exists()) {
            mDeleteStatus = mProtectTestFile.delete();
            if (mDeleteStatus) {
                isPass = false;
            }
        } else {
            try {
                mProtectTestFile.createNewFile();
                isPass = false;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        mProtectTestFile.delete();
        btn_exit.performClick();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void showAlert(CharSequence string_Message) {
        new AlertDialog.Builder(this).setTitle(mToolKit.getStringResource(R.string.sdcard_dialog_title))
                .setPositiveButton(mToolKit.getStringResource(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // btn_start.setEnabled(true);
                    }
                }).setIcon(getResources().getDrawable(R.drawable.alert_dialog_icon)).setMessage(string_Message).setCancelable(false).show();
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v == btn_start) {
            if (cb_wr.isChecked()) {
                Log.i("SDCard", "path:" + mSDPath);
                isPass = true;
                mSDCardRWHandler = new WisStorageRW(this, WisStorageRW.FLAG_EXTERNAL_SDCARD, mSDPath);
                mSDCardRWHandler.setSDCardFileSize(100);
                mSDCardRWHandler.setSDCardFileNumber(20);
                mSDCardRWHandler.setRepeatTest(false);
                mSDCardRWHandler.setOnSDCardTestStateChangedListener(this);
                mSDCardRWHandler.start();
            } else if (cb_protect.isChecked()) {
                isPass = true;
                startProtectTest();
            } else {
                btn_exit.performClick();
            }
            cb_protect.setEnabled(false);
            cb_wr.setEnabled(false);
            mCardPath.setEnabled(false);
        } else if (v == btn_exit) {
            mSDCardRWHandler.stop();
            if (mComponentMode) {
                displayResult();
            } else {
                backToPQAA();
            }
        } else if (v == mResultButton) {
            backToPQAA();
        }
    }

    private void startProtectTest() {
        // TODO Auto-generated method stub
        isProtectTest = true;
        mPromptDialog.show();
        initializeTimerTask();
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            if (msg.what == MESSAGE_UPDATE_TIME) {
                mPromptDialog.setMessage(mToolKit.getStringResource(R.string.sdcard_dialog_msg_plug) + (TIMEOUT - mTimes));
            } else if (msg.what == MESSAGE_BACK_RESULT) {
                if (mPromptDialog.isShowing()) {
                    mPromptDialog.dismiss();
                }
                btn_exit.performClick();
            }
        }

    };

    private void initializeTimerTask() {
        // TODO Auto-generated method stub
        mTimeOutTask = new TimerTask() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                mTimes++;
                mHandler.sendEmptyMessage(MESSAGE_UPDATE_TIME);
                if (mTimes >= TIMEOUT) {
                    isPass = false;
                    cancelTimer();
                    mHandler.sendEmptyMessage(MESSAGE_BACK_RESULT);
                }
            }
        };
        mTimer.schedule(mTimeOutTask, 1000, 1000);
    }

    private void cancelTimer() {
        // TODO Auto-generated method stub
        if (mTimeOutTask != null) {
            mTimeOutTask.cancel();
            mTimeOutTask = null;
        }
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        mTimeOutTask = null;
        mTimer = null;
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

    private void backToPQAA() {
        if (isRegisterBroadcast) {
            unregisterReceiver(protectReciver);
            isRegisterBroadcast = false;
        }
        mToolKit.returnWithResult(isPass);
    }

    // SDcard test status listener

    @Override
    public void onStateIsDeleteProgressChanged(int flag, int progress) {
        // TODO Auto-generated method stub
        Log.i("SDCard", "delete start...");
        mTestProgress.setProgress(progress);
    }

    @Override
    public void onStateIsDeleteStart(int flag) {
        // TODO Auto-generated method stub
        Log.i("SDCard", "test delete...");
        mTestStatus.setText(mToolKit.getStringResource(R.string.sdcard_deleting));
    }

    @Override
    public void onStateIsResultFail(int flag) {
        // TODO Auto-generated method stub
        Log.i("SDCard", "test fail...");
        isPass = false;
    }

    @Override
    public void onStateIsTestAbort(int flag, String exception) {
        // TODO Auto-generated method stub
        Log.i("SDCard", "test abort...");
        isPass = false;
    }

    @Override
    public void onStateIsTestDone(int flag) {
        // TODO Auto-generated method stub
        Log.i("SDCard", "test done...");
        mTestResult.setVisibility(ImageView.VISIBLE);
        mProgressLayout.setVisibility(View.GONE);
        if (isPass) {
            mTestResult.setImageResource(R.drawable.pass);
        } else {
            mTestResult.setImageResource(R.drawable.failed);
        }

        if (cb_protect.isChecked() && isPass) {
            startProtectTest();
        } else {
            btn_exit.performClick();
        }
    }

    @Override
    public void onStateIsTestProgressChanged(int flag, int progress) {
        // TODO Auto-generated method stub
        Log.i("SDCard", "test progress...");
        mTestProgress.setProgress(progress);
    }

    @Override
    public void onStateIsTestStart(int flag) {
        // TODO Auto-generated method stub
        Log.i("SDCard", "test start...");
        disableViews();
        mTestStatus.setText(mToolKit.getStringResource(R.string.sdcard_testing));
    }
}