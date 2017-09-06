package com.wistron.generic.pqaa;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.wistron.generic.pqaa.utility.TestItem;
import com.wistron.pqaa_common.jar.global.WisCommonConst;
import com.wistron.pqaa_common.jar.global.WisToolKit;

import java.util.ArrayList;

public class TestAdapter extends BaseAdapter {
    private final int ITEM_DISPLAY_MODE_GONE = -1;
    private final int ITEM_DISPLAY_MODE_DISABLE = 0;
    private final int ITEM_DISPLAY_MODE_ENABLE = 1;
    private TestItemsList context;
    private final ArrayList<TestItem> mTestItemList;
    private final LayoutInflater inflater;
    private final Handler mHandler;

    private WisToolKit mToolsKit;

    public TestAdapter(TestItemsList context, ArrayList<TestItem> list, Handler mHandler) {
        // TODO Auto-generated constructor stub
        this.context = context;
        this.mTestItemList = list;
        this.mHandler = mHandler;
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
    public View getView(final int arg0, View arg1, ViewGroup arg2) {
        // TODO Auto-generated method stub
        final Holder mHolder;
        if (arg1 == null) {
            arg1 = inflater.inflate(R.layout.pqaa_listadapter, null);
            mHolder = new Holder();
            mHolder.mTestIndex = (TextView) arg1.findViewById(R.id.adapter_index);
            mHolder.mTestItem = (CheckBox) arg1.findViewById(R.id.adapter_item);
            mHolder.mCmdLine = (TextView) arg1.findViewById(R.id.adapter_cmdline);
            mHolder.mResult = (ImageView) arg1.findViewById(R.id.adapter_result);
            arg1.setTag(mHolder);
            switch (context.getItemDisplayMode()) {
                case ITEM_DISPLAY_MODE_GONE:
                    mHolder.mTestItem.setButtonDrawable(new ColorDrawable(Color.TRANSPARENT));
                    mHolder.mTestItem.setPadding(mHolder.mTestItem.getWidth(), 0, 0, 0);
                    mHolder.mTestItem.setEnabled(false);
                    break;
                case ITEM_DISPLAY_MODE_DISABLE:
                    mHolder.mTestItem.setEnabled(false);
                    break;
                case ITEM_DISPLAY_MODE_ENABLE:
                    break;
                default:
                    break;
            }
        } else {
            mHolder = (Holder) arg1.getTag();
        }

        if (mTestItemList.get(arg0).isInstalled()) {
            mHolder.mTestIndex.setTextColor(context.getResources().getColor(R.color.color_black));
            mHolder.mTestItem.setTextColor(context.getResources().getColor(R.color.color_black));
            if (context.getItemDisplayMode() == ITEM_DISPLAY_MODE_ENABLE) {
                mHolder.mTestItem.setEnabled(true);
            }
        } else {
            mHolder.mTestIndex.setTextColor(Color.RED);
            mHolder.mTestItem.setTextColor(Color.RED);
            mHolder.mTestItem.setEnabled(false);
        }
        mHolder.mTestIndex.setText(String.valueOf(arg0 + 1));
        if (mToolsKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH) {
            mHolder.mTestItem.setText(mTestItemList.get(arg0).getTestItemName());
        } else {
            mHolder.mTestItem.setText(mTestItemList.get(arg0).getTestItemCNName());
        }
        if (TestItemsList.isHideCmdLine) {
            mHolder.mCmdLine.setVisibility(View.GONE);
        } else {
            mHolder.mCmdLine.setText(mTestItemList.get(arg0).getTestItemCmdLine());
        }
        if (mTestItemList.get(arg0).getTestItemResult() == TestItem.RESULT_PASS) {
            mHolder.mResult.setImageResource(R.drawable.pass);
        } else if (mTestItemList.get(arg0).getTestItemResult() == TestItem.RESULT_FAIL) {
            mHolder.mResult.setImageResource(R.drawable.failed);
        } else {
            mHolder.mResult.setImageBitmap(null);
        }
        if (mTestItemList.get(arg0).isChecked()) {
            mHolder.mTestItem.setChecked(true);
        } else {
            mHolder.mTestItem.setChecked(false);
        }
        mHolder.mTestItem.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (mHolder.mTestItem.isChecked()) {
                    mTestItemList.get(arg0).setChecked(true);
                } else {
                    mTestItemList.get(arg0).setChecked(false);
                }
                mHandler.sendEmptyMessage(arg0);
            }
        });
        arg1.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // TODO Auto-generated method stub
                if (hasFocus) {
                    mHandler.sendEmptyMessage(arg0);
                }
            }
        });
        return arg1;
    }

    private class Holder {
        TextView mTestIndex;
        CheckBox mTestItem;
        TextView mCmdLine;
        ImageView mResult;
    }
}
