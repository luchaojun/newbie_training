package com.wistron.generic.touchpanel;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

public class TouchPanel extends Activity implements OnClickListener {
    private final int ITEM_CORNER = 1;
    private final int ITEM_CROSS = 2;
    private final int ITEM_DIAGONAL = 4;
    // ----------------------result---------------
    private TextView mResultContent;
    private Button mResultButton;
    // --------------------------------------
    private Button btn_start, btn_exit;
    private CheckBox cb_corner, cb_cross, cb_diagonal;
    private static final int RequestCode = 0;
    // --------------------variables-------------------
    private int mTestTime;
    private int mTestItem;

    private boolean isPass = false;
    private boolean mComponentMode = true;
    private boolean isPCBStage = false;

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
        setContentView(R.layout.touchpanel);

        mToolKit = new WisToolKit(this);

        findView();
        getTestArguments();
        setViewByLanguage();

//		Intent mCalibrationIntent = new Intent();
//		mCalibrationIntent.setClassName("com.sis.FwTool", "com.sis.FwTool.Recalibration.ReCalibrate");
//		mCalibrationIntent.putExtra("language", mLanguage);
//		startActivity(mCalibrationIntent);
    }

    private void setViewByLanguage() {
        // TODO Auto-generated method stub
        TextView mItemTitle = (TextView) findViewById(R.id.item_title);
        mItemTitle.setText(mToolKit.getStringResource(R.string.touchpanel_test_title));
        ((TextView) findViewById(R.id.touchpanel_item)).setText(mToolKit.getStringResource(R.string.touchpanel_item));
        cb_corner.setText(mToolKit.getStringResource(R.string.touchpanel_corner));
        cb_cross.setText(mToolKit.getStringResource(R.string.touchpanel_cross));
        cb_diagonal.setText(mToolKit.getStringResource(R.string.touchpanel_diagonal));
        btn_start.setText(mToolKit.getStringResource(R.string.button_start));
        btn_exit.setText(mToolKit.getStringResource(R.string.button_exit));
    }

    private void findView() {
        // TODO Auto-generated method stub
        btn_start = (Button) findViewById(R.id.touchpanel_start);
        btn_exit = (Button) findViewById(R.id.touchpanel_exit);

        cb_corner = (CheckBox) findViewById(R.id.touchpanel_corner);
        cb_cross = (CheckBox) findViewById(R.id.touchpanel_cross);
        cb_diagonal = (CheckBox) findViewById(R.id.touchpanel_diagonal);

        btn_start.setOnClickListener(this);
        btn_exit.setOnClickListener(this);
    }

    private void getTestArguments() {
        // TODO Auto-generated method stub
        String mTestStyle = mToolKit.getCurrentTestType();
        if (mTestStyle != null) {
            if (mTestStyle.equals(WisCommonConst.TESTSTYLE_SAVED_CONFIG)) {
                mComponentMode = false;
                WisParseValue mParse = new WisParseValue(this, mToolKit.getCurrentItem(), mToolKit.getCurrentDatabaseAuthorities());
                mTestItem = Integer.parseInt(mParse.getArg1());
                mTestTime = Integer.parseInt(mParse.getArg2());

                isPCBStage = mToolKit.isPCBATestStage();
                if ((mTestItem & ITEM_CORNER) != 0) {
                    cb_corner.setChecked(true);
                } else {
                    cb_corner.setChecked(false);
                }
                if ((mTestItem & ITEM_CROSS) != 0) {
                    cb_cross.setChecked(true);
                } else {
                    cb_cross.setChecked(false);
                }
                if ((mTestItem & ITEM_DIAGONAL) != 0) {
                    cb_diagonal.setChecked(true);
                } else {
                    cb_diagonal.setChecked(false);
                }
                btn_start.performClick();
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
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        // TODO Auto-generated method stub
        Log.i("Tag", "TouchPanel result");
        switch (requestCode) {
            case RequestCode:
                isPass = data.getBooleanExtra(WisCommonConst.EXTRA_PASS, false);
                if (mComponentMode) {
                    displayResult();
                } else {
                    mToolKit.returnWithResult(isPass);
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void showAlert(CharSequence string_Message) {
        new AlertDialog.Builder(this).setTitle(mToolKit.getStringResource(R.string.touchpanel_dialog_title))
                .setPositiveButton(mToolKit.getStringResource(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        setResult(RESULT_OK);
                    }
                }).setIcon(getResources().getDrawable(R.drawable.alert_dialog_icon)).setMessage(string_Message).setCancelable(false).show();
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v == btn_start) {
            if (!(cb_corner.isChecked() || cb_cross.isChecked() || cb_diagonal.isChecked())) {
                showAlert(mToolKit.getStringResource(R.string.touchpanel_dialog_msg));
            } else {
                Intent in = new Intent();
                in.putExtra("corner", cb_corner.isChecked());
                in.putExtra("cross", cb_cross.isChecked());
                in.putExtra("diagonal", cb_diagonal.isChecked());
                in.putExtra("timeout", mTestTime);
                in.putExtra("stage", isPCBStage);
                in.putExtra("language", mToolKit.getCurrentLanguage());
                in.setClass(TouchPanel.this, TouchPanelTest.class);
                TouchPanel.this.startActivityForResult(in, RequestCode);
            }
        } else if (v == btn_exit) {
            if (mComponentMode) {
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