package com.wistron.generic.touchpanel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;

import com.wistron.generic.pqaa.R;
import com.wistron.pqaa_common.jar.global.WisToolKit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class TouchPanelTest extends Activity implements OnTouchListener {
    private static final int TEST_CORNER = 1;
    private static final int TEST_CROSS = 2;
    private static final int TEST_DIAGONAL = 3;

    private boolean mFinishTest = false;
    private final int CORNER_WIDTH = 80;
    private final int CORNER_HEIGHT = 80;
    private final int CROSS_BASE = 55;
    private final int CROSS_EXTRA = 55;

    private int mScreenWidth, mScreenHeight;
    private myViewTouch mvt;
    private boolean b_corner, b_cross, b_diagonal;
    private int mCurrentTestItem;
    private Set<Rect> mCornerSet, mCrossSet;
    private Rect mTempRect;

    private int mTimes = 0;
    private Timer mTimer = new Timer();
    private TimerTask mTimeOutTask;
    private int TIMEOUT = 90; // seconds
    private boolean isPCBStage = false;

    // ----------Diagonal test---------
    private Set<ArrayList<Integer>> mPathSet;
    private ArrayList<Integer> mVertex;
    private int mOffsetx = 50;
    private Polygon mPolygon;

    // common tool kit
    private WisToolKit mToolKit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;

        mToolKit = new WisToolKit(this);

        Intent in = this.getIntent();
        Bundle bundle = in.getExtras();
        b_corner = bundle.getBoolean("corner");
        b_cross = bundle.getBoolean("cross");
        b_diagonal = bundle.getBoolean("diagonal");
        isPCBStage = bundle.getBoolean("stage");
        mToolKit.setCurrentLanguage(bundle.getInt("language"));
        int mTestTime = bundle.getInt("timeout");
        if (mTestTime > 0) {
            TIMEOUT = mTestTime;
        }
        if (b_corner) {
            mCurrentTestItem = TEST_CORNER;
        } else if (b_cross) {
            mCurrentTestItem = TEST_CROSS;
        } else if (b_diagonal) {
            mCurrentTestItem = TEST_DIAGONAL;
        }
        initializeSet();
        initializeTimerTask();

        mvt = new myViewTouch(this);
        mvt.setOnTouchListener(this);
        setContentView(mvt);
    }

    private void initializeTimerTask() {
        // TODO Auto-generated method stub
        mTimeOutTask = new TimerTask() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                mTimes++;
                handler.sendEmptyMessage(0);
                if (mTimes >= TIMEOUT) {
                    cancelTimer();
                }
            }
        };
        mTimer.schedule(mTimeOutTask, 1000, 1000);
    }

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            mvt.invalidate();
        }

    };

    protected void cancelTimer() {
        // TODO Auto-generated method stub
        if (mTimeOutTask != null) {
            mTimeOutTask.cancel();
            mTimeOutTask = null;
        }
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        mToolKit.returnWithResult(mFinishTest);
    }

    private void initializeSet() {
        // TODO Auto-generated method stub
        // -----------corner initial---------------
        mCornerSet = new HashSet<Rect>();
        int i = 0;
        if (!isPCBStage) {
            for (i = 0; i < mScreenWidth / CROSS_BASE; i++) {
                mTempRect = new Rect(i * CROSS_BASE, 0, (i + 1) * CROSS_BASE, CROSS_EXTRA);
                mCornerSet.add(mTempRect);
            }
            if (mScreenWidth % CROSS_BASE != 0) {
                mTempRect = new Rect(i * CROSS_BASE, 0, mScreenWidth, CROSS_EXTRA);
                mCornerSet.add(mTempRect);
            }
            for (i = 0; i < mScreenWidth / CROSS_BASE; i++) {
                mTempRect = new Rect(i * CROSS_BASE, mScreenHeight - CROSS_EXTRA, (i + 1) * CROSS_BASE, mScreenHeight);
                mCornerSet.add(mTempRect);
            }
            if (mScreenWidth % CROSS_BASE != 0) {
                mTempRect = new Rect(i * CROSS_BASE, mScreenHeight - CROSS_EXTRA, mScreenWidth, mScreenHeight);
                mCornerSet.add(mTempRect);
            }
            int mRightHeight = CROSS_EXTRA;
            for (i = mRightHeight; i < mScreenHeight - 2 * CROSS_EXTRA; i = i + CROSS_EXTRA) {
                mTempRect = new Rect(mScreenWidth - CROSS_BASE, i, mScreenWidth, i + CROSS_EXTRA);
                mCornerSet.add(mTempRect);
            }
            if (i >= mScreenHeight - 2 * CROSS_EXTRA) {
                mTempRect = new Rect(mScreenWidth - CROSS_BASE, i, mScreenWidth, mScreenHeight - CROSS_EXTRA);
                mCornerSet.add(mTempRect);
            }
            int mLeftHeight = CROSS_EXTRA;
            for (i = mLeftHeight; i < mScreenHeight - 2 * CROSS_EXTRA; i = i + CROSS_EXTRA) {
                mTempRect = new Rect(0, i, CROSS_BASE, i + CROSS_EXTRA);
                mCornerSet.add(mTempRect);
            }
            if (i >= mScreenHeight - 2 * CROSS_EXTRA) {
                mTempRect = new Rect(0, i, CROSS_BASE, mScreenHeight - CROSS_EXTRA);
                mCornerSet.add(mTempRect);
            }
        } else {
            mTempRect = new Rect(0, 0, CORNER_WIDTH, CORNER_HEIGHT);
            mCornerSet.add(mTempRect);
            mTempRect = new Rect(mScreenWidth - CORNER_WIDTH, 0, mScreenWidth, CORNER_HEIGHT);
            mCornerSet.add(mTempRect);
            mTempRect = new Rect(0, mScreenHeight - CORNER_HEIGHT, CORNER_WIDTH, mScreenHeight);
            mCornerSet.add(mTempRect);
            mTempRect = new Rect(mScreenWidth - CORNER_WIDTH, mScreenHeight - CORNER_HEIGHT, mScreenWidth, mScreenHeight);
            mCornerSet.add(mTempRect);
            int mMiddleRectX = (mScreenWidth - CORNER_WIDTH) / 2;
            int mMiddleRectY = (mScreenHeight - CORNER_HEIGHT) / 2;
            mTempRect = new Rect(mMiddleRectX, mMiddleRectY, mMiddleRectX + CORNER_WIDTH, mMiddleRectY + CORNER_HEIGHT);
            mCornerSet.add(mTempRect);
        }

        // ---------cross initial----------------
        mCrossSet = new HashSet<Rect>();
        for (i = 0; i < mScreenWidth / CROSS_BASE; i++) {
            mTempRect = new Rect(i * CROSS_BASE, mScreenHeight / 2 - CROSS_EXTRA / 2, CROSS_BASE * (i + 1), mScreenHeight / 2 + CROSS_EXTRA / 2);
            mCrossSet.add(mTempRect);
        }
        if (mScreenWidth % CROSS_BASE != 0) {
            mTempRect = new Rect(i * CROSS_BASE, mScreenHeight / 2 - CROSS_EXTRA / 2, CROSS_BASE * i + mScreenWidth % CROSS_BASE, mScreenHeight / 2
                    + CROSS_EXTRA / 2);
            mCrossSet.add(mTempRect);
        }
        for (i = 0; i < mScreenHeight / CROSS_BASE; i++) {
            mTempRect = new Rect(mScreenWidth / 2 - CROSS_EXTRA / 2, CROSS_BASE * i, mScreenWidth / 2 + CROSS_EXTRA / 2, CROSS_BASE * (i + 1));
            mCrossSet.add(mTempRect);
        }
        if (mScreenHeight % CROSS_BASE != 0) {
            mTempRect = new Rect(mScreenWidth / 2 - CROSS_EXTRA / 2, CROSS_BASE * i, mScreenWidth / 2 + CROSS_EXTRA / 2, CROSS_BASE * i
                    + mScreenHeight % CROSS_BASE);
            mCrossSet.add(mTempRect);
        }

        // --------------------diagonal initial-------------------
        mPathSet = new HashSet<ArrayList<Integer>>();
        for (i = 0; i + mOffsetx < mScreenWidth - mOffsetx; i = i + mOffsetx) {
            // Path mPath = new Path();
            // mPath.moveTo(width, getUnderTop(width));
            // mPath.lineTo(width + mOffsetx, getUnderBottom(width + mOffsetx));
            // mPath.lineTo(width + 2 * mOffsetx, getUnderBottom(width + 2 *
            // mOffsetx));
            // mPath.lineTo(width + mOffsetx, getUnderTop(width + mOffsetx));
            // mPath.close();
            // mPathSet.add(mPath);
            mVertex = new ArrayList<Integer>();
            mVertex.add(i);
            mVertex.add(getUnderTop(i));
            mVertex.add(i + mOffsetx);
            mVertex.add(getUnderBottom(i + mOffsetx));
            mVertex.add(i + 2 * mOffsetx);
            mVertex.add(getUnderBottom(i + 2 * mOffsetx));
            mVertex.add(i + mOffsetx);
            mVertex.add(getUnderTop(i + mOffsetx));
            mPathSet.add(mVertex);
        }
        if (i < mScreenWidth - mOffsetx) {
            // Path mPath = new Path();
            // mPath.moveTo(width, getUnderTop(width));
            // mPath.lineTo(width + mOffsetx, getUnderBottom(width + mOffsetx));
            // mPath.lineTo(mScreenWidth, mOffsetx);
            // mPath.lineTo(mScreenWidth - mOffsetx, 0);
            // mPath.close();
            // mPathSet.add(mPath);
            mVertex = new ArrayList<Integer>();
            mVertex.add(i);
            mVertex.add(getUnderTop(i));

            mVertex.add(i + mOffsetx);
            mVertex.add(getUnderBottom(i + mOffsetx));

            mVertex.add(mScreenWidth);
            mVertex.add(mOffsetx);

            mVertex.add(mScreenWidth - mOffsetx);
            mVertex.add(0);
            mPathSet.add(mVertex);
        }
        for (i = mOffsetx; i + mOffsetx < mScreenWidth; i = i + mOffsetx) {
            // Path mPath = new Path();
            // mPath.moveTo(width, getTopTop(width));
            // mPath.lineTo(width - mOffsetx, getTopBottom(width - mOffsetx));
            // mPath.lineTo(width, getTopBottom(width));
            // mPath.lineTo(width + mOffsetx, getTopTop(width + mOffsetx));
            // mPath.close();
            // mPathSet.add(mPath);
            mVertex = new ArrayList<Integer>();
            mVertex.add(i);
            mVertex.add(getTopTop(i));

            mVertex.add(i - mOffsetx);
            mVertex.add(getTopBottom(i - mOffsetx));

            mVertex.add(i);
            mVertex.add(getTopBottom(i));

            mVertex.add(i + mOffsetx);
            mVertex.add(getTopTop(i + mOffsetx));
            mPathSet.add(mVertex);
        }
        if (i < mScreenWidth) {
            // Path mPath = new Path();
            // mPath.moveTo(width, getTopTop(width));
            // mPath.lineTo(width - mOffsetx, getTopBottom(width - mOffsetx));
            // mPath.lineTo(mScreenWidth - mOffsetx, mScreenHeight);
            // mPath.lineTo(mScreenWidth, mScreenHeight - mOffsetx);
            // mPath.close();
            // mPathSet.add(mPath);
            mVertex = new ArrayList<Integer>();
            mVertex.add(i);
            mVertex.add(getTopTop(i));

            mVertex.add(i - mOffsetx);
            mVertex.add(getTopBottom(i - mOffsetx));

            mVertex.add(mScreenWidth - mOffsetx);
            mVertex.add(mScreenHeight);

            mVertex.add(mScreenWidth);
            mVertex.add(mScreenHeight - mOffsetx);
            mPathSet.add(mVertex);
        }
    }

    private int getTopTop(int offset) {
        // TODO Auto-generated method stub
        int mvalue;
        mvalue = (offset - mOffsetx) * (mScreenHeight - mOffsetx) / (mScreenWidth - mOffsetx);
        return mvalue;
    }

    private int getTopBottom(int offset) {
        // TODO Auto-generated method stub
        int mvalue;
        mvalue = offset * (mScreenHeight - mOffsetx) / (mScreenWidth - mOffsetx) + mOffsetx;
        return mvalue;
    }

    private int getUnderTop(int offset) {
        int mvalue;
        mvalue = (mScreenHeight - mOffsetx) - offset * (mScreenHeight - mOffsetx) / (mScreenWidth - mOffsetx);
        return mvalue;
    }

    private int getUnderBottom(int offset) {
        int mvalue;
        mvalue = (offset - mOffsetx) * (mOffsetx - mScreenHeight) / (mScreenWidth - mOffsetx) + mScreenHeight;
        return mvalue;
    }

    private class myViewTouch extends View {
        public myViewTouch(Context context) {
            super(context);
            this.setBackgroundColor(Color.WHITE);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            // TODO Auto-generated method stub
            Paint paint = new Paint();
            paint.setColor(Color.BLUE);
            if (mCurrentTestItem == TEST_CORNER) {
                for (Iterator<Rect> mIterator = mCornerSet.iterator(); mIterator.hasNext(); ) {
                    mTempRect = mIterator.next();
                    canvas.drawRect(mTempRect, paint);
                }
            } else if (mCurrentTestItem == TEST_CROSS) {
                for (Iterator<Rect> mIterator = mCrossSet.iterator(); mIterator.hasNext(); ) {
                    mTempRect = mIterator.next();
                    canvas.drawRect(mTempRect, paint);
                }
            } else if (mCurrentTestItem == TEST_DIAGONAL) {
                Path mPath;
                // paint.setPathEffect(new CornerPathEffect(5));
                for (Iterator<ArrayList<Integer>> mIterator = mPathSet.iterator(); mIterator.hasNext(); ) {
                    mPath = getPath(mIterator.next());
                    canvas.drawPath(mPath, paint);
                }
            }
            paint.setColor(Color.RED);
            paint.setTextSize(20);
            canvas.drawText(mToolKit.getStringResource(R.string.remain_time) + mToolKit.formatCountDownTime(TIMEOUT, mTimes), 100, 100, paint);
            canvas.drawText(mToolKit.getStringResource(R.string.total_time) + mToolKit.formatCountDownTime(TIMEOUT, 0), 100, 150, paint);
            super.onDraw(canvas);
        }

        private Path getPath(ArrayList<Integer> arrayList) {
            // TODO Auto-generated method stub
            Path path = new Path();
            boolean mStartPoint = true;
            for (Iterator<Integer> iterator = arrayList.iterator(); iterator.hasNext(); ) {
                int xPos = (Integer) iterator.next();
                int yPos = (Integer) iterator.next();
                if (mStartPoint) {
                    mStartPoint = false;
                    path.moveTo(xPos, yPos);
                } else {
                    path.lineTo(xPos, yPos);
                }
            }
            path.close();
            return path;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent me) {
        // TODO Auto-generated method stub
        int xPos = (int) me.getX();
        int yPos = (int) me.getY();
        if (mCurrentTestItem == TEST_CORNER) {
            for (Iterator<Rect> mIterator = mCornerSet.iterator(); mIterator.hasNext(); ) {
                mTempRect = mIterator.next();
                if (mTempRect.contains(xPos, yPos)) {
                    mCornerSet.remove(mTempRect);
                    break;
                }
            }
            if (mCornerSet.isEmpty()) {
                if (isPCBStage) {
                    mFinishTest = true;
                    cancelTimer();
                } else {
                    if (b_cross) {
                        mCurrentTestItem = TEST_CROSS;
                    } else if (b_diagonal) {
                        mCurrentTestItem = TEST_DIAGONAL;
                    } else {
                        mFinishTest = true;
                        cancelTimer();
                    }
                }
            }
        } else if (mCurrentTestItem == TEST_CROSS) {
            for (Iterator<Rect> mIterator = mCrossSet.iterator(); mIterator.hasNext(); ) {
                mTempRect = mIterator.next();
                if (mTempRect.contains(xPos, yPos)) {
                    mCrossSet.remove(mTempRect);
                    removeItem();
                    break;
                }
            }
            if (mCrossSet.isEmpty()) {
                if (b_diagonal) {
                    mCurrentTestItem = TEST_DIAGONAL;
                } else {
                    mFinishTest = true;
                    cancelTimer();
                }
            }
        } else if (mCurrentTestItem == TEST_DIAGONAL) {
            for (Iterator<ArrayList<Integer>> mIterator = mPathSet.iterator(); mIterator.hasNext(); ) {
                mVertex = mIterator.next();
                getPolygon(mVertex);
                if (mPolygon.contains(xPos, yPos)) {
                    mPathSet.remove(mVertex);
                    break;
                }
            }
            if (mPathSet.isEmpty()) {
                mFinishTest = true;
                cancelTimer();
            }
        }
        mvt.invalidate();
        return true;
    }

    private void getPolygon(ArrayList<Integer> mVertexs) {
        // TODO Auto-generated method stub
        int[] xVer = new int[4];
        int[] yVer = new int[4];
        int index = 0;
        for (Iterator<Integer> iterator = mVertexs.iterator(); iterator.hasNext(); ) {
            int xPos = (Integer) iterator.next();
            int yPos = (Integer) iterator.next();
            xVer[index] = xPos;
            yVer[index] = yPos;
            index++;
        }
        mPolygon = new Polygon(xVer, yVer);
    }

    private void removeItem() {
        Rect mRect;
        for (Iterator<Rect> mIterator = mCrossSet.iterator(); mIterator.hasNext(); ) {
            mRect = mIterator.next();
            if (Rect.intersects(mTempRect, mRect)) {
                mCrossSet.remove(mRect);
                mIterator = mCrossSet.iterator();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
