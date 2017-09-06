package com.wistron.pqaa_common.jar.audio;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class WisAudioDecode {
	private OnAudioDecodeListener mListener;
	
	private Context context;
	private RecordTask recordTask;
	private RecognizerTask recognizerTask;
	private BlockingQueue<DataBlock> blockingQueue;
	private AudioFrequency mCurFrequency;
	private ArrayList<Tone> mDTMFTones;
	private boolean isStarted;
	
	public WisAudioDecode(Context context) {
		super();
		this.context = context;
		initial();
	}

	private void initial() {
		// TODO Auto-generated method stub
		mDTMFTones = new ArrayList<Tone>();
		mDTMFTones.add(new Tone(45, 77, '1'));    
	    mDTMFTones.add(new Tone(45, 86, '2'));    
	    mDTMFTones.add(new Tone(45, 95, '3'));
	    mDTMFTones.add(new Tone(45, 104, 'A'));
	    mDTMFTones.add(new Tone(49, 77, '4'));    
	    mDTMFTones.add(new Tone(49, 86, '5'));    
	    mDTMFTones.add(new Tone(49, 95, '6'));  
	    mDTMFTones.add(new Tone(49, 104, 'B'));
	    mDTMFTones.add(new Tone(55, 77, '7'));    
	    mDTMFTones.add(new Tone(55, 86, '8'));    
	    mDTMFTones.add(new Tone(55, 95, '9')); 
	    mDTMFTones.add(new Tone(55, 104, 'C'));
	    mDTMFTones.add(new Tone(60, 77, '*'));    
	    mDTMFTones.add(new Tone(60, 86, '0'));    
	    mDTMFTones.add(new Tone(60, 95, '#')); 
	    mDTMFTones.add(new Tone(60, 104, 'D'));
	    
	    mCurFrequency = new AudioFrequency(0, 0);
	}

	/**
	 * Set the audio decode listener, you should implements this listener for AudioLoopback test
	 * @param listener
	 * AudioDecodeListener
	 */
	public void setOnAudioDecodeListener(OnAudioDecodeListener listener){
		this.mListener=listener;
	}
	
	/**
	 * start to AudioLoopback test
	 */
	public void start(){
		if (!isStarted) {
			blockingQueue = new LinkedBlockingQueue<>();

			isStarted = true;

			recordTask = new RecordTask(this,context, blockingQueue);
//			recordTask.execute();
			recordTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

			recognizerTask = new RecognizerTask(this,context, blockingQueue);
//			recognizerTask.execute();
			recognizerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}
	
	/**
	 * stop test
	 */
	public void stop(){
		if (recognizerTask != null) {
			recognizerTask.cancel(true);
		}
		if (recordTask != null) {
			recordTask.cancel(true);
		}
		isStarted = false;
	}
	
	/**
	 * @return
	 * Return if being decode test
	 */
	public boolean isStarted(){
		return isStarted;
	}
	
	protected void decodeResult(AudioFrequency frequency){
//		Log.i("WisAudioDecode", "decodeResult"+frequency+" ~~ "+mCurFrequency);
		if (frequency != null && mCurFrequency != null) {
//			Log.i("WisAudioDecode", "!=null");
//			Log.i("WisAudioDecode", frequency.getLowFrequency() + ": "+mCurFrequency.getLowFrequency()+","
//									+frequency.getHighFrequency()+"~~~~"+mCurFrequency.getHighFrequency());
			if (Math.abs(frequency.getLowFrequency() - mCurFrequency.getLowFrequency()) > 2 
					|| Math.abs(frequency.getHighFrequency() - mCurFrequency.getHighFrequency()) > 2) {
				mListener.onAudioDecode(frequency.getLowFrequency(), frequency.getHighFrequency(), frequency.getDecibel());
				Log.i("WisAudioDecode", frequency.getLowFrequency()+" ~~ "+frequency.getHighFrequency());
			}
		}
		mCurFrequency.resetFrequency();
	}
	
	/**
	 * Decode the DTMF tone with frequency
	 * @param low
	 * low frequency
	 * @param high
	 * high frequency
	 * @return
	 * Return the decoded DTMF tone, default return ' '(32)
	 */
	public char decodeDTMFToneWithFrequency(int low,int high){
		for (Tone t : mDTMFTones) {
			if (t.match(low, high)) {
				return t.getKey();
			}
		}
		return ' ';
	}
	
	/**
	 * @author dragon
	 * Decode audio listener
	 */
	public abstract interface OnAudioDecodeListener{
		/**
		 * Set the audio decode listener, you should implements this listener for AudioLoopback test
		 * @param low
		 * the low frequency
		 * @param high
		 * the high frequency
		 * @param decibel
		 * the decibel value
		 */
		public abstract void onAudioDecode(int low, int high, double decibel);
	}
}
