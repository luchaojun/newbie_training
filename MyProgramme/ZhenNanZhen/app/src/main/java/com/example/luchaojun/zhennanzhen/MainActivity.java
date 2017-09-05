package com.example.luchaojun.zhennanzhen;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    private ImageView zhinanzhen_iv;
    private Bitmap zhinanzhen;
    private SensorManager manager;
    private Sensor sensor;
    private MyListener myListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        zhinanzhen_iv = (ImageView) findViewById(R.id.zhinanzhen_IV); //拿到ImageView的实例
        zhinanzhen = BitmapFactory.decodeResource(getResources(), R.drawable.zhinanzhen); //拿到指南针的Bitmap对象
        zhinanzhen_iv.setImageBitmap(zhinanzhen); //给ImageView设置图片

        manager = (SensorManager) getSystemService(SENSOR_SERVICE); //拿Sensor的管理者
        sensor = manager.getDefaultSensor(Sensor.TYPE_ORIENTATION); //拿sensor的对象
        myListener = new MyListener();
        manager.registerListener(myListener,sensor,SensorManager.SENSOR_DELAY_GAME); //注册一个监听者
    }

    //定义一个类去实现SensorEventListener的借口配合registerListener的第一个参数
    private class MyListener implements SensorEventListener{
        float startAngle = 0; //设置起始的角度
        //当手机位置发生变化的时候
        @Override
        public void onSensorChanged(SensorEvent event) {
            float[] values = event.values;
            float angle = values[0];   //拿正北角度0度
            RotateAnimation ra = new RotateAnimation(startAngle,angle, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f); //使用补间动画设置图片的旋转角度
            ra.setDuration(100); //设置动画执行整个过程需要的时间
            zhinanzhen_iv.startAnimation(ra); //开始执行动画的效果
            startAngle = -angle; //这个的作用是保存前一次旋转的角度的相反值 目的是先让指针回到零的位置然后再进行旋转
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    @Override
    protected void onDestroy() {
        manager.unregisterListener(myListener); //关闭注册的监听者 目的是不再进行监听 使手机能够达到省电的目的
        super.onDestroy();
    }
}
