package com.wistron.generic.wifi;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.wistron.generic.pqaa.R;
import com.wistron.pqaa_common.jar.global.WisCommonConst;
import com.wistron.pqaa_common.jar.global.WisLog;
import com.wistron.pqaa_common.jar.global.WisParseValue;
import com.wistron.pqaa_common.jar.global.WisPingTool;
import com.wistron.pqaa_common.jar.global.WisPingTool.OnWisPingStateChangedListener;
import com.wistron.pqaa_common.jar.global.WisToolKit;
import com.wistron.pqaa_common.jar.global.WisWifi;
import com.wistron.pqaa_common.jar.wcis.WisSubHtmlBuilder;
import com.wistron.pqaa_common.jar.wcis.WisWCISCommonConst;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Wireless extends Activity implements OnClickListener {
	private String mWLANConfigFileName = "wifi.cfg";
	private String WLAN_CONFIG_FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
			+ mWLANConfigFileName;

	private String mWCISWLANGeneralLogFileName = "wifi_log.txt";
	private String mWCISWLANHtmlLogFileName = "wifi_html.html";
	private String mWCISWLANGeneralLogFilePath;
	private String mWCISWLANHtmlLogFilePath;

	private static final String WIFI_LOG_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
			+ "wistron/wifi.txt";

	private static final String WIFI_SAVE_MAC_ADDRESS_PATH = "/mnt/sdcard/sysinfo.txt";
	private static final int QUITTIMEOUT = 1;
	// -----------------config file-----------
	private static final String WIFI_CONFIG_PATH = "/mnt/sdcard/pqaa_config/wifi.cfg";
	private String ssid = "wistron5793";
	private int security = 0;
	private String password;
	private int mPingCount = 10, mNeedPass = 10;
	private int mMinRssi = -60, mMatchRssiCount = 0;
	private int mPingInterval = 1;
	private String mPingIPAddress = "192.168.43.1";
	// --------------------------------------
	private static int TIMEOUT = 50;

	private static final int MSG_CONNECT_STATE = 0;
	private static final int MSG_CONNECT_TIMEOUT = 1;
	private static final int MSG_UPDATE_CONNECT_TIME = 2;
	private static final int MSG_GET_MAC = 3;
	private static final int MSG_QUIT = 4;

	private TextView tv_WifiMacAddress;
	private TextView tv_WifiAp, tv_WifiConnectState;
	private Button btn_WifiPing, btn_Exit;
	private TextView tv_WifiCurrentState;
	private TextView tv_PingResult;

	private boolean isWCISTest = false;
	private boolean isComponentMode = true;
	private boolean isPass = false;
	private String mWWANLog = "";
	private String mWCISTestRemark = "";
	private int mTotalCount, mPassCount;

	private String mSelfAddress = null;
	private boolean isPCBStage = false;
	private boolean isRegisterBroadcast = false;
	private boolean isDoPing = false;
	private int mTimerCounter = 0;

	private ProgressDialog mOpenWifiDialog, mCloseWifiDialog;
	private AlertDialog mConnectWifi, mGetMacDialog;

	private Timer mTimer;
	private TimerTask mTask;

	// common tool kit
	private WisToolKit mToolKit;
	private WisWifi mWifiHandler;
	private WisPingTool mPingTool;
	// private WisTextViewVerticalScrollHelper mVerticalScrollHelper;

	// Log class
	private WisLog mLogHandler;

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.wireless);

		mToolKit = new WisToolKit(this);

		getTestArguments();
		findView();
		if (!isWCISTest) {
			try {
				mLogHandler = new WisLog(WIFI_LOG_PATH);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			doCreate();
		}
		setViewByLanguage();
	}

	private void findView() {
		// TODO Auto-generated method stub
		tv_WifiMacAddress = (TextView) findViewById(R.id.wifi_mac_address);
		tv_WifiAp = (TextView) findViewById(R.id.wifi_ap);
		tv_WifiConnectState = (TextView) findViewById(R.id.wifi_ap_state);
		btn_WifiPing = (Button) findViewById(R.id.wifi_ping);
		tv_WifiCurrentState = (TextView) findViewById(R.id.wifi_cur_state);
		tv_PingResult = (TextView) findViewById(R.id.wifi_ping_result);
		tv_PingResult.setMovementMethod(ScrollingMovementMethod.getInstance());
		btn_WifiPing.setOnClickListener(this);
		tv_WifiMacAddress.setText(String.format(mToolKit.getStringResource(R.string.wifi_mac_address),
				mToolKit.getStringResource(R.string.unknown)));

		btn_Exit = (Button) findViewById(R.id.button_exit);
		btn_Exit.setOnClickListener(this);
	}

	private void doCreate() {
		// TODO Auto-generated method stub
		registerBroadcast();
		initialize();
	}

	private void registerBroadcast() {
		// TODO Auto-generated method stub
		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		registerReceiver(wifiChange, mFilter);
		isRegisterBroadcast = true;
	}

	private void setViewByLanguage() {
		// TODO Auto-generated method stub
		TextView mItemTitle = (TextView) findViewById(R.id.item_title);
		mItemTitle.setText(mToolKit.getStringResource(R.string.wifi_test_title));
		btn_Exit.setText(mToolKit.getStringResource(R.string.button_exit));
	}

	private void getMacAddress() {
		// TODO Auto-generated method stub
		mSelfAddress = mWifiHandler.getMacAddress();
		System.out.println("--->" + mSelfAddress);
		if (mSelfAddress != null) {
			if (isPCBStage) {
				isPass = true;
			}
		} else {
			mSelfAddress = mToolKit.getStringResource(R.string.unknown);
		}
		tv_WifiMacAddress.setText(String.format(mToolKit.getStringResource(R.string.wifi_mac_address), mSelfAddress));
	}

	private void getTestArguments() {
		// TODO Auto-generated method stub
		String mTestStyle = mToolKit.getCurrentTestType();
		if (mTestStyle != null) {
			if (mTestStyle.equals(WisCommonConst.TESTSTYLE_SAVED_CONFIG)) {
				isWCISTest = getIntent().getBooleanExtra(WisCommonConst.EXTRA_IS_WCIS_TEST, false);
				isComponentMode = false;
				if (!isWCISTest) {
					isPCBStage = mToolKit.isPCBATestStage();
					WisParseValue mParse = new WisParseValue(this, mToolKit.getCurrentItem(), mToolKit.getCurrentDatabaseAuthorities());
					String arg = mParse.getArg1();
					if (arg != null && arg.length() > 0) {
						int mTestTime = Integer.parseInt(arg);
						if (mTestTime > 0) {
							TIMEOUT = mTestTime;
						}
					}
					arg = mParse.getArg2();
					if (arg != null && arg.length() > 0) {
						ssid = arg;
					}
					arg = mParse.getArg3();
					if (arg != null && arg.length() > 0) {
						security = Integer.parseInt(arg);
					}
					arg = mParse.getArg4();
					if (arg != null && arg.length() > 0) {
						if (security != 0) {
							password = arg;
						}
					}
					arg = mParse.getArg5();
					if (arg != null && arg.length() > 0) {
						mPingCount = Integer.parseInt(arg);
					}
					arg = mParse.getArg6();
					if (arg != null && arg.length() > 0) {
						mNeedPass = Integer.parseInt(arg);
					}
					arg = mParse.getArg7();
					if (arg != null && arg.length() > 0) {
						mMinRssi = Integer.parseInt(arg);
					}
					arg = mParse.getArg8();
					if (arg != null && arg.length() > 0) {
						mPingInterval = Integer.parseInt(arg);
					}
					arg = mParse.getArg9();
					if (arg != null && arg.length() > 0) {
						mPingIPAddress = arg;
					}

				} else {
					WLAN_CONFIG_FILE_PATH = getIntent().getStringExtra(WisWCISCommonConst.EXTRA_CONFIG_FOLDER) + mWLANConfigFileName;
					mWCISWLANGeneralLogFilePath = getIntent().getStringExtra(WisWCISCommonConst.EXTRA_LOG_FOLDER)
							+ mWCISWLANGeneralLogFileName;
					mWCISWLANHtmlLogFilePath = getIntent().getStringExtra(WisWCISCommonConst.EXTRA_LOG_FOLDER) + mWCISWLANHtmlLogFileName;

					registerReceiver(readConfigReceiver, new IntentFilter(WisWCISCommonConst.ACTION_WCIS_FEEDBACK_CONFIG));

					Intent getConfigIntent = new Intent(WisWCISCommonConst.ACTION_WCIS_READ_CONFIG);
					getConfigIntent.putExtra(WisWCISCommonConst.EXTRA_CONFIG_PATH, WLAN_CONFIG_FILE_PATH);
					sendBroadcast(getConfigIntent);
				}
			}
		}
	}

	private BroadcastReceiver readConfigReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			unregisterReceiver(this);
			Map<String, String> mParametersList = (Map<String, String>) intent
					.getSerializableExtra(WisWCISCommonConst.EXTRA_CONFIG_CONTENT);
			updateTestParamsByConfig(mParametersList);
			doCreate();
		}
	};

	private void updateTestParamsByConfig(Map<String, String> mParametersList) {
		if (mParametersList != null && mParametersList.size() > 0) {
			for (String key : mParametersList.keySet()) {
				if (key.equals("ssid")) {
					ssid = mParametersList.get(key);
				} else if (key.equals("security")) {
					security = Integer.parseInt(mParametersList.get(key));
				} else if (key.equals("password")) {
					if (security != 0) {
						password = mParametersList.get(key);
					}
				} else if (key.equals("count")) {
					mPingCount = Integer.parseInt(mParametersList.get(key));
				} else if (key.equals("needPass")) {
					mNeedPass = Integer.parseInt(mParametersList.get(key));
				} else if (key.equals("minRssi")) {
					mMinRssi = Integer.parseInt(mParametersList.get(key));
				} else if (key.equals("interval")) {
					mPingInterval = Integer.parseInt(mParametersList.get(key));
				} else if (key.equals("pingIP")) {
					mPingIPAddress = mParametersList.get(key);
				}
			}
		} else {
			Toast.makeText(this, mToolKit.getStringResource(R.string.noconfigfile_msg), Toast.LENGTH_SHORT).show();
		}
	}

	private void initialize() {
		// TODO Auto-generated method stub
		mWifiHandler = new WisWifi(this);
		mPingTool = new WisPingTool(this);
		mPingTool.setOnWisPingStateChangedListener(mWifiPingListener);

		mOpenWifiDialog = new ProgressDialog(this);
		mOpenWifiDialog.setTitle(mToolKit.getStringResource(R.string.wifi_dialog_title));
		mOpenWifiDialog.setMessage(mToolKit.getStringResource(R.string.wifi_open_dialog_msg));
		mOpenWifiDialog.setIndeterminate(true);

		mCloseWifiDialog = new ProgressDialog(this);
		mCloseWifiDialog.setTitle(mToolKit.getStringResource(R.string.wifi_dialog_title));
		mCloseWifiDialog.setMessage(mToolKit.getStringResource(R.string.wifi_close_dialog_msg));
		mCloseWifiDialog.setIndeterminate(true);

		mGetMacDialog = new AlertDialog.Builder(this).setTitle(mToolKit.getStringResource(R.string.wifi_dialog_title))
				.setMessage(String.format(mToolKit.getStringResource(R.string.wifi_get_mac), TIMEOUT)).setCancelable(false).create();

		if (!isPCBStage) {

			defineDialog();
			mConnectWifi.show();
			if (mWifiHandler.isWifiOn()) {
				getMacAddress();
				mWifiHandler.disCurrentConnect();
				if (isNeedReConnectAp()) {
					connectAp();
				}
			} else {
				mWifiHandler.openWifi();
			}
			initializeWifiConnectTimerTask();
			tv_WifiAp.setText(ssid);
		} else {
			if (!mWifiHandler.isWifiCanUse()) {
				new Thread(quitWait).start();
			} else {
				mGetMacDialog.show();
				initializeWifiGetMacTimer();
				mWifiHandler.openWifi();
				findViewById(R.id.wifi_ping_layout).setVisibility(View.INVISIBLE);
			}
		}
	}

	private void defineDialog() {
		// TODO Auto-generated method stub
		AlertDialog.Builder mConnectWifiBuilder = new AlertDialog.Builder(this);
		mConnectWifiBuilder.setTitle(mToolKit.getStringResource(R.string.wifi_connect_title));
		mConnectWifi = mConnectWifiBuilder.create();
		mConnectWifi.setCancelable(false);
		mConnectWifi.setCanceledOnTouchOutside(false);
		mConnectWifi.setMessage(mToolKit.getStringResource(R.string.wifi_connect_msg) + "\t" + (TIMEOUT - mTimerCounter));
	}

	private void initializeWifiConnectTimerTask() {
		// TODO Auto-generated method stub
		mTimerCounter = 0;
		mTask = new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				mTimerCounter++;
				if (mTimerCounter >= TIMEOUT) {
					handler.sendEmptyMessage(MSG_CONNECT_TIMEOUT);
				} else {
					handler.sendEmptyMessage(MSG_UPDATE_CONNECT_TIME);
				}
			}
		};
		mTimer = new Timer();
		mTimer.schedule(mTask, 1000, 1000);
	}

	private void initializeWifiGetMacTimer() {
		mTimerCounter = 0;
		mTask = new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				mTimerCounter++;
				if (mTimerCounter >= TIMEOUT) {
					handler.sendEmptyMessage(MSG_CONNECT_TIMEOUT);
				} else {
					handler.sendEmptyMessage(MSG_GET_MAC);
				}
			}
		};
		mTimer = new Timer();
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

	private final Runnable quitWait = new Runnable() {

		public void run() {
			// TODO Auto-generated method stub
			try {
				Thread.sleep(QUITTIMEOUT * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				handler.sendEmptyMessage(MSG_QUIT);
			}
		}
	};

	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if (msg.what == MSG_CONNECT_STATE) {
				Log.i("Wireless", "check state.........");
				State mWifiState = mWifiHandler.getWifiConnectState();
				if (mWifiState == State.CONNECTED) {
					Log.i("Wireless", "Connected...");
					tv_WifiConnectState.setText(mToolKit.getStringResource(R.string.wifi_state_connected));
					tv_WifiAp.setText(mWifiHandler.getConnectApSSID());

					if (isNeedReConnectAp()) {
						connectAp();
						return;
					}

					if (mConnectWifi.isShowing()) {
						mConnectWifi.dismiss();
					}
					cancelTimer();
					btn_WifiPing.performClick();
				} else if (mWifiState == State.CONNECTING) {
					Log.i("Wireless", "Connecting...");
					tv_WifiConnectState.setText(mToolKit.getStringResource(R.string.wifi_state_connecting));
				} else if (mWifiState == State.DISCONNECTED) {
					Log.i("Wireless", "DisConnected...");
					tv_WifiConnectState.setText(mToolKit.getStringResource(R.string.wifi_state_disconnected));
					connectAp();
				} else if (mWifiState == State.DISCONNECTING) {
					Log.i("Wireless", "DisConnecting...");
					tv_WifiConnectState.setText(mToolKit.getStringResource(R.string.wifi_state_disconnecting));
				} else if (mWifiState == State.SUSPENDED) {
					Log.i("Wireless", "Suspended...");
					tv_WifiConnectState.setText(mToolKit.getStringResource(R.string.wifi_state_suspended));
				} else if (mWifiState == State.UNKNOWN) {
					Log.i("Wireless", "unknown...");
					tv_WifiConnectState.setText(mToolKit.getStringResource(R.string.wifi_state_unknown));
				}
			} else if (msg.what == MSG_GET_MAC) {
				mGetMacDialog.setMessage(String.format(mToolKit.getStringResource(R.string.wifi_get_mac), TIMEOUT - mTimerCounter));
				if (mSelfAddress != null) {
					if (mGetMacDialog.isShowing()) {
						mGetMacDialog.dismiss();
					}
					cancelTimer();
					new Thread(quitWait).start();
				}
			} else if (msg.what == MSG_UPDATE_CONNECT_TIME) {
				mConnectWifi.setMessage(mToolKit.getStringResource(R.string.wifi_connect_msg) + "\t" + (TIMEOUT - mTimerCounter));
				sendEmptyMessage(MSG_CONNECT_STATE);
			} else if (msg.what == MSG_CONNECT_TIMEOUT) {
				if (mConnectWifi.isShowing()) {
					mConnectWifi.dismiss();
				}
				if (mGetMacDialog.isShowing()) {
					mGetMacDialog.dismiss();
				}
				cancelTimer();
				endTest();
			} else if (msg.what == MSG_QUIT) {
				endTest();
			}
		}
	};

	private void getConfigFile() {
		// TODO Auto-generated method stub
		Map<String, String> mParametersList = mToolKit.getSingleParameters(WIFI_CONFIG_PATH);
		updateTestParamsByConfig(mParametersList);
	}

	private BroadcastReceiver wifiChange = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String mAction = intent.getAction();
			if (mAction.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
				int mState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLED);
				System.out.println("wifi state......" + mState);
				switch (mState) {
				case WifiManager.WIFI_STATE_ENABLED:
					getMacAddress();
					if (!isPCBStage) {
						if (isNeedReConnectAp()) {
							connectAp();
						}
					}
					tv_WifiCurrentState.setText(mToolKit.getStringResource(R.string.wifi_state_turn_on));
					break;
				case WifiManager.WIFI_STATE_ENABLING:
					tv_WifiCurrentState.setText(mToolKit.getStringResource(R.string.wifi_state_turning_on));
					break;
				case WifiManager.WIFI_STATE_DISABLING:
					tv_WifiCurrentState.setText(mToolKit.getStringResource(R.string.wifi_state_turning_off));
					break;
				case WifiManager.WIFI_STATE_DISABLED:
					tv_WifiCurrentState.setText(mToolKit.getStringResource(R.string.wifi_state_turn_off));
					break;
				default:
					break;
				}
			} else if (mAction.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
				handler.sendEmptyMessage(MSG_CONNECT_STATE);
			}
		}
	};

	private void connectAp() {
		mWifiHandler.connectAp(ssid, security, password);
	}

	private boolean isNeedReConnectAp() {
		boolean isReConnect = false;
		String connectSSID = mWifiHandler.getConnectApSSID();
		if (connectSSID != null) {
			Log.i("Wireless", "SSID:" + ssid + "," + connectSSID.replace("\"", ""));
			if (!ssid.equals(connectSSID.replace("\"", ""))) {
				mWifiHandler.disCurrentConnect();
				isReConnect = true;
			}
		} else {
			isReConnect = true;
		}
		Log.i("Wireless", "needReConnect: " + isReConnect);
		return isReConnect;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void updateLog(String log) {
		saveGeneralLog(log);

		mWWANLog += log;
		tv_PingResult.setText(mWWANLog);

		// if (mVerticalScrollHelper == null) {
		// mVerticalScrollHelper = new WisTextViewVerticalScrollHelper(tv_PingResult);
		// }
		// mVerticalScrollHelper.autoScrollByCurrentLine(log);
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == btn_WifiPing) {
			if (!isDoPing) {
				mMatchRssiCount = 0;
				addWCISRemark(String.format(getString(R.string.wifi_ping_start), mPingCount));
				updateLog(String.format(mToolKit.getStringResource(R.string.wifi_ping_start), mPingCount));
				mPingTool.doPing(mPingIPAddress, mPingCount, mPingInterval);
				isDoPing = true;
			}
		} else if (v == btn_Exit) {
			mToolKit.returnWithResult(isPass);
		}
	}

	private void saveGeneralLog(String fileContent) {
		if (isWCISTest) {
			Intent intent = new Intent(WisWCISCommonConst.ACTION_WCIS_WRITE_LOG);
			intent.putExtra(WisWCISCommonConst.EXTRA_LOG_FILE_NAME, mWCISWLANGeneralLogFilePath);
			intent.putExtra(WisWCISCommonConst.EXTRA_LOG_FILE_CONTENT, fileContent);
			intent.putExtra(WisWCISCommonConst.EXTRA_LOG_TYPE, WisWCISCommonConst.LOG_TYPE_GENERAL_LOG);
			sendBroadcast(intent);
		} else {
			try {
				mLogHandler.write(fileContent, true);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void saveHtmlLog() {
		WisSubHtmlBuilder mHtmlBuilder = new WisSubHtmlBuilder(this);
		mHtmlBuilder.makeTitle(getString(R.string.wifi_test_title));
		mHtmlBuilder.date();
		mHtmlBuilder.makeString(mWCISTestRemark);
		Intent intent = new Intent(WisWCISCommonConst.ACTION_WCIS_WRITE_LOG);
		intent.putExtra(WisWCISCommonConst.EXTRA_LOG_FILE_NAME, mWCISWLANHtmlLogFilePath);
		intent.putExtra(WisWCISCommonConst.EXTRA_LOG_FILE_CONTENT, mHtmlBuilder.getResult());
		intent.putExtra(WisWCISCommonConst.EXTRA_LOG_TYPE, WisWCISCommonConst.LOG_TYPE_HTML_LOG);
		sendBroadcast(intent);
	}

	private void addWCISRemark(String remark) {
		if (isWCISTest) {
			mWCISTestRemark += remark;
		}
	}

	private void endTest() {
		getMacAddress();
		if (!isComponentMode && !isWCISTest) {
			try {
				mLogHandler = new WisLog(WIFI_SAVE_MAC_ADDRESS_PATH);
				mLogHandler.write("Wifi MAC Address:" + mSelfAddress, true);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		closeWifi.execute(new Void[] {});
	}

	private AsyncTask<Void, Void, Void> closeWifi = new AsyncTask<Void, Void, Void>() {

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			mWifiHandler.closeWifi();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (mCloseWifiDialog.isShowing()) {
				mCloseWifiDialog.dismiss();
			}
			if (isWCISTest) {
				saveHtmlLog();
				mToolKit.returnWithResultAndRemark(isPass, String.format(getString(R.string.wifi_remark_format), mTotalCount, mPassCount));
			} else {
				if (isComponentMode) {
					if (isPass) {
						Toast.makeText(Wireless.this, mToolKit.getStringResource(R.string.pass), Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(Wireless.this, mToolKit.getStringResource(R.string.fail), Toast.LENGTH_SHORT).show();
					}
				}
				if (isPass) {
					mToolKit.returnWithResult(isPass);
				}else {
					btn_Exit.setVisibility(View.VISIBLE);
				}
			}
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			if (!mCloseWifiDialog.isShowing()) {
				mCloseWifiDialog.show();
			}
		}

	};

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (isRegisterBroadcast) {
			unregisterReceiver(wifiChange);
		}
	}

	private OnWisPingStateChangedListener mWifiPingListener = new OnWisPingStateChangedListener() {

		public void onStateIsPingProgress(String result) {
			// TODO Auto-generated method stub
			String remark = result;
			if (result.contains("icmp_seq")) {
				int rssi = mWifiHandler.getWifiRssi();
				remark = String.format(getString(R.string.wifi_ping_result), result, rssi);
				result = String.format(mToolKit.getStringResource(R.string.wifi_ping_result), result, rssi);
				if (rssi > mMinRssi && result.contains("64 bytes")) {
					mMatchRssiCount++;
				}
			}
			addWCISRemark(remark + "\n");
			updateLog(result + "\n");
		}

		public void onStateIsPingFinish(int[] pingResult) {
			// TODO Auto-generated method stub
			mTotalCount = pingResult[0];
			mPassCount = pingResult[1];
			if (mPassCount >= mNeedPass && mMatchRssiCount >= mNeedPass && mSelfAddress != null) {
				isPass = true;
			}
			endTest();
		}

		public void onStateIsPingStart() {
			// TODO Auto-generated method stub

		}
	};

}