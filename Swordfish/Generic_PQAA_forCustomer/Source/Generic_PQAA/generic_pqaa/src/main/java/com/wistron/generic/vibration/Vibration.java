package com.wistron.generic.vibration;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wistron.generic.pqaa.R;
import com.wistron.pqaa_common.jar.global.WisAlertDialog;
import com.wistron.pqaa_common.jar.global.WisCommonConst;
import com.wistron.pqaa_common.jar.global.WisParseValue;
import com.wistron.pqaa_common.jar.global.WisToolKit;

import java.util.Random;

public class Vibration extends Activity implements OnClickListener {
    private final int TIMES_FIRST = 1;
    private final int TIMES_SECOND = 2;
    private final int TIMES_THIRD = 3;
    private final int TIMES_FORTH = 4;
    private final int TIMES_FIFTH = 5;
    // ----------------------result---------------
    private TextView mResultContent;
    private Button mResultButton;
    // --------------------------------------
    private TextView mVibrationState;
    private Button mStartButton;
    private Button mPassExitButton, mFailExitButton;
    private Vibrator mVibrator;
    private boolean isPass = false;

    private boolean mComponentMode = true;
    // //////////////////////////////////////////////
    private WisAlertDialog mPromptDialog;
    private AlertDialog mSelectDialog;
    private Button mFirstTimes, mSecondTimes, mThirdTimes, mFourthTimes, mFifthTimes;
    private int mCurIndex = 0, mSelectIndex = 0;
    private int mVibrateSeconds = 2;

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
        setContentView(R.layout.vibration);

        mToolKit = new WisToolKit(this);

        findView();
        getTestArguments();
        initialDialog();
        setViewByLanguage();
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        mPromptDialog.showDialog();
    }

    private void setViewByLanguage() {
        // TODO Auto-generated method stub
        TextView mItemTitle = (TextView) findViewById(R.id.item_title);
        mItemTitle.setText(mToolKit.getStringResource(R.string.vibration_test_title));
        mFirstTimes.setText(mToolKit.getStringResource(R.string.vibration_one));
        mSecondTimes.setText(mToolKit.getStringResource(R.string.vibration_two));
        mThirdTimes.setText(mToolKit.getStringResource(R.string.vibration_three));
        mFourthTimes.setText(mToolKit.getStringResource(R.string.vibration_four));
        mFifthTimes.setText(mToolKit.getStringResource(R.string.vibration_five));
        mStartButton.setText(mToolKit.getStringResource(R.string.button_start));
        mPassExitButton.setText(mToolKit.getStringResource(R.string.button_pass));
        mFailExitButton.setText(mToolKit.getStringResource(R.string.button_fail));
        mVibrationState.setText(mToolKit.getStringResource(R.string.vibration_warning_msg));
    }

    private void initialDialog() {
        // TODO Auto-generated method stub
        mPromptDialog = new WisAlertDialog(this).setTitle(mToolKit.getStringResource(R.string.vibration_warning_title))
                .setMessage(mToolKit.getStringResource(R.string.vibration_warning_msg), getResources().getDimension(R.dimen.alert_dialog_message_size))
                .setPositiveButton(mToolKit.getStringResource(R.string.vibration_warning_query), getResources().getDimension(R.dimen.alert_dialog_button_size),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                mStartButton.performClick();
                            }
                        }).createDialog();

        LayoutInflater mInflater = LayoutInflater.from(this);
        View mSelect = mInflater.inflate(R.layout.vibration_select, null);
        mSelect.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        findButtonView(mSelect);
        mSelectDialog = new AlertDialog.Builder(this).setTitle(mToolKit.getStringResource(R.string.vibration_select)).setView(mSelect).create();
        mSelectDialog.setCancelable(false);
        mSelectDialog.setCanceledOnTouchOutside(false);
    }

    private void findButtonView(View mSelect) {
        // TODO Auto-generated method stub
        mFirstTimes = (Button) mSelect.findViewById(R.id.track_one);
        mSecondTimes = (Button) mSelect.findViewById(R.id.track_two);
        mThirdTimes = (Button) mSelect.findViewById(R.id.track_three);
        mFourthTimes = (Button) mSelect.findViewById(R.id.track_four);
        mFifthTimes = (Button) mSelect.findViewById(R.id.track_five);

        mFirstTimes.setOnClickListener(this);
        mSecondTimes.setOnClickListener(this);
        mThirdTimes.setOnClickListener(this);
        mFourthTimes.setOnClickListener(this);
        mFifthTimes.setOnClickListener(this);
        mFourthTimes.setVisibility(View.GONE);
        mFifthTimes.setVisibility(View.GONE);
    }

    private void findView() {
        // TODO Auto-generated method stub
        mVibrationState = (TextView) findViewById(R.id.vibration_prompt);
        mStartButton = (Button) findViewById(R.id.vibration_start);
        mPassExitButton = (Button) findViewById(R.id.vibration_pass);
        mFailExitButton = (Button) findViewById(R.id.vibration_fail);
        mStartButton.setOnClickListener(this);
        mPassExitButton.setOnClickListener(this);
        mFailExitButton.setOnClickListener(this);
    }

    private void getTestArguments() {
        // TODO Auto-generated method stub
        String mTestStyle = mToolKit.getCurrentTestType();
        if (mTestStyle != null) {
            if (mTestStyle.equals(WisCommonConst.TESTSTYLE_SAVED_CONFIG)) {
                mComponentMode = false;
                WisParseValue mParse = new WisParseValue(this, mToolKit.getCurrentItem(), mToolKit.getCurrentDatabaseAuthorities());
                int frequency = Integer.parseInt(mParse.getArg1());
                if (frequency > 0) {
                    mVibrateSeconds = frequency;
                }
            }
        }
    }

    private void setRandom() {
        Random mRandom = new Random(System.currentTimeMillis());
        mCurIndex = mRandom.nextInt(3) + 1;
    }

    private final Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            mVibrationState.setText(mToolKit.getStringResource(R.string.vibration_end));
            mVibrator.cancel();
            mSelectDialog.show();
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
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (mSelectDialog.isShowing()) {
            mSelectDialog.dismiss();
        }
        if (v == mStartButton) {
            mStartButton.setEnabled(false);
            // if (mComponentMode) {
            // long[] battern = { 500, 1500 };
            // mVibrator.vibrate(battern, 0);
            // } else {
            // mVibrator.vibrate(5000);
            // new Thread(waitExit).start();
            // }
            Log.i("Tag", "Vibrator");
            mPromptDialog.dismissDialog();
            mVibrationState.setText(mToolKit.getStringResource(R.string.vibration_vibration));
            new Thread(startVibrate).start();
        } else if (v == mPassExitButton) {
            if (mComponentMode) {
                displayResult();
            } else {
                backToPQAA();
            }
        } else if (v == mFailExitButton) {
            if (mComponentMode) {
                displayResult();
            } else {
                backToPQAA();
            }
        } else if (v == mResultButton) {
            backToPQAA();
        } else if (v == mFirstTimes) {
            mSelectIndex = TIMES_FIRST;
        } else if (v == mSecondTimes) {
            mSelectIndex = TIMES_SECOND;
        } else if (v == mThirdTimes) {
            mSelectIndex = TIMES_THIRD;
        } else if (v == mFourthTimes) {
            mSelectIndex = TIMES_FORTH;
        } else if (v == mFifthTimes) {
            mSelectIndex = TIMES_FIFTH;
        }
        if (v == mFirstTimes || v == mSecondTimes || v == mThirdTimes || v == mFourthTimes || v == mFifthTimes) {
            if (mCurIndex != mSelectIndex) {
                isPass = false;
                mFailExitButton.performClick();
            } else {
                isPass = true;
                mPassExitButton.performClick();
            }
        }
    }

    private Runnable startVibrate = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            int mTempIndex = 0;
            setRandom();
            for (; mTempIndex < mCurIndex; mTempIndex++) {
                mVibrator.vibrate(mVibrateSeconds * 1000);
                System.out.println("--------------->");
                try {
                    Thread.sleep((mVibrateSeconds + 1) * 1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            handler.sendEmptyMessage(1);
        }
    };

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
        mToolKit.returnWithResult(isPass);
    }

}
