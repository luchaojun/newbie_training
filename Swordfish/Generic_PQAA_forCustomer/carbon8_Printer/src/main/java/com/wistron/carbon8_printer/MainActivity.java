package com.wistron.carbon8_printer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.Instrumentation;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.print.PrintHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.wistron.pqaa_common.jar.global.WisShellCommandHelper;
import com.wistron.pqaa_common.jar.global.WisToolKit;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements OnClickListener {

	private WisToolKit mToolKit;
	private Button print;
	private WisShellCommandHelper mShellCommandHelper;

	private Timer mTimer;
	private TimerTask mTask;
	private int mTimeCount = 0;
	
	private void doPhotoPrint() {
		PrintHelper photoPrinter = new PrintHelper(this);
		photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);

		mShellCommandHelper = new WisShellCommandHelper();
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test);
		
		if (bitmap == null){
			Log.e("Printer", "Image file not found!");
			return;
		}
		
		photoPrinter.printBitmap("droids.jpg - test print", bitmap);
		/*mShellCommandHelper.exec("input tap 50 50");
		mShellCommandHelper.exec("input tap 50 50");
		try{
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		mShellCommandHelper.exec("input tap 1250 150");*/
	}
	
	private AlertDialog mAlertDialog; 
	
	private void ShowDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle("Printer Test").setCancelable(false);
		builder.setPositiveButton(mToolKit.getStringResource(R.string.pass), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				mToolKit.returnWithResult(true);
			}
		});
		
		builder.setNegativeButton(mToolKit.getStringResource(R.string.fail), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				mToolKit.returnWithResult(false);
			}
		});
		
		builder.show();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mToolKit=new WisToolKit(this);
		if (!mToolKit.isWistronLockKey()) {
			Toast.makeText(this, mToolKit.getStringResource(R.string.device_not_match_lock_key), Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		print = (Button)findViewById(R.id.print);
		print.setOnClickListener(this);
		print.performClick();
	}

	@Override
	public void onClick(View v) {
		if (v == print){
			initialTimer();
			doPhotoPrint();

		}
	}
	private void initialTimer() {
		// TODO Auto-generated method stub
		mTimer=new Timer();
		mTask = new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				mTimeCount++;
				if (mTimeCount == 2){
					autotouch(332,397);
				}
				if (mTimeCount == 3){
					autotouch(932,470);
				}
				if (mTimeCount == 4){
					autotouch(50,50);
				}
				if (mTimeCount == 5){
					autotouch(50,50);
				}
				if (mTimeCount == 6) {
					autotouch(1250,150);
					handler.sendEmptyMessage(0);
				}
			}
		};
		mTimer.schedule(mTask, 1000, 1000);
	}

	private void cancelTimer() {
		if (mTask != null) {
			mTask.cancel();
			mTask = null;
		}
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
	}
	private Handler handler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			cancelTimer();
			ShowDialog();
		}

	};

	private void autotouch(int X, int Y){
		Instrumentation inst=new Instrumentation();
		try {
			Log.i("WKS","----start onkeydown!");
			Log.i("WKS","---->X:\n"+X+"----->Y:"+Y);
			inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, X, Y, 0));
			Log.i("WKS","----start onkeyUp!");
			inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, X, Y, 0));
			Log.i("WKS","----completed test!");
		}catch (Exception e){
			Log.i("WKS","---->Exception: "+e.toString());
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
