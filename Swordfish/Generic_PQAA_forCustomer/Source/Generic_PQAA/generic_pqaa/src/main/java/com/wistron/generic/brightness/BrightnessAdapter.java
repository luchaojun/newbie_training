package com.wistron.generic.brightness;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class BrightnessAdapter extends BaseAdapter {
	private final Context context;
	private final List<String> mList;

	public BrightnessAdapter(Context context, List<String> lCurrent) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.mList = lCurrent;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		TextView mItemView = new TextView(context);
		mItemView.setText(mList.get(position));
		mItemView.setTextColor(Color.BLACK);
		if (position == Brightness.mCurrentItem) {
			mItemView.setBackgroundColor(Color.BLUE);
		}
		return mItemView;
	}
}
