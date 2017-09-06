package com.wistron.carbon8_cash_drawer;

import java.io.File;
import java.util.ArrayList;

import android.R.bool;
import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.os.Build;

import com.wistron.pqaa_common.jar.global.WisCommonConst;
import com.wistron.pqaa_common.jar.global.WisParseValue;
import com.wistron.pqaa_common.jar.global.WisToolKit;
import com.wistron.carbon8_cash_drawer.WisShellCommandHelper.onResultChangedListener;
import com.wistron.carbon8_cash_drawer.WisShellCommandHelper;

public class MainActivity extends Activity {

	private WisShellCommandHelper mShellCommandHelper;
	private WisToolKit mWisToolKit;
	private WisToolKit mToolKit;
	
private AlertDialog mAlertDialog; 
	
	private void ShowDialog(Context context){
		AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle("Cash Drawer Test").setCancelable(false);
		builder.setPositiveButton("通过", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				mWisToolKit.returnWithResult(true);
			}
		});
		
		builder.setNegativeButton("失败", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				mWisToolKit.returnWithResult(false);
			}
		});
		
		builder.show();
	}
	
	public boolean fileIsExists(){
		try {
			File f = new File("/sys/devices/platform/carbon8-drawer-device/drawer_control");
			if (!f.exists()){
				Log.i("cash drawer", "the node don't exist!");
				return false;
			}
			else{
				ArrayList<String> mResult = mShellCommandHelper.exec("echo 1 > /sys/devices/platform/carbon8-drawer-device/drawer_control");
				Log.i("cash drawer", "the value of the node = "+mResult+"");
			}
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
		return true;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mShellCommandHelper = new WisShellCommandHelper();
		mWisToolKit = new WisToolKit(this);
		mToolKit=new WisToolKit(this);
		if (!mToolKit.isWistronLockKey()) {
			Toast.makeText(this, mToolKit.getStringResource(R.string.device_not_match_lock_key), Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		getTestArguments();
		
		//ArrayList<String> mResult = mShellCommandHelper.exec("echo 1 > /sys/devices/platform/carbon8-drawer-device/drawer_control");
		//Log.i("cash drawer", "the value of the node = "+mResult+"");
		
		if (fileIsExists()){
			ShowDialog(this);
		}
	}

	private void getTestArguments() {
		// TODO Auto-generated method stub
		String mTestStyle = mWisToolKit.getCurrentTestType();
		if (mTestStyle != null) {
			if (mTestStyle.equals(WisCommonConst.TESTSTYLE_SAVED_CONFIG)) {
				//mComponentMode = false;
				WisParseValue mParse = new WisParseValue(this, mWisToolKit.getCurrentItem(),mWisToolKit.getCurrentDatabaseAuthorities());
				/*int mTestTime = Integer.parseInt(mParse.getArg1());
				if (mTestTime > 0) {
					TIMEOUT = mTestTime;
				}*/
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

}
