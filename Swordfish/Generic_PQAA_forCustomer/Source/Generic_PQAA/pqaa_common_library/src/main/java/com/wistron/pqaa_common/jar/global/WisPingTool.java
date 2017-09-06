package com.wistron.pqaa_common.jar.global;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.wistron.pqaa_common.jar.global.WisShellCommandHelper.onResultChangedListener;

import java.util.ArrayList;

public class WisPingTool{
	private static final int MSG_PING_START = 0;
	private static final int MSG_PING_PROGRESS = 1;
	private static final int MSG_PING_FINISH = 2;
	private WisShellCommandHelper mShellCommandHelper;
	private Thread mPingThread;
	
	private OnWisPingStateChangedListener mPingStateChangedListener;
	private String mIPAddress;
	private int mTotalCount,mSleepSeconds;
	private boolean isDoingPing;
	
	public WisPingTool(Context context) {
		super();
		initial(context);
	}
	
	private void initial(Context context){
		mShellCommandHelper = new WisShellCommandHelper();
		mShellCommandHelper.setOnResultChangedListener(new onResultChangedListener() {
			
			@Override
			public void onResultChanged(String result) {
				// TODO Auto-generated method stub
				Message msg = new Message();
				msg.what = MSG_PING_PROGRESS;
				msg.obj = result;
				handler.sendMessage(msg);
			}
		});
		mPingThread = new Thread(ping);
	}
	
	/**
	 * Register a callback to be invoked when test status changed during ping test
	 * @param listener
	 * The callback that will run
	 */
	public void setOnWisPingStateChangedListener(OnWisPingStateChangedListener listener){
		this.mPingStateChangedListener = listener;
	}
	
	/**
	 * Return if ping action is doing
	 * @return
	 * Return ping status
	 */
	public boolean isDoingPing() {
		return isDoingPing;
	}

	/**
	 * start to ping the specified IP address with ping count and interval
	 * @param ipAddress
	 * the ping destination
	 * @param totalCount
	 * Specified the total count of Ping
	 * @param sleepSeconds
	 * The sleep time between ping operate
	 */
	public void doPing(String ipAddress,int totalCount,int sleepSeconds) {
		mIPAddress=ipAddress;
		mTotalCount=totalCount;
		mSleepSeconds=sleepSeconds;
		if (!isDoingPing) {
			mPingThread.start();
			isDoingPing = true;
		}
	}
	
	/**
	 * stop ping operate
	 */
	public void stopPing(){
		if (isDoingPing) {
			if (mShellCommandHelper != null) {
				mShellCommandHelper.destroy();
			}
			isDoingPing = false;
		}
	}
	
	private Runnable ping=new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			handler.sendEmptyMessage(MSG_PING_START);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ArrayList<String> pingResult = null;
			if (isDoingPing) {
				pingResult= mShellCommandHelper.exec(String.format("ping -c %1$d -i %2$d -w %3$d %4$s", mTotalCount, mSleepSeconds,(mTotalCount+1)*mSleepSeconds,mIPAddress));
			}
			Log.i("WisPing", "Handler message...");
			isDoingPing = false;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			int total=0,pass=0;
			if (pingResult != null) {
				for(String line : pingResult){
					if (line.contains("packets transmitted")) {
						String[] result=line.split(",");
						String transmitted=result[0].trim();
						String received = result[1].trim();
						total = Integer.parseInt(transmitted.substring(0, transmitted.indexOf(" ")));
						pass = Integer.parseInt(received.substring(0, received.indexOf(" ")));
						break;
					}
				}
				Message msg = new Message();
				msg.what = MSG_PING_FINISH;
				msg.obj = new int[]{total,pass};
				handler.sendMessage(msg);
			}
		}
	};
	
	private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if (msg.what == MSG_PING_START) {
				mPingStateChangedListener.onStateIsPingStart();
			}else if (msg.what == MSG_PING_PROGRESS) {
				mPingStateChangedListener.onStateIsPingProgress(msg.obj.toString());
			}else if (msg.what == MSG_PING_FINISH) {
				mPingStateChangedListener.onStateIsPingFinish((int[])msg.obj);
			}
		}
    	
    };
	
	/**
	 * @author dragon
	 * Interface definition for a callback to be invoked when test status changed during data ping test
	 */
	public abstract interface OnWisPingStateChangedListener{
		/**
		 * start to ping the specified address.
		 */
		public void onStateIsPingStart();
		/**
		 * you can refresh your UI during ping with current result and rssi
		 * @param result
		 * the ping result of current time
		 * @param rssi
		 * the device RSSI value
		 */
		public abstract void onStateIsPingProgress(String result);
		/**
		 * Final ping test Result with total ping count and pass count
		 * @param pingResult
		 * pingResult[0]: total ping count; pingResult[1]: pass count
		 */
		public abstract void onStateIsPingFinish(int[] pingResult);
	}
}
