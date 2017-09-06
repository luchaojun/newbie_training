package com.wistron.generic.pqaa.utility;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wistron.generic.pqaa.R;
import com.wistron.pqaa_common.jar.global.WisCommonConst;
import com.wistron.pqaa_common.jar.global.WisToolKit;

import java.util.ArrayList;

public class AboutWindowAdapter extends BaseAdapter {
    private Context context;
    private final ArrayList<TestItem> mTestItemList;
    private final LayoutInflater inflater;

    private WisToolKit mToolsKit;

    public AboutWindowAdapter(Context context, ArrayList<TestItem> list) {
        // TODO Auto-generated constructor stub
        this.context = context;
        this.mTestItemList = list;
        inflater = LayoutInflater.from(context);
        mToolsKit = WisToolKit.getToolsKitInstance(context);
    }


    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mTestItemList.size();
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return mTestItemList.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return arg0;
    }

    @Override
    public View getView(int arg0, View arg1, ViewGroup arg2) {
        // TODO Auto-generated method stub
        final Holder mHolder;
        if (arg1 == null) {
            arg1 = inflater.inflate(R.layout.about_frame, null);
            mHolder = new Holder();
            mHolder.mBackground = (LinearLayout) arg1.findViewById(R.id.about_key_background);
            mHolder.mTestItem = (TextView) arg1.findViewById(R.id.about_key_item);
            mHolder.mItemVersion = (TextView) arg1.findViewById(R.id.about_key_version);
            arg1.setTag(mHolder);
        } else {
            mHolder = (Holder) arg1.getTag();
        }

        if (arg0 % 2 == 0) {
            mHolder.mBackground.setBackgroundColor(Color.WHITE);
        } else {
            mHolder.mBackground.setBackgroundColor(Color.LTGRAY);
        }
        if (mToolsKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH) {
            mHolder.mTestItem.setText(mTestItemList.get(arg0).getTestItemName());
        } else {
            mHolder.mTestItem.setText(mTestItemList.get(arg0).getTestItemCNName());
        }
        mHolder.mItemVersion.setText(mTestItemList.get(arg0).getTestItemVersion());
        return arg1;
    }

    private class Holder {
        LinearLayout mBackground;
        TextView mTestItem;
        TextView mItemVersion;
    }
}
