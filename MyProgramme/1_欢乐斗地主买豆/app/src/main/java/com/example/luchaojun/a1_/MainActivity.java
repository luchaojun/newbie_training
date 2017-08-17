package com.example.luchaojun.a1_;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.luchaojun.a2_.IService;

public class MainActivity extends AppCompatActivity {

    private MyServiceConnection myServiceConnection;
    private IService.Stub stub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent();
        intent.setAction("com.wistrol.buyDouService");
        myServiceConnection = new MyServiceConnection();
        bindService(intent,myServiceConnection,BIND_AUTO_CREATE);
    }
    private class MyServiceConnection implements ServiceConnection{
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            stub = (IService.Stub) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    }
    public void click(View view){
        try{
            stub.callbuyDou("abc","123",4000);
        }catch (Exception exception){
            exception.printStackTrace();
        }
    }
}
