package com.wistron.pqaa_common.jar.audio;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.concurrent.BlockingQueue;

public class RecordTask extends AsyncTask<Void, Object, Void> {
	private String tag="tag";

	private Context context;
	private WisAudioDecode mAudioDecoder;
	private BlockingQueue<DataBlock> blockingQueue;

	protected RecordTask(WisAudioDecode decoder,Context context,BlockingQueue<DataBlock> blockingQueue) {
		this.context=context;
		this.mAudioDecoder = decoder;
		this.blockingQueue = blockingQueue;
	}

	@Override
	protected Void doInBackground(Void... params) {
		
		int frequency = 8000;
		int channelConfig = AudioFormat.CHANNEL_IN_MONO;
		int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
		int blockSize = 512;
		
		int bufferSize = AudioRecord.getMinBufferSize(frequency,channelConfig, audioEncoding);
		Log.d(tag, "bufferSize = "+bufferSize);
		
		AudioRecord audioRecord = new AudioRecord(getAudioSource(),
				frequency, channelConfig, audioEncoding, bufferSize);
//		AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
//				frequency, channelConfig, audioEncoding, bufferSize);

		short[] buffer = new short[blockSize];
		try {
			audioRecord.startRecording();
			while (mAudioDecoder.isStarted()) {
				int bufferReadSize = audioRecord.read(buffer, 0, blockSize);
//				Log.i(tag,"buffer read size: " + bufferReadSize);
				DataBlock dataBlock = new DataBlock(buffer, blockSize,bufferReadSize);
				if (mAudioDecoder.isStarted()) {
					blockingQueue.put(dataBlock);
				}
			}

		} catch (Exception e) {
			Log.e(tag, "Recording Error", e);
		}

		if (audioRecord != null) {
			audioRecord.stop();
			audioRecord.release();
			audioRecord = null;
		}
		return null;
	}
	
	private int getAudioSource() {
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

		if (telephonyManager.getCallState() != TelephonyManager.PHONE_TYPE_NONE)
			return MediaRecorder.AudioSource.VOICE_DOWNLINK;

		return MediaRecorder.AudioSource.MIC;
	}
}