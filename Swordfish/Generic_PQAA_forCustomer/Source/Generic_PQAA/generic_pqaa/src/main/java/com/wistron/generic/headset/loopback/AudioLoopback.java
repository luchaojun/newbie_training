package com.wistron.generic.headset.loopback;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.wistron.generic.pqaa.R;
import com.wistron.generic.pqaa.utility.CommonParams;
import com.wistron.pqaa_common.jar.autotest.WisAudioLoopback_Service;
import com.wistron.pqaa_common.jar.global.WisAutoScrollTextView;
import com.wistron.pqaa_common.jar.global.WisCommonConst;
import com.wistron.pqaa_common.jar.global.WisLog;
import com.wistron.pqaa_common.jar.global.WisParseValue;
import com.wistron.pqaa_common.jar.global.WisToolKit;
import com.wistron.pqaa_common.jar.wcis.WisSubHtmlBuilder;
import com.wistron.pqaa_common.jar.wcis.WisWCISCommonConst;
import com.wistron.pqaa_common.jar.wcis.WisWCISSubLogItem;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class AudioLoopback extends Activity implements OnClickListener {
    // WCIS options
    private String mAudioLoopbackConfigFileName = "headsetloopback.cfg";
    private String AUDIOLOOPBACK_CONFIG_FILE_PATH = CommonParams.GENERIC_PATH +
            mAudioLoopbackConfigFileName;
    private String mWCISAudioLoopbackGeneralLogFileName = "headsetloopback_log.txt";
    private String mWCISAudioLoopbackHtmlLogFileName = "headsetloopback_html.html";
    private String mWCISAudioLoopbackGeneralLogFilePath;
    private String mWCISAudioLoopbackHtmlLogFilePath;

    private static final String AUDIOLOOPBACK_LOG_PATH = CommonParams.GENERIC_PATH
            + "headsetloopback.txt";

    private final String AUDIO_CONFIG_PATH = CommonParams.GENERIC_PATH +
            "pqaa_config/headsetloopback.cfg";

    private EditText et_DTMFAudio, et_DTMFDecode;
    private EditText et_DTMFDecode_MP3, et_DTMFDecode_OGG, et_DTMFDecode_AAC,
            et_DTMFDecode_WAV;
    private Button btn_Start, btn_Exit;
    // DTMF: 057A*#81
    // Custom: 0123
    private String mAudioValue = "0123456789*#", mDecodeValue = "";
    private int mNeedMatchCount = mAudioValue.length();
    private boolean isDTMFAudio = true;
    private int mTestCycles = 1, mCyclesIndex = 0;
    private int mCurVolume = 15;

    private boolean isWCISTest = false;
    private boolean isComponentMode = true;
    private String mAudioLoopbackLog = "";
    private String mWCISTestRemark = "";
    private ArrayList<WisWCISSubLogItem> mAudioLoopbackLogList;
    private int mPassCount, mFailCount;
    private boolean isRegisteredBroadcast = false;

    private WisToolKit mToolKit;
    // Log class
    private WisLog mLogHandler;

    private Intent mAudioLoopbackServiceIntent;

    // WCIS parameters
    private int mWCISDTMFTestTypes = 0;
    private int mWCISDTMFTypeIndex = 0;

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
        getWindow().setSoftInputMode(WindowManager.LayoutParams
                .SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.loopback);

        mToolKit = new WisToolKit(this);

        getTestArguments();
        findView();
        setViewByLanguage();
        if (!isWCISTest) {
            try {
                mLogHandler = new WisLog(AUDIOLOOPBACK_LOG_PATH);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            doCreate();
        }

    }

    private void doCreate() {
        // TODO Auto-generated method stub
        if (isDTMFAudio) {
           // findViewById(R.id.audioloopback_dtmf_decode_normal).setVisibility(View.GONE);
            if ((mWCISDTMFTestTypes & AudioLoopbackService.WCIS_DTMF_TYPES_MP3) != 0) {
                findViewById(R.id.audioloopback_dtmf_decode_mp3).setVisibility(View
                        .VISIBLE);
            }
            if ((mWCISDTMFTestTypes & AudioLoopbackService.WCIS_DTMF_TYPES_OGG) != 0) {
                findViewById(R.id.audioloopback_dtmf_decode_ogg).setVisibility(View
                        .VISIBLE);
            }
            if ((mWCISDTMFTestTypes & AudioLoopbackService.WCIS_DTMF_TYPES_AAC) != 0) {
                findViewById(R.id.audioloopback_dtmf_decode_aac).setVisibility(View
                        .VISIBLE);
            }
            if ((mWCISDTMFTestTypes & AudioLoopbackService.WCIS_DTMF_TYPES_WAV) != 0) {
                findViewById(R.id.audioloopback_dtmf_decode_wav).setVisibility(View
                        .VISIBLE);
            }
        }
        initialize();

        if (!isComponentMode) {
            btn_Start.performClick();
        }
    }

    // headset
    private boolean isRegisterBroadcast = false;
    private Timer mHeadsetTimer = null;
    private TimerTask mHeadsetTask = null;
    private int mHeadsetTimeout = 15, mHeadsetTimes = 0;
    private AlertDialog mHeadsetDialog = null, mHeadsetPullDialog = null;
    private boolean isHeasePluginStatus = false;
    private boolean isHeadsetPluginTimeout = false;

    // Headset
    private void registerHeadSet() {
        // TODO Auto-generated method stub
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(headsetReceiver, mFilter);
        isRegisterBroadcast = true;
    }

    private BroadcastReceiver headsetReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            int mState = intent.getIntExtra("state", 4);
            int mMicroPhone = intent.getIntExtra("microphone", 0);
            Log.i("W", "headset state: " + mState + ", microphone: " + mMicroPhone);
            if (mState == 1) {
                isHeasePluginStatus = true;
                mHeadsetHandler.obtainMessage(3).sendToTarget();
            } else {
                isHeasePluginStatus = false;
                if (isEndTest) {
                    pullHeadsetHandler.sendEmptyMessage(3);
                }
            }
        }
    };

    private void initHeadsetTimer() {
        mHeadsetTimer = new Timer();
        mHeadsetTask = new TimerTask() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                mHeadsetTimes++;
                if (mHeadsetTimes >= mHeadsetTimeout) {
                    mHeadsetHandler.obtainMessage(2).sendToTarget();
                } else {
                    mHeadsetHandler.obtainMessage(1).sendToTarget();
                }
            }
        };
        mHeadsetTimer.schedule(mHeadsetTask, 1000, 1000);
    }

    private void cancelHeadsetTimer() {
        if (mHeadsetTimer != null) {
            mHeadsetTimer.cancel();
            mHeadsetTimer = null;
        }

        if (mHeadsetTask != null) {
            mHeadsetTask.cancel();
            mHeadsetTask = null;
        }
    }

    private Handler mHeadsetHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    mHeadsetDialog.setMessage(String.format(
                            mToolKit.getStringResource(R.string
                                    .headset_loopback_plugin_headset),
                            mHeadsetTimeout - mHeadsetTimes));
                    break;
                case 2:
                    if (mHeadsetDialog != null && mHeadsetDialog.isShowing()) {
                        mHeadsetDialog.dismiss();
                    }
                    cancelHeadsetTimer();
                    isHeadsetPluginTimeout = true;
                    btn_Exit.performClick();
                    break;
                case 3:
                    if (mHeadsetDialog != null && mHeadsetDialog.isShowing()) {
                        mHeadsetDialog.dismiss();
                    }
                    cancelHeadsetTimer();

//                updateView(false);
//                updateResult();
                    mAudioLoopbackServiceIntent.putExtra(WisAudioLoopback_Service
                                    .EXTRA_IS_DTMF,
                            isDTMFAudio);
                    mAudioLoopbackServiceIntent.putExtra(WisAudioLoopback_Service
                                    .EXTRA_IS_WCIS,
                            isWCISTest);
                    mAudioLoopbackServiceIntent.putExtra("dtmfTypes", mWCISDTMFTestTypes);
                    mAudioLoopbackServiceIntent.putExtra(WisAudioLoopback_Service
                                    .EXTRA_TEST_QUEUE,
                            mAudioValue);
                    mAudioLoopbackServiceIntent.putExtra(
                            WisAudioLoopback_Service.EXTRA_NEED_MATCH_COUNT,
                            mNeedMatchCount);
                    mAudioLoopbackServiceIntent.putExtra(WisAudioLoopback_Service
                                    .EXTRA_VOLUME,
                            mCurVolume);
                    mAudioLoopbackServiceIntent.putExtra(WisAudioLoopback_Service
                                    .EXTRA_TEST_CYCLES,
                            mTestCycles);
                    startService(mAudioLoopbackServiceIntent);
                    break;
            }
        }

    };

    private void defineHeadsetDialog() {
        mHeadsetDialog = new AlertDialog.Builder(this).create();
        mHeadsetDialog.setCancelable(false);
        mHeadsetDialog.setCanceledOnTouchOutside(false);
        mHeadsetDialog.setMessage(String.format(
                mToolKit.getStringResource(R.string.headset_loopback_plugin_headset),
                mHeadsetTimeout - mHeadsetTimes));
        mHeadsetDialog.show();
    }

    private void getTestArguments() {
        // TODO Auto-generated method stub
        String mTestStyle = mToolKit.getCurrentTestType();
        if (mTestStyle != null) {
            if (mTestStyle.equals(WisCommonConst.TESTSTYLE_SAVED_CONFIG)) {
                isWCISTest = getIntent().getBooleanExtra(WisCommonConst
                        .EXTRA_IS_WCIS_TEST, false);
                isComponentMode = false;
                if (isWCISTest) {
                    AUDIOLOOPBACK_CONFIG_FILE_PATH = getIntent().getStringExtra(
                            WisWCISCommonConst.EXTRA_CONFIG_FOLDER)
                            + mAudioLoopbackConfigFileName;
                    mWCISAudioLoopbackGeneralLogFilePath = getIntent().getStringExtra(
                            WisWCISCommonConst.EXTRA_LOG_FOLDER)
                            + mWCISAudioLoopbackGeneralLogFileName;
                    mWCISAudioLoopbackHtmlLogFilePath = getIntent().getStringExtra(
                            WisWCISCommonConst.EXTRA_LOG_FOLDER)
                            + mWCISAudioLoopbackHtmlLogFileName;

                    registerReceiver(readConfigReceiver, new IntentFilter(
                            WisWCISCommonConst.ACTION_WCIS_FEEDBACK_CONFIG));

                    Intent getConfigIntent = new Intent(WisWCISCommonConst
                            .ACTION_WCIS_READ_CONFIG);
                    getConfigIntent.putExtra(WisWCISCommonConst.EXTRA_CONFIG_PATH,
                            AUDIOLOOPBACK_CONFIG_FILE_PATH);
                    sendBroadcast(getConfigIntent);
                } else {
                    WisParseValue mParse = new WisParseValue(this, mToolKit.getCurrentItem(),
                            mToolKit.getCurrentDatabaseAuthorities());
                    int isDTMF = Integer.parseInt(mParse.getArg1());
                    if (isDTMF == 1) {
                        isDTMFAudio = true;
                    } else {
                        isDTMFAudio = false;
                    }
                    String mValue = mParse.getArg2();
                    if (mValue != null) {
                        mAudioValue = mValue;
                    }
                    int needpass = Integer.parseInt(mParse.getArg3());
                    mNeedMatchCount = needpass;
                    if (mNeedMatchCount > mAudioValue.length()) {
                        mNeedMatchCount = mAudioValue.length();
                    }
                    int Volume = Integer.parseInt(mParse.getArg4());
                    if (Volume > 0) {
                        mCurVolume = Volume;
                    }
                }
            }
        }
    }

    private BroadcastReceiver readConfigReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            unregisterReceiver(this);
            Map<String, String> mParametersList = (Map<String, String>) intent
                    .getSerializableExtra(WisWCISCommonConst.EXTRA_CONFIG_CONTENT);
            updateTestParamsByConfig(mParametersList);
            doCreate();
        }
    };

    private void updateTestParamsByConfig(Map<String, String> mParametersList) {
        if (mParametersList != null && mParametersList.size() > 0) {
            for (String key : mParametersList.keySet()) {
                if (key.equals("isDTMF")) {
                    isDTMFAudio = mParametersList.get(key).equals("1");
                } else if (key.equals("testQueue")) {
                    mAudioValue = mParametersList.get(key);
                } else if (key.equals("needMatchCount")) {
                    mNeedMatchCount = Integer.parseInt(mParametersList.get(key));
                    if (mNeedMatchCount > mAudioValue.length()) {
                        mNeedMatchCount = mAudioValue.length();
                    }
                } else if (key.equals("volume")) {
                    mCurVolume = Integer.parseInt(mParametersList.get(key));
                } else if (key.equals("testCycles")) {
                    mTestCycles = Integer.parseInt(mParametersList.get(key));
                } else if (key.equals("dtmfTestTypes")) {
                    mWCISDTMFTestTypes = Integer.parseInt(mParametersList.get(key));
                }
            }
        } else {
            Toast.makeText(this, mToolKit.getStringResource(R.string.noconfigfile_msg),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void setViewByLanguage() {
        // TODO Auto-generated method stub
        ((TextView) findViewById(R.id.item_title)).setText(mToolKit
                .getStringResource(R.string.headset_loopback_test_title));
        ((TextView) findViewById(R.id.audioloopback_title_dtmf_set)).setText(mToolKit
                .getStringResource(R.string.audio_loopback_dtmf_set));
        ((TextView) findViewById(R.id.audioloopback_dtmf_decode_normal_title)).setText
                (mToolKit
                        .getStringResource(R.string.audio_loopback_dtmf_decode));
        ((TextView) findViewById(R.id.audioloopback_dtmf_decode_mp3_title)).setText
                (mToolKit
                        .getStringResource(R.string.audio_loopback_dtmf_decode_mp3));
        ((TextView) findViewById(R.id.audioloopback_dtmf_decode_ogg_title)).setText
                (mToolKit
                        .getStringResource(R.string.audio_loopback_dtmf_decode_ogg));
        ((TextView) findViewById(R.id.audioloopback_dtmf_decode_aac_title)).setText
                (mToolKit
                        .getStringResource(R.string.audio_loopback_decode_aac));
        ((TextView) findViewById(R.id.audioloopback_dtmf_decode_wav_title)).setText
                (mToolKit
                        .getStringResource(R.string.audio_loopback_dtmf_decode_wav));
        btn_Start.setText(mToolKit.getStringResource(R.string.button_start));
        btn_Exit.setText(mToolKit.getStringResource(R.string.button_exit));
    }

    private void initialize() {
        // TODO Auto-generated method stub
//        mAudioLoopbackServiceIntent = new Intent(
//                WisAudioLoopback_Service.ACTION_AUDIOLOOPBACK_SERVICE);
        mAudioLoopbackServiceIntent = new Intent();
        mAudioLoopbackServiceIntent.setClass(AudioLoopback.this, AudioLoopbackService
                .class);
        et_DTMFAudio.setText(mAudioValue);
        et_DTMFAudio.setTextColor(Color.BLACK);

        if (isDTMFAudio) {
            if (isWCISTest) {
                setVolumeControlStream(AudioManager.STREAM_MUSIC);
            } else {
                setVolumeControlStream(AudioManager.STREAM_DTMF);
            }
        } else {
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
        }

        mAudioLoopbackLogList = new ArrayList<WisWCISSubLogItem>();

        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(WisAudioLoopback_Service.ACTION_AUDIOLOOPBACK_STATE_CHANGED);
        registerReceiver(audioLoopbackChanged, mFilter);
        isRegisteredBroadcast = true;
    }

    private void findView() {
        // TODO Auto-generated method stub
        et_DTMFAudio = (EditText) findViewById(R.id.audioloopback_dtmf_set);
        et_DTMFDecode = (EditText) findViewById(R.id
                .audioloopback_dtmf_decode_normal_value);
        et_DTMFDecode_MP3 = (EditText) findViewById(R.id
                .audioloopback_dtmf_decode_mp3_value);
        et_DTMFDecode_OGG = (EditText) findViewById(R.id
                .audioloopback_dtmf_decode_ogg_value);
        et_DTMFDecode_AAC = (EditText) findViewById(R.id
                .audioloopback_dtmf_decode_aac_value);
        et_DTMFDecode_WAV = (EditText) findViewById(R.id
                .audioloopback_dtmf_decode_wav_value);
        btn_Start = (Button) findViewById(R.id.button_state);
        btn_Exit = (Button) findViewById(R.id.button_exit);

        et_DTMFDecode.setTextColor(Color.BLACK);
        et_DTMFDecode_MP3.setTextColor(Color.BLACK);
        et_DTMFDecode_OGG.setTextColor(Color.BLACK);
        et_DTMFDecode_AAC.setTextColor(Color.BLACK);
        et_DTMFDecode_WAV.setTextColor(Color.BLACK);

        btn_Start.setOnClickListener(this);
        btn_Exit.setOnClickListener(this);

        if (!isWCISTest) {
            findViewById(R.id.audioloopback_decode_result).setVisibility(View.GONE);
        }
        if (!isComponentMode) {
            findViewById(R.id.audioloopback_button_group).setVisibility(View.INVISIBLE);
        }
    }

    private void updateView(boolean enable) {
        et_DTMFAudio.setEnabled(enable);
        et_DTMFDecode.setEnabled(enable);
        btn_Start.setEnabled(enable);
        btn_Exit.setEnabled(!enable);
    }

    private void updateResult() {
        ((TextView) findViewById(R.id.audioloopback_decode_result)).setText(String.format(
                mToolKit.getStringResource(R.string.audio_loopback_dtmf_result),
                mTestCycles,
                mPassCount, mFailCount));
    }

    private void updateLog(String log) {
        saveGeneralLog(log);

        mAudioLoopbackLog += log;
        ((WisAutoScrollTextView) findViewById(R.id.audioloopback_decode_result_log))
                .addText(log);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v == btn_Start) {
            updateView(false);
            updateResult();
//            mAudioLoopbackServiceIntent.putExtra(WisAudioLoopback_Service.EXTRA_IS_DTMF,
//                    isDTMFAudio);
//            mAudioLoopbackServiceIntent
//                    .putExtra(WisAudioLoopback_Service.EXTRA_IS_WCIS, isWCISTest);
//            mAudioLoopbackServiceIntent.putExtra("dtmfTypes", mWCISDTMFTestTypes);
//            mAudioLoopbackServiceIntent.putExtra(WisAudioLoopback_Service
// .EXTRA_TEST_QUEUE,
//                    mAudioValue);
//            mAudioLoopbackServiceIntent.putExtra(WisAudioLoopback_Service
// .EXTRA_NEED_MATCH_COUNT,
//                    mNeedMatchCount);
//            mAudioLoopbackServiceIntent.putExtra(WisAudioLoopback_Service
// .EXTRA_VOLUME, mCurVolume);
//            mAudioLoopbackServiceIntent.putExtra(WisAudioLoopback_Service
// .EXTRA_TEST_CYCLES,
//                    mTestCycles);
//            startService(mAudioLoopbackServiceIntent);

            registerHeadSet();
            initHeadsetTimer();
            defineHeadsetDialog();
        } else if (v == btn_Exit) {
            updateView(true);
            stopService(mAudioLoopbackServiceIntent);
            endTest();
        }
    }

    private BroadcastReceiver audioLoopbackChanged = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (intent.getAction().equals(
                    WisAudioLoopback_Service.ACTION_AUDIOLOOPBACK_STATE_CHANGED)) {
                int state = intent.getIntExtra(WisAudioLoopback_Service
                                .EXTRA_AUDIOLOOPBACK_STATE,
                        0);
                int type = 0;
                Log.i("Payne",""+state);
                switch (state) {
                    case WisAudioLoopback_Service.AUDIOLOOPBACK_STATE_PLAYER_ERROR:
                    case WisAudioLoopback_Service.AUDIOLOOPBACK_STATE_TO_EXIT:
                        btn_Exit.performClick();
                        break;
                    case WisAudioLoopback_Service.AUDIOLOOPBACK_STATE_ONE_CYCLE_START:
                        mCyclesIndex = intent.getIntExtra(
                                WisAudioLoopback_Service
                                        .EXTRA_AUDIOLOOPBACK_START_CYCLE_INDEX,
                                mCyclesIndex + 1);
                        mWCISTestRemark = "";
                        mDecodeValue = "";
                        et_DTMFDecode.setText(mDecodeValue);
                        et_DTMFDecode_MP3.setText(mDecodeValue);
                        et_DTMFDecode_OGG.setText(mDecodeValue);
                        et_DTMFDecode_AAC.setText(mDecodeValue);
                        et_DTMFDecode_WAV.setText(mDecodeValue);
                        updateLog(String.format(mToolKit
                                        .getStringResource(R.string
                                                .audio_loopback_log_cycle_index_start),
                                mCyclesIndex));
                        updateLog(String.format(
                                mToolKit.getStringResource(R.string
                                        .audio_loopback_log_test_queue),
                                mAudioValue));
                        addWCISRemark(String.format(
                                getString(R.string.audio_loopback_remark_test_queue),
                                mAudioValue));
                        break;
                    case WisAudioLoopback_Service.AUDIOLOOPBACK_STATE_UPDATE_DECODE_VALUE:
                        type = intent.getIntExtra("curType", 0);
                        mDecodeValue = intent
                                .getStringExtra(WisAudioLoopback_Service
                                        .EXTRA_AUDIOLOOPBACK_DECODE_VALUE);
                        Log.i("Payne","Type="+type);
                        switch (type) {
                            case 0:
                                et_DTMFDecode.setText(mDecodeValue);
                                break;
                            case AudioLoopbackService.WCIS_DTMF_TYPES_MP3:
                                et_DTMFDecode_MP3.setText(mDecodeValue);
                                break;
                            case AudioLoopbackService.WCIS_DTMF_TYPES_OGG:
                                et_DTMFDecode_OGG.setText(mDecodeValue);
                                break;
                            case AudioLoopbackService.WCIS_DTMF_TYPES_AAC:
                                et_DTMFDecode_AAC.setText(mDecodeValue);
                                break;
                            case AudioLoopbackService.WCIS_DTMF_TYPES_WAV:
                                et_DTMFDecode_WAV.setText(mDecodeValue);
                                break;
                            default:
                                break;
                        }
                        break;
                    case 10:
                        type = intent.getIntExtra("curType", 0);
                        Log.i("Payne","Type="+type);
                        switch (type) {
                            case 0:
                                addWCISRemark(String.format(
                                        getString(R.string
                                                .audio_loopback_remark_decode_queue),
                                        mDecodeValue));
                                updateLog(String.format(mToolKit
                                                .getStringResource(R.string
                                                        .audio_loopback_log_decode_queue),
                                        mDecodeValue));
                                break;
                            case AudioLoopbackService.WCIS_DTMF_TYPES_MP3:
                                addWCISRemark(String.format(
                                        getString(R.string
                                                .audio_loopback_remark_decode_queue_mp3),
                                        mDecodeValue));
                                updateLog(String.format(mToolKit
                                                .getStringResource(R.string
                                                        .audio_loopback_log_decode_queue_mp3),
                                        mDecodeValue));
                                break;
                            case AudioLoopbackService.WCIS_DTMF_TYPES_OGG:
                                addWCISRemark(String.format(
                                        getString(R.string
                                                .audio_loopback_remark_decode_queue_ogg),
                                        mDecodeValue));
                                updateLog(String.format(mToolKit
                                                .getStringResource(R.string
                                                        .audio_loopback_log_decode_queue_ogg),
                                        mDecodeValue));
                                break;
                            case AudioLoopbackService.WCIS_DTMF_TYPES_AAC:
                                addWCISRemark(String.format(
                                        getString(R.string
                                                .audio_loopback_remark_decode_queue_aac),
                                        mDecodeValue));
                                updateLog(String.format(mToolKit
                                                .getStringResource(R.string
                                                        .audio_loopback_log_decode_queue_aac),
                                        mDecodeValue));
                                break;
                            case AudioLoopbackService.WCIS_DTMF_TYPES_WAV:
                                addWCISRemark(String.format(
                                        getString(R.string
                                                .audio_loopback_remark_decode_queue_wav),
                                        mDecodeValue));
                                updateLog(String.format(mToolKit
                                                .getStringResource(R.string
                                                        .audio_loopback_log_decode_queue_wav),
                                        mDecodeValue));
                                break;
                            default:
                                break;
                        }
                        break;
                    case WisAudioLoopback_Service.AUDIOLOOPBACK_STATE_ONE_CYCLE_DONE:
                        boolean isTempPass = intent.getBooleanExtra(
                                WisAudioLoopback_Service
                                        .EXTRA_AUDIOLOOPBACK_CYCLE_DONE_RESULT, false);
                        if (isTempPass) {
                            mPassCount++;
                        } else {
                            mFailCount++;
                        }
                        updateResult();
                        mAudioLoopbackLogList.add(new WisWCISSubLogItem(isTempPass,
                                mWCISTestRemark));
                        updateLog(String.format(
                                mToolKit.getStringResource(R.string
                                        .audio_loopback_log_cycle_result),
                                isTempPass));
                        break;
                    default:
                        break;
                }
            }
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

    private void saveGeneralLog(String fileContent) {
        if (isWCISTest) {
            Intent intent = new Intent(WisWCISCommonConst.ACTION_WCIS_WRITE_LOG);
            intent.putExtra(WisWCISCommonConst.EXTRA_LOG_FILE_NAME,
                    mWCISAudioLoopbackGeneralLogFilePath);
            intent.putExtra(WisWCISCommonConst.EXTRA_LOG_FILE_CONTENT, fileContent);
            intent.putExtra(WisWCISCommonConst.EXTRA_LOG_TYPE,
                    WisWCISCommonConst.LOG_TYPE_GENERAL_LOG);
            sendBroadcast(intent);
        } else {
            try {
                mLogHandler.write(fileContent, true);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void saveHtmlLog() {
        WisSubHtmlBuilder mHtmlBuilder = new WisSubHtmlBuilder(this);
        mHtmlBuilder.makeTitle(getString(R.string.audio_loopback_test_title));
        mHtmlBuilder.date();
        mHtmlBuilder.startTable();
        for (int i = 0; i < mAudioLoopbackLogList.size(); i++) {
            WisWCISSubLogItem log = mAudioLoopbackLogList.get(i);
            mHtmlBuilder.addTableItem(i + 1, log.isPass(), log.getRemark());
        }
        mHtmlBuilder.endTable();
        Intent intent = new Intent(WisWCISCommonConst.ACTION_WCIS_WRITE_LOG);
        intent.putExtra(WisWCISCommonConst.EXTRA_LOG_FILE_NAME,
                mWCISAudioLoopbackHtmlLogFilePath);
        intent.putExtra(WisWCISCommonConst.EXTRA_LOG_FILE_CONTENT, mHtmlBuilder
                .getResult());
        intent.putExtra(WisWCISCommonConst.EXTRA_LOG_TYPE, WisWCISCommonConst
                .LOG_TYPE_HTML_LOG);
        sendBroadcast(intent);
    }

    private void addWCISRemark(String remark) {
        if (isWCISTest) {
            mWCISTestRemark += remark;
        }
    }

    private void defineHeadsetPullDialog() {
        mHeadsetPullDialog = new AlertDialog.Builder(this).create();
        mHeadsetPullDialog.setCancelable(false);
        mHeadsetPullDialog.setCanceledOnTouchOutside(false);
        mHeadsetPullDialog.setMessage(String.format(
                mToolKit.getStringResource(R.string.headset_loopback_pull_end_test),
                mHeadsetTimeout - pullTimes));
        mHeadsetPullDialog.show();
    }

    private void initPullHeadsetTimer() {
        pullHeadsetTimer = new Timer();
        pullHeadsetTask = new TimerTask() {
            @Override
            public void run() {
                pullTimes++;
                if (pullTimes >= mHeadsetTimeout) {
                    pullHeadsetHandler.sendEmptyMessage(2);
                } else {
                    pullHeadsetHandler.sendEmptyMessage(1);
                }
            }
        };
        pullHeadsetTimer.schedule(pullHeadsetTask, 1000, 1000);
    }

    private void cancelPullHeadsetTimer() {
        if (pullHeadsetTimer != null) {
            pullHeadsetTimer.cancel();
            pullHeadsetTimer = null;
        }
        if (pullHeadsetTask != null) {
            pullHeadsetTask.cancel();
            pullHeadsetTask = null;
        }
    }

    private Handler pullHeadsetHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    mHeadsetPullDialog.setMessage(String.format(
                            mToolKit.getStringResource(R.string.headset_loopback_pull_end_test),
                            mHeadsetTimeout - pullTimes));
                    break;
                case 2:
                    cancelPullHeadsetTimer();
                    if (mHeadsetPullDialog != null && mHeadsetPullDialog.isShowing()) {
                        mHeadsetPullDialog.dismiss();
                    }
                    backToMain();
                    break;
                case 3:
                    cancelPullHeadsetTimer();
                    if (mHeadsetPullDialog != null && mHeadsetPullDialog.isShowing()) {
                        mHeadsetPullDialog.dismiss();
                    }
                    backToMain();
                    break;
            }
            return false;
        }
    });

    private Timer pullHeadsetTimer = null;
    private TimerTask pullHeadsetTask = null;
    private int pullTimes = 0;
    private boolean isPass = false;
    private boolean isEndTest = false;

    private void endTest() {
        boolean result = mPassCount >= mTestCycles && mFailCount <= 0;
        result = result && isHeasePluginStatus;

        if (!isHeadsetPluginTimeout) {
            isPass = result;
            isEndTest = true;
            defineHeadsetPullDialog();
            initPullHeadsetTimer();
        } else {
            backToMain();
        }
    }

    private void backToMain() {
        if (isWCISTest) {
            saveHtmlLog();
            mToolKit.returnWithResultAndRemark(isPass, String.format(
                    getString(R.string.wcis_remark_format), mTestCycles, mPassCount,
                    mFailCount));
        } else {
            if (isComponentMode) {
                if (isPass) {
                    Toast.makeText(this, mToolKit.getStringResource(R.string.pass),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, mToolKit.getStringResource(R.string.fail),
                            Toast.LENGTH_SHORT).show();
                }
            }
            Toast.makeText(this, "headset plugin status: " + isHeasePluginStatus, Toast
                    .LENGTH_SHORT).show();
            mToolKit.returnWithResult(isPass);
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (isRegisteredBroadcast) {
            unregisterReceiver(audioLoopbackChanged);
            isRegisteredBroadcast = false;
        }

        if (isRegisterBroadcast) {
            unregisterReceiver(headsetReceiver);
        }
    }
}