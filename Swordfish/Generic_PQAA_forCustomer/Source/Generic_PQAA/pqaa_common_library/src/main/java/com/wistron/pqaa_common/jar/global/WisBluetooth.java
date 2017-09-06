package com.wistron.pqaa_common.jar.global;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class WisBluetooth {
	private static final int MSG_BT_INTERVAL_ATTACHED=0;
	
	/**
	 * Broadcast extra name, indicates the name of device
	 */
	public static final String BLUETOOTH_EXTRA_DATA_DEVICE_NAME = "name";
	/**
	 * Broadcast extra address, indicates the address of device
	 */
	public static final String BLUETOOTH_EXTRA_DATA_DEVICE_ADDRESS = "address";
	
	private Context context;
	private OnWisBTScanStateChangedListener mBtScanStateChangedListener;
	private OnWisBTOnOffStateChangedListener mBtOnOffStateChangedListener;
	private boolean isStart;
	private BluetoothAdapter mBTAdapter;
	
	// Search
	private ArrayList<Map<String, String>> mPairedList;
	private ArrayList<Map<String, String>> mNewDevicesList;
	
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
	
	public WisBluetooth(Context context) {
		super();
		this.context = context;
		initial();
	}

	private void initial() {
		// TODO Auto-generated method stub
		mPairedList = new ArrayList<Map<String,String>>();
		mNewDevicesList = new ArrayList<Map<String,String>>();
		
		mBTAdapter = BluetoothAdapter.getDefaultAdapter();
		
		mScheduledTimer = Executors.newScheduledThreadPool(1);
		if (mBTAdapter == null || (mBTAdapter.getState()!=BluetoothAdapter.STATE_ON && mBTAdapter.getState()!=BluetoothAdapter.STATE_OFF)) {
		
		}else {
			mTempState=mState=mBTAdapter.getState();
		}
	}
	
	/**
	 * Return whether Bluetooth is can use.
	 * @return
	 * Return whether Bluetooth is can use.
	 */
	public boolean isBluetoothCanUse(){
		return mBTAdapter != null;
	}
	
	/**
	 * Return if the status of Bluetooth is valid: on or off
	 * @return
	 * Return if the status of Bluetooth is valid: on or off
	 */
	public boolean isBluetoothStateValid(){
		return mBTAdapter.getState()!=BluetoothAdapter.STATE_ON && mBTAdapter.getState()!=BluetoothAdapter.STATE_OFF;
	}
	
	/**
	 * Return true if Bluetooth is currently enabled and ready for use.
	 * @return
	 * Return true if Bluetooth is currently enabled and ready for use.
	 */
	public boolean isBluetoothEnable(){
		return mBTAdapter.isEnabled();
	}
	
	/**
	 * Open bluetooth
	 */
	public void openBluetooth(){
		mBTAdapter.enable();
	}
	
	/**
	 * Close Bluetooth
	 */
	public void closeBluetooth(){
		mBTAdapter.disable();
	}
	
	/**
	 * Returns the hardware address of the local Bluetooth adapter.
	 * @return
	 * Returns the hardware address of the local Bluetooth adapter.
	 */
	public String getBluetoothAddress(){
		return mBTAdapter.getAddress();
	}
	
	/**
	 * Get a handle to the default local Bluetooth adapter
	 * @return
	 * the default local adapter, or null if Bluetooth is not supported on this hardware platform
	 */
	public BluetoothAdapter getBluetoothAdapter(){
		return mBTAdapter;
	}
	
	/**
	 * Get the current state of the local Bluetooth adapter.
	 * <p>
	 * Possible return values are 
	 * {@link #STATE_OFF},
	 * {@link #STATE_TURNING_ON},
	 * {@link #STATE_ON},
	 * {@link #STATE_TURNING_OFF}.
	 * @return current state of Bluetooth adapter
	 */
	public int getBluetoothState(){
		return mBTAdapter.getState();
	}
	
	private BroadcastReceiver mNewDeviceReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			// When discovery finds a device
			if (action.equals(BluetoothDevice.ACTION_FOUND)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// If it's already paired, skip it, because it's been listed already
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					Map<String, String> mTempMap = new HashMap<String, String>();
					mTempMap.put(BLUETOOTH_EXTRA_DATA_DEVICE_NAME, device.getName());
					mTempMap.put(BLUETOOTH_EXTRA_DATA_DEVICE_ADDRESS, device.getAddress());
					mNewDevicesList.add(mTempMap);
				}
				mBtScanStateChangedListener.onStateIsFoundNewDevice(mNewDevicesList);
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				mBtScanStateChangedListener.onStateIsScanFinish();
				if (isStart) {
					isStart=false;
					unregisterDiscoveryBroadcast();
				}
			}
		}
	};
	
	private void registerDiscoveryBroadcast(){
		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(BluetoothDevice.ACTION_FOUND);
		mFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		context.registerReceiver(mNewDeviceReceiver, mFilter);
	}
	
	private void unregisterDiscoveryBroadcast(){
		context.unregisterReceiver(mNewDeviceReceiver);
	}
	
	/**
	 * Start to discovery bluetooth device
	 */
	public void doDiscovery() {
		// TODO Auto-generated method stub
		if (!isStart) {
			isStart=true;
			registerDiscoveryBroadcast();
		}
		if (isBluetoothEnable()) {
			mBtScanStateChangedListener.onStateIsStartDiscovery();
			
			mBtScanStateChangedListener.onStateIsStartFindPairedDevices();
			searchPairedDevice();
			
			mBtScanStateChangedListener.onStateIsStartFindNewDevices();
			searchNewDevice();
		}
	}
	
	/**
	 * Cancel to discovery bluetooth device
	 */
	public void cancelDiscovery() {
		// TODO Auto-generated method stub
		// Make sure we're not doing discovery anymore
		if (mBTAdapter != null) {
			if (mBTAdapter.isDiscovering()) {
				mBTAdapter.cancelDiscovery();
				Log.i("WisBluetooth", "cancelDiscovery...");
			}
		}
	}
	
	/**
	 * Return true if the local Bluetooth adapter is currently in the device discovery process.
	 * @return
	 * Return true if the local Bluetooth adapter is currently in the device discovery process.
	 */
	public boolean isDiscovering(){
		if (mBTAdapter != null) {
			return mBTAdapter.isDiscovering();
		}else {
			return false;
		}
	}
	
	private void searchPairedDevice() {
		// TODO Auto-generated method stub
		mPairedList.clear();
		// Get a set of currently paired devices
		Set<BluetoothDevice> pairedDevices = mBTAdapter.getBondedDevices();
		// If there are paired devices, add each one to the ArrayAdapter
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				Map<String, String> mTempMap = new HashMap<String, String>();
				mTempMap.put(BLUETOOTH_EXTRA_DATA_DEVICE_NAME, device.getName());
				mTempMap.put(BLUETOOTH_EXTRA_DATA_DEVICE_ADDRESS, device.getAddress());
				mPairedList.add(mTempMap);
			}
		}
		mBtScanStateChangedListener.onStateIsFoundPairedDevices(mPairedList);
	}

	private void searchNewDevice() {
		// TODO Auto-generated method stub
		if (mBTAdapter.isDiscovering()) {
			mBTAdapter.cancelDiscovery();
		}
		mBTAdapter.startDiscovery();
	}
	
////     On/Off test   ////
    private Runnable checkBTState=new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			mTask.cancel(true);
			handler.sendEmptyMessage(MSG_BT_INTERVAL_ATTACHED);
		}
	};
	
    private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if (msg.what==MSG_BT_INTERVAL_ATTACHED) {
				if (mState != mTempState) {
					mState=mTempState;
					if (mState == BluetoothAdapter.STATE_ON) {
						mBtOnOffStateChangedListener.onStateIsSuccessTurnOn();
					} else if(mState == BluetoothAdapter.STATE_OFF){
						mBtOnOffStateChangedListener.onStateIsSuccessTurnOff();
					} else {
						isTempPass = false;
						mBtOnOffStateChangedListener.onStateIsInvalidStatus(mState);
					}
				}else {
					isTempPass = false;
					if (mState == BluetoothAdapter.STATE_OFF) {
						mBtOnOffStateChangedListener.onStateIsFailTurnOn();
					} else if(mState == BluetoothAdapter.STATE_ON){
						mBtOnOffStateChangedListener.onStateIsFailTurnOff();
					} else {
						mBtOnOffStateChangedListener.onStateIsInvalidStatus(mState);
					}
				}
				toStartTestBT();
			}
		}
    	
    };
    
	private void toStartTestBT() {
		// TODO Auto-generated method stub
		if (!isReverse) {
			if (mLeftTimesIndex != mInputLeftTimes) {
				if (isTempPass) {
					mPassCount++;
				}else {
					mFailCount++;
				}
				int[] result = new int[]{mInputLeftTimes,mPassCount,mFailCount};
				mBtOnOffStateChangedListener.onStateIsUpdateResultSummary(result,isTempPass);
			}
			mLeftTimesIndex--;
			isTempPass = true;
			if (mLeftTimesIndex >= 0) {
				mBtOnOffStateChangedListener.onStateIsUpdateTestLeftTimes(mLeftTimesIndex);
			}
		}
		isReverse=!isReverse;
		if (mLeftTimesIndex>=0) {
			if (mState == BluetoothAdapter.STATE_ON) {
				mBtOnOffStateChangedListener.onStateIsStartTurnOff();
				mBTAdapter.disable();
			}else if(mState == BluetoothAdapter.STATE_OFF){
				mBtOnOffStateChangedListener.onStateIsStartTurnOn();
				mBTAdapter.enable();
			}else {
				mBtOnOffStateChangedListener.onStateIsInvalidStatus(mState);
			}
			mTask=mScheduledTimer.schedule(checkBTState, mInputInterval, TimeUnit.SECONDS);
		}else {
			stop();
		}
	}
	
    // Broadcast
	private BroadcastReceiver bluetoothState = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				int state=intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
				if (state == BluetoothAdapter.STATE_ON ||state == BluetoothAdapter.STATE_OFF) {
					mTempState=state;
				}
			}
		}
	};
	
	private void registerBTBroadcast() {
		// TODO Auto-generated method stub
    	IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		context.registerReceiver(bluetoothState, mFilter);
		isRegisterBroadcast=true;
	}

    private void unRegisterBTBroadcast(){
    	cancelTask();
    	if (isRegisterBroadcast) {
    		context.unregisterReceiver(bluetoothState);
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
	 * start to test Bluetooth on/off
	 */
	public void start(){
		isReverse=false;
		mPassCount=mFailCount=0;
		registerBTBroadcast();
		toStartTestBT();
	}
	
	/**
	 * stop current test item
	 */
	public void stop(){
		isReverse=false;
		unRegisterBTBroadcast();
		mBtOnOffStateChangedListener.onStateIsTestDone();
	}
	
	/**
	 * Register a callback to be invoked when test status changed during Bluetooth scanning
	 * @param listener
	 * The callback that will run
	 */
	public void setOnWisBTScanStateChangedListener(OnWisBTScanStateChangedListener listener){
		this.mBtScanStateChangedListener = listener;
	}
	
	/**
	 * Register a callback to be invoked when test status changed during Bluetooth mode switch test
	 * @param listener
	 * The callback that will run
	 */
	public void setOnWisBTOnOffStateChangedListener(OnWisBTOnOffStateChangedListener listener){
		this.mBtOnOffStateChangedListener = listener;
	}
	
	/**
	 * @author dragon
	 * Interface definition for a callback to be invoked when test status changed during Bluetooth scanning
	 */
	public abstract interface OnWisBTScanStateChangedListener{
		/**
		 * start discovery action
		 */
		public abstract void onStateIsStartDiscovery();
		/**
		 * start to find the paired bluetooth devices
		 */
		public abstract void onStateIsStartFindPairedDevices();
		/**
		 * found the paired devices
		 * @param pairedList
		 * the paired devices list
		 */
		public abstract void onStateIsFoundPairedDevices(ArrayList<Map<String, String>> pairedList);
		/**
		 * start to find the new bluetooth devices around current device
		 */
		public abstract void onStateIsStartFindNewDevices();
		/**
		 * found the new devices
		 * @param newDeviceList
		 * the new devices list
		 */
		public abstract void onStateIsFoundNewDevice(ArrayList<Map<String, String>> newDeviceList);
		/**
		 * end to scan test
		 */
		public abstract void onStateIsScanFinish();
	}
	
	/**
	 * @author dragon
	 * Interface definition for a callback to be invoked when test status changed during Bluetooth mode switch test
	 */
	public abstract interface OnWisBTOnOffStateChangedListener{
		/**
		 * can not check the correct bluetooth state
		 * @param state
		 * current bluetooth state
		 */
		public abstract void onStateIsInvalidStatus(int state);
		/**
		 * start to turn on BT
		 */
		public abstract void onStateIsStartTurnOn();
		/**
		 * success to turn on BT
		 */
		public abstract void onStateIsSuccessTurnOn();
		/**
		 * fail to turn on BT
		 */
		public abstract void onStateIsFailTurnOn();
		/**
		 * start to turn off BT
		 */
		public abstract void onStateIsStartTurnOff();
		/**
		 * success to turn off BT
		 */
		public abstract void onStateIsSuccessTurnOff();
		/**
		 * fail to turn off BT
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
