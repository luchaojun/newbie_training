package com.wistron.generic.hdmi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

public class FindView extends View {
    private final int SCAN_INCREASE_VALUE = 5;
    private int mFillColor;
    private int mScanLine;
    private boolean isReverseDraw;

    private Paint mPaint;

    public FindView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        mFillColor = Color.RED;

        mPaint = new Paint();
        mPaint.setStyle(Style.STROKE);
        mPaint.setColor(mFillColor);
        mPaint.setStrokeWidth(1.0f);
    }

    public FindView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub

    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);

        if (mScanLine < getHeight() / 3) {
            isReverseDraw = false;
            mScanLine = getHeight() / 3;
        }
        if (mScanLine > getHeight() * 3 / 4) {
            isReverseDraw = true;
            mScanLine = getHeight() * 3 / 4;
        }

        canvas.drawLine(0, mScanLine, getWidth(), mScanLine, mPaint);
    }

    public void increaseScan() {
        if (isReverseDraw) {
            mScanLine -= SCAN_INCREASE_VALUE;
        } else {
            mScanLine += SCAN_INCREASE_VALUE;
        }
        invalidate();
    }
}
