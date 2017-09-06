package com.wistron.generic.headset.loopback;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.wistron.pqaa_common.jar.audio.Tone;
import com.wistron.pqaa_common.jar.audio.WisAudioDecode;
import com.wistron.pqaa_common.jar.audio.WisAudioDecode.OnAudioDecodeListener;
import com.wistron.pqaa_common.jar.autotest.WisAudioLoopback_Service;

import java.io.IOException;
import java.util.ArrayList;

public class AudioLoopbackService extends Service implements OnAudioDecodeListener {
    public static final int WCIS_DTMF_TYPES_MP3 = 1;
    public static final int WCIS_DTMF_TYPES_OGG = 2;
    public static final int WCIS_DTMF_TYPES_AAC = 4;
    public static final int WCIS_DTMF_TYPES_WAV = 8;

    private static final int MSG_UPDATE_RESULT = 100;
    // DTMF: 057A*#81
    // Custom: 0123
    private String mAudioValue = "0123456789*#", mDecodeValue = "";
    private char mCurValue;
    private int mNeedMatchCount = mAudioValue.length(), mMatchCount = 0;
    private boolean isMatched = false;
    private boolean isDTMFAudio = true;
    private boolean isWCISTest = false;
    private int mTestCycles = 1, mCyclesIndex = 0;

    private WisAudioDecode mAudioDecoder;
    private ToneGenerator mGenerator;
    private MediaPlayer mPlayer;
    private ArrayList<Tone> mCustomFrequencyList; // For not DTMF tone test
    private int mLastTime = 800;
    private int mSleepTime = 700;

    private AudioManager mManager;
    private int mOriginalVolume, mCurVolume = 14;

    private int mWCISDTMFTestTypes = 0;
    private int mWCISDTMFFirstTestType = 0, mWCISDTMFTypeIndex = 0;
    private boolean isWCISDTMFPass = true;

    private Intent stateChangedIntent;

    public AudioLoopbackService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        stateChangedIntent = new Intent(WisAudioLoopback_Service.ACTION_AUDIOLOOPBACK_STATE_CHANGED);
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        Log.i("Wistron", "AudioLoopbackService stoped!");
        if (mPlayer != null) {
            mPlayer.release();
        }
        mAudioDecoder.stop();
        setSystemVolume(mOriginalVolume);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        if (intent != null) {
            int action = intent.getIntExtra(WisAudioLoopback_Service.EXTRA_AUDIOLOOPBACK_SERVICE_DO_ACTION, WisAudioLoopback_Service.AUDIOLOOPBACK_SERVICE_START_TEST);
            switch (action) {
                case WisAudioLoopback_Service.AUDIOLOOPBACK_SERVICE_START_TEST:
                    isDTMFAudio = intent.getBooleanExtra(WisAudioLoopback_Service.EXTRA_IS_DTMF, isDTMFAudio);
                    isWCISTest = intent.getBooleanExtra(WisAudioLoopback_Service.EXTRA_IS_WCIS, isWCISTest);
                    mWCISDTMFTestTypes = intent.getIntExtra("dtmfTypes", mWCISDTMFTestTypes);
                    mAudioValue = intent.getStringExtra(WisAudioLoopback_Service.EXTRA_TEST_QUEUE);
                    mNeedMatchCount = intent.getIntExtra(WisAudioLoopback_Service.EXTRA_NEED_MATCH_COUNT, mNeedMatchCount);
                    mCurVolume = intent.getIntExtra(WisAudioLoopback_Service.EXTRA_VOLUME, mCurVolume);
                    mTestCycles = intent.getIntExtra(WisAudioLoopback_Service.EXTRA_TEST_CYCLES, mTestCycles);
                    initial();
                    mAudioDecoder.start();
                    start();
                    break;
                case WisAudioLoopback_Service.AUDIOLOOPBACK_SERVICE_STOP_TEST:
                    stopSelf();
                    break;
                default:
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void initial() {
        // TODO Auto-generated method stub
        mAudioDecoder = new WisAudioDecode(this);
        mAudioDecoder.setOnAudioDecodeListener(this);

        mManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mGenerator = new ToneGenerator(AudioManager.STREAM_DTMF, ToneGenerator.MAX_VOLUME);

        if (isWCISTest && isDTMFAudio) {
            for (int i = WCIS_DTMF_TYPES_MP3; i <= WCIS_DTMF_TYPES_WAV; i *= 2) {
                if ((mWCISDTMFTestTypes & i) != 0) {
                    mWCISDTMFFirstTestType = mWCISDTMFTypeIndex = i;
                    break;
                }
            }
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
                stateChangedIntent.putExtra(WisAudioLoopback_Service.EXTRA_AUDIOLOOPBACK_STATE, WisAudioLoopback_Service.AUDIOLOOPBACK_STATE_PLAYER_ERROR);
                sendBroadcast(stateChangedIntent);
                return false;
            }

        });

        getOriginalVolume();
        setSystemVolume(mCurVolume);

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

    private void setPlayer(AssetFileDescriptor mDescriptor) {
        try {
            mPlayer.reset();
            mPlayer.setDataSource(mDescriptor.getFileDescriptor(), mDescriptor.getStartOffset(),
                    mDescriptor.getLength());
            mPlayer.setLooping(false);
            mPlayer.prepare();
            mPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setSystemVolume(int volume) {
        // TODO Auto-generated method stub
        if (isDTMFAudio && !isWCISTest) {
            mManager.setStreamVolume(AudioManager.STREAM_DTMF, volume, AudioManager.FLAG_SHOW_UI);
        } else {
            mManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_SHOW_UI);
        }
    }

    private void getOriginalVolume() {
        // TODO Auto-generated method stub
        if (isDTMFAudio && !isWCISTest) {
            mOriginalVolume = mManager.getStreamVolume(AudioManager.STREAM_DTMF);
        } else {
            mOriginalVolume = mManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        }
    }

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            if (msg.what == MSG_UPDATE_RESULT) {
                stateChangedIntent.putExtra(WisAudioLoopback_Service.EXTRA_AUDIOLOOPBACK_STATE, 10);
                stateChangedIntent.putExtra("curType", mWCISDTMFTypeIndex);
                sendBroadcast(stateChangedIntent);

                boolean isTempPass = mMatchCount >= mNeedMatchCount;
                boolean isDoNextCycle = true;
                if (isWCISTest && isDTMFAudio) {
                    isWCISDTMFPass = isWCISDTMFPass && isTempPass;
                    for (mWCISDTMFTypeIndex *= 2; mWCISDTMFTypeIndex <= WCIS_DTMF_TYPES_WAV; mWCISDTMFTypeIndex *= 2) {
                        if ((mWCISDTMFTestTypes & mWCISDTMFTypeIndex) != 0) {
                            isDoNextCycle = false;
                            break;
                        }
                    }
                    isTempPass = isWCISDTMFPass;
                }
                if (isDoNextCycle) {
                    stateChangedIntent.putExtra(WisAudioLoopback_Service.EXTRA_AUDIOLOOPBACK_STATE, WisAudioLoopback_Service.AUDIOLOOPBACK_STATE_ONE_CYCLE_DONE);
                    stateChangedIntent.putExtra(WisAudioLoopback_Service.EXTRA_AUDIOLOOPBACK_CYCLE_DONE_RESULT, isTempPass);
                    sendBroadcast(stateChangedIntent);
                    if (mCyclesIndex >= mTestCycles) {
                        stateChangedIntent.putExtra(WisAudioLoopback_Service.EXTRA_AUDIOLOOPBACK_STATE, WisAudioLoopback_Service.AUDIOLOOPBACK_STATE_TO_EXIT);
                        sendBroadcast(stateChangedIntent);
                    } else {
                        start();
                    }
                } else {
                    start();
                }
            }
        }

    };

    private void start() {
        mDecodeValue = "";
        mMatchCount = 0;

        if (isWCISTest && isDTMFAudio) {
            for (int i = mWCISDTMFTypeIndex; i <= WCIS_DTMF_TYPES_WAV; i *= 2) {
                if ((mWCISDTMFTestTypes & i) != 0) {
                    mWCISDTMFTypeIndex = i;
                    break;
                }
            }
            if (mWCISDTMFTypeIndex > WCIS_DTMF_TYPES_WAV) {
                mWCISDTMFTypeIndex = mWCISDTMFFirstTestType;
            }
        }

        Log.i("AudioLoopbackService", "index: " + mWCISDTMFTypeIndex + "  -- First: " + mWCISDTMFFirstTestType);
        if (!isWCISTest || !isDTMFAudio || mWCISDTMFTypeIndex == mWCISDTMFFirstTestType) {
            mCyclesIndex++;
            isWCISDTMFPass = true;
            stateChangedIntent.putExtra(WisAudioLoopback_Service.EXTRA_AUDIOLOOPBACK_STATE, WisAudioLoopback_Service.AUDIOLOOPBACK_STATE_ONE_CYCLE_START);
            stateChangedIntent.putExtra(WisAudioLoopback_Service.EXTRA_AUDIOLOOPBACK_START_CYCLE_INDEX, mCyclesIndex);
            sendBroadcast(stateChangedIntent);
        }

        new Thread(startPlay).start();
    }

    private Runnable startPlay = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            try {
                Thread.sleep(mSleepTime);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            int mTone = 0;
            while (mTone < mAudioValue.length() && mAudioDecoder.isStarted()) {
                mCurValue = mAudioValue.charAt(mTone);
                Log.i("AudioLoopbackService", "cur value: " + mCurValue);
                isMatched = false;
                if (isDTMFAudio) {
                    if (mAudioDecoder.isStarted()) {
                        if (isWCISTest) {
                            String type = "";
                            if (mWCISDTMFTypeIndex == WCIS_DTMF_TYPES_MP3) {
                                type = "mp3";
                            } else if (mWCISDTMFTypeIndex == WCIS_DTMF_TYPES_OGG) {
                                type = "ogg";
                            } else if (mWCISDTMFTypeIndex == WCIS_DTMF_TYPES_AAC) {
                                type = "aac";
                            } else if (mWCISDTMFTypeIndex == WCIS_DTMF_TYPES_WAV) {
                                type = "wav";
                            }
                            try {
                                if (mCurValue == '*') {
                                    setPlayer(getAssets().openFd(String.format("%1$s/DtmfStar.%1$s", type)));
                                } else if (mCurValue == '#') {
                                    setPlayer(getAssets().openFd(String.format("%1$s/DtmfWell.%1$s", type)));
                                } else {
                                    setPlayer(getAssets().openFd(String.format("%1$s/Dtmf%2$c.%1$s", type, mCurValue)));
                                }
                            } catch (IOException e) {
                                // TODO: handle exception
                                e.printStackTrace();
                            }
                        } else {
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
                            e.printStackTrace();
                        }
                    }
                }
                mTone++;
                try {
                    Thread.sleep(mSleepTime);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    break;
                }
            }

            handler.sendEmptyMessage(MSG_UPDATE_RESULT);
        }
    };

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
                stateChangedIntent.putExtra(WisAudioLoopback_Service.EXTRA_AUDIOLOOPBACK_STATE, WisAudioLoopback_Service.AUDIOLOOPBACK_STATE_UPDATE_DECODE_VALUE);
                stateChangedIntent.putExtra("curType", mWCISDTMFTypeIndex);
                stateChangedIntent.putExtra(WisAudioLoopback_Service.EXTRA_AUDIOLOOPBACK_DECODE_VALUE, mDecodeValue);
                sendBroadcast(stateChangedIntent);
            }
            if (mTone == mCurValue && !isMatched) {
                mMatchCount++;
                isMatched = true;
            }
            Log.i("AudioLoopback", lowFrequency + " - " + highFrequency + " , " + mTone);
        }
    }
}
