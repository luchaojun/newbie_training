package com.example.luchaojun.shoujifangdao;

import android.content.Intent;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button start_btn;
    private Button stop_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start_btn = (Button) findViewById(R.id.start_btn); //拿到开始按钮的实例
        stop_btn = (Button) findViewById(R.id.stop_btn);  //拿到关闭按钮的实例

        start_btn.setOnClickListener(this); //给start_btn设置点击事件
        stop_btn.setOnClickListener(this);  //给stop_btn设置点击事件
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.start_btn:
                Intent intent = new Intent(this,ShouJiFangDaoService.class); //设置服务的intent
                System.out.println("开始沉睡15秒");
                SystemClock.sleep(15000);
                startService(intent); //开启一个服务
                break;

            case R.id.stop_btn:
                break;
        }
    }
}
