package com.wistron.generic.display;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class FullScreenActivity extends Activity {
    ActivityManager manager;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //     getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                , WindowManager.LayoutParams.FLAG_FULLSCREEN);

        RelativeLayout mmRelativeLayout = new RelativeLayout(this);
        mmRelativeLayout.setBackgroundColor(Color.RED);


        setContentView(mmRelativeLayout);

        new Thread(thread00).start();

    }

    static public String getSystemOutput(String cmd) {
        String retString = "";
        try {
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(cmd);
            InputStream is = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;

            while ((line = br.readLine()) != null) {
                retString += line;
                retString += "\n";
            }

            int exitVal = proc.waitFor();
            System.out.println("Process exitValue: " + exitVal);
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return retString;
    }


    public Runnable thread00 = new Runnable() {

        public void run() {
            // TODO Auto-generated method stub
            while (true) {

                ActivityManager mActivityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                List<ActivityManager.RunningAppProcessInfo> localRunningAppProcessInfo = (List<RunningAppProcessInfo>) mActivityManager.getRunningAppProcesses();

                //StringBuilder localStringBuilder = new StringBuilder("kill ");
                //ActivityManager.RunningAppProcessInfo localRunningAppProcessInfo1 = localRunningAppProcessInfo.iterator().next();
                for (ActivityManager.RunningAppProcessInfo localRunningAppProcessInfo1 : localRunningAppProcessInfo) {
                    if (localRunningAppProcessInfo1.processName.contains("com.android.systemui")) {
                        String str = "kill ";
                        str += localRunningAppProcessInfo1.pid;
                        getSystemOutput(str);
                        break;
                    }

                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    };
}