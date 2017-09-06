package com.wistron.pqaa_common.jar.global;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class WisAirplane {
	private static final int MSG_AIRPLANE_INTERVAL_ATTACHED=0;
	
	private Context context;
	private OnWisAirplaneOnOffStateChangedListener mWisAirplaneStateListener;

	// On/Off
	private ScheduledExecutorService mScheduledTimer;
	private ScheduledFuture<?> mTask;
	private boolean isReverse;
    private int mInputInterval;
    private int mInputLeftTimes;
    private int mLeftTimesIndex;
    private int mPassCount,mFailCount;
    private boolean isTempPass = true;  // the temp result during on/off test
	private boolean mState,mTempState;
	private boolean isRegisterBroadcast=false;

	public WisAirplane(Context context) {
		super();
		this.context = context;
		initial();
	}

	private void initial() {
		// TODO Auto-generated method stub
		mScheduledTimer = Executors.newScheduledThreadPool(1);
		mTempState=mState=isAirplaneModeOn();
	}

	public boolean isAirplaneModeOn() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
			return Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0;
		}else {
			return Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
		}
	}
	
	/**
	 * Switch Airplane mode
	 */
	public void setAirplaneMode(boolean turnOn) {
		// Change the system setting
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
			Settings.System.putInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, turnOn ? 1 : 0);
		}else {
			Settings.Global.putInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, turnOn ? 1 : 0);
		}

		// Post the intent
		Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		intent.putExtra("state", turnOn);
		context.sendBroadcast(intent);
	}
	
	private Runnable checkAirplaneState=new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			mTask.cancel(true);
			handler.sendEmptyMessage(MSG_AIRPLANE_INTERVAL_ATTACHED);
		}
	};

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if (msg.what == MSG_AIRPLANE_INTERVAL_ATTACHED) {
				if (mState != mTempState) {
					mState=mTempState;
					if (mState) {
						mWisAirplaneStateListener.onStateIsSuccessTurnOn();
					}else {
						mWisAirplaneStateListener.onStateIsSuccessTurnOff();
					}
				}else {
					isTempPass=false;
					if (!mState) {
						mWisAirplaneStateListener.onStateIsFailTurnOn();
					}else {
						mWisAirplaneStateListener.onStateIsFailTurnOff();
					}
				}
				toStartTestAirplane();
			}
		}

	};
	
	private void toStartTestAirplane() {
		// TODO Auto-generated method stub
		if (!isReverse) {
			if (mLeftTimesIndex != mInputLeftTimes) {
				if (isTempPass) {
					mPassCount++;
				}else {
					mFailCount++;
				}
				int[] result = new int[]{mInputLeftTimes,mPassCount,mFailCount};
				mWisAirplaneStateListener.onStateIsUpdateResultSummary(result,isTempPass);
			}
			mLeftTimesIndex--;
			isTempPass=true;
			if (mLeftTimesIndex >= 0) {
				mWisAirplaneStateListener.onStateIsUpdateTestLeftTimes(mLeftTimesIndex);
			}
		}
		isReverse=!isReverse;
		if (mLeftTimesIndex>=0) {
			if (mState) {
				mWisAirplaneStateListener.onStateIsStartTurnOff();
				setAirplaneMode(false);
			}else {
				mWisAirplaneStateListener.onStateIsStartTurnOn();
				setAirplaneMode(true);
			}
			mTask=mScheduledTimer.schedule(checkAirplaneState, mInputInterval, TimeUnit.SECONDS);
		}else {
			stop();
		}
	}
	
	// Broadcast
	private BroadcastReceiver airplaneState = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
				mTempState=intent.getBooleanExtra("state", false);
			}
		}
	};
	
    private void cancelTask(){
    	if (mTask!=null && !mTask.isDone() && !mTask.isCancelled()) {
			mTask.cancel(true);
		}
    }
    
    private void registerAirplaneBroadcast() {
		// TODO Auto-generated method stub
    	IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		context.registerReceiver(airplaneState, mFilter); 
		isRegisterBroadcast=true;
	}

    private void unRegisterAirplaneBroadcast(){
    	cancelTask();
    	if (isRegisterBroadcast) {
    		context.unregisterReceiver(airplaneState);
			isRegisterBroadcast=false;
		}
    }
    
	/**
	 * For RF On/Off test, specify the test interval and left times
	 * @param interval
	 * the interval time in seconds between on and off
	 * @param times
	 * the test times
	 */
	public void setTestIntervalAndLeftTimes(int interval,int times){
		this.mInputInterval=interval;
		this.mInputLeftTimes=times;
		this.mLeftTimesIndex=times;
	}
	
	/**
	 * start to test Airplane on/off
	 */
	public void start(){
		isReverse=false;
		mPassCount=mFailCount=0;
		registerAirplaneBroadcast();
		toStartTestAirplane();
	}
	
	/**
	 * stop current test item
	 */
	public void stop(){
		isReverse=false;
		unRegisterAirplaneBroadcast();
		mWisAirplaneStateListener.onStateIsTestDone();
	}
	
	/**
	 * Register a callback to be invoked when test status changed during Airplane mode switch test
	 * @param listener
	 * The callback that will run
	 */
	public void setOnWisAirplaneStateChangedListener(OnWisAirplaneOnOffStateChangedListener listener){
		this.mWisAirplaneStateListener = listener;
	}
	
	/**
	 * @author dragon
	 * Interface definition for a callback to be invoked when test status changed during Airplane mode switch test
	 */
	public abstract interface OnWisAirplaneOnOffStateChangedListener{
		/**
		 * start to turn on Airplane mode
		 */
		public abstract void onStateIsStartTurnOn();
		/**
		 * success to open Airplane mode
		 */
		public abstract void onStateIsSuccessTurnOn();
		/**
		 * fail to open Airplane mode
		 */
		public abstract void onStateIsFailTurnOn();
		/**
		 * start to turn off Airplane mode
		 */
		public abstract void onStateIsStartTurnOff();
		/**
		 * success to close Airplane mode
		 */
		public abstract void onStateIsSuccessTurnOff();
		/**
		 * fail to close Airplane mode
		 */
		public abstract void onStateIsFailTurnOff();
		/**
		 * you can refresh your UI with this method, indicates the left test time
		 * @param leftTimes
		 * the left test time
		 */
		public abstract void onStateIsUpdateTestLeftTimes(int leftTimes);
		/**
		 * you can refresh your UI with this method, indicates the total test times,the pass times and the fail times 
		 * @param resultSummary
		 * resultSummary[0]: the total test times; 
		 * resultSummary[1]: the pass times; 
		 * resultSummary[2]: the fail times
		 * 
		 * @param resultOfCurCycle
		 * the result of current cycle test
		 */
		public abstract void onStateIsUpdateResultSummary(int[] resultSummary, boolean resultOfCurCycle);
		/**
		 * end to test
		 */
		public abstract void onStateIsTestDone();
	}
}
