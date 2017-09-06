package com.wistron.generic.display;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;

import com.wistron.pqaa_common.jar.global.WisCommonConst;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class graphics extends Activity {
    private static int TIMEOUT = 2;
    private drawpicture view;
    private static final int ResultCode = 0;

    private boolean[] mSelectStatus;
    private int mTestItem;
    private int mCurrentSubItem;
    private int mTempTestedSum = 0;
    private boolean isHaveTestItem;
    private final int[] mSubSumOfEachItem = {3, 2, 4, 1, 2, 2, 2, 0};
    private final int[] mLocationOfEachItem = {0, 3, 5, 9, 10, 12, 14};
    private boolean mFinishTest = false;
    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    private final Handler myHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case 0:
                    view.setiCnt(mCurrentSubItem);
                    break;
                case 1:
                    mFinishTest = true;
                    Intent intent = getIntent();
                    intent.putExtra(WisCommonConst.EXTRA_PASS, mFinishTest);
                    setResult(ResultCode, intent);
                    finish();
            }
            super.handleMessage(msg);
        }

    };
    private final Runnable autoTap = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            try {
                while (!mFinishTest) {
                    Thread.sleep(TIMEOUT * 1000);
                    mCurrentSubItem++;
                    mTempTestedSum++;
                    Log.i("TAg", "mTempTestedSum: " + mTempTestedSum);
                    if (mTempTestedSum >= mSubSumOfEachItem[mTestItem]) {
                        mTempTestedSum = 0;
                        isHaveTestItem = false;
                        for (int i = mTestItem + 1; i < mSelectStatus.length - 1; i++) {
                            if (mSelectStatus[i]) {
                                isHaveTestItem = true;
                                mTestItem = i;
                                break;
                            }
                        }
                        if (isHaveTestItem) {
                            mCurrentSubItem = mLocationOfEachItem[mTestItem];
                            Message msg = new Message();
                            msg.what = 0;
                            myHandler.sendMessage(msg);
                        } else {
                            Message msg = new Message();
                            msg.what = 1;
                            myHandler.sendMessage(msg);
                            break;
                        }
                    } else {
                        Message msg = new Message();
                        msg.what = 0;
                        myHandler.sendMessage(msg);
                    }
                }
            } catch (Exception e) {
                e.toString();
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mSelectStatus = getIntent().getExtras().getBooleanArray("Selected Items");
        int mInterval = getIntent().getExtras().getInt("interval", TIMEOUT);
        TIMEOUT = mInterval > 0 ? mInterval : TIMEOUT;
        for (int i = 0; i < mSelectStatus.length - 1; i++) {
            if (mSelectStatus[i]) {
                mTestItem = i;
                mCurrentSubItem = mLocationOfEachItem[i];
                break;
            }
        }

        view = new drawpicture(this);
        try {
            if (mSelectStatus[7]) {
                new Thread(autoTap).start();
            }
        } catch (Exception e) {
            e.toString();
        }
        view.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!mSelectStatus[7]) {
                    mCurrentSubItem++;
                    mTempTestedSum++;
                    Log.i("TAg", "mTempTestedSum: " + mTempTestedSum);
                    if (mTempTestedSum >= mSubSumOfEachItem[mTestItem]) {
                        mTempTestedSum = 0;
                        isHaveTestItem = false;
                        for (int i = mTestItem + 1; i < mSelectStatus.length - 1; i++) {
                            if (mSelectStatus[i]) {
                                isHaveTestItem = true;
                                mTestItem = i;
                                break;
                            }
                        }
                        if (isHaveTestItem) {
                            mCurrentSubItem = mLocationOfEachItem[mTestItem];
                            Message msg = new Message();
                            msg.what = 0;
                            myHandler.sendMessage(msg);
                        } else {
                            Message msg = new Message();
                            msg.what = 1;
                            myHandler.sendMessage(msg);
                        }
                    } else {
                        Message msg = new Message();
                        msg.what = 0;
                        myHandler.sendMessage(msg);
                    }
                }
            }
        });
        setContentView(view);
//		new Thread(fullScreenProcess).start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public String getSystemOutput(String cmd) {
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

    public Runnable fullScreenProcess = new Runnable() {

        public void run() {
            // TODO Auto-generated method stub
            while (!mFinishTest) {

                ActivityManager mActivityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
//				mActivityManager.killBackgroundProcesses("com.android.systemui");
                List<ActivityManager.RunningAppProcessInfo> localRunningAppProcessInfo = (List<RunningAppProcessInfo>) mActivityManager
                        .getRunningAppProcesses();

                // StringBuilder localStringBuilder = new
                // StringBuilder("kill ");
                // ActivityManager.RunningAppProcessInfo
                // localRunningAppProcessInfo1 =
                // localRunningAppProcessInfo.iterator().next();
                for (ActivityManager.RunningAppProcessInfo localRunningAppProcessInfo1 : localRunningAppProcessInfo) {
                    if (localRunningAppProcessInfo1.processName.contains("com.android.systemui")) {
                        String str = "kill ";
                        str += localRunningAppProcessInfo1.pid;
                        getSystemOutput(str);
                        break;
                    }

                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    };

    private class drawpicture extends View {
        public int iWidth;
        public int iHeight;

        @Override
        protected void onDraw(Canvas canvas) {
            // TODO Auto-generated method stub
            Log.i("Tag", "mTestItem: " + mTestItem);
            Log.i("Tag", "mCurrentSubItem: " + mCurrentSubItem);
            Paint paint;
            if (mSelectStatus[0]) {
                switch (mCurrentSubItem) {
                    case 0:
                        this.setBackgroundColor(Color.RED);
                        break;
                    case 1:
                        this.setBackgroundColor(Color.GREEN);
                        break;
                    case 2:
                        this.setBackgroundColor(Color.BLUE);
                        break;
                }
            }
            if (mSelectStatus[1]) {
                switch (mCurrentSubItem) {
                    case 3:
                        this.setBackgroundColor(Color.BLACK);
                        break;
                    case 4:
                        this.setBackgroundColor(Color.WHITE);
                        break;
                }
            }
            if (mSelectStatus[2]) {
                switch (mCurrentSubItem) {
                    case 5:
                        for (float ia = 0; ia < iHeight; ia = ia + 2) {
                            Paint p2 = new Paint();
                            p2.setColor(Color.WHITE);
                            canvas.drawLine(0.0f, ia, iWidth, ia, p2);
                            p2.setColor(Color.BLACK);
                            canvas.drawLine(0.0f, ia + 1, iWidth, ia + 1, p2);
                        }
                        break;
                    case 6:
                        for (float ia = 0; ia < iHeight; ia = ia + 2) {
                            Paint p2 = new Paint();
                            p2.setColor(Color.BLACK);
                            canvas.drawLine(0.0f, ia, iWidth, ia, p2);
                            p2.setColor(Color.WHITE);
                            canvas.drawLine(0.0f, ia + 1, iWidth, ia + 1, p2);
                        }
                        break;
                    case 7:
                        for (float ia = 0; ia < iWidth; ia = ia + 2) {
                            Paint p2 = new Paint();
                            p2.setColor(Color.BLACK);
                            canvas.drawLine(ia, 0.0f, ia, iHeight, p2);
                            p2.setColor(Color.WHITE);
                            canvas.drawLine(ia + 1, 0.0f, ia + 1, iHeight, p2);
                        }
                        break;
                    case 8:
                        for (float ia = 0; ia < iWidth; ia = ia + 2) {
                            Paint p2 = new Paint();
                            p2.setColor(Color.WHITE);
                            canvas.drawLine(ia, 0.0f, ia, iHeight, p2);
                            p2.setColor(Color.BLACK);
                            canvas.drawLine(ia + 1, 0.0f, ia + 1, iHeight, p2);
                        }
                        break;
                }
            }
            if (mSelectStatus[3]) {
                int mSubWidth = iWidth / 16;
                switch (mCurrentSubItem) {
                    case 9:
                        for (int ia = 0; ia < 16; ia++) {
                            paint = new Paint();
                            paint.setStyle(Style.FILL_AND_STROKE);
                            paint.setColor(Color.rgb(255 - ia * 16, 255 - ia * 16, 255 - ia * 16));
                            canvas.drawRect(mSubWidth * ia, 0, mSubWidth * (ia + 1), iHeight, paint);
                        }

                        break;
                }
            }
            if (mSelectStatus[4]) {
                switch (mCurrentSubItem) {
                    case 10:
                        paint = new Paint();
                        paint.setColor(Color.RED);
                        canvas.drawRect(new Rect(0, 0, iWidth, iHeight / 4), paint);
                        paint.setColor(Color.GREEN);
                        canvas.drawRect(new Rect(0, iHeight / 4, iWidth, iHeight / 2), paint);
                        paint.setColor(Color.BLUE);
                        canvas.drawRect(new Rect(0, iHeight / 2, iWidth, (iHeight / 4) * 3), paint);
                        paint.setColor(Color.WHITE);
                        canvas.drawRect(new Rect(0, (iHeight / 4) * 3, iWidth, iHeight), paint);
                        break;
                    case 11:
                        paint = new Paint();
                        paint.setColor(Color.RED);
                        canvas.drawRect(new Rect(0, 0, iWidth / 4, iHeight), paint);
                        paint.setColor(Color.GREEN);
                        canvas.drawRect(new Rect(iWidth / 4, 0, iWidth / 2, iHeight), paint);
                        paint.setColor(Color.BLUE);
                        canvas.drawRect(new Rect(iWidth / 2, 0, 3 * iWidth / 4, iHeight), paint);
                        paint.setColor(Color.WHITE);
                        canvas.drawRect(new Rect(3 * iWidth / 4, 0, iWidth, iHeight), paint);
                        break;
                }
            }
            if (mSelectStatus[5]) {
                switch (mCurrentSubItem) {
                    case 12:
                        this.setBackgroundColor(Color.WHITE);
                        paint = new Paint();
                        paint.setColor(Color.BLACK);
                        for (int ia = 0; ia < iHeight; ia = ia + 2) {
                            canvas.drawLine(0, ia, iWidth, ia, paint);
                        }
                        break;
                    case 13:
                        this.setBackgroundColor(Color.WHITE);
                        paint = new Paint();
                        paint.setColor(Color.BLACK);
                        for (int ia = 0; ia < iWidth; ia = ia + 2) {
                            canvas.drawLine(ia, 0, ia, iHeight, paint);
                        }
                        break;
                }
            }
            if (mSelectStatus[6]) {
                switch (mCurrentSubItem) {
                    case 14:
                        this.setBackgroundColor(Color.BLACK);
                        paint = new Paint();
                        paint.setColor(Color.WHITE);
                        Rect rc = new Rect(0, iHeight / 2 - 60, iWidth, iHeight / 2 + 60);
                        canvas.drawRect(rc, paint);
                        break;
                    case 15:
                        this.setBackgroundColor(Color.BLACK);
                        paint = new Paint();
                        paint.setColor(Color.WHITE);
                        Rect rc1 = new Rect(iWidth / 2 - 60, 0, iWidth / 2 + 60, iHeight);
                        canvas.drawRect(rc1, paint);
                        break;
                }
            }
            super.onDraw(canvas);
        }

        public void setiCnt(int icount) {
            invalidate();
        }

        public drawpicture(Context context) {
            super(context);
            // TODO Auto-generated constructor stub
            Intent in = ((Activity) context).getIntent();
            Bundle bundle = in.getExtras();
            mSelectStatus = bundle.getBooleanArray("Selected Items");

            DisplayMetrics dm = new DisplayMetrics();
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);

            iWidth = dm.widthPixels;
            iHeight = dm.heightPixels + 50;

        }
    }
}
