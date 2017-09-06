package com.wistron.generic.display;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.wistron.generic.pqaa.R;
import com.wistron.pqaa_common.jar.global.WisCommonConst;
import com.wistron.pqaa_common.jar.global.WisParseValue;
import com.wistron.pqaa_common.jar.global.WisToolKit;

public class Display extends Activity implements OnClickListener {
    private final int ITEM_SOLID_COLOR = 1;
    private final int ITEM_BWCOLOR = 2;
    private final int ITEM_SOLID_LINE = 4;
    private final int ITEM_FLICKERS = 8;
    private final int ITEM_COLOR_BARS = 16;
    private final int ITEM_CHECKER_BOARD = 32;
    private final int ITEM_GHOSTS = 64;
    private final int AUTO_TAP = 128;
    // ----------------------result---------------
    private TextView mResultContent;
    private Button mResultButton;
    private boolean isResultPage = false;
    // --------------------------------------
    private CheckBox[] mTestItems;
    private Button mStartButton;
    private Button mExitButton;
    private boolean[] mCheckStatus;
    private static final int ResultCode = 0;

    private int mItems;
    private int mInterval = 2;

    private boolean mComponentMode = true;

    // common tool kit
    private WisToolKit mToolKit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.display);

        mToolKit = new WisToolKit(this);

        findView();
        getTestArguments();
        setViewByLanguage();
    }

    private void setViewByLanguage() {
        // TODO Auto-generated method stub
        TextView mItemTitle = (TextView) findViewById(R.id.item_title);
        mItemTitle.setText(mToolKit.getStringResource(R.string.display_test_title));
        ((TextView) findViewById(R.id.display_testitem)).setText(mToolKit.getStringResource(R.string.display_testitem));
        ((TextView) findViewById(R.id.display_setting)).setText(mToolKit.getStringResource(R.string.display_setting));
        mTestItems[0].setText(mToolKit.getStringResource(R.string.display_solid ));
        mTestItems[1].setText(mToolKit.getStringResource(R.string.display_color ));
        mTestItems[2].setText(mToolKit.getStringResource(R.string.display_line ));
        mTestItems[3].setText(mToolKit.getStringResource(R.string.display_flickers ));
        mTestItems[4].setText(mToolKit.getStringResource(R.string.display_colorbars ));
        mTestItems[5].setText(mToolKit.getStringResource(R.string.display_checker ));
        mTestItems[6].setText(mToolKit.getStringResource(R.string.display_ghosts ));
        mTestItems[7].setText(mToolKit.getStringResource(R.string.display_auto));
        mStartButton.setText(mToolKit.getStringResource(R.string.button_start));
        mExitButton.setText(mToolKit.getStringResource(R.string.button_exit));
    }

    private void findView() {
        // TODO Auto-generated method stub
        mTestItems = new CheckBox[8];
        mCheckStatus = new boolean[8];
        for (int i = 0; i < 7; i++) {
            mTestItems[i] = (CheckBox) findViewById(R.id.display_solid + i);
        }
        mTestItems[7] = (CheckBox) findViewById(R.id.display_autotap);
        mStartButton = (Button) findViewById(R.id.display_start);
        mExitButton = (Button) findViewById(R.id.display_exit);
        mStartButton.setOnClickListener(this);
        mExitButton.setOnClickListener(this);

        mExitButton.setVisibility(View.INVISIBLE);
    }

    private void getTestArguments() {
        // TODO Auto-generated method stub
        String mTestStyle = mToolKit.getCurrentTestType();
        if (mTestStyle != null) {
            if (mTestStyle.equals(WisCommonConst.TESTSTYLE_SAVED_CONFIG)) {
                mComponentMode = false;
                WisParseValue mParse = new WisParseValue(this, mToolKit.getCurrentItem(), mToolKit.getCurrentDatabaseAuthorities());
                mItems = Integer.parseInt(mParse.getArg1());
                mInterval = Integer.parseInt(mParse.getArg2());

                for (int i = 0; i < mTestItems.length; i++) {
                    if ((mItems & (int) Math.pow(2, i)) != 0) {
                        mTestItems[i].setChecked(true);
                    } else {
                        mTestItems[i].setChecked(false);
                    }
                }
                mStartButton.performClick();
            }
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isResultPage) {
                mResultButton.performClick();
            } else {
                mExitButton.performClick();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void showAlert(CharSequence string_Message) {
        new AlertDialog.Builder(this).setTitle(mToolKit.getStringResource(R.string.display_dialog_title))
                .setPositiveButton(mToolKit.getStringResource(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        setResult(RESULT_OK);
                    }
                }).setIcon(getResources().getDrawable(R.drawable.alert_dialog_icon)).setMessage(string_Message)
                .setCancelable(false).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        // TODO Auto-generated method stub
        switch (requestCode) {
            case ResultCode:
                if (mComponentMode) {
                    displayResult();
                } else {
                    backToPQAA();
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v == mStartButton) {
            v.setVisibility(View.INVISIBLE);
            for (int i = 0; i < mTestItems.length; i++) {
                if (mTestItems[i].isChecked()) {
                    mCheckStatus[i] = true;
                } else {
                    mCheckStatus[i] = false;
                }
            }
            if (!(mCheckStatus[0] || mCheckStatus[1] || mCheckStatus[2] || mCheckStatus[3] || mCheckStatus[4]
                    || mCheckStatus[5] || mCheckStatus[6])) {
                showAlert(mToolKit.getStringResource(R.string.display_dialog_msg));
            } else {
                Intent intent = new Intent();
                intent.setClass(Display.this, graphics.class);
                intent.putExtra("Selected Items", mCheckStatus);
                intent.putExtra("interval", mInterval);
                Display.this.startActivityForResult(intent, ResultCode);
            }
        } else if (v == mExitButton) {
            if (mComponentMode) {
                isResultPage = true;
                displayResult();
            } else {
                backToPQAA();
            }
        } else if (v == mResultButton) {
            backToPQAA();
        }
    }

    private void displayResult() {
        // TODO Auto-generated method stub
        // setContentView(R.layout.result);
        // mResultContent = (TextView) findViewById(R.id.result_result);
        // mResultButton = (Button) findViewById(R.id.result_back);
        // if (finish) {
        // mResultContent.setText(R.string.pass);
        // } else {
        // mResultContent.setText(R.string.fail);
        // }
        // mResultButton.setOnClickListener(this);
        backToPQAA();
    }

    private void backToPQAA() {
        mToolKit.selectTestResultByManual(mToolKit.getStringResource(R.string.manual_testresult), getResources().getDimension(R.dimen.alert_dialog_message_size),
                mToolKit.getStringResource(R.string.pass), mToolKit.getStringResource(R.string.fail), getResources().getDimension(R.dimen.alert_dialog_button_size));
    }
}