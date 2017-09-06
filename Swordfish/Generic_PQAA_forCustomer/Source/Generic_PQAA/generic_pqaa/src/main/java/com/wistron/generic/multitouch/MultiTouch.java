package com.wistron.generic.multitouch;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wistron.generic.pqaa.R;
import com.wistron.pqaa_common.jar.global.WisCommonConst;
import com.wistron.pqaa_common.jar.global.WisParseValue;
import com.wistron.pqaa_common.jar.global.WisToolKit;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MultiTouch extends Activity implements OnTouchListener {
    private myViewTouch mvt;
    private int mTestNumber = 2;

    // Drag Drop
    private ArrayList<Float> Circle_X = new ArrayList<Float>();
    private ArrayList<Float> Circle_Y = new ArrayList<Float>();
    private int Circle_number = 0;
    private boolean isDown = false;
    private ArrayList<Radius> Point = new ArrayList<MultiTouch.Radius>();
    private int pointSize = 0;
    public AlertDialog builder = null;

    private LinearLayout mLayout;

    private WisToolKit mToolKit;
    private boolean mComponentMode = true;
    private int TIMEOUT = 30;
    private int mTimes = 0;
    private Timer mTimer;
    private TimerTask mTask;
    private boolean isPass = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.multitouch);
        mToolKit = new WisToolKit(this);
        findView();
        getTestArguments();
        initTimer();
    }

    private void initTimer() {
        mTimer = new Timer();
        mTask = new TimerTask() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                mTimes++;
                mHandler.sendEmptyMessage(1);
                if (mTimes >= TIMEOUT) {
                    mHandler.sendEmptyMessage(2);
                }
            }
        };
        mTimer.schedule(mTask, 1000, 1000);
    }

    private void CancelTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTask != null) {
            mTask.cancel();
            mTask = null;
        }
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            if (msg.what == 1) {
                mvt.invalidate();
            } else if (msg.what == 2) {
                CancelTimer();
                BackToPQAA();
            }
        }

    };

    private void BackToPQAA() {
        mToolKit.returnWithResult(isPass);
    }

    private void findView() {
        ((TextView) findViewById(R.id.item_title)).setText(mToolKit.getStringResource(R.string.multitouch_title));
        mvt = new myViewTouch(this);
        mvt.setOnTouchListener(this);
        mLayout = (LinearLayout) findViewById(R.id.multi_layout);
        mLayout.addView(mvt);
    }

    private void getTestArguments() {
        // TODO Auto-generated method stub
        String mTestStyle = mToolKit.getCurrentTestType();
        if (mTestStyle != null) {
            if (mTestStyle.equals(WisCommonConst.TESTSTYLE_SAVED_CONFIG)) {
                mComponentMode = false;
                WisParseValue mParse = new WisParseValue(this, mToolKit.getCurrentItem(), mToolKit.getCurrentDatabaseAuthorities());
                mTestNumber = Integer.parseInt(mParse.getArg1());
                TIMEOUT = Integer.parseInt(mParse.getArg2());


            }
        }
    }

    private class myViewTouch extends View {
        public myViewTouch(Context context) {
            super(context);
        }

        @SuppressLint("DrawAllocation")
        @Override
        protected void onDraw(Canvas canvas) {
            // TODO Auto-generated method stub
            this.setBackgroundColor(Color.WHITE);
            Paint paint = new Paint();
            paint.setColor(Color.BLUE);
            paint.setStyle(Paint.Style.FILL);
            if (isDown) {
                for (int i = 0; i < Circle_number; i++) {
                    canvas.drawCircle(Circle_X.get(i), Circle_Y.get(i), 50,
                            paint);
                }
            }
            paint.setColor(Color.RED);
            paint.setTextSize(35);
            canvas.drawText(mToolKit.getStringResource(R.string.remain_time) + mToolKit.formatCountDownTime(TIMEOUT, mTimes), 100, 100, paint);
            canvas.drawText(mToolKit.getStringResource(R.string.total_time) + mToolKit.formatCountDownTime(TIMEOUT, 0), 100, 150, paint);
            canvas.drawText(mToolKit.getStringResource(R.string.multitouch_test_point) + " " + mTestNumber, 100, 200, paint);
            super.onDraw(canvas);
        }


    }

    public boolean onTouch(View v, MotionEvent me) {
        // TODO Auto-generated method stub
        if (me.getAction() == MotionEvent.ACTION_UP) {
            isDown = false;

        } else {
            Circle_number = me.getPointerCount();
            Circle_X.clear();
            Circle_Y.clear();
            for (int i = 0; i < Circle_number; i++) {
                Circle_X.add(me.getX(i));
                Circle_Y.add(me.getY(i));
                int id = me.getPointerId(i);
                boolean isHave = true;
                for (int j = 0; j < Point.size(); j++) {
                    Radius radius = Point.get(j);
                    if (radius.RadiusID == id) {
                        isHave = false;
                    }
                }
                if (isHave) {
                    Radius radius = new Radius();
                    radius.RadiusX = me.getX(i);
                    radius.RadiusY = me.getY(i);
                    radius.RadiusID = me.getPointerId(i);
                    Point.add(radius);
                }
                pointSize = Point.size();
            }
            if (pointSize >= mTestNumber) {
                isPass = true;
                mHandler.sendEmptyMessage(2);
            }
            isDown = true;
        }
        mvt.invalidate();
        return true;
    }


    public class Radius {
        float RadiusX;
        float RadiusY;
        int RadiusID;
    }
}
