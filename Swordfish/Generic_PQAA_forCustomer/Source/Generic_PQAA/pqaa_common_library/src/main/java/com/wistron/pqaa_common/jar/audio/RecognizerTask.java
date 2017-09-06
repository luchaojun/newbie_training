package com.wistron.pqaa_common.jar.audio;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

public class RecognizerTask extends AsyncTask<Void, Object, Void> {
	private String TAG="RecognizerTask";	
	
	private WisAudioDecode mAudioDecoder;
	private BlockingQueue<DataBlock> blockingQueue;
	private ArrayList<AudioFrequency> mFrequencyList;

	protected RecognizerTask(WisAudioDecode decoder,Context context, BlockingQueue<DataBlock> blockingQueue) {
		this.mAudioDecoder = decoder;
		this.blockingQueue = blockingQueue;
		Log.i(TAG,"RecognizerTask init");
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		mFrequencyList = new ArrayList<>();
	}

	@Override
	protected Void doInBackground(Void... params) {
//		Log.i(TAG,"start recognized: " + mAudioDecoder.isStarted());
		while (mAudioDecoder.isStarted()) {
//			Log.i(TAG,"start recognized");
			try {
				DataBlock dataBlock = blockingQueue.take();
				Spectrum spectrum = dataBlock.FFT();
				spectrum.normalize();

				publishProgress(getRecognizedFrequency(spectrum));
			} catch (InterruptedException e) {
				Log.e(TAG, "Spectrum Error",e);
			}
		}

		return null;
	}

	protected void onProgressUpdate(Object... progress) {
		AudioFrequency frequency=(AudioFrequency)progress[0];
		if (frequency != null) {
			mAudioDecoder.decodeResult(frequency);
		}
	}
	
	private AudioFrequency getRecognizedFrequency(Spectrum spectrum){
		SpectrumFragment lowFragment= new SpectrumFragment(40, 65, spectrum);
	    SpectrumFragment highFragment= new SpectrumFragment(66, 110, spectrum);
	    
	    int lowMax = lowFragment.getMax();
	    int highMax = highFragment.getMax();
	    
	    AudioFrequency mFrequency=new AudioFrequency(lowMax, highMax);
	    
	    Log.i(TAG, lowMax + ":"+highMax);
	    
	    mFrequencyList.add(mFrequency);
	    if (mFrequencyList.size() > 4) {
	    	mFrequencyList.remove(0);
		    
		    int count = 0;
		    for (AudioFrequency frequency: mFrequencyList) 
		    {
		      if(lowMax == frequency.getLowFrequency() && highMax == frequency.getHighFrequency())
		        count++;
		    }
		    
		    if(count >= 3){
		    	mFrequency.setDecibel(spectrum.getDecibel());
		    	return mFrequency;
		    }
		}
		return null;
	}
}
