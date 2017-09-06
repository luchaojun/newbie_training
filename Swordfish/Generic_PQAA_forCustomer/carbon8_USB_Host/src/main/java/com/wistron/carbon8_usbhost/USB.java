package com.wistron.carbon8_usbhost;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.usb.UsbManager;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.wistron.pqaa_common.jar.global.WisCommonConst;
import com.wistron.pqaa_common.jar.global.WisParseValue;
import com.wistron.pqaa_common.jar.global.WisStorageRW;
import com.wistron.pqaa_common.jar.global.WisToolKit;
import com.wistron.pqaa_common.jar.global.WisShellCommandHelper;
import com.wistron.pqaa_common.jar.global.WisStorageRW.OnSDCardTestStateChangedListener;


public class USB extends Activity implements OnClickListener, OnSDCardTestStateChangedListener {
	// ----------------------result---------------
	private TextView mResultContent;
	private Button mResultButton;
	// --------------------------------------
	private int TIMEOUT = 30;
	private static final int MESSAGE_UPDATE_TIME = 1;
	private static final int MESSAGE_BACK_RESULT = 2;
	private static final int MESSAGE_DETECT_USB = 3;

	private final String USB_PATH = "/dev/block/sda";
	private final String USB_BACKUP_PATH = "/dev/block/sda1";

	private TextView mPromptTextView;
	private TextView mStatusTextView;
	private Button mPassExitButton, mFailExitButton;

	private boolean isPass;
	private int mTimeCounter;
	private boolean mComponentMode = true;

	// -----------------backup test method
	private int mOriginalDevices = -1;
	private int mTimes = 0;
	private Timer mTimer;
	private TimerTask mTask;
	// common tool kit
	private WisToolKit mToolKit;
	private WisStorageRW mSDCardRWHandler;
	private String mSDPath = "/storage/usbdisk/";
	private ProgressBar mTestProgress;
	private TextView mTestStatus;
	private LinearLayout mProgressLayout;
	private WisShellCommandHelper mShellCommandHelper;

	/** Called when the activity is first created. */
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.usbhost);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		mToolKit = new WisToolKit(this);
		mShellCommandHelper = new WisShellCommandHelper();
		if (!mToolKit.isWistronLockKey()) {
			Toast.makeText(
					this,
					mToolKit.getStringResource(R.string.device_not_match_lock_key),
					Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		findView();
		getTestArguments();
		setViewByLanguage();
		initialTimer();
	}
	//20161018
	private void disableViews() {
		// TODO Auto-generated method stub
		mPromptTextView.setVisibility(View.INVISIBLE);
		mProgressLayout.setVisibility(View.VISIBLE);
		mTestStatus.setVisibility(View.VISIBLE);
		mTestProgress.setProgress(0);
		mTestStatus.setText(mToolKit.getStringResource(R.string.sdcard_testing));
	}
	private void findView() {
		// TODO Auto-generated method stub
		mPromptTextView = (TextView) findViewById(R.id.usb_prompt);
		mPassExitButton = (Button) findViewById(R.id.usb_pass);
		mFailExitButton = (Button) findViewById(R.id.usb_fail);
		mPassExitButton.setOnClickListener(this);
		mFailExitButton.setOnClickListener(this);
		//20161018
		mProgressLayout = (LinearLayout) findViewById(R.id.sdcard_progress_layout);
		mTestProgress = (ProgressBar) findViewById(R.id.sdcard_progress);
		mTestStatus = (TextView) findViewById(R.id.sdcard_status);
	}

	private void setViewByLanguage() {
		// TODO Auto-generated method stub
		TextView mItemTitle = (TextView) findViewById(R.id.item_title);
		mItemTitle.setText(mToolKit
				.getStringResource(R.string.usbhost_test_title));
		mPromptTextView.setText(mToolKit
				.getStringResource(R.string.usbhost_prompt)
				+ (TIMEOUT - mTimes));
		mPassExitButton.setText(mToolKit
				.getStringResource(R.string.button_pass));
		mFailExitButton.setText(mToolKit
				.getStringResource(R.string.button_fail));
	}

	private void getTestArguments() {
		// TODO Auto-generated method stub
		String mTestStyle = mToolKit.getCurrentTestType();
		if (mTestStyle != null) {
			if (mTestStyle.equals(WisCommonConst.TESTSTYLE_SAVED_CONFIG)) {
				mComponentMode = false;
				WisParseValue mParse = new WisParseValue(this,
						mToolKit.getCurrentItem(),
						mToolKit.getCurrentDatabaseAuthorities());
				int mTestTime = Integer.parseInt(mParse.getArg1());
				if (mTestTime > 0) {
					TIMEOUT = mTestTime;
				}
			}
		}
	}

	private void initialTimer() {
		// TODO Auto-generated method stub
		mTimer = new Timer();
		mTask = new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				mTimes++;
				if (mTimes >= TIMEOUT) {
					cancelTimer();
					mHandleResult.sendEmptyMessage(MESSAGE_BACK_RESULT);
				} else {
					mHandleResult.sendEmptyMessage(MESSAGE_DETECT_USB);
				}
			}
		};
		mTimer.schedule(mTask, 2000, 1000);
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

	private final Handler mHandleResult = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if (msg.what == MESSAGE_UPDATE_TIME) {
				mPromptTextView.setText(mToolKit
						.getStringResource(R.string.usbhost_prompt)
						+ (TIMEOUT - mTimeCounter));
			} else if (msg.what == MESSAGE_BACK_RESULT) {
				mFailExitButton.performClick();
			} else if (msg.what == MESSAGE_DETECT_USB) {
				mPromptTextView.setText(mToolKit
						.getStringResource(R.string.usbhost_prompt)
						+ (TIMEOUT - mTimes));
				detectUSBStatus();
			}
		}

	};

	public class ListenUsbStatus extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction()
					.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
				// Toast.makeText(USB.this, "1", 2000).show();
				Log.i("Tag---", "carbon8_sdcard is mounted*");
				/*mStatusTextView.setText(mToolKit
						.getStringResource(R.string.usbhost_mount));*/
				isPass = true;
			}/* else if (intent.getAction().equals(
					UsbManager.ACTION_USB_DEVICE_DETACHED)) {
				// Toast.makeText(USB.this, "2", 2000).show();
				Log.i("Tag---", "carbon8_sdcard is unmounted**");
				mStatusTextView.setText(mToolKit
						.getStringResource(R.string.usbhost_unmount));
				isPass = true;
			}*/
			if (isPass) {
				mPassExitButton.performClick();
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

	protected void detectUSBStatus() {
		// TODO Auto-generated method stub
		//20161018
		/*File file = new File(USB_PATH);
		File file_backup = new File(USB_BACKUP_PATH);
		if (file.exists() || file_backup.exists()) {
			isPass = true;
			cancelTimer();
			mPassExitButton.performClick();
			
		}*/
		ArrayList<String> mResultList=mShellCommandHelper.exec("ls /storage/usbdisk");
		if(mResultList.get(0).trim().equals("opendir failed, Permission denied")){
			Log.i("Payne", "1"+mResultList.get(0)+mResultList.size());
		}else{
			Log.i("Payne", "2"+mResultList.get(0)+mResultList.size());
			//isPass = true;
			cancelTimer();
			disableViews();
			try{
				Thread.sleep(2000);
			}catch(Exception e){
				e.printStackTrace();
			}
			mPassExitButton.performClick();
			//backToPQAA();
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == mPassExitButton) {
			Log.i("Payne", "prepare rw");
			isPass = true;
			mSDCardRWHandler = new WisStorageRW(this, WisStorageRW.FLAG_EXTERNAL_SDCARD, mSDPath);
			mSDCardRWHandler.setSDCardFileSize(100);
			mSDCardRWHandler.setSDCardFileNumber(20);
			mSDCardRWHandler.setRepeatTest(false);
			mSDCardRWHandler.setOnSDCardTestStateChangedListener(this);
			mSDCardRWHandler.start();
			Log.i("Payne", "start rw"+mSDPath);
		} else if (v == mFailExitButton) {
			isPass = false;
			if (mComponentMode) {
				displayResult();
			} else {
				backToPQAA();
			}
		} else if (v == mResultButton) {
			//20161018
			backToPQAA();
			//mSDCardRWHandler.stop();
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

	private void backToPQAA() {
		
		File file = null;
		if (isPass) {
			file = new File("/storage/sdcard0/OTG.PASS");
		} else {
			file = new File("/storage/sdcard0/OTG.FAIL");
		}
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mToolKit.returnWithResult(isPass);
	}
	// USBdisk test status listener
	//20161018
		@Override
		public void onStateIsDeleteProgressChanged(int flag, int progress) {
			// TODO Auto-generated method stub
			Log.i("Payne", "delete start...");
			mTestProgress.setProgress(progress);
		}

		@Override
		public void onStateIsDeleteStart(int flag) {
			// TODO Auto-generated method stub
			Log.i("Payne", "test delete...");
			mTestStatus.setText(mToolKit.getStringResource(R.string.sdcard_deleting));
		}

		@Override
		public void onStateIsResultFail(int flag) {
			// TODO Auto-generated method stub
			Log.i("Payne", "test fail...");
			isPass = false;
		}

		@Override
		public void onStateIsTestAbort(int flag, String exception) {
			// TODO Auto-generated method stub
			Log.i("Payne", "test abort...");
			isPass = false;
		}

		@Override
		public void onStateIsTestDone(int flag) {
			// TODO Auto-generated method stub
			Log.i("Payne", "test done...");
			mSDCardRWHandler.stop();
			backToPQAA();
		}
	@Override
	public void onStateIsTestProgressChanged(int flag, int progress) {
		// TODO Auto-generated method stub
		Log.i("Payne", "test progress...");
		mTestProgress.setProgress(progress);
	}

	@Override
	public void onStateIsTestStart(int flag) {
		// TODO Auto-generated method stub
		Log.i("Payne", "test start...");
		//disableViews();
		mTestStatus.setText(mToolKit.getStringResource(R.string.sdcard_testing));
	}
}