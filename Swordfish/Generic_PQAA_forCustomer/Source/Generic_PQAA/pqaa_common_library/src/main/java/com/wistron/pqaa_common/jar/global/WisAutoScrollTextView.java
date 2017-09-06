package com.wistron.pqaa_common.jar.global;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint.FontMetrics;
import android.text.TextPaint;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

public class WisAutoScrollTextView extends TextView {
	private TextPaint mPaint;
	private FontMetrics fm;
	private int mMeasuredTextHeight = 0;
	private float lineHeight;
	private int mTextViewWidth,mTextViewHeight;
	private int mScrollValue=0;
	private StringBuilder content;
	
	public WisAutoScrollTextView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public WisAutoScrollTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		this.mPaint = getPaint();
		fm = mPaint.getFontMetrics();
		lineHeight = fm.descent - fm.ascent + fm.leading;
		this.content = new StringBuilder();
		setMovementMethod(ScrollingMovementMethod.getInstance());
	}

	public WisAutoScrollTextView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Append content to the end of TextView
	 * @param text
	 * the text that you want to append.
	 */
	public void addText(String text){
		this.content.append(text);
		this.setText(getText() + text);
		invalidate();
	}
	
	/**
	 * Clear the content of TextView
	 */
	public void clearText(){
		this.content.delete(0, content.length());
		setText("");
		invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		mTextViewWidth=getWidth();
		mTextViewHeight=getHeight();
		mScrollValue = 0;
		if (mTextViewWidth > 0 && mTextViewHeight > 0) {
			if (content != null && content.length()>0) {
				BufferedReader reader = new BufferedReader(new StringReader(content.toString()));
				String line;
				try {
					while((line = reader.readLine()) != null){
						measureTextHeight(line);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (mMeasuredTextHeight >= mTextViewHeight) {
					if (mMeasuredTextHeight == mScrollValue) {
						this.scrollTo(0, mMeasuredTextHeight - mTextViewHeight+(int)lineHeight);
					}else {
						this.scrollBy(0, mScrollValue);
					}
				}
				content.delete(0, content.length());
			}
		}
	}
	
	private void measureTextHeight(String text){
		int lineCount = (int)Math.ceil(mPaint.measureText(text) / mTextViewWidth);
		if (lineCount == 0) {
			lineCount = 1;
		}
		mScrollValue += (int) (lineCount*lineHeight);
		mMeasuredTextHeight += (int) (lineCount*lineHeight);
	}
	
}
