package com.wistron.generic.cameraled;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wistron.generic.pqaa.R;
import com.wistron.pqaa_common.jar.global.WisCommonConst;
import com.wistron.pqaa_common.jar.global.WisParseValue;
import com.wistron.pqaa_common.jar.global.WisShellCommandHelper;
import com.wistron.pqaa_common.jar.global.WisToolKit;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Camera_LED extends Activity {
	// ----------------------result---------------
	private boolean mComponentMode = true;
	private boolean isPass;
	private Timer mTimer = new Timer();
	private TimerTask mTask;
	private Timer mLedTimer = new Timer();
	private TimerTask mLedTask;
	private int mLedTimes;
	private int TIMEOUT = 20;
	private int mTimes;
	private int mCurLedIndex = 0;
	private int mCurXXIndex = 0;
	private int mTempSelectIndex = 0;
	private AlertDialog mPromptDialog, mSelectDialog;
	private Button mFirstMusic, mSecondMusic, mThirdMusic, mNGButton;
	// common tool kit
	private WisToolKit mToolKit;
	
	private Camera mCamera;

	private WisShellCommandHelper mCommandHelper;
	/** Called when the activity is first created. */
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.led);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		mToolKit = new WisToolKit(this);

		mCommandHelper = new WisShellCommandHelper();
		
		getTestArguments();
		setViewByLanguage();
		setLedRandom();
		initializeDialog();
		mPromptDialog.show();
		initial();
	}
	
	private void setLedRandom() {
		Random mRandom = new Random(System.currentTimeMillis());
		mCurLedIndex = mRandom.nextInt(3) + 1;
		Log.i("WKSMFG",">>>>>>>>>>>>>>>>Index4 is:"+mCurLedIndex);
	}
	private void setXXRandom() {
		Random mRandom = new Random(System.currentTimeMillis());
		mCurXXIndex = mRandom.nextInt(9) + 1;
		Log.i("WKSMFG",">>>>>>>>>>>>>>>>Index4 is:"+mCurXXIndex);
	}
	
	private void initializeDialog() {
		// TODO Auto-generated method stub
		mPromptDialog = new AlertDialog.Builder(this)
				.setTitle(mToolKit.getStringResource(R.string.CameraLED))
				.setMessage(mToolKit.getStringResource(R.string.select))
				.setPositiveButton(mToolKit.getStringResource(R.string.automatic_start_test),
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								initialTimer();
							}
						}).create();
		mPromptDialog.setCancelable(false);
		mPromptDialog.setCanceledOnTouchOutside(false);

		LayoutInflater mInflater = LayoutInflater.from(this);
		setXXRandom();
		if(mCurXXIndex == 1){
			View mSelect = mInflater.inflate(R.layout.select_track0, null);
			mSelect.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
					LinearLayout.LayoutParams.FILL_PARENT));
			findButtonView(mSelect);
			mSelectDialog = new AlertDialog.Builder(this).setTitle(mToolKit.getStringResource(R.string.select))
					.setView(mSelect).create();
			mSelectDialog.setCancelable(false);
			mSelectDialog.setCanceledOnTouchOutside(false);
		}else if (mCurXXIndex == 2){
			View mSelect = mInflater.inflate(R.layout.select_track1, null);
			mSelect.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
					LinearLayout.LayoutParams.FILL_PARENT));
			findButtonView(mSelect);
			mSelectDialog = new AlertDialog.Builder(this).setTitle(mToolKit.getStringResource(R.string.select))
					.setView(mSelect).create();
			mSelectDialog.setCancelable(false);
			mSelectDialog.setCanceledOnTouchOutside(false);
		}else if (mCurXXIndex == 3){
			View mSelect = mInflater.inflate(R.layout.select_track2, null);
			mSelect.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
					LinearLayout.LayoutParams.FILL_PARENT));
			findButtonView(mSelect);
			mSelectDialog = new AlertDialog.Builder(this).setTitle(mToolKit.getStringResource(R.string.select))
					.setView(mSelect).create();
			mSelectDialog.setCancelable(false);
			mSelectDialog.setCanceledOnTouchOutside(false);
		}else if(mCurXXIndex == 4){
			View mSelect = mInflater.inflate(R.layout.select_track3, null);
			mSelect.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
					LinearLayout.LayoutParams.FILL_PARENT));
			findButtonView(mSelect);
			mSelectDialog = new AlertDialog.Builder(this).setTitle(mToolKit.getStringResource(R.string.select))
					.setView(mSelect).create();
			mSelectDialog.setCancelable(false);
			mSelectDialog.setCanceledOnTouchOutside(false);
		}else if(mCurXXIndex == 5){
			View mSelect = mInflater.inflate(R.layout.select_track4, null);
			mSelect.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
					LinearLayout.LayoutParams.FILL_PARENT));
			findButtonView(mSelect);
			mSelectDialog = new AlertDialog.Builder(this).setTitle(mToolKit.getStringResource(R.string.select))
					.setView(mSelect).create();
			mSelectDialog.setCancelable(false);
			mSelectDialog.setCanceledOnTouchOutside(false);
		}else if(mCurXXIndex == 6){
			View mSelect = mInflater.inflate(R.layout.select_track5, null);
			mSelect.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
					LinearLayout.LayoutParams.FILL_PARENT));
			findButtonView(mSelect);
			mSelectDialog = new AlertDialog.Builder(this).setTitle(mToolKit.getStringResource(R.string.select))
					.setView(mSelect).create();
			mSelectDialog.setCancelable(false);
			mSelectDialog.setCanceledOnTouchOutside(false);
		}else if(mCurXXIndex == 7){
			View mSelect = mInflater.inflate(R.layout.select_track6, null);
			mSelect.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
					LinearLayout.LayoutParams.FILL_PARENT));
			findButtonView(mSelect);
			mSelectDialog = new AlertDialog.Builder(this).setTitle(mToolKit.getStringResource(R.string.select))
					.setView(mSelect).create();
			mSelectDialog.setCancelable(false);
			mSelectDialog.setCanceledOnTouchOutside(false);
		}else if(mCurXXIndex == 8){
			View mSelect = mInflater.inflate(R.layout.select_track7, null);
			mSelect.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
					LinearLayout.LayoutParams.FILL_PARENT));
			findButtonView(mSelect);
			mSelectDialog = new AlertDialog.Builder(this).setTitle(mToolKit.getStringResource(R.string.select))
					.setView(mSelect).create();
			mSelectDialog.setCancelable(false);
			mSelectDialog.setCanceledOnTouchOutside(false);
		}else if(mCurXXIndex == 9){
			View mSelect = mInflater.inflate(R.layout.select_track8, null);
			mSelect.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
					LinearLayout.LayoutParams.FILL_PARENT));
			findButtonView(mSelect);
			mSelectDialog = new AlertDialog.Builder(this).setTitle(mToolKit.getStringResource(R.string.select))
					.setView(mSelect).create();
			mSelectDialog.setCancelable(false);
			mSelectDialog.setCanceledOnTouchOutside(false);
		}
	}
	
	private void findButtonView(View mSelect) {
		// TODO Auto-generated method stub
		mFirstMusic = (Button) mSelect.findViewById(R.id.track_one);
		mSecondMusic = (Button) mSelect.findViewById(R.id.track_two);
		mThirdMusic = (Button) mSelect.findViewById(R.id.track_three);
		mNGButton = (Button) mSelect.findViewById(R.id.ng_button);

		mFirstMusic.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mTempSelectIndex = 1;
				compareresult();
				Log.i("WKSMFG","The click number is :"+ mTempSelectIndex);
			}
		});
		mSecondMusic.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mTempSelectIndex = 2;	
			   compareresult();
			   Log.i("WKSMFG","The click number is :"+ mTempSelectIndex);
			}
		});
		mThirdMusic.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mTempSelectIndex = 3;
				compareresult();
				Log.i("WKSMFG","The click number is :"+ mTempSelectIndex);
			}
		});
		mNGButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				isPass = false;
				backToPQAA();
			}
		});
	}
	
	private void compareresult(){
		mCommandHelper.exec("echo 0 > /sys/class/leds/swordfish-scan-led/brightness");
		if (mTempSelectIndex != mCurLedIndex) {
			isPass = false;
			backToPQAA();
		} else {
			isPass = true;
			backToPQAA();
		}
	}
	
	private void initial() {
		// TODO Auto-generated method stub
		mTimer = new Timer();
		mTask = new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				mTimes++;
				if(mTimes > TIMEOUT){
					CancelTimer();
					backToPQAA();
				}
			}
		};
		mTimer.schedule(mTask, 1000, 1000);
	}
	
	private void initialTimer(){
		mLedTask = new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mLedTimes++;
				if(mCurLedIndex == 1){
					if(mLedTimes == 1){
						mHandler.sendEmptyMessage(0x111);
					}else if(mLedTimes == 2){
						mHandler.sendEmptyMessage(0x112);
					}else if(mLedTimes == 3){
						mHandler.sendEmptyMessage(0x113);
					}
				}else if(mCurLedIndex == 2){
					if(mLedTimes == 1){
						mHandler.sendEmptyMessage(0x111);
					}else if(mLedTimes == 2){
						mHandler.sendEmptyMessage(0x112);
					}else if(mLedTimes == 3){
						mHandler.sendEmptyMessage(0x111);
					}else if(mLedTimes == 4){
						mHandler.sendEmptyMessage(0x112);
					}else if(mLedTimes == 5){
						mHandler.sendEmptyMessage(0x113);
					}
				}else if(mCurLedIndex == 3){
					if(mLedTimes == 1){
						mHandler.sendEmptyMessage(0x111);
					}else if(mLedTimes == 2){
						mHandler.sendEmptyMessage(0x112);
					}else if(mLedTimes == 3){
						mHandler.sendEmptyMessage(0x111);
					}else if(mLedTimes == 4){
						mHandler.sendEmptyMessage(0x112);
					}else if(mLedTimes == 5){
						mHandler.sendEmptyMessage(0x111);
					}else if(mLedTimes == 6){
						mHandler.sendEmptyMessage(0x112);
					}else if(mLedTimes == 7){
						mHandler.sendEmptyMessage(0x113);
					}
				}
			}
		};
		mLedTimer.schedule(mLedTask, 2000, 1000);
	}
	
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			if(msg.what == 0x111){
				openCamera();	
			}else if(msg.what == 0x112){
				closeCamera();
			}else if(msg.what == 0x113){
				mSelectDialog.show();
				CancelLedTimer();
			}
		};
	};
	
	private void openCamera(){
		if (getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)&&Camera.getNumberOfCameras()>=1){
			try{
			mCamera = Camera
					.open(Camera.CameraInfo.CAMERA_FACING_BACK);
			mCommandHelper.exec("echo 1 > /sys/class/leds/swordfish-scan-led/brightness");
			//Log.i("LED",""+mCommandHelper.exec("cat /sys/class/leds/swordfish-scan-led/brightness"));
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	private void closeCamera(){
		if (mCamera != null) {
			mCommandHelper.exec("echo 0 > /sys/class/leds/swordfish-scan-led/brightness");
			mCamera.release();
			mCamera = null;
		}
	}

	private void CancelTimer() {
		if (mTask != null) {
			mTask.cancel();
			mTask = null;
		}
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
	}
	
	private void CancelLedTimer(){
		if(mLedTask !=null){
			mLedTask.cancel();
			mLedTask = null;
		}
		if(mLedTimer !=null){
			mLedTimer.cancel();
			mLedTimer = null;
		}
	}

	private void getTestArguments() {
		// TODO Auto-generated method stub
		String mTestStyle = mToolKit.getCurrentTestType();
		if (mTestStyle != null) {
			if (mTestStyle.equals(WisCommonConst.TESTSTYLE_SAVED_CONFIG)) {
				mComponentMode = false;
				WisParseValue mParse=new WisParseValue(this, mToolKit.getCurrentItem(),mToolKit.getCurrentDatabaseAuthorities());
				TIMEOUT=Integer.parseInt(mParse.getArg1());
			}
		}
	}

	private void setViewByLanguage() {
		// TODO Auto-generated method stub
		TextView mItemTitle = (TextView) findViewById(R.id.item_title);
		mItemTitle.setText(mToolKit.getStringResource(R.string.led_test_title));
	}

	protected void onDestroy() {
		mCommandHelper.exec("echo 0 > /sys/class/leds/swordfish-scan-led/brightness");
		if(mSelectDialog!=null){
			mSelectDialog.dismiss();
		}
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void backToPQAA(){
		mToolKit.returnWithResult(isPass);
	}
}
