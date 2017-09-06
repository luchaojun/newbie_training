package com.wistron.generic.ram;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wistron.generic.pqaa.R;
import com.wistron.pqaa_common.jar.global.WisCommonConst;
import com.wistron.pqaa_common.jar.global.WisParseValue;
import com.wistron.pqaa_common.jar.global.WisStorageRW;
import com.wistron.pqaa_common.jar.global.WisStorageRW.OnMemoryTestStateChangedListener;
import com.wistron.pqaa_common.jar.global.WisToolKit;

public class RAM extends Activity implements android.view.View.OnClickListener {
    // ----result data----
    private boolean finish = true;

    private TextView ram_value;
    private Button mStartButton, mExitButton;
    private ProgressBar mProgressBar;
    private int MemorySize = 10 * 1024;

    // common library
    private WisToolKit mWisToolKit;
    private WisStorageRW mWisStorageRW;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.ram);

        mWisToolKit = new WisToolKit(this);

        setByLanguage();
        getTestArguments();
        mStartButton.performClick();
    }

    private void getTestArguments() {
        // TODO Auto-generated method stub
        String mTestStyle = mWisToolKit.getCurrentTestType();
        if (mTestStyle != null) {
            if (mTestStyle.equals(WisCommonConst.TESTSTYLE_SAVED_CONFIG)) {
                WisParseValue mParse = new WisParseValue(this, mWisToolKit.getCurrentItem(), mWisToolKit.getCurrentDatabaseAuthorities());
            }
        }
    }


    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            if (msg.what == 1) {
            } else if (msg.what == 2) {
                BackToPQAA();
            }
        }

    };


    private void setByLanguage() {
        ((TextView) findViewById(R.id.item_title)).setText(mWisToolKit.getStringResource(R.string.ram_title));
        ram_value = (TextView) findViewById(R.id.ram_info_value);
        ram_value.setMovementMethod(ScrollingMovementMethod.getInstance());
        ((TextView) findViewById(R.id.ram_write_read)).setText(mWisToolKit.getStringResource(R.string.ram_sdcard_test));
        ((TextView) findViewById(R.id.ram_progressbar_status)).setText(mWisToolKit.getStringResource(R.string.sdcard_status));

        mProgressBar = (ProgressBar) findViewById(R.id.ram_progressbar);
        mStartButton = (Button) findViewById(R.id.start);
        mStartButton.setText(mWisToolKit.getStringResource(R.string.button_start));
        mExitButton = (Button) findViewById(R.id.exit);
        mExitButton.setText(mWisToolKit.getStringResource(R.string.button_exit));
        mStartButton.setOnClickListener(this);
        mExitButton.setOnClickListener(this);
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
        if (v == mStartButton) {
            mWisStorageRW = new WisStorageRW(RAM.this, WisStorageRW.FLAG_MEMORY);
            mWisStorageRW.setOnMemoryTestStateChangedListener(memoryListener);
            mWisStorageRW.setMemorySize(MemorySize);
            mWisStorageRW.start();
        } else if (v == mExitButton) {
            BackToPQAA();
        }
    }

    private OnMemoryTestStateChangedListener memoryListener = new OnMemoryTestStateChangedListener() {

        @Override
        public void onStateIsTestStart(int flag) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStateIsTestProgressChanged(int flag, int progress) {
            // TODO Auto-generated method stub
            mProgressBar.setProgress(progress);
            if (progress == 100) {
                mWisStorageRW.stop();
                finish = true;
            }
        }

        @Override
        public void onStateIsTestDone(int flag) {
            // TODO Auto-generated method stub
            mHandler.obtainMessage(2).sendToTarget();
        }

        @Override
        public void onStateIsTestAbort(int flag, String exception) {
            // TODO Auto-generated method stub
            System.out.println(exception);
        }

        @Override
        public void onStateIsResultFail(int flag) {
            // TODO Auto-generated method stub

        }
    };

    private void BackToPQAA() {
        mWisToolKit.returnWithResult(finish);
    }

}
