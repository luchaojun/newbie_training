package com.example.luchaojun.a2_;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

/**
 * Created by luchaojun on 8/16/17.
 */

public class AlipayService extends Service{
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    public boolean buyDou(String username,String password,int money){
        System.out.println("检测用户名和密码是否正确");
        System.out.println("检测是否携带毒");
        System.out.println("...............");
        if("abc".equals(username) && "123".equals(password) && money<=5000){
            return true;
        }else{
            return false;
        }
    }
    private class MyBinder extends IService.Stub{
        @Override
        public void callbuyDou(String username, String password, int money) {
            boolean b = buyDou(username, password, money);
            if(b){
                Toast.makeText(getApplicationContext(),"买豆成功！！！",Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(getApplicationContext(),"买豆失败！！！",Toast.LENGTH_LONG).show();
            }
        }
    }
}
