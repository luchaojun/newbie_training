package com.wistron.generic.hdmi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.wistron.generic.pqaa.R;
import com.wistron.pqaa_common.jar.global.WisToolKit;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class VidPlayback extends Activity {
    private static final int LANGUAGE_ENGLISH = 0;
    private static final int LANGUAGE_CHINESE_SIMPLE = 1;

    public boolean finish = false;
    public AlertDialog builder = null;
    private VideoView videoView;

    private int mLanguage;
    private int mCurVedioIndex = 0;
    private AlertDialog mPromptDialog, mSelectDialog;
    private Button mFirstMusic, mSecondMusic, mThirdMusic, mFourthMusic, mFifthMusic, mNGButton;
    private int mTempMusicIndex = 0;

    private int mTimes = 0;
    private Timer mTimer = new Timer();
    private TimerTask mTask;
    private WisToolKit mToolKit;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.hdmi_main);

        mToolKit = new WisToolKit(this);

        ((TextView) findViewById(R.id.item_title)).setText(mToolKit
                .getStringResource(R.string.hdmi_test_title));

        initializeDialog();
        mPromptDialog.show();
    }

    private void setscreendown(int i) {
        try {
            String tempiString = "";
            tempiString = String.valueOf(i);
            Log.i("WKSMFG", "has gone to here");
            BufferedWriter output = new BufferedWriter(new FileWriter(
                    "/sys/class/backlight/pwm-backlight/brightness"));
            output.write(tempiString);
            output.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    private void setRandom() {
        Random mRandom = new Random(System.currentTimeMillis());
        mCurVedioIndex = mRandom.nextInt(5) + 1;
    }

    private void findView() {
        // TODO Auto-generated method stub
        videoView = (VideoView) findViewById(R.id.hdmi_video);
        if (mCurVedioIndex == 1) {
            videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/"
                    + R.raw.hdmi1));
        } else if (mCurVedioIndex == 2) {
            videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/"
                    + R.raw.hdmi2));
        } else if (mCurVedioIndex == 3) {
            videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/"
                    + R.raw.hdmi3));
        } else if (mCurVedioIndex == 4) {
            videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/"
                    + R.raw.hdmi4));
        } else if (mCurVedioIndex == 5) {
            videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/"
                    + R.raw.hdmi5));
        }

        videoView.setMediaController(new MediaController(VidPlayback.this));
        videoView.requestFocus();

        OnCompletionListener listener = new OnCompletionListener() {

            public void onCompletion(MediaPlayer mp) {
                // TODO Auto-generated method stub
                handler.sendEmptyMessage(1);
            }

        };
        videoView.setOnCompletionListener(listener);
//		startTime();
    }

    private void startTime() {
        // TODO Auto-generated method stub
        mTask = new TimerTask() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                mTimes++;
                if (mTimes >= 1) {
                    // setscreendown(80);
                }
                if (mTimes == 4) {
                    handler.sendEmptyMessage(1);
                }
                if (mTimes == 5) {
                    setscreendown(80);
                    // mSelectDialog.show();
                    handler.sendEmptyMessage(0);
                }
                Log.i("WKSMFGR", ">>>>>>>>>>>>>>>>********mTimes: " + mTimes);
            }
        };
        mTimer.schedule(mTask, 1000, 1000);
    }

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            // videoView.setVisibility(View.INVISIBLE);
            switch (msg.what) {
                case 0:
                    mSelectDialog.show();
                    break;
                case 1:
                    videoView.setVisibility(View.INVISIBLE);
                    handler.sendEmptyMessage(0);
                    break;
                default:
                    break;
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

    private void findButtonView(View mSelect) {
        // TODO Auto-generated method stub
        mFirstMusic = (Button) mSelect.findViewById(R.id.track_one);
        mSecondMusic = (Button) mSelect.findViewById(R.id.track_two);
        mThirdMusic = (Button) mSelect.findViewById(R.id.track_three);
        mFourthMusic = (Button) mSelect.findViewById(R.id.track_four);
        mFifthMusic = (Button) mSelect.findViewById(R.id.track_five);
        mNGButton = (Button) mSelect.findViewById(R.id.ng_button);

        mFirstMusic.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mTempMusicIndex = 1;
                compareresult();
                Log.i("WKSMFG", "The click number is :" + mTempMusicIndex);
            }
        });
        mSecondMusic.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mTempMusicIndex = 2;
                compareresult();
            }
        });
        mThirdMusic.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mTempMusicIndex = 3;
                compareresult();
            }
        });
        mFourthMusic.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mTempMusicIndex = 4;
                compareresult();
            }
        });
        mFifthMusic.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mTempMusicIndex = 5;
                compareresult();
            }
        });
        mNGButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                finish = false;
                backResult();
            }
        });
    }

    private void compareresult() {
        if (mTempMusicIndex != mCurVedioIndex) {
            finish = false;
            backResult();
        } else {
            finish = true;
            backResult();
        }
    }

    private void initializeDialog() {
        // TODO Auto-generated method stub
        mPromptDialog = new AlertDialog.Builder(this).setTitle("HDMI Test")
                .setMessage("Please look the number from HDMI screen")
                .setPositiveButton("Start", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        // mSelectDialog.show();
                        setscreendown(0);
                        setRandom();
                        findView();
                        videoView.start();
                    }
                }).create();
        mPromptDialog.setCancelable(false);
        mPromptDialog.setCanceledOnTouchOutside(false);

        LayoutInflater mInflater = LayoutInflater.from(this);
        View mSelect = mInflater.inflate(R.layout.hdmi_select_track, null);
        mSelect.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        findButtonView(mSelect);
        mSelectDialog = new AlertDialog.Builder(this)
                .setTitle("Please make a choose according to see the number from the HDMI screen")
                .setView(mSelect).create();
        mSelectDialog.setCancelable(false);
        mSelectDialog.setCanceledOnTouchOutside(false);
    }

    public void onClick(View v) {
        // TODO Auto-generated method stub
        int mTempMusicIndex = 0;
        if (v == mFirstMusic) {
            mTempMusicIndex = 1;
        } else if (v == mSecondMusic) {
            mTempMusicIndex = 2;
        } else if (v == mThirdMusic) {
            mTempMusicIndex = 3;
        } else if (v == mFourthMusic) {
            mTempMusicIndex = 4;
        } else if (v == mFifthMusic) {
            mTempMusicIndex = 5;
        } else if (v == mNGButton) {
            // backToPQAA();
            finish = false;
            backResult();
        }
        if (mTempMusicIndex != mCurVedioIndex) {
            finish = false;
            backResult();
        } else {
            finish = true;
            backResult();
        }
    }

    protected void backResult() {
        // TODO Auto-generated method stub
        mToolKit.returnWithResult(finish);
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
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        cancelTimer();
    }

}
