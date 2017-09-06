package com.wistron.generic.pqaa.utility;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.wistron.generic.pqaa.R;
import com.wistron.pqaa_common.jar.global.WisCommonConst;
import com.wistron.pqaa_common.jar.global.WisToolKit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class AboutWindow {
    private Context context;
    private PopupWindow mAboutWindow;
    private ArrayList<TestItem> mCurTestItems;
    private AboutWindowAdapter mAboutAdapter;
    private TextView mContentView;

    private WisToolKit mToolsKit;

    public AboutWindow(Context context) {
        super();
        this.context = context;
        mToolsKit = WisToolKit.getToolsKitInstance(context);
        initialAboutWindow();
        addPQAAContent();
    }

    private void initialAboutWindow() {
        // TODO Auto-generated method stub
        mCurTestItems = new ArrayList<TestItem>();

        final View mAboutView = LayoutInflater.from(context).inflate(R.layout.aboutwindow, null);
        mAboutWindow = new PopupWindow(mAboutView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mAboutWindow.setAnimationStyle(R.style.popup_show_style);
        mAboutWindow.setFocusable(true);
        mAboutWindow.setTouchable(true);
        mAboutWindow.setOutsideTouchable(true);
        mAboutWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));   // dismiss window
//		mAboutWindow.update();
        mAboutView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (mAboutWindow.isShowing()) {
                    mAboutWindow.dismiss();
                }
            }
        });

        mContentView = (TextView) mAboutView.findViewById(R.id.about_content);

		/*mAboutInfoScrollView.setOnTouchListener(new OnTouchListener() {
			
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if (mAboutWindow.isShowing()) {
					mAboutWindow.dismiss();
				}
				return true;
			}
		});*/

        ListView mVersionView = (ListView) mAboutView.findViewById(R.id.about_version_list);
        mVersionView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mVersionView.setCacheColorHint(0);
        mVersionView.setFadingEdgeLength(0);
        mVersionView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                // TODO Auto-generated method stub
                if (mAboutWindow.isShowing()) {
                    mAboutWindow.dismiss();
                }
            }

        });
        mVersionView.setFooterDividersEnabled(true);
        TextView mFootView = new TextView(context);
        mVersionView.addFooterView(mFootView);

        View mHeaderView = mAboutView.findViewById(R.id.about_version_header);
        mHeaderView.setBackgroundColor(context.getResources().getColor(R.color.color_list_title_key_background));
        ((TextView) mHeaderView.findViewById(R.id.about_key_item)).setTextAppearance(context, R.style.test_content_primary_middle);
        ((TextView) mHeaderView.findViewById(R.id.about_key_item)).setText(mToolsKit.getStringResource(R.string.pqaa_about_key_item));
        ((TextView) mHeaderView.findViewById(R.id.about_key_version)).setTextAppearance(context, R.style.test_content_primary_middle);
        ((TextView) mHeaderView.findViewById(R.id.about_key_version)).setText(mToolsKit.getStringResource(R.string.pqaa_about_key_version));
        mAboutAdapter = new AboutWindowAdapter(context, mCurTestItems);
        mVersionView.setAdapter(mAboutAdapter);

        mAboutView.findViewById(R.id.about_version_header).setVisibility(View.INVISIBLE);
    }

    public void setCurTestItems(ArrayList<TestItem> itemList) {
        mCurTestItems.clear();
        mCurTestItems.addAll(itemList);
//		mAboutAdapter.notifyDataSetChanged();
    }

    private void addPQAAContent() {
        // TODO Auto-generated method stub
        try {
            InputStream mInputStream;
            // if(mLocale.equals(Locale.SIMPLIFIED_CHINESE)){
            // mInputStream = getAssets().open("version_cn.txt");
            // }else if(mLocale.equals(Locale.TRADITIONAL_CHINESE)){
            // mInputStream = getAssets().open("version_tw.txt");
            // }else{
            // mInputStream = getAssets().open("version.txt");
            // }
            if (mToolsKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH) {
                mInputStream = context.getAssets().open("version.txt");
            } else if (mToolsKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_CHINESE_SIMPLE) {
                mInputStream = context.getAssets().open("version_cn.txt");
            } else {
                mInputStream = context.getAssets().open("version.txt");
            }
            BufferedReader mBis = new BufferedReader(new InputStreamReader(mInputStream));
            StringBuffer mStringBuffer = new StringBuffer();
            String mReadContent;
            while ((mReadContent = mBis.readLine()) != null) {
                mStringBuffer.append(mReadContent);
                mStringBuffer.append("\n");
            }
            mBis.close();

            int versionCode = mToolsKit.getAppVersionCode(context.getPackageName());
            int year = versionCode / 10000;
            int month = (versionCode % 10000) / 100;
            int day = versionCode % 100;
            if (mToolsKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH) {
                mContentView.setText(String.format(mStringBuffer.toString(), mToolsKit.getAppVersion(), month, day, year));
            } else if (mToolsKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_CHINESE_SIMPLE) {
                mContentView.setText(String.format(mStringBuffer.toString(), mToolsKit.getAppVersion(), year, month, day));
            } else {
                mContentView.setText(String.format(mStringBuffer.toString(), mToolsKit.getAppVersion(), month, day, year));
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            mContentView.setText(mToolsKit.getStringResource(R.string.pqaa_about_error));
        }
    }

    public void popupUpAboutWindow(View v) {
        // TODO Auto-generated method stub
        if (!mAboutWindow.isShowing()) {
//			mAboutWindow.showAsDropDown(v);
//			mAboutWindow.getContentView().setPadding(0, v.getBottom(), 0, 0);
            mAboutWindow.showAtLocation(v, Gravity.NO_GRAVITY, 0, 0);
        }
    }
}
