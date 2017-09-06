package com.wistron.generic.bluetooth;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.wistron.generic.pqaa.R;
import com.wistron.pqaa_common.jar.global.WisBluetooth;
import com.wistron.pqaa_common.jar.global.WisBluetooth.OnWisBTScanStateChangedListener;
import com.wistron.pqaa_common.jar.global.WisCommonConst;
import com.wistron.pqaa_common.jar.global.WisLog;
import com.wistron.pqaa_common.jar.global.WisParseValue;
import com.wistron.pqaa_common.jar.global.WisToolKit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class BlueTooth extends Activity implements OnClickListener {
    private static final String BLUETOOTH_LOG_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "sysinfo.txt";
    private static final int MSG_QUIT = 0;
    private static final int MSG_STOPDISCOVERY = 1;
    private static final int MSG_GET_MAC_ADDRESS = 2;

    private static final int QUITTIMEOUT = 2;
    // ----------------------result---------------
    private TextView mResultContent;
    private Button mResultButton;
    private boolean isResultPage = false;
    // --------------------------------------
    private TextView mSelfAddress;
    private ListView mPairedView, mNewDevicesView;
    private Button mStartScanButton, mExitButton;
    private ArrayList<Map<String, String>> mPairedList;
    private ArrayList<Map<String, String>> mNewDevicesList;

    private SimpleAdapter mPairedAdapter;
    private SimpleAdapter mNewDevicesAdapter;
    private ProgressDialog mOpenBTDialog;
    private AlertDialog mGetAddressDialog;
    private String mBTMacAddress = null;
    private boolean isPass = false;
    private boolean isRegisterBroadcast = false;

    private boolean mComponentMode = false;
    private boolean isPCBStage = false;
    // --------------------write log
    private int mTimes = 0;
    private Timer mTimer = new Timer();
    private TimerTask mTimeOutTask;
    private int TIMEOUT = 10; // seconds

    // common tool kit
    private WisToolKit mToolKit;
    private WisBluetooth mBluetoothHandler;

    // Log class
    private WisLog mLogHandler;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.bluetooth);

        mToolKit = new WisToolKit(this);

        try {
            mLogHandler = new WisLog(BLUETOOTH_LOG_PATH);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        getTestArguments();
        registerBroadcast();
        findView();
        setViewByLanguage();
        initial();
    }

    private void registerBroadcast() {
        // TODO Auto-generated method stub
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothChanged, mFilter);
        isRegisterBroadcast = true;
    }

    private void initial() {
        // TODO Auto-generated method stub
        mBluetoothHandler = new WisBluetooth(this);
        mBluetoothHandler.setOnWisBTScanStateChangedListener(mBtScanStateChangedListener);

        mOpenBTDialog = new ProgressDialog(this);
        mOpenBTDialog.setTitle(mToolKit.getStringResource(R.string.bluetooth_dialog_title));
        mOpenBTDialog.setMessage(mToolKit.getStringResource(R.string.bluetooth_open_bt_dialog_msg));
        mOpenBTDialog.setIndeterminate(true);
        mOpenBTDialog.setCancelable(false);
        mOpenBTDialog.setCanceledOnTouchOutside(false);

        mGetAddressDialog = new AlertDialog.Builder(this)
                .setTitle(mToolKit.getStringResource(R.string.bluetooth_dialog_title))
                .setMessage(String.format(mToolKit.getStringResource(R.string.bluetooth_get_mac_dialog_msg), TIMEOUT))
                .setCancelable(false)
                .create();

        if (!mBluetoothHandler.isBluetoothCanUse()) {
            Toast.makeText(this, mToolKit.getStringResource(R.string.bluetooth_disable), Toast.LENGTH_LONG).show();
            displayResult();
        } else if (!mBluetoothHandler.isBluetoothEnable()) {
            mOpenBTDialog.show();
            mBluetoothHandler.openBluetooth();
            initializeTimerTask();
        } else {
            showMacAddress();
            if (isPCBStage) {
                mGetAddressDialog.show();
            } else {
                mStartScanButton.performClick();
            }
            initializeTimerTask();
        }
    }

    private void setViewByLanguage() {
        // TODO Auto-generated method stub
        TextView mItemTitle = (TextView) findViewById(R.id.item_title);
        mItemTitle.setText(mToolKit.getStringResource(R.string.bluetooth_test_title));
        mSelfAddress.setText(String.format(mToolKit.getStringResource(R.string.bluetooth_local_address), mToolKit.getStringResource(R.string.unknown)));
        ((TextView) findViewById(R.id.bluetooth_title_paired_devices)).setText(mToolKit.getStringResource(R.string.bluetooth_title_paired_devices));
        ((TextView) findViewById(R.id.bluetooth_title_new_devices)).setText(mToolKit.getStringResource(R.string.bluetooth_title_other_devices));
        mStartScanButton.setText(mToolKit.getStringResource(R.string.button_scan));
        mExitButton.setText(mToolKit.getStringResource(R.string.button_exit));
    }

    private void initializeTimerTask() {
        // TODO Auto-generated method stub
        mTimeOutTask = new TimerTask() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                mTimes++;
                if (mTimes >= TIMEOUT) {
                    if (isPCBStage) {
                        mHandler.sendEmptyMessage(MSG_QUIT);
                    } else {
                        mHandler.sendEmptyMessage(MSG_STOPDISCOVERY);
                    }
                } else {
                    if (isPCBStage) {
                        mHandler.sendEmptyMessage(MSG_GET_MAC_ADDRESS);
                    }
                }
            }
        };
        mTimer.schedule(mTimeOutTask, 1000, 1000);
    }

    private void cancelTimer() {
        // TODO Auto-generated method stub
        if (mTimeOutTask != null) {
            mTimeOutTask.cancel();
            mTimeOutTask = null;
        }
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        mTimeOutTask = null;
        mTimer = null;
    }

    private void getTestArguments() {
        // TODO Auto-generated method stub
        String mTestStyle = mToolKit.getCurrentTestType();
        if (mTestStyle != null) {
            if (mTestStyle.equals(WisCommonConst.TESTSTYLE_SAVED_CONFIG)) {
                mComponentMode = false;
                isPCBStage = mToolKit.isPCBATestStage();
                WisParseValue mParse = new WisParseValue(this, mToolKit.getCurrentItem(), mToolKit.getCurrentDatabaseAuthorities());
                int mTestTime = Integer.parseInt(mParse.getArg1());
                if (mTestTime > 0) {
                    TIMEOUT = mTestTime;
                }
            }
        }
    }

    private void findView() {
        // TODO Auto-generated method stub
        mSelfAddress = (TextView) findViewById(R.id.bluetooth_self_address);
        mPairedView = (ListView) findViewById(R.id.bluetooth_paired_devices);
        mNewDevicesView = (ListView) findViewById(R.id.bluetooth_new_devices);
        mStartScanButton = (Button) findViewById(R.id.bluetooth_button_scan);
        mExitButton = (Button) findViewById(R.id.bluetooth_button_exit);

        mPairedList = new ArrayList<Map<String, String>>();
        mNewDevicesList = new ArrayList<Map<String, String>>();
        mPairedAdapter = new SimpleAdapter(this, mPairedList, R.layout.simple_list_item_2,
                new String[]{WisBluetooth.BLUETOOTH_EXTRA_DATA_DEVICE_NAME, WisBluetooth.BLUETOOTH_EXTRA_DATA_DEVICE_ADDRESS},
                new int[]{R.id.text1, R.id.text2});
        mNewDevicesAdapter = new SimpleAdapter(this, mNewDevicesList, R.layout.simple_list_item_2,
                new String[]{WisBluetooth.BLUETOOTH_EXTRA_DATA_DEVICE_NAME, WisBluetooth.BLUETOOTH_EXTRA_DATA_DEVICE_ADDRESS},
                new int[]{R.id.text1, R.id.text2});
        mPairedView.setAdapter(mPairedAdapter);
        mNewDevicesView.setAdapter(mNewDevicesAdapter);

        mStartScanButton.setOnClickListener(this);
        mExitButton.setOnClickListener(this);
        mStartScanButton.setVisibility(View.INVISIBLE);
        mExitButton.setVisibility(View.INVISIBLE);

        if (isPCBStage) {
            findViewById(R.id.bluetooth_search_area).setVisibility(View.GONE);
            mStartScanButton.setEnabled(false);
            mExitButton.setEnabled(false);
        }
    }

    private void showMacAddress() {
        mBTMacAddress = mBluetoothHandler.getBluetoothAddress();
        if (mBTMacAddress != null) {
            if (isPCBStage) {
                isPass = true;
            }
        } else {
            mBTMacAddress = mToolKit.getStringResource(R.string.unknown);
        }
        mSelfAddress.setText(String.format(mToolKit.getStringResource(R.string.bluetooth_local_address), mBTMacAddress));
    }

    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            if (msg.what == MSG_QUIT) {
                if (mGetAddressDialog.isShowing()) {
                    mGetAddressDialog.dismiss();
                }
                mExitButton.performClick();
            } else if (msg.what == MSG_STOPDISCOVERY) {
                cancelTimer();
                mBluetoothHandler.cancelDiscovery();
            } else if (msg.what == MSG_GET_MAC_ADDRESS) {
                showMacAddress();
                if (mBTMacAddress != null && mBTMacAddress.length() > 0) {
                    if (mGetAddressDialog.isShowing()) {
                        mGetAddressDialog.dismiss();
                    }
                    cancelTimer();
                    new Thread(quitWait).start();
                } else {
                    mGetAddressDialog.setMessage(String.format(mToolKit.getStringResource(R.string.bluetooth_get_mac_dialog_msg), TIMEOUT - mTimes));
                }
            }
        }

    };

    private final Runnable quitWait = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            try {
                Thread.sleep(QUITTIMEOUT * 1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                mHandler.sendEmptyMessage(MSG_QUIT);
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, mToolKit.getStringResource(R.string.bluetooth_cannot_open), Toast.LENGTH_LONG).show();
            displayResult();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isResultPage) {
                mResultButton.performClick();
            } else {
                mExitButton.performClick();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v == mStartScanButton) {
            mStartScanButton.setVisibility(View.INVISIBLE);
            mExitButton.setVisibility(View.INVISIBLE);
            mBluetoothHandler.doDiscovery();
            mStartScanButton.setEnabled(false);
        } else if (v == mExitButton) {
            mBluetoothHandler.cancelDiscovery();
            if (mComponentMode) {
                isResultPage = true;
                displayResult();
            } else {
                releaseResourceAndReturn();
            }
        } else if (v == mResultButton) {
            releaseResourceAndReturn();
        }
    }

    private void displayResult() {
        // TODO Auto-generated method stub
        setContentView(R.layout.result);
        mResultContent = (TextView) findViewById(R.id.result_result);
        mResultButton = (Button) findViewById(R.id.result_back);
        mResultButton.setText(mToolKit.getStringResource(R.string.ok));
        if (isPass) {
            mResultContent.setText(mToolKit.getStringResource(R.string.pass));
        } else {
            mResultContent.setText(mToolKit.getStringResource(R.string.fail));
        }
        mResultButton.setOnClickListener(this);
    }

    private BroadcastReceiver bluetoothChanged = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String mAction = intent.getAction();
            if (mAction.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                showMacAddress();
                if (mBluetoothHandler.isBluetoothEnable()) {
                    if (mOpenBTDialog.isShowing()) {
                        mOpenBTDialog.dismiss();
                    }
                    if (!isPCBStage) {
                        mStartScanButton.performClick();
                    }
                }
            }
        }
    };

    private void releaseResourceAndReturn() {
        cancelTimer();
        try {
            mLogHandler.write("Bluetooth address:" + mBTMacAddress, true);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (!isPCBStage) {
            mBluetoothHandler.cancelDiscovery();
        }
        mBluetoothHandler.closeBluetooth();
        mToolKit.returnWithResult(isPass);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        // Unregister broadcast listeners
        if (isRegisterBroadcast) {
            unregisterReceiver(bluetoothChanged);
            isRegisterBroadcast = false;
        }
    }

    private OnWisBTScanStateChangedListener mBtScanStateChangedListener = new OnWisBTScanStateChangedListener() {

        @Override
        public void onStateIsStartFindPairedDevices() {
            // TODO Auto-generated method stub
            findViewById(R.id.bluetooth_search_paired_indicator).setVisibility(View.VISIBLE);
        }

        @Override
        public void onStateIsStartFindNewDevices() {
            // TODO Auto-generated method stub
            findViewById(R.id.bluetooth_search_new_indicator).setVisibility(View.VISIBLE);
        }

        @Override
        public void onStateIsStartDiscovery() {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStateIsScanFinish() {
            // TODO Auto-generated method stub
            findViewById(R.id.bluetooth_search_paired_indicator).setVisibility(View.INVISIBLE);
            if (mBTMacAddress != null && mNewDevicesList.size() > 0) {
                isPass = true;
            }
            if (mNewDevicesList.size() == 0) {
                String noDevices = mToolKit.getStringResource(R.string.bluetooth_none_found).toString();
                Map<String, String> mTempMap = new HashMap<String, String>();
                mTempMap.put(WisBluetooth.BLUETOOTH_EXTRA_DATA_DEVICE_NAME, noDevices);
                mTempMap.put(WisBluetooth.BLUETOOTH_EXTRA_DATA_DEVICE_ADDRESS, null);
                mNewDevicesList.add(mTempMap);
            }
            mNewDevicesAdapter.notifyDataSetChanged();
            mExitButton.performClick();
        }

        @Override
        public void onStateIsFoundPairedDevices(ArrayList<Map<String, String>> pairedList) {
            // TODO Auto-generated method stub
            findViewById(R.id.bluetooth_search_paired_indicator).setVisibility(View.INVISIBLE);
            mPairedList.addAll(pairedList);
            if (mPairedList.size() <= 0) {
                String noDevices = mToolKit.getStringResource(R.string.bluetooth_none_paired).toString();
                Map<String, String> mTempMap = new HashMap<String, String>();
                mTempMap.put(WisBluetooth.BLUETOOTH_EXTRA_DATA_DEVICE_NAME, noDevices);
                mTempMap.put(WisBluetooth.BLUETOOTH_EXTRA_DATA_DEVICE_ADDRESS, null);
                mPairedList.add(mTempMap);
            }
            mPairedAdapter.notifyDataSetChanged();
        }

        @Override
        public void onStateIsFoundNewDevice(ArrayList<Map<String, String>> newDeviceList) {
            // TODO Auto-generated method stub
            Log.i("Bluetooth", "Get new device...");
            mNewDevicesList.clear();
            mNewDevicesList.addAll(newDeviceList);
            mNewDevicesAdapter.notifyDataSetChanged();
        }
    };

}