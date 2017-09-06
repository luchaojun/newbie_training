package com.wistron.generic.audio;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wistron.generic.pqaa.R;
import com.wistron.pqaa_common.jar.global.WisAlertDialog;
import com.wistron.pqaa_common.jar.global.WisCommonConst;
import com.wistron.pqaa_common.jar.global.WisParseValue;
import com.wistron.pqaa_common.jar.global.WisToolKit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Audio extends Activity implements OnClickListener {
    private final String TEST_AUDIO = "Audio";
    private final String TEST_HEADSET = "Headset";
    private int mCurrentLanguage;

    private final byte MSG_START_RECORD = 0;
    private final byte MSG_STOP_RECORD = 1;
    private final byte MSG_PLAY_RECORD = 2;
    private final byte MSG_PLAY_END = 3;

    private final byte TRACK_LEFT = 0;
    private final byte TRACK_RIGHT = 1;

    private final int ITEM_STEREO = 1;
    private final int ITEM_TRACK = 2;
    private final int ITEM_RECEIVER = 4;
    private final int ITEM_RECORD = 8;

    private static final int INDEX_STEREO = 0;
    private static final int INDEX_TRACK = 1;
    private static final int INDEX_RECEIVER = 2;
    private static final int INDEX_RECORD = 3;

    private final int MUSIC_FIRST = 1;
    private final int MUSIC_SECOND = 2;
    private final int MUSIC_THIRD = 3;
    private final int MUSIC_FOURTH = 4;
    private final int MUSIC_FIFTH = 5;

    private final int DEFAULT_RECORD_TIME = 5;
    // -----------------------main------------------
    private LinearLayout mTimeLayout, mTestLayout;
    private RelativeLayout mButtonLayout;
    private CheckBox cbStereo, cbTrack, cbReceiver, cbRecord;
    private ProgressBar pRecordBar;
    private Button btn_start, btn_exit;
    private AudioManager mManager;
    private int mOriginalVolume, mCurVolume = 6; /* 0~15 */
    // ------------------variables---------------//
    private int mTrackType = -1;

    private final String strTempFile = "audio";
    private File myRecAudioFile;
    private boolean isStartRec = false;

    private int mSubItem = 13;
    private int mCurIndex = 0;
    private int mRecordTime;
    // ------------------service-----------------//
    private MediaPlayer mPlayer;
    private MediaRecorder mMediaRecorder;
    // ----------------------prevent stay-------------
    private WisAlertDialog mPromptDialog;
    private AlertDialog mSelectDialog;
    private WisAlertDialog mManualMicResultDialog;
    private Button mFirstMusic, mSecondMusic, mThirdMusic, mFourthMusic, mFifthMusic;
    private int mCurMusicIndex = 1;
    private String mCurTestItem = TEST_AUDIO;
    // -----------------------headset///////////////
    private final int MSG_TIME = 4;
    private final int MSG_TIMEOUT = 5;
    private TextView tvRemainTime, tvTotalTime;
    private Timer mTimer = new Timer();
    private TimerTask mTask;
    private int mTimes = 0;
    private int mPluginState = -1;
    private boolean isRegisterBroadcast = false;
    private int TIMEOUT = 15; // seconds

    // common tool kit
    private WisToolKit mToolKit;

    private void startPlayRecording() {
        try {
            mPlayer.reset();
//			mPlayer.setDataSource(myRecAudioFile.getAbsolutePath());
            File file = new File(myRecAudioFile.getAbsolutePath());
            FileInputStream inputStream = new FileInputStream(file);
            mPlayer.setDataSource(inputStream.getFD());
            mPlayer.prepare();
            mPlayer.start();
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            releaseResource(false);
        }
    }

    private boolean startRec() {
        try {
            if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
                Log.i("Tag", "begin to record");
                myRecAudioFile = File.createTempFile(strTempFile, ".amr", Environment.getExternalStorageDirectory());

                mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
                mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                mMediaRecorder.setOutputFile(myRecAudioFile.getAbsolutePath());
                mMediaRecorder.setMaxDuration(mRecordTime * 1000);
                mMediaRecorder.prepare();
                mMediaRecorder.start();

                isStartRec = true;
                Log.i("MediaRecorder", myRecAudioFile.getAbsolutePath());
            } else {
                Toast.makeText(this, mToolKit.getStringResource(R.string.audio_nosdcard), Toast.LENGTH_SHORT).show();
                isStartRec = false;
            }
        } catch (Exception e) {
            Log.i("Tag", "error----------");
            isStartRec = false;
            mMediaRecorder.reset();
            e.printStackTrace();
        }
        return isStartRec;
    }

    private void stopRec() {
        try {
            if (isStartRec) {
                Log.i("Tag", "stop Rec");
                mMediaRecorder.stop();
                isStartRec = false;
                Message msg = new Message();
                msg.what = MSG_STOP_RECORD;
                myHandler.sendMessage(msg);
            }
        } catch (Exception e) {
            e.toString();
            releaseResource(false);
        }
    }

    private void playStereo() {
        // TODO Auto-generated method stub
        try {
            setRandom();
            AssetFileDescriptor mDescriptor = null;
            if (mCurrentLanguage == 1) {
                switch (mCurMusicIndex) {
                    case MUSIC_FIRST:
                        mDescriptor = getAssets().openFd("stereo1.wav");
                        break;
                    case MUSIC_SECOND:
                        mDescriptor = getAssets().openFd("stereo2.wav");
                        break;
                    case MUSIC_THIRD:
                        mDescriptor = getAssets().openFd("stereo3.wav");
                        break;
                    case MUSIC_FOURTH:
                        mDescriptor = getAssets().openFd("stereo4.wav");
                        break;
                    case MUSIC_FIFTH:
                        mDescriptor = getAssets().openFd("stereo5.wav");
                        break;
                    default:
                        mDescriptor = getAssets().openFd("stereo1.wav");
                        break;
                }
            } else {
                switch (mCurMusicIndex) {
                    case MUSIC_FIRST:
                        mDescriptor = getAssets().openFd("Record_12341234.wav");
                        break;
                    case MUSIC_SECOND:
                        mDescriptor = getAssets().openFd("Record_5678.wav");
                        break;
                    case MUSIC_THIRD:
                        mDescriptor = getAssets().openFd("Record_abcd.wav");
                        break;
                    case MUSIC_FOURTH:
                        mDescriptor = getAssets().openFd("Record_defg.wav");
                        break;
                    case MUSIC_FIFTH:
                        mDescriptor = getAssets().openFd("Record_wxyz.wav");
                        break;
                    default:
                        mDescriptor = getAssets().openFd("Record_12341234.wav");
                        break;
                }
            }
            if (mCurIndex == INDEX_STEREO) {
                findViewById(R.id.audio_tvstereo).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.audio_tvstereo)).setText(mToolKit.getStringResource(R.string.audio_playing));
            } else {
                findViewById(R.id.audio_tvreceiver).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.audio_tvreceiver)).setText(mToolKit.getStringResource(R.string.audio_playing));
            }
            setPlayer(mDescriptor);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void playTrack() {
        try {
            setRandom();
            AssetFileDescriptor mDescriptor = null;
            if (mTrackType == TRACK_LEFT) {
                if (mCurrentLanguage == 1) {
                    switch (mCurMusicIndex) {
                        case MUSIC_FIRST:
                            mDescriptor = getAssets().openFd("track1_l.wav");
                            break;
                        case MUSIC_SECOND:
                            mDescriptor = getAssets().openFd("track2_l.wav");
                            break;
                        case MUSIC_THIRD:
                            mDescriptor = getAssets().openFd("track3_l.wav");
                            break;
                        case MUSIC_FOURTH:
                            mDescriptor = getAssets().openFd("track4_l.wav");
                            break;
                        case MUSIC_FIFTH:
                            mDescriptor = getAssets().openFd("track5_l.wav");
                            break;
                        default:
                            mDescriptor = getAssets().openFd("track1_l.wav");
                            break;
                    }
                } else {
                    switch (mCurMusicIndex) {
                        case MUSIC_FIRST:
                            mDescriptor = getAssets().openFd("Record_12341234_left.wav");
                            break;
                        case MUSIC_SECOND:
                            mDescriptor = getAssets().openFd("Record_5678_left.wav");
                            break;
                        case MUSIC_THIRD:
                            mDescriptor = getAssets().openFd("Record_abcd_left.wav");
                            break;
                        case MUSIC_FOURTH:
                            mDescriptor = getAssets().openFd("Record_defg_left.wav");
                            break;
                        case MUSIC_FIFTH:
                            mDescriptor = getAssets().openFd("Record_wxyz_left.wav");
                            break;
                        default:
                            mDescriptor = getAssets().openFd("Record_12341234_left.wav");
                            break;
                    }
                }
                findViewById(R.id.audio_ivtrack).setVisibility(View.VISIBLE);
                findViewById(R.id.audio_tvtrack).setVisibility(View.VISIBLE);
                ((ImageView) findViewById(R.id.audio_ivtrack)).setImageResource(R.drawable.audio_ch_l);
                ((TextView) findViewById(R.id.audio_tvtrack)).setText(mToolKit.getStringResource(R.string.audio_lefttrack));
            } else if (mTrackType == TRACK_RIGHT) {
                if (mCurrentLanguage == 1) {
                    switch (mCurMusicIndex) {
                        case MUSIC_FIRST:
                            mDescriptor = getAssets().openFd("track1_r.wav");
                            break;
                        case MUSIC_SECOND:
                            mDescriptor = getAssets().openFd("track2_r.wav");
                            break;
                        case MUSIC_THIRD:
                            mDescriptor = getAssets().openFd("track3_r.wav");
                            break;
                        case MUSIC_FOURTH:
                            mDescriptor = getAssets().openFd("track4_r.wav");
                            break;
                        case MUSIC_FIFTH:
                            mDescriptor = getAssets().openFd("track5_r.wav");
                            break;
                        default:
                            mDescriptor = getAssets().openFd("track1_r.wav");
                            break;
                    }
                } else {
                    switch (mCurMusicIndex) {
                        case MUSIC_FIRST:
                            mDescriptor = getAssets().openFd("Record_12341234_right.wav");
                            break;
                        case MUSIC_SECOND:
                            mDescriptor = getAssets().openFd("Record_5678_right.wav");
                            break;
                        case MUSIC_THIRD:
                            mDescriptor = getAssets().openFd("Record_abcd_right.wav");
                            break;
                        case MUSIC_FOURTH:
                            mDescriptor = getAssets().openFd("Record_defg_right.wav");
                            break;
                        case MUSIC_FIFTH:
                            mDescriptor = getAssets().openFd("Record_wxyz_right.wav");
                            break;
                        default:
                            mDescriptor = getAssets().openFd("Record_12341234_right.wav");
                            break;
                    }
                }

                findViewById(R.id.audio_ivtrack).setVisibility(View.VISIBLE);
                findViewById(R.id.audio_tvtrack).setVisibility(View.VISIBLE);
                ((ImageView) findViewById(R.id.audio_ivtrack)).setImageResource(R.drawable.audio_ch_r);
                ((TextView) findViewById(R.id.audio_tvtrack)).setText(mToolKit.getStringResource(R.string.audio_righttrack));
            }
            setPlayer(mDescriptor);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void setPlayer(AssetFileDescriptor mDescriptor) {
        try {
            mPlayer.reset();
            mPlayer.setDataSource(mDescriptor.getFileDescriptor(), mDescriptor.getStartOffset(), mDescriptor.getLength());
            mPlayer.prepare();
            mPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startRecord() {
        // TODO Auto-generated method stub
        if (mRecordTime <= 0) {
            showAlert(mToolKit.getStringResource(R.string.audio_time_invalid));
        } else {
            showProgressView();
            if (startRec()) {
                try {
                    new Thread(myRecord).start();
                } catch (Exception e) {
                    e.toString();
                }
            } else {
                Toast.makeText(this, mToolKit.getStringResource(R.string.audio_nosdcard), Toast.LENGTH_SHORT).show();
                releaseResource(false);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.audio);

        mToolKit = new WisToolKit(this);
        mCurrentLanguage = mToolKit.getCurrentLanguage();
        if (mToolKit.getCurrentItem() != null) {
            mCurTestItem = mToolKit.getCurrentItem();
        }

        getViewId();
        getTestArguments();
        initial();
        setViewByLanguage();

        if (mCurTestItem.equals(TEST_HEADSET)) {
            registerHeadSet();
            initializeTimerTask();
            tvTotalTime.setText(mToolKit.getStringResource(R.string.total_time) + mToolKit.formatCountDownTime(TIMEOUT, mTimes));
            tvRemainTime.setText(mToolKit.getStringResource(R.string.remain_time) + mToolKit.formatCountDownTime(TIMEOUT, mTimes));
        } else if (mCurTestItem.equals(TEST_AUDIO)) {
            btn_start.performClick();
        }
    }

    private void initial() {
        // TODO Auto-generated method stub
        mManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        getOriginalVolume();
        if (mCurTestItem.equals(TEST_AUDIO)) {
            setSystemVolume(mCurVolume);
        }

        initializeDialog();
        initializeMediaPlayer();
        mMediaRecorder = new MediaRecorder();

        if (mCurTestItem.equals(TEST_HEADSET)) {
            mSubItem &= 11;
            cbReceiver.setChecked(false);
            findViewById(R.id.audio_receiver_layout).setVisibility(View.GONE);
        }

        if ((mSubItem & ITEM_STEREO) != 0) {
            cbStereo.setChecked(true);
        } else {
            cbStereo.setChecked(false);
        }
        if ((mSubItem & ITEM_TRACK) != 0) {
            cbTrack.setChecked(true);
        } else {
            cbTrack.setChecked(false);
        }
        if ((mSubItem & ITEM_RECEIVER) != 0) {
            cbReceiver.setChecked(true);
        } else {
            cbReceiver.setChecked(false);
        }
        if ((mSubItem & ITEM_RECORD) != 0) {
            cbRecord.setChecked(true);
        } else {
            cbRecord.setChecked(false);
        }
    }

    private void setSystemVolume(int volume) {
        // TODO Auto-generated method stub
        mManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_SHOW_UI);
    }

    private void getOriginalVolume() {
        // TODO Auto-generated method stub
        mOriginalVolume = mManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

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
            if (mState == 1 && mPluginState == -1) {
                cancelTimer();
                mPluginState = mState;
                if (btn_start.isEnabled()) {
                    mTimeLayout.setVisibility(View.GONE);
                    mTestLayout.setVisibility(View.VISIBLE);
                    mButtonLayout.setVisibility(View.VISIBLE);
                    btn_start.performClick();
                }
            } else if (mState == 0) {
                if (mPluginState == 1) {
                    releaseResource(false);
                    mPluginState = mState;
                }
            }
        }
    };

    private void initializeTimerTask() {
        // TODO Auto-generated method stub
        mTask = new TimerTask() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                mTimes++;
                if (mTimes >= TIMEOUT) {
                    myHandler.sendEmptyMessage(MSG_TIMEOUT);
                } else {
                    myHandler.sendEmptyMessage(MSG_TIME);
                }
            }
        };
        mTimer.schedule(mTask, 1000, 1000);
    }

    private void cancelTimer() {
        if (mTask != null) {
            mTask.cancel();
            mTask = null;
        }
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        tvRemainTime.setVisibility(View.INVISIBLE);
        tvTotalTime.setVisibility(View.INVISIBLE);
    }

    private void setViewByLanguage() {
        // TODO Auto-generated method stub
        TextView mItemTitle = (TextView) findViewById(R.id.item_title);
        mItemTitle.setText(mToolKit.getStringResource(mCurTestItem.equals(TEST_AUDIO) ? R.string.audio_test_title
                : R.string.headset_test_title));
        ((TextView) findViewById(R.id.audio_plugin_headset_title)).setText(mToolKit.getStringResource(R.string.audio_plugin_headset));
        cbStereo.setText(mToolKit.getStringResource(R.string.audio_stereotest));
        cbTrack.setText(mToolKit.getStringResource(R.string.audio_track_test));
        cbReceiver.setText(mToolKit.getStringResource(R.string.audio_receiver));
        cbRecord.setText(mToolKit.getStringResource(R.string.audio_recordtest));
        btn_start.setText(mToolKit.getStringResource(R.string.button_start));
        btn_exit.setText(mToolKit.getStringResource(R.string.button_exit));

        mFirstMusic.setText(mToolKit.getStringResource(R.string.audio_track_one));
        mSecondMusic.setText(mToolKit.getStringResource(R.string.audio_track_two));
        mThirdMusic.setText(mToolKit.getStringResource(R.string.audio_track_three));
        mFourthMusic.setText(mToolKit.getStringResource(R.string.audio_track_four));
        mFifthMusic.setText(mToolKit.getStringResource(R.string.audio_track_five));
    }

    private void getTestArguments() {
        // TODO Auto-generated method stub
        String mTestStyle = mToolKit.getCurrentTestType();
        if (mTestStyle != null) {
            if (mTestStyle.equals(WisCommonConst.TESTSTYLE_SAVED_CONFIG)) {
                WisParseValue mParse = new WisParseValue(this, mToolKit.getCurrentItem(), mToolKit.getCurrentDatabaseAuthorities());
                mSubItem = Integer.parseInt(mParse.getArg1());
                int mGetTime = Integer.parseInt(mParse.getArg2());
                mCurVolume = Integer.parseInt(mParse.getArg3());
                if (mCurTestItem.equals(TEST_HEADSET)) {
                    int mHeadsetTimeout = Integer.parseInt(mParse.getArg4());
                    TIMEOUT = mHeadsetTimeout > 0 ? mHeadsetTimeout : TIMEOUT;
                }
                mRecordTime = mGetTime > 0 ? mGetTime : DEFAULT_RECORD_TIME;
            }
        }
    }

    private void setRandom() {
        Random mRandom = new Random(System.currentTimeMillis());
        mCurMusicIndex = mRandom.nextInt(5) + 1;
    }

    private void initializeDialog() {
        // TODO Auto-generated method stub
        mPromptDialog = new WisAlertDialog(this)
                .setTitle(mToolKit.getStringResource(R.string.audio_promptdialog_title))
                .setMessage(mToolKit.getStringResource(R.string.audio_promptdialog_msg),
                        getResources().getDimension(R.dimen.alert_dialog_message_size))
                .setPositiveButton(mToolKit.getStringResource(R.string.audio_promptdialog_query),
                        getResources().getDimension(R.dimen.alert_dialog_button_size), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                if (mCurIndex == INDEX_STEREO || mCurIndex == INDEX_RECEIVER) {
                                    playStereo();
                                } else {
                                    playTrack();
                                }
                                mSelectDialog.show();
                            }
                        }).createDialog();

        LayoutInflater mInflater = LayoutInflater.from(this);
        View mSelect = mInflater.inflate(R.layout.audio_select_track, null);
        mSelect.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        findButtonView(mSelect);
        mSelectDialog = new AlertDialog.Builder(this).setTitle(mToolKit.getStringResource(R.string.audio_selectdialog_title))
                .setView(mSelect).create();
        mSelectDialog.setCancelable(false);
        mSelectDialog.setCanceledOnTouchOutside(false);

        mManualMicResultDialog = new WisAlertDialog(this)
                .setPositiveButton(mToolKit.getStringResource(R.string.pass),
                        getResources().getDimension(R.dimen.alert_dialog_button_size), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                mCurIndex++;
                                startButtonEvent();
                            }
                        })
                .setNegativeButton(mToolKit.getStringResource(R.string.fail),
                        getResources().getDimension(R.dimen.alert_dialog_button_size), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                releaseResource(false);
                            }
                        }).createDialog();
    }

    private void findButtonView(View mSelect) {
        // TODO Auto-generated method stub
        mFirstMusic = (Button) mSelect.findViewById(R.id.track_one);
        mSecondMusic = (Button) mSelect.findViewById(R.id.track_two);
        mThirdMusic = (Button) mSelect.findViewById(R.id.track_three);
        mFourthMusic = (Button) mSelect.findViewById(R.id.track_four);
        mFifthMusic = (Button) mSelect.findViewById(R.id.track_five);

        mFirstMusic.setOnClickListener(this);
        mSecondMusic.setOnClickListener(this);
        mThirdMusic.setOnClickListener(this);
        mFourthMusic.setOnClickListener(this);
        mFifthMusic.setOnClickListener(this);
    }

    private void initializeMediaPlayer() {
        // TODO Auto-generated method stub
        try {
            mPlayer = new MediaPlayer();
            mPlayer.setLooping(false);
        } catch (Exception e) {
            e.toString();
        }
        mPlayer.setOnCompletionListener(new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                // TODO Auto-generated method stub
                nextAction();
            }

        });
        mPlayer.setOnErrorListener(new OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                // TODO Auto-generated method stub
                mp.release();
                releaseResource(false);
                return false;
            }

        });
    }

    protected void nextAction() {
        // TODO Auto-generated method stub
        if (mCurIndex == INDEX_STEREO) {
            ((TextView) findViewById(R.id.audio_tvstereo)).setText(mToolKit.getStringResource(R.string.audio_finish));
        } else if (mCurIndex == INDEX_TRACK) {
            findViewById(R.id.audio_ivtrack).setVisibility(View.GONE);
            ((TextView) findViewById(R.id.audio_tvtrack)).setText(mToolKit.getStringResource(R.string.audio_finish));
        } else if (mCurIndex == INDEX_RECEIVER) {
            mManager.setMode(AudioManager.MODE_NORMAL);
            ((TextView) findViewById(R.id.audio_tvreceiver)).setText(mToolKit.getStringResource(R.string.audio_finish));
        } else if (mCurIndex == INDEX_RECORD) {
            ((TextView) findViewById(R.id.audio_tvrecord)).setText(mToolKit.getStringResource(R.string.audio_finish));
            showManualSelectMicResultDialog();
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

    private void hideProgressView() {
        pRecordBar.setVisibility(View.GONE);
    }

    private void showProgressView() {
        pRecordBar.setVisibility(View.VISIBLE);
        pRecordBar.setProgress(0);
        if (mCurIndex == INDEX_RECORD) {
            findViewById(R.id.audio_tvrecord).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.audio_tvrecord)).setText(mToolKit.getStringResource(R.string.audio_recording));
        }
    }

    private final Handler myHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_START_RECORD:
                    pRecordBar.setProgress(msg.arg1);
                    break;
                case MSG_STOP_RECORD:
                    pRecordBar.setProgress(0);
                    ((TextView) findViewById(R.id.audio_tvrecord)).setText(mToolKit.getStringResource(R.string.audio_playing));
                    break;
                case MSG_PLAY_RECORD:
                    pRecordBar.setProgress(msg.arg1);
                    break;
                case MSG_PLAY_END:

                    break;
                case MSG_TIME:
                    tvRemainTime.setText(mToolKit.getStringResource(R.string.remain_time) + mToolKit.formatCountDownTime(TIMEOUT, mTimes));
                    break;
                case MSG_TIMEOUT:
                    cancelTimer();
                    releaseResource(false);
                    break;
                default:
                    break;
            }
        }
    };

    private final Runnable myPlayback = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            for (int i = 0; i < mRecordTime; i++) {
                Message msg = new Message();
                msg.what = MSG_START_RECORD;
                msg.arg1 = (int) ((i + 1) * 100f / mRecordTime);
                myHandler.sendMessage(msg);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            myHandler.sendEmptyMessage(MSG_PLAY_END);
        }

    };
    private final Runnable myRecord = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            for (int i = 0; i < mRecordTime; i++) {
                Message msg = new Message();
                msg.what = MSG_START_RECORD;
                msg.arg1 = (int) ((i + 1) * 100f / mRecordTime);
                myHandler.sendMessage(msg);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            stopRec();
            startPlayRecording();
            new Thread(myPlayback).start();
        }
    };

    private void getViewId() {
        mTimeLayout = (LinearLayout) findViewById(R.id.audio_time_layout);
        mTestLayout = (LinearLayout) findViewById(R.id.audio_test_layout);
        mButtonLayout = (RelativeLayout) findViewById(R.id.audio_button_layout);

        tvRemainTime = (TextView) findViewById(R.id.remaintime);
        tvTotalTime = (TextView) findViewById(R.id.totaltime);

        btn_start = (Button) findViewById(R.id.audio_start);
        btn_exit = (Button) findViewById(R.id.audio_exit);
        btn_start.setOnClickListener(this);
        btn_exit.setOnClickListener(this);

        cbStereo = (CheckBox) findViewById(R.id.audio_cbstereo);
        cbTrack = (CheckBox) findViewById(R.id.audio_cbtrack);
        cbReceiver = (CheckBox) findViewById(R.id.audio_cbreceiver);
        cbRecord = (CheckBox) findViewById(R.id.audio_cbrecord);

        pRecordBar = (ProgressBar) findViewById(R.id.audio_record_progress);

        mRecordTime = DEFAULT_RECORD_TIME;
        hideGUI();
    }

    private void hideGUI() {
        // TODO Auto-generated method stub
        findViewById(R.id.audio_ivtrack).setVisibility(ImageView.INVISIBLE);
        pRecordBar.setVisibility(View.GONE);

        if (mCurTestItem.equals(TEST_AUDIO)) {
            mTimeLayout.setVisibility(View.GONE);
        } else {
            mTestLayout.setVisibility(View.INVISIBLE);
            mButtonLayout.setVisibility(View.INVISIBLE);
        }
    }

    public void showAlert(CharSequence string_Message) {
        new AlertDialog.Builder(this).setTitle(mToolKit.getStringResource(R.string.audio_warning))
                .setPositiveButton(mToolKit.getStringResource(R.string.confirm), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        releaseResource(false);
                    }
                }).setIcon(getResources().getDrawable(R.drawable.alert_dialog_icon)).setMessage(string_Message).setCancelable(false).show();
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        int mTempMusicIndex = 0;
        if (v == btn_start) {
            if (mSubItem > 0) {
                disableViews();
                startButtonEvent();
                btn_exit.setVisibility(View.INVISIBLE);
            } else {
                showAlert(mToolKit.getStringResource(R.string.audio_select_none));
            }
        } else if (v == btn_exit) {
            releaseResource(false);
        } else if (v == mFirstMusic) {
            mTempMusicIndex = MUSIC_FIRST;
        } else if (v == mSecondMusic) {
            mTempMusicIndex = MUSIC_SECOND;
        } else if (v == mThirdMusic) {
            mTempMusicIndex = MUSIC_THIRD;
        } else if (v == mFourthMusic) {
            mTempMusicIndex = MUSIC_FOURTH;
        } else if (v == mFifthMusic) {
            mTempMusicIndex = MUSIC_FIFTH;
        }
        if (v == mFirstMusic || v == mSecondMusic || v == mThirdMusic || v == mFourthMusic || v == mFifthMusic) {
            if (mPlayer.isPlaying()) {
                mPlayer.stop();
                nextAction();
            }
            mSelectDialog.dismiss();
            if (mCurMusicIndex != mTempMusicIndex) {
                releaseResource(false);
            } else {
                if (mCurIndex != INDEX_TRACK || mTrackType != TRACK_LEFT) {
                    mCurIndex++;
                }
                startButtonEvent();
            }
        }
    }

    private void disableViews() {
        // TODO Auto-generated method stub
        cbStereo.setEnabled(false);
        cbTrack.setEnabled(false);
        cbReceiver.setEnabled(false);
        cbRecord.setEnabled(false);
        btn_start.setVisibility(View.INVISIBLE);
    }

    private void enableViews() {
        cbStereo.setEnabled(true);
        cbTrack.setEnabled(true);
        cbReceiver.setEnabled(true);
        cbRecord.setEnabled(true);
    }

    private void startButtonEvent() {
        // TODO Auto-generated method stub
        switch (mCurIndex) {
            case INDEX_STEREO:
                if (cbStereo.isChecked()) {
                    mPromptDialog.showDialog();
                } else {
                    mCurIndex++;
                    startButtonEvent();
                }
                break;
            case INDEX_TRACK:
                if (cbTrack.isChecked()) {
                    if (mTrackType == -1) {
                        mTrackType = TRACK_LEFT;
                    } else {
                        mTrackType = TRACK_RIGHT;
                    }
                    mPromptDialog.showDialog();
                } else {
                    mCurIndex++;
                    startButtonEvent();
                }
                break;
            case INDEX_RECEIVER:
                if (cbReceiver.isChecked()) {
                    mManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                    mPromptDialog.show();
                } else {
                    mCurIndex++;
                    startButtonEvent();
                }
                break;
            case INDEX_RECORD:
                if (cbRecord.isChecked()) {
                    startRecord();
                } else {
                    mCurIndex++;
                    startButtonEvent();
                }
                break;
            default:
                hideProgressView();
                enableViews();
                releaseResource(true);
                break;
        }
    }

    private void showManualSelectMicResultDialog() {
        if (mCurIndex == INDEX_RECORD) {
            mManualMicResultDialog.setMessage(mToolKit.getStringResource(R.string.audio_manual_mic_testresult), getResources()
                    .getDimension(R.dimen.alert_dialog_message_size));
        }
        mManualMicResultDialog.showDialog();
    }

    private void releaseResource(boolean isPass) {
        // TODO Auto-generated method stub
        myHandler.removeCallbacks(myRecord);
        myHandler.removeCallbacks(myPlayback);
        if (mPlayer != null) {
            try {
                if (mPlayer.isPlaying()) {
                    mPlayer.stop();
                    mPlayer.reset();
                }
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
            mPlayer.release();
            mPlayer = null;
        }
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }

        if (mPromptDialog != null) {
            mPromptDialog.dismissDialog();
        }
        if (mSelectDialog != null && mSelectDialog.isShowing()) {
            mSelectDialog.dismiss();
        }

        if (mCurTestItem.equals(TEST_HEADSET) && isRegisterBroadcast) {
            unregisterReceiver(headsetReceiver);
            isRegisterBroadcast = false;
        }
        deleteAudioFile();
        mToolKit.returnWithResult(isPass);
    }

    private void deleteAudioFile() {
        // TODO Auto-generated method stub
        FilenameFilter mFilter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String filename) { //
                // TODO Auto-generated method stub
                return filename.startsWith("audio") && filename.endsWith(".amr");
            }
        };

        File[] files = Environment.getExternalStorageDirectory().listFiles(mFilter);
        if (files != null && files.length > 0) {
            for (File audioFile : files) {
                audioFile.delete();
            }
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (mManager != null) {
            mManager.setMode(AudioManager.MODE_NORMAL);
        }
    }
}