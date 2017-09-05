package com.example.luchaojun.shoujifangdao;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;

import java.io.File;

/**
 * Created by luchaojun on 9/4/17.
 */

//一个手机防盗的服务
public class ShouJiFangDaoService extends Service{

    private SensorManager manager;
    private Sensor lightSensor;
    private MyListener listener;
    private boolean flag;  //设置一个标记

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        manager = (SensorManager) getSystemService(SENSOR_SERVICE); //拿Sensor管理者实例
        lightSensor = manager.getDefaultSensor(Sensor.TYPE_LIGHT);  //拿Sensor的传感器参数
        listener = new MyListener();
        manager.registerListener(listener,lightSensor,SensorManager.SENSOR_DELAY_FASTEST); //注册监听者
        super.onCreate();
    }
// /storage/emulated/0/依栏爱情故事.mp3
    private class MyListener implements SensorEventListener{
        @Override
        public void onSensorChanged(SensorEvent event) {
            float[] values = event.values; //拿光感的值的数组
            float lightSize = values[0]; //拿光感值
            System.out.println(lightSize);
            //当光感值大于5时开启音乐
            if(lightSize > 5 && !flag){
                try{
                    MediaPlayer player = new MediaPlayer(); //拿一个mediaPlayer的实例
                    player.setDataSource(Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"a.mp3");//设置歌曲的路径
                    player.prepare(); //歌曲准备播放
                    player.start();  //歌曲开始播放
                    flag = true;  //当歌曲播放时标记变为true 下载循环就不会在重复开启歌曲了
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    @Override
    public void onDestroy() {
        manager.unregisterListener(listener);
        super.onDestroy();
    }
}
