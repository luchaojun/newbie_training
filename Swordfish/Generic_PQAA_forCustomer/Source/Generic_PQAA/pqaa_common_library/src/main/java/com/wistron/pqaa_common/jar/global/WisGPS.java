package com.wistron.pqaa_common.jar.global;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class WisGPS {
	private static final int MSG_GPS_INTERVAL_ATTACHED=0;
	
	private Context context;
	private OnWisGPSOnOffStateChangedListener mGpsOnOffStateChangedListener;
	private LocationManager mLocationManager;

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

	public WisGPS(Context context) {
		super();
		this.context = context;
		initial();
	}

	private void initial() {
		// TODO Auto-generated method stub
		mScheduledTimer = Executors.newScheduledThreadPool(1);
		mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		mTempState=mState=mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}
	
	public boolean isHasGPSModule() {
		if (mLocationManager != null) {
			List<String> mProviders = mLocationManager.getAllProviders();
			if (mProviders != null && mProviders.contains(LocationManager.GPS_PROVIDER)) {
				return true;
			}
		}
		return false;
	}
	
	private void openGPS() {
		// TODO Auto-generated method stub
		Settings.Secure.setLocationProviderEnabled(context.getContentResolver(), LocationManager.GPS_PROVIDER, true);
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 1f, mLocationListener);
	}

	private void closeGPS() {
		Settings.Secure.setLocationProviderEnabled(context.getContentResolver(),LocationManager.GPS_PROVIDER, false);
	}
	
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if (msg.what == MSG_GPS_INTERVAL_ATTACHED) {
				mTempState = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
				if (mState != mTempState) {
					mState=mTempState;
					if (mState) {
						mGpsOnOffStateChangedListener.onStateIsSuccessTurnOn();
					}else {
						mGpsOnOffStateChangedListener.onStateIsSuccessTurnOff();
					}
				}else {
					isTempPass=false;
					if (!mState) {
						mGpsOnOffStateChangedListener.onStateIsFailTurnOn();
					}else {
						mGpsOnOffStateChangedListener.onStateIsFailTurnOff();
					}
				}
				toStartTestGPS();
			}
		}

	};
	
	private Runnable checkAirplaneState=new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			mTask.cancel(true);
			handler.sendEmptyMessage(MSG_GPS_INTERVAL_ATTACHED);
		}
	};
	
	private void toStartTestGPS() {
		// TODO Auto-generated method stub
		if (!isReverse) {
			if (mLeftTimesIndex != mInputLeftTimes) {
				if (isTempPass) {
					mPassCount++;
				}else {
					mFailCount++;
				}
				int[] result = new int[]{mInputLeftTimes,mPassCount,mFailCount};
				mGpsOnOffStateChangedListener.onStateIsUpdateResultSummary(result,isTempPass);
			}
			mLeftTimesIndex--;
			isTempPass=true;
			if (mLeftTimesIndex >= 0) {
				mGpsOnOffStateChangedListener.onStateIsUpdateTestLeftTimes(mLeftTimesIndex);
			}
		}
		isReverse=!isReverse;
		if (mLeftTimesIndex>=0) {
			if (mState) {
				mGpsOnOffStateChangedListener.onStateIsStartTurnOff();
				closeGPS();
			}else {
				mGpsOnOffStateChangedListener.onStateIsStartTurnOn();
				openGPS();
			}
			mTask=mScheduledTimer.schedule(checkAirplaneState, mInputInterval, TimeUnit.SECONDS);
		}else {
			stop();
		}
	}
	
	private void cancelTask(){
    	if (mTask!=null && !mTask.isDone() && !mTask.isCancelled()) {
			mTask.cancel(true);
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
	 * start to test GPS on/off
	 */
	public void start(){
		isReverse=false;
		mPassCount=mFailCount=0;
		toStartTestGPS();
	}
	
	/**
	 * stop current test item
	 */
	public void stop(){
		isReverse=false;
		cancelTask();
		mGpsOnOffStateChangedListener.onStateIsTestDone();
	}
	
	private LocationListener mLocationListener = new LocationListener() {

		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}

		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}

		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
		}

		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
		}
	};
	
	/**
	 * Register a callback to be invoked when test status changed during GPS switch test
	 * @param listener
	 * The callback that will run
	 */
	public void setOnWisGPSOnOffStateChangedListener(OnWisGPSOnOffStateChangedListener listener){
		this.mGpsOnOffStateChangedListener = listener;
	}
	
	/**
	 * @author dragon
	 * Interface definition for a callback to be invoked when test status changed during GPS switch test
	 */
	public abstract interface OnWisGPSOnOffStateChangedListener{
		/**
		 * start to turn on GPS module
		 */
		public abstract void onStateIsStartTurnOn();
		/**
		 * success to open GPS module
		 */
		public abstract void onStateIsSuccessTurnOn();
		/**
		 * fail to open GPS module
		 */
		public abstract void onStateIsFailTurnOn();
		/**
		 * start to turn off GPS module
		 */
		public abstract void onStateIsStartTurnOff();
		/**
		 * success to close GPS module
		 */
		public abstract void onStateIsSuccessTurnOff();
		/**
		 * fail to close GPS module
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
