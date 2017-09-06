package com.wistron.generic.audio.loopback;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.wistron.generic.pqaa.R;
import com.wistron.pqaa_common.jar.audio.Tone;
import com.wistron.pqaa_common.jar.audio.WisAudioDecode;
import com.wistron.pqaa_common.jar.audio.WisAudioDecode.OnAudioDecodeListener;
import com.wistron.pqaa_common.jar.global.WisCommonConst;
import com.wistron.pqaa_common.jar.global.WisLog;
import com.wistron.pqaa_common.jar.global.WisParseValue;
import com.wistron.pqaa_common.jar.global.WisToolKit;
import com.wistron.pqaa_common.jar.wcis.WisSubHtmlBuilder;
import com.wistron.pqaa_common.jar.wcis.WisWCISCommonConst;
import com.wistron.pqaa_common.jar.wcis.WisWCISSubLogItem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

//import com.wistron.pqaa_common.jar.global.WisTextViewVerticalScrollHelper;

public class AudioLoopback extends Activity implements OnClickListener, OnAudioDecodeListener {
    // WCIS options
    private String mAudioLoopbackConfigFileName = "audio_loopback.cfg";
    private String AUDIOLOOPBACK_CONFIG_FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + mAudioLoopbackConfigFileName;
    private String mWCISAudioLoopbackGeneralLogFileName = "audio_loopback_log.txt";
    private String mWCISAudioLoopbackHtmlLogFileName = "audio_loopback_html.html";
    private String mWCISAudioLoopbackGeneralLogFilePath;
    private String mWCISAudioLoopbackHtmlLogFilePath;

    private static final String AUDIOLOOPBACK_LOG_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "wistron/audio_loopback.txt";

    private final int MSG_UPDATE_RESULT = 1;
    private final String AUDIO_CONFIG_PATH = "/mnt/sdcard/pqaa_config/audio_loopback.cfg";

    private EditText et_DTMFAudio, et_DTMFDecode;
    private Button btn_Start, btn_Exit;
    // DTMF: 057A*#81
    // Custom: 0123
    private String mAudioValue = "0123456789*#", mDecodeValue = "";
    private char mCurValue;
    private int mNeedMatchCount = mAudioValue.length(), mMatchCount = 0;
    private boolean isMatched = false;
    private boolean isDTMFAudio = true;
    private int mTestCycles = 1, mCyclesIndex = 0;

    private boolean isWCISTest = false;
    private boolean isComponentMode = true;
    private String mAudioLoopbackLog = "";
    private String mWCISTestRemark = "";
    private ArrayList<WisWCISSubLogItem> mAudioLoopbackLogList;
    private int mPassCount, mFailCount;

    private WisAudioDecode mAudioDecoder;
    private ToneGenerator mGenerator;
    private MediaPlayer mPlayer;
    private ArrayList<Tone> mCustomFrequencyList; // For not DTMF tone test
    private int mLastTime = 500;
    private int mSleepTime = 1000;

    private AudioManager mManager;
    private int mOriginalVolume, mCurVolume = 10;

    private WisToolKit mToolKit;
//	private WisTextViewVerticalScrollHelper mVerticalScrollHelper;

    // Log class
    private WisLog mLogHandler;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.audio_loopback);

        mToolKit = new WisToolKit(this);
        getTestArguments();
        findView();
        if (!isWCISTest) {
            try {
                mLogHandler = new WisLog(AUDIOLOOPBACK_LOG_PATH);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            doCreate();
        }
        setViewByLanguage();
    }

    private void doCreate() {
        // TODO Auto-generated method stub
        initialize();
        if (!isComponentMode) {
            btn_Start.performClick();
        }
    }

    private void getTestArguments() {
        // TODO Auto-generated method stub
        String mTestStyle = mToolKit.getCurrentTestType();
        if (mTestStyle != null) {
            if (mTestStyle.equals(WisCommonConst.TESTSTYLE_SAVED_CONFIG)) {
                isWCISTest = getIntent().getBooleanExtra(WisCommonConst.EXTRA_IS_WCIS_TEST, false);
                isComponentMode = false;
                if (isWCISTest) {
                    AUDIOLOOPBACK_CONFIG_FILE_PATH = getIntent().getStringExtra(WisWCISCommonConst.EXTRA_CONFIG_FOLDER) + mAudioLoopbackConfigFileName;
                    mWCISAudioLoopbackGeneralLogFilePath = getIntent().getStringExtra(WisWCISCommonConst.EXTRA_LOG_FOLDER) + mWCISAudioLoopbackGeneralLogFileName;
                    mWCISAudioLoopbackHtmlLogFilePath = getIntent().getStringExtra(WisWCISCommonConst.EXTRA_LOG_FOLDER) + mWCISAudioLoopbackHtmlLogFileName;

                    registerReceiver(readConfigReceiver, new IntentFilter(WisWCISCommonConst.ACTION_WCIS_FEEDBACK_CONFIG));

                    Intent getConfigIntent = new Intent(WisWCISCommonConst.ACTION_WCIS_READ_CONFIG);
                    getConfigIntent.putExtra(WisWCISCommonConst.EXTRA_CONFIG_PATH, AUDIOLOOPBACK_CONFIG_FILE_PATH);
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
            Map<String, String> mParametersList = (Map<String, String>) intent.getSerializableExtra(WisWCISCommonConst.EXTRA_CONFIG_CONTENT);
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
                }
            }
        } else {
            Toast.makeText(this, mToolKit.getStringResource(R.string.noconfigfile_msg), Toast.LENGTH_SHORT).show();
        }
    }

    private void setSystemVolume(int volume) {
        // TODO Auto-generated method stub
        if (isDTMFAudio) {
            mManager.setStreamVolume(AudioManager.STREAM_DTMF, volume, AudioManager.FLAG_SHOW_UI);
        } else {
            mManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_SHOW_UI);
        }
    }

    private void getOriginalVolume() {
        // TODO Auto-generated method stub
        if (isDTMFAudio) {
            mOriginalVolume = mManager.getStreamVolume(AudioManager.STREAM_DTMF);
        } else {
            mOriginalVolume = mManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        }
    }

    private void setViewByLanguage() {
        // TODO Auto-generated method stub
        ((TextView) findViewById(R.id.item_title)).setText(mToolKit.getStringResource(R.string.audio_loopback_test_title));
        ((TextView) findViewById(R.id.audioloopback_title_dtmf_set)).setText(mToolKit.getStringResource(R.string.audio_loopback_dtmf_set));
        ((TextView) findViewById(R.id.audioloopback_title_dtmf_decode)).setText(mToolKit.getStringResource(R.string.audio_loopback_dtmf_decode));
        btn_Start.setText(mToolKit.getStringResource(R.string.button_start));
        btn_Exit.setText(mToolKit.getStringResource(R.string.button_exit));
    }

    private void initialize() {
        // TODO Auto-generated method stub
        mAudioDecoder = new WisAudioDecode(this);
        mAudioDecoder.setOnAudioDecodeListener(this);

        mManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mGenerator = new ToneGenerator(AudioManager.STREAM_DTMF, ToneGenerator.MAX_VOLUME);
        if (isDTMFAudio) {
            setVolumeControlStream(AudioManager.STREAM_DTMF);
        } else {
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
        }

        try {
            mPlayer = new MediaPlayer();
            mPlayer.setLooping(false);
        } catch (Exception e) {
            e.toString();
        }
        mPlayer.setOnErrorListener(new OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                // TODO Auto-generated method stub
                btn_Exit.performClick();
                return false;
            }

        });

        et_DTMFAudio.setText(mAudioValue);

        getOriginalVolume();
        setSystemVolume(mCurVolume);

        mAudioLoopbackLogList = new ArrayList<WisWCISSubLogItem>();

        mCustomFrequencyList = new ArrayList<Tone>();
//		mCustomFrequencyList.add(new Tone(32, 93, '0'));
//		mCustomFrequencyList.add(new Tone(63, 64, '1'));
//		mCustomFrequencyList.add(new Tone(63, 129, '2'));
//		mCustomFrequencyList.add(new Tone(55, 129, '3'));
        mCustomFrequencyList.add(new Tone(40, 93, '0'));
        mCustomFrequencyList.add(new Tone(64, 65, '1'));
        mCustomFrequencyList.add(new Tone(64, 109, '2'));
        mCustomFrequencyList.add(new Tone(55, 129, '3'));
    }

    private void findView() {
        // TODO Auto-generated method stub
        et_DTMFAudio = (EditText) findViewById(R.id.audioloopback_dtmf_set);
        et_DTMFDecode = (EditText) findViewById(R.id.audioloopback_dtmf_decode);
        btn_Start = (Button) findViewById(R.id.button_state);
        btn_Exit = (Button) findViewById(R.id.button_exit);

        btn_Start.setOnClickListener(this);
        btn_Exit.setOnClickListener(this);

        if (!isWCISTest) {
            findViewById(R.id.audioloopback_decode_result).setVisibility(View.GONE);
        }
        if (!isComponentMode) {
            findViewById(R.id.audioloopback_button_group).setVisibility(View.INVISIBLE);
        }
    }

    private void start() {
        mCyclesIndex++;
        btn_Start.setEnabled(false);
        mDecodeValue = "";
        mMatchCount = 0;
        mWCISTestRemark = "";
        et_DTMFDecode.setText(mDecodeValue);
        updateLog(String.format(mToolKit.getStringResource(R.string.audio_loopback_log_cycle_index_start), mCyclesIndex));
        updateLog(String.format(mToolKit.getStringResource(R.string.audio_loopback_log_test_queue), mAudioValue));
        addWCISRemark(String.format(getString(R.string.audio_loopback_remark_test_queue), mAudioValue));
        new Thread(startPlay).start();
    }

    private void setPlayer(AssetFileDescriptor mDescriptor) {
        try {
            mPlayer.reset();
            mPlayer.setDataSource(mDescriptor.getFileDescriptor(), mDescriptor.getStartOffset(),
                    mDescriptor.getLength());
            mPlayer.prepare();
            mPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Runnable startPlay = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            int mTone = 0;
            while (mTone < mAudioValue.length() && mAudioDecoder.isStarted()) {
                try {
                    Thread.sleep(mSleepTime);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    break;
                }
                mCurValue = mAudioValue.charAt(mTone);
                isMatched = false;
                if (isDTMFAudio) {
                    if (mAudioDecoder.isStarted()) {
                        switch (mCurValue) {
                            case '0':
                                mGenerator.startTone(ToneGenerator.TONE_DTMF_0, mLastTime);
                                break;
                            case '1':
                                mGenerator.startTone(ToneGenerator.TONE_DTMF_1, mLastTime);
                                break;
                            case '2':
                                mGenerator.startTone(ToneGenerator.TONE_DTMF_2, mLastTime);
                                break;
                            case '3':
                                mGenerator.startTone(ToneGenerator.TONE_DTMF_3, mLastTime);
                                break;
                            case '4':
                                mGenerator.startTone(ToneGenerator.TONE_DTMF_4, mLastTime);
                                break;
                            case '5':
                                mGenerator.startTone(ToneGenerator.TONE_DTMF_5, mLastTime);
                                break;
                            case '6':
                                mGenerator.startTone(ToneGenerator.TONE_DTMF_6, mLastTime);
                                break;
                            case '7':
                                mGenerator.startTone(ToneGenerator.TONE_DTMF_7, mLastTime);
                                break;
                            case '8':
                                mGenerator.startTone(ToneGenerator.TONE_DTMF_8, mLastTime);
                                break;
                            case '9':
                                mGenerator.startTone(ToneGenerator.TONE_DTMF_9, mLastTime);
                                break;
                            case '*':
                                mGenerator.startTone(ToneGenerator.TONE_DTMF_S, mLastTime);
                                break;
                            case '#':
                                mGenerator.startTone(ToneGenerator.TONE_DTMF_P, mLastTime);
                                break;
                            case 'A':
                                mGenerator.startTone(ToneGenerator.TONE_DTMF_A, mLastTime);
                                break;
                            case 'B':
                                mGenerator.startTone(ToneGenerator.TONE_DTMF_B, mLastTime);
                                break;
                            case 'C':
                                mGenerator.startTone(ToneGenerator.TONE_DTMF_C, mLastTime);
                                break;
                            case 'D':
                                mGenerator.startTone(ToneGenerator.TONE_DTMF_D, mLastTime);
                                break;
                            default:
                                break;
                        }
                    }
                } else {
                    if (mAudioDecoder.isStarted()) {
                        try {
                            switch (mCurValue) {
                                case '0':
                                    setPlayer(getAssets().openFd("500_3K_ALL.mp3"));
                                    break;
                                case '1':
                                    setPlayer(getAssets().openFd("1K_4K_ALL.mp3"));
                                    break;
                                case '2':
                                    setPlayer(getAssets().openFd("2K_5K_ALL.mp3"));
                                    break;
                                case '3':
                                    setPlayer(getAssets().openFd("2K_6K_ALL.mp3"));
                                    break;
                                default:
                                    break;
                            }
                        } catch (IOException e) {
                            // TODO: handle exception
                        }
                    }
                }
                mTone++;
            }
            try {
                Thread.sleep(mSleepTime);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            handler.sendEmptyMessage(MSG_UPDATE_RESULT);
        }
    };
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            if (msg.what == MSG_UPDATE_RESULT) {
                boolean isTempPass = mMatchCount >= mNeedMatchCount;
                if (isTempPass) {
                    mPassCount++;
                } else {
                    mFailCount++;
                }
                updateResult();
                addWCISRemark(String.format(getString(R.string.audio_loopback_remark_decode_queue), mDecodeValue));
                mAudioLoopbackLogList.add(new WisWCISSubLogItem(isTempPass, mWCISTestRemark));
                updateLog(String.format(mToolKit.getStringResource(R.string.audio_loopback_log_decode_queue), mDecodeValue));
                updateLog(String.format(mToolKit.getStringResource(R.string.audio_loopback_log_cycle_result), isTempPass));
                if (mCyclesIndex >= mTestCycles) {
                    btn_Exit.performClick();
                } else {
                    start();
                }
            }
        }

    };

    private void updateView(boolean enable) {
        et_DTMFAudio.setEnabled(enable);
        et_DTMFDecode.setEnabled(enable);
        btn_Start.setEnabled(enable);
        btn_Exit.setEnabled(!enable);
    }

    private void updateResult() {
        ((TextView) findViewById(R.id.audioloopback_decode_result)).setText(String.format(mToolKit.getStringResource(R.string.audio_loopback_dtmf_result), mTestCycles, mPassCount, mFailCount));
    }

    private void updateLog(String log) {
        saveGeneralLog(log);

        mAudioLoopbackLog += log;
        ((TextView) findViewById(R.id.audioloopback_decode_result_log)).setText(mAudioLoopbackLog);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v == btn_Start) {
            updateView(false);
            updateResult();
            mAudioDecoder.start();
            start();
        } else if (v == btn_Exit) {
            updateView(true);
            if (mPlayer != null && mPlayer.isPlaying()) {
                mPlayer.stop();
            }
            mAudioDecoder.stop();
            endTest();
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
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        setSystemVolume(mOriginalVolume);
    }

    private void saveGeneralLog(String fileContent) {
        if (isWCISTest) {
            Intent intent = new Intent(WisWCISCommonConst.ACTION_WCIS_WRITE_LOG);
            intent.putExtra(WisWCISCommonConst.EXTRA_LOG_FILE_NAME, mWCISAudioLoopbackGeneralLogFilePath);
            intent.putExtra(WisWCISCommonConst.EXTRA_LOG_FILE_CONTENT, fileContent);
            intent.putExtra(WisWCISCommonConst.EXTRA_LOG_TYPE, WisWCISCommonConst.LOG_TYPE_GENERAL_LOG);
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
        intent.putExtra(WisWCISCommonConst.EXTRA_LOG_FILE_NAME, mWCISAudioLoopbackHtmlLogFilePath);
        intent.putExtra(WisWCISCommonConst.EXTRA_LOG_FILE_CONTENT, mHtmlBuilder.getResult());
        intent.putExtra(WisWCISCommonConst.EXTRA_LOG_TYPE, WisWCISCommonConst.LOG_TYPE_HTML_LOG);
        sendBroadcast(intent);
    }

    private void addWCISRemark(String remark) {
        if (isWCISTest) {
            mWCISTestRemark += remark;
        }
    }

    private void endTest() {
        if (isWCISTest) {
            saveHtmlLog();
            mToolKit.returnWithResultAndRemark(mFailCount <= 0, String.format(getString(R.string.wcis_remark_format), mTestCycles, mPassCount, mFailCount));
        } else {
            if (isComponentMode) {
                if (mFailCount <= 0) {
                    Toast.makeText(this, mToolKit.getStringResource(R.string.pass), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, mToolKit.getStringResource(R.string.fail), Toast.LENGTH_SHORT).show();
                }
            }
            mToolKit.returnWithResult(mFailCount <= 0);
        }
    }

    @Override
    public void onAudioDecode(int low, int high, double decibel) {
        // TODO Auto-generated method stub
        int lowFrequency = low;
        int highFrequency = high;

        char mTone = 0;
        if (isDTMFAudio) {
            mTone = mAudioDecoder.decodeDTMFToneWithFrequency(lowFrequency, highFrequency);
        } else {
            for (Tone t : mCustomFrequencyList) {
                if (t.match(lowFrequency, highFrequency)) {
                    mTone = t.getKey();
                }
            }
        }
        if (mTone != 0 && mTone != ' ') {
            if (!isMatched) {
                mDecodeValue += mTone;
                et_DTMFDecode.setText(mDecodeValue);
            }
            if (mTone == mCurValue && !isMatched) {
                mMatchCount++;
                isMatched = true;
            }
            Log.i("AudioLoopback", lowFrequency + " - " + highFrequency + " , " + mTone);
        }
    }
}