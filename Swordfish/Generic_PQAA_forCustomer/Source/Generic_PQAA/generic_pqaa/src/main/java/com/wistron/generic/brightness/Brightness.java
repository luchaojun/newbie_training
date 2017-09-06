package com.wistron.generic.brightness;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.wistron.generic.pqaa.R;
import com.wistron.pqaa_common.jar.global.WisCommonConst;
import com.wistron.pqaa_common.jar.global.WisParseValue;
import com.wistron.pqaa_common.jar.global.WisToolKit;
import com.wistron.pqaa_common.jar.global.WisToolKit.onManualSelectedListener;

import java.util.ArrayList;
import java.util.List;

public class Brightness extends Activity implements OnClickListener {
    private final int DISPLAY_INTERVAL = 500; // millisecond
    // ----------------------result---------------
    private Button mResultButton;
    private boolean isResultPage = false;
    // --------------------------------------
    private ImageView iv_left, iv_right;
    private ImageView iv_back, iv_wrap;
    private ListView lv_dis;

    private Button btn_add_current, btn_clear_all, btn_remove;
    private Button btn_start, btn_pass, btn_fail;
    private RadioButton rd_wrap, rd_back;
    private SeekBar sb_bright;
    private EditText et_times;
    // ----------------****----------------//
    private int mTestType, mTestCycles;
    private int iBrightValue = 0;
    private int iPos = 0;
    private List<String> l_current = null;
    private int iListPos = 0;
    private int iRunTimes;
    private BrightnessAdapter mAdapter;
    public static int mCurrentItem;
    private boolean mForward = false;
    private boolean mFirstRun = true;
    private int mOriginalBrightness = 0;

    private boolean mComponentMode = true;
    private boolean isExit = false;

    // common tool kit
    private WisToolKit mToolKit;

    private final Handler myHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case 0:
                    mAdapter.notifyDataSetChanged();
                    lv_dis.setSelection(msg.arg1);
                    disableAllWigdet();
                    setBrightness(Integer.parseInt(l_current.get(msg.arg1)) * 255 / 10);
                    break;
                case 1:
                    mCurrentItem = 0;
                    mAdapter.notifyDataSetChanged();
                    lv_dis.setSelection(mCurrentItem);
                    setBrightness(120);
                    enableAllWigdet();
                    backToPQAA();
                    break;
            }
            super.handleMessage(msg);
        }

    };
    private final Runnable wrapRunnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            Message msg;
            for (int ia = 0; ia < iRunTimes; ia++) {
                for (int ib = 0; ib < l_current.size(); ib++) {
                    try {
                        Thread.sleep(DISPLAY_INTERVAL);
                        if (isExit) {
                            return;
                        } else {
                            msg = new Message();
                            msg.arg1 = ib;
                            msg.what = 0;
                            mCurrentItem = ib;
                            myHandler.sendMessage(msg);
                        }
                    } catch (Exception e) {
                        e.toString();
                    }
                }
            }
            try {
                Thread.sleep(DISPLAY_INTERVAL);
                msg = new Message();
                msg.what = 1;
                myHandler.sendMessage(msg);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    };
    private final Runnable backRunnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            Message msg;
            int ib;
            for (int ia = 0; ia < iRunTimes; ia++) {
                mForward = !mForward;
                if (mFirstRun) {
                    mFirstRun = false;
                    ib = mForward ? 0 : l_current.size() - 1;
                } else {
                    ib = mForward ? 1 : l_current.size() - 2;
                }
                for (; (mForward ? ib < l_current.size() : ib >= 0); ib = (mForward ? ib + 1 : ib - 1)) {
                    try {
                        Thread.sleep(DISPLAY_INTERVAL);
                        if (isExit) {
                            return;
                        } else {
                            msg = new Message();
                            msg.arg1 = ib;
                            msg.what = 0;
                            mCurrentItem = ib;
                            myHandler.sendMessage(msg);
                        }
                    } catch (Exception e) {
                        e.toString();
                    }
                }
            }
            try {
                Thread.sleep(DISPLAY_INTERVAL);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            mForward = false;
            mFirstRun = true;
            msg = new Message();
            msg.what = 1;
            myHandler.sendMessage(msg);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.brightness);

        mToolKit = new WisToolKit(this);

        initializeGUI();
        initializeValue();
        getOriginalBrightness();
        getTestArguments();
        setViewByLanguage();
    }

    private void setViewByLanguage() {
        // TODO Auto-generated method stub
        TextView mItemTitle = (TextView) findViewById(R.id.item_title);
        mItemTitle.setText(mToolKit.getStringResource(R.string.brightness_test_title));
        ((TextView) findViewById(R.id.brightness_sequence_title)).setText(mToolKit.getStringResource(R.string.brightness_sequence));
        ((TextView) findViewById(R.id.brightness_mode_title)).setText(mToolKit.getStringResource(R.string.brightness_mode));
        ((TextView) findViewById(R.id.brightness_times_title)).setText(mToolKit.getStringResource(R.string.brightness_times));
        btn_add_current.setText(mToolKit.getStringResource(R.string.brightness_button_current));
        btn_remove.setText(mToolKit.getStringResource(R.string.brightness_button_remove));
        btn_clear_all.setText(mToolKit.getStringResource(R.string.brightness_button_clear));
        rd_wrap.setText(mToolKit.getStringResource(R.string.brightness_mode_wrap));
        rd_back.setText(mToolKit.getStringResource(R.string.brightness_mode_back));
        btn_start.setText(mToolKit.getStringResource(R.string.button_start));
        btn_pass.setText(mToolKit.getStringResource(R.string.button_pass));
        btn_fail.setText(mToolKit.getStringResource(R.string.button_fail));
    }

    private void getTestArguments() {
        // TODO Auto-generated method stub
        String mTestStyle = mToolKit.getCurrentTestType();
        if (mTestStyle != null) {
            if (mTestStyle.equals(WisCommonConst.TESTSTYLE_SAVED_CONFIG)) {
                mComponentMode = false;
                WisParseValue mParse = new WisParseValue(this, mToolKit.getCurrentItem(), mToolKit.getCurrentDatabaseAuthorities());
                mTestType = Integer.parseInt(mParse.getArg1());
                mTestCycles = Integer.parseInt(mParse.getArg2());
                if (mTestType == 0) {
                    rd_wrap.setChecked(true);
                } else {
                    rd_back.setChecked(true);
                }
                et_times.setText(String.valueOf(mTestCycles));
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

    private void setBrightness(int iValue) {
        Settings.System.putString(this.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, String.valueOf(iValue));
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        Float tmpFloat = (float) iValue / 255;
        if (tmpFloat < 0.1f) {
            tmpFloat = 0.1f;
        }
        lp.screenBrightness = tmpFloat;
        getWindow().setAttributes(lp);
    }

    private void getOriginalBrightness() {
        // TODO Auto-generated method stub
        try {
            mOriginalBrightness = Settings.System.getInt(this.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            Log.i("Brightness", ": " + mOriginalBrightness);
        } catch (SettingNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void initializeValue() { // 2011/02/28
        try {
            l_current = new ArrayList<String>();
            et_times.setText("2");
            iBrightValue = Settings.System.getInt(this.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            iPos = iBrightValue * 10 / 255;
            sb_bright.setProgress(iPos);
            rd_wrap.setChecked(true);
            for (int i = 1; i < 11; i++) {
                l_current.add(String.valueOf(i));
            }
            mAdapter = new BrightnessAdapter(this, l_current);
            lv_dis.setAdapter(mAdapter);
        } catch (Exception e) {
            e.toString();
        }
    }

    private void initializeGUI() {
        iv_left = (ImageView) findViewById(R.id.brightness_ivleft);
        iv_right = (ImageView) findViewById(R.id.brightness_ivright);
        iv_back = (ImageView) findViewById(R.id.brightness_back_image);
        iv_wrap = (ImageView) findViewById(R.id.brightness_wrap_image);
        lv_dis = (ListView) findViewById(R.id.brightness_lightlist);
        btn_add_current = (Button) findViewById(R.id.brightness_addcur);
        btn_remove = (Button) findViewById(R.id.brightness_remove);
        btn_clear_all = (Button) findViewById(R.id.brightness_clear);

        btn_start = (Button) findViewById(R.id.brightness_start);
        btn_fail = (Button) findViewById(R.id.brightness_fail_exit);
        btn_pass = (Button) findViewById(R.id.brightness_pass_exit);

        rd_wrap = (RadioButton) findViewById(R.id.brightness_wrap);
        rd_back = (RadioButton) findViewById(R.id.brightness_back);

        sb_bright = (SeekBar) findViewById(R.id.brightness_sblight);

        et_times = (EditText) findViewById(R.id.brightness_times);

        iv_left.setImageResource(R.drawable.brightness_left);
        iv_right.setImageResource(R.drawable.brightness_right);
        iv_back.setImageResource(R.drawable.brightness_back);
        iv_wrap.setImageResource(R.drawable.brightness_wrap);

        lv_dis.setCacheColorHint(0);

        sb_bright.setMax(10);

        btn_pass.setOnClickListener(this);
        btn_fail.setOnClickListener(this);
        btn_clear_all.setOnClickListener(this);
        btn_start.setOnClickListener(this);
        btn_remove.setOnClickListener(this);
        btn_add_current.setOnClickListener(this);
        iv_left.setOnClickListener(this);
        iv_right.setOnClickListener(this);
        lv_dis.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub
                iListPos = arg2;
                mCurrentItem = arg2;
                mAdapter.notifyDataSetChanged();
            }

        });
        lv_dis.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub
                iListPos = arg2;
                mCurrentItem = arg2;
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });
        sb_bright.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar arg0, int pos, boolean arg2) {
                // TODO Auto-generated method stub
                iPos = pos;
                if (iPos > 9) {
                    iPos = 10;
                    iv_right.setImageResource(R.drawable.brightness_rightg);
                    sb_bright.setProgress(10);
                    setBrightness(255);
                } else {
                    if (iPos < 1) {
                        iPos = 0;
                        iv_left.setImageResource(R.drawable.brightness_leftg);
                        sb_bright.setProgress(0);
                        setBrightness(0);
                    } else {
                        iv_right.setImageResource(R.drawable.brightness_right);
                        iv_left.setImageResource(R.drawable.brightness_left);
                        setBrightness(iPos * 255 / 10);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
                // TODO Auto-generated method stub

            }

        });

        mToolKit.setOnManualSelectedListener(new onManualSelectedListener() {

            @Override
            public void onManualSelected(DialogInterface arg0, int arg1) {
                // TODO Auto-generated method stub
                mToolKit.returnWithResult(arg1 == DialogInterface.BUTTON_POSITIVE);
            }
        });
    }

    private void disableAllWigdet() {
        iv_left.setEnabled(false);
        iv_right.setEnabled(false);
        lv_dis.setEnabled(false);
        btn_add_current.setEnabled(false);
        btn_clear_all.setEnabled(false);
        btn_remove.setEnabled(false);
        btn_start.setEnabled(false);
        rd_wrap.setEnabled(false);
        rd_back.setEnabled(false);
        sb_bright.setEnabled(false);
        et_times.setEnabled(false);
    }

    private void enableAllWigdet() {
        iv_left.setEnabled(true);
        iv_right.setEnabled(true);
        lv_dis.setEnabled(true);
        btn_add_current.setEnabled(true);
        btn_clear_all.setEnabled(true);
        btn_remove.setEnabled(true);
        btn_start.setEnabled(true);
        rd_wrap.setEnabled(true);
        rd_back.setEnabled(true);
        sb_bright.setEnabled(true);
        et_times.setEnabled(true);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v == btn_pass) {
            backResult(true);
        } else if (v == btn_fail) {
            backResult(false);
        } else if (v == btn_clear_all) {
            try {
                l_current.clear();
                mAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.toString();
            }
        } else if (v == btn_start) {
            try {
                iRunTimes = Integer.parseInt(et_times.getText().toString());
                if (iRunTimes > 0) {
                    btn_start.setVisibility(View.INVISIBLE);
                    btn_pass.setVisibility(View.INVISIBLE);
                    btn_fail.setVisibility(View.INVISIBLE);
                    if (rd_wrap.isChecked()) {
                        new Thread(wrapRunnable).start();
                    } else if (rd_back.isChecked()) {
                        new Thread(backRunnable).start();
                    }
                } else {
                    Toast.makeText(this, mToolKit.getStringResource(R.string.brightness_warning), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.toString();
                Toast.makeText(this, mToolKit.getStringResource(R.string.brightness_warning), Toast.LENGTH_SHORT).show();
            }
        } else if (v == btn_remove) {
            try {
                l_current.remove(iListPos);
                mAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.toString();
            }
        } else if (v == btn_add_current) {
            l_current.add(String.valueOf(iPos));
            mAdapter.notifyDataSetChanged();
        } else if (v == iv_left) {
            iPos--;
            if (iPos < 0) {
                iPos = 0;
                iv_left.setImageResource(R.drawable.brightness_leftg);
                sb_bright.setProgress(0);
                setBrightness(0);
            } else {
                iv_left.setImageResource(R.drawable.brightness_left);
                sb_bright.setProgress(iPos);
                setBrightness(iPos * 255 / 10);
            }
        } else if (v == iv_right) {
            iPos++;
            if (iPos > 10) {
                iPos = 10;
                iv_right.setImageResource(R.drawable.brightness_rightg);
                sb_bright.setProgress(10);
                setBrightness(255);
            } else {
                iv_right.setImageResource(R.drawable.brightness_right);
                sb_bright.setProgress(iPos);
                setBrightness(iPos * 255 / 10);
            }
        } else if (v == mResultButton) {
            backResult(true);
        }
    }

    private void backToPQAA() {
        isExit = true;
        mToolKit.selectTestResultByManual(mToolKit.getStringResource(R.string.manual_testresult), getResources().getDimension(R.dimen.alert_dialog_message_size),
                mToolKit.getStringResource(R.string.pass), mToolKit.getStringResource(R.string.fail), getResources().getDimension(R.dimen.alert_dialog_button_size));
    }

    private void backResult(boolean finish) {
        isExit = true;
        mToolKit.returnWithResult(finish);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        setBrightness(mOriginalBrightness);
    }

}