package com.wistron.pqaa_common.jar.global;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class WisWifi{
	public static final int SECURITY_NONE = 0;
	public static final int SECURITY_WEP = 1;
	public static final int SECURITY_PSK = 2;
	public static final int SECURITY_EAP = 3;
	
	private static final int MSG_WIFI_INTERVAL_ATTACHED = 0;
	
	private Context context;
	private OnWisWifiOnOffStateChangedListener mWifiOnOffStateChangedListener;
	private ConnectivityManager mConnectivityManager;
	private WifiManager mWifiManager;
	private String HOTSPOT_SSID="";
	
	// On/Off
	private ScheduledExecutorService mScheduledTimer;
	private ScheduledFuture<?> mTask;
	private boolean isReverse;
    private int mInputInterval;
    private int mInputLeftTimes;
    private int mLeftTimesIndex;
    private int mPassCount,mFailCount;
    private boolean isTempPass = true;  // the temp result during on/off test
	private int mState,mTempState;
	private boolean isRegisterBroadcast=false;
	
	/**
	 * WisWifi constructed function
	 * @param context
	 * context
	 */
	public WisWifi(Context context) {
		super();
		this.context = context;
		initial();
	}

	private void initial() {
		// TODO Auto-generated method stub
		mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		
		mScheduledTimer = Executors.newScheduledThreadPool(1);
		if (mWifiManager == null || isWifiUnknownState()) {
			
		}else {
			mTempState=mState=mWifiManager.getWifiState();
		}
	}
	
	/**
	 * If the Wifi state is unknown
	 * @return
	 * Return if the Wifi state is unknown
	 */
	public boolean isWifiUnknownState() {
		int mWifiState = mWifiManager.getWifiState();
		if (mWifiState == WifiManager.WIFI_STATE_UNKNOWN) {
			return true;
		}
		return false;
	}
	
	/**
	 * To open wifi, you should listen the wifi state if it is WIFI_STATE_UNKNOWN.
	 */
	public void openWifi(){
		int mWifiState = mWifiManager.getWifiState();
		if (mWifiState == WifiManager.WIFI_STATE_UNKNOWN) {
			
		}else {
			if (mWifiState != WifiManager.WIFI_STATE_ENABLED && mWifiState != WifiManager.WIFI_STATE_ENABLING) {
				mWifiManager.setWifiEnabled(true);
			}
		}
	}
	
	/**
	 * To close wifi
	 */
	public void closeWifi(){
		disCurrentConnect();
		
		int mWifiState = mWifiManager.getWifiState();
		if (mWifiState != WifiManager.WIFI_STATE_DISABLING && mWifiState != WifiManager.WIFI_STATE_DISABLED) {
			mWifiManager.setWifiEnabled(false);
		}
	}
	
	/**
	 * Disconnect current wifi connect.
	 */
	public void disCurrentConnect() {
		// TODO Auto-generated method stub
		if (mWifiManager != null) {
			List<WifiConfiguration> mWifiAPList = mWifiManager.getConfiguredNetworks();
			if (mWifiAPList != null) {
				for (WifiConfiguration mCurWifi : mWifiAPList) {
					if (mCurWifi.SSID.replace("\"", "").equals(HOTSPOT_SSID.replace("\"", ""))) {
						mWifiManager.disableNetwork(mCurWifi.networkId);
						mWifiManager.removeNetwork(mCurWifi.networkId);
						Log.i("WisWifi", "disconnect the wifi hotpot: "+HOTSPOT_SSID);
					}
				}
				mWifiManager.disconnect();
				mWifiManager.saveConfiguration();
			}
		}
	}
	
	/**
	 * Return whether Wi-Fi is can use.
	 * @return
	 * whether Wi-Fi is can use.
	 */
	public boolean isWifiCanUse(){
		return mWifiManager != null;
	}
	
	/**
	 * Return whether Wi-Fi is enabled or disabled.
	 * @return
	 * whether Wi-Fi is enabled or disabled.
	 */
	public boolean isWifiOn(){
		return mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED;
	}
	
	/**
	 * Gets the Wi-Fi enabled state.
	 * @return
	 * One of 
	 * {@link #WIFI_STATE_DISABLED},
	 * {@link #WIFI_STATE_DISABLING},
	 * {@link #WIFI_STATE_ENABLED},
	 * {@link #WIFI_STATE_ENABLING},
	 * {@link #WIFI_STATE_UNKNOWN}
	 */
	public int getWifiState(){
		return mWifiManager.getWifiState();
	}
	
	/**
	 * Return the wifi connect state
	 * @return
	 * the wifi connect state
	 */
	public State getWifiConnectState(){
		return mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
	}
	
	/**
	 * Return the SSID of the connected wifi hotspot
	 * @return
	 * the SSID of the connected wifi hotspot
	 */
	public String getConnectApSSID(){
		return mWifiManager.getConnectionInfo().getSSID();
	}
	
	/**
	 * Return Wifi MAC address
	 * @return
	 * Wifi MAC address, return null if can't get the address.
	 */
	public String getMacAddress() {
		// TODO Auto-generated method stub
		mWifiManager.reassociate();
		WifiInfo mInfo = mWifiManager.getConnectionInfo();
		String mMacAddress=mInfo.getMacAddress();
		if (mMacAddress != null) {
			return mMacAddress.toUpperCase();
		}else {
			return mMacAddress;
		}
	}
	
	/**
	 * Return the Wifi Rssi value, return -200 if wifi is unavailable or can't get wifi connection;
	 * @return
	 * Returns the received signal strength indicator of the current 802.11 network.
	 */
	public int getWifiRssi(){
		int rssi = -200;
		if (mWifiManager != null) {
			WifiInfo mWifiInfo=mWifiManager.getConnectionInfo();
			if (mWifiInfo != null) {
				rssi = mWifiInfo.getRssi();
			}
		}
		return rssi;
	}
	
	/**
	 * Connect the specified wifi ap with ssid,security and password
	 * @param ssid
	 * The network's SSID.
	 * @param security
	 * The security type:none,wep,psk
	 * @param password
	 * Pre-shared key for use 
	 */
	public void connectAp(String ssid,int security,String password){
		WifiConfiguration config = getConfig(ssid,security,password);
		if (config != null) {
			int mNetworkID = mWifiManager.addNetwork(config);
			Log.i("Wireless", mNetworkID + " connected");
			if (mNetworkID != -1) {
				mWifiManager.enableNetwork(mNetworkID, true);
				config.networkId = mNetworkID;
				mWifiManager.saveConfiguration();
			}
		}
	}
	
	private WifiConfiguration getConfig(String ssid,int security,String password) {
		if (ssid == null || ssid.equals("")) {
			return null;
		}
		WifiConfiguration config = new WifiConfiguration();
		config.SSID = "\"" + ssid + "\"";
		HOTSPOT_SSID = ssid;

		switch (security) {
		case SECURITY_NONE:
			config.allowedKeyManagement.set(KeyMgmt.NONE);
			break;
		case SECURITY_WEP:
			config.allowedKeyManagement.set(KeyMgmt.NONE);
			if (password != null && password.length() != 0) {
				config.wepKeys[0] = "\"" + password + "\"";
			}
			break;
		case SECURITY_PSK:
			config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
			if (password != null && password.length() != 0) {
				config.preSharedKey = "\"" + password + "\"";
			}
			break;
		case SECURITY_EAP:
			break;
		}
		return config;
	}
	
	private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if (msg.what == MSG_WIFI_INTERVAL_ATTACHED) {
				if (mState != mTempState) {
					mState=mTempState;
					if (mState == WifiManager.WIFI_STATE_ENABLED) {
						mWifiOnOffStateChangedListener.onStateIsSuccessTurnOn();
					}else if(mState == WifiManager.WIFI_STATE_DISABLED){
						mWifiOnOffStateChangedListener.onStateIsSuccessTurnOff();
					}else {
						isTempPass = false;
						mWifiOnOffStateChangedListener.onStateIsInvalidStatus(mState);
					}
				}else {
					isTempPass = false;
					if (mState == WifiManager.WIFI_STATE_DISABLED) {
						mWifiOnOffStateChangedListener.onStateIsFailTurnOn();
					}else if(mState == WifiManager.WIFI_STATE_ENABLED){
						mWifiOnOffStateChangedListener.onStateIsFailTurnOff();
					}else {
						mWifiOnOffStateChangedListener.onStateIsInvalidStatus(mState);
					}
				}
				toStartTestWifi();
			}
		}
    	
    };
	
///////////////////////  Wifi On/Off test    ///////////////////////////////////
    private Runnable checkWifiState=new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			mTask.cancel(true);
			handler.sendEmptyMessage(MSG_WIFI_INTERVAL_ATTACHED);
		}
	};
    
	private void toStartTestWifi() {
		// TODO Auto-generated method stub
		if (!isReverse) {
			if (mLeftTimesIndex != mInputLeftTimes) {
				if (isTempPass) {
					mPassCount++;
				}else {
					mFailCount++;
				}
				int[] result = new int[]{mInputLeftTimes,mPassCount,mFailCount};
				mWifiOnOffStateChangedListener.onStateIsUpdateResultSummary(result,isTempPass);
			}
			mLeftTimesIndex--;
			isTempPass = true;
			if (mLeftTimesIndex >= 0) {
				mWifiOnOffStateChangedListener.onStateIsUpdateTestLeftTimes(mLeftTimesIndex);
			}
		}
		isReverse=!isReverse;
		if (mLeftTimesIndex>=0) {
			if (mState == WifiManager.WIFI_STATE_ENABLED) {
				mWifiOnOffStateChangedListener.onStateIsStartTurnOff();
				mWifiManager.setWifiEnabled(false);
			}else if(mState == WifiManager.WIFI_STATE_DISABLED){
				mWifiOnOffStateChangedListener.onStateIsStartTurnOn();
				mWifiManager.setWifiEnabled(true);
			}else {
				mWifiOnOffStateChangedListener.onStateIsInvalidStatus(mState);
			}
			mTask=mScheduledTimer.schedule(checkWifiState, mInputInterval, TimeUnit.SECONDS);
		}else {
			stop();
		}
	}
	
	private BroadcastReceiver wifiState = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
				int state=intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLED);
				if (state == WifiManager.WIFI_STATE_ENABLED || state == WifiManager.WIFI_STATE_DISABLED) {
					mTempState=state;
				}
			}
		}
	};
	
    private void registerWifiBroadcast() {
		// TODO Auto-generated method stub
    	IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		context.registerReceiver(wifiState, mFilter);
		isRegisterBroadcast=true;
	}

    private void unRegisterWifiBroadcast(){
    	cancelTask();
    	if (isRegisterBroadcast) {
    		context.unregisterReceiver(wifiState);
			isRegisterBroadcast=false;
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
	 * start to test Wifi on/off
	 */
	public void start(){
		isReverse=false;
		mPassCount=mFailCount=0;
		registerWifiBroadcast();
		toStartTestWifi();
	}
	
	/**
	 * stop current test item
	 */
	public void stop(){
		isReverse=false;
		unRegisterWifiBroadcast();
		mWifiOnOffStateChangedListener.onStateIsTestDone();
	}
	
	/**
	 * Register a callback to be invoked when test status changed during Wifi ping test
	 * @param listener
	 * The callback that will run
	 */
	public void setOnWisWifiOnOffStateChangedListener(OnWisWifiOnOffStateChangedListener listener){
		this.mWifiOnOffStateChangedListener = listener;
	}
	
	/**
	 * @author dragon
	 * Interface definition for a callback to be invoked when test status changed during Wifi mode switch test
	 */
	public abstract interface OnWisWifiOnOffStateChangedListener{
		/**
		 * can not check the correct Wifi state
		 * @param state
		 * current Wifi state
		 */
		public abstract void onStateIsInvalidStatus(int state);
		/**
		 * start to turn on Wifi
		 */
		public abstract void onStateIsStartTurnOn();
		/**
		 * success to turn on Wifi
		 */
		public abstract void onStateIsSuccessTurnOn();
		/**
		 * fail to turn on Wifi
		 */
		public abstract void onStateIsFailTurnOn();
		/**
		 * start to turn off Wifi
		 */
		public abstract void onStateIsStartTurnOff();
		/**
		 * success to turn off Wifi
		 */
		public abstract void onStateIsSuccessTurnOff();
		/**
		 * fail to turn off Wifi
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
