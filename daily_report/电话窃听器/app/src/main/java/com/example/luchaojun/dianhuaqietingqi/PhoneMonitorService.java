package com.example.luchaojun.dianhuaqietingqi;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

/**
 * Created by luchaojun on 8/14/17.
 */

public class PhoneMonitorService extends Service{

    private TelephonyManager telephonyManager;
    private MediaRecorder recorder;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(new MyPhoneStateListener(),PhoneStateListener.LISTEN_CALL_STATE);
        super.onCreate();
    }
    private class MyPhoneStateListener extends PhoneStateListener {
        private MediaRecorder recorder;

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state){
                case TelephonyManager.CALL_STATE_IDLE:
                    recorder.stop();
                    recorder.reset();
                    recorder.release();
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    System.out.println("准备一个录音机");
                    recorder = new MediaRecorder();
                    recorder.setAudioSource(MediaRecorder.AudioSource.MIC); //音频来源
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); //输出格式
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB); //设置音频的编码格式
                    recorder.setOutputFile("/mnt/sdcard/luying.3gp"); //设置保存的路径
                    try{
                        recorder.prepare();
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    System.out.println("收音机开启");
                    recorder.start();
                    break;
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    }
}
