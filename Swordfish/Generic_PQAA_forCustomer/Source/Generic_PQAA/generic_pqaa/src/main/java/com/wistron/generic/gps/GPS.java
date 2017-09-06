package com.wistron.generic.gps;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.wistron.generic.internal.gps.IGPSService;
import com.wistron.generic.pqaa.R;
import com.wistron.pqaa_common.jar.autotest.WisGPS_Service;
import com.wistron.pqaa_common.jar.global.WisCommonConst;
import com.wistron.pqaa_common.jar.global.WisParseValue;
import com.wistron.pqaa_common.jar.global.WisToolKit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class GPS extends Activity implements OnClickListener {
    private static final String GPS_ADAPTER_INDEX = "index";
    private static final String GPS_ADAPTER_SNR = "snr";
    private static final String GPS_ADAPTER_VALUE = "value";

    private static final int QUITTIMEOUT = 1;
    private final int GPS_TYPE_CN = 0;
    private final int GPS_TYPE_LOCATION = 1;
    // ----------------------result---------------
    private TextView mResultContent;
    private Button mResultButton;
    // --------------------------------------
    private final int MSG_REFRESHTIME = 0;
    private final int MSG_EXIT = 1;
    private final int MSG_SEARCHFOUND = 2;
    private ProgressBar pbSearchingBar;
    private TextView tvLocationInformation;
    private ListView lvSatellite;
    private Button btnStart, btnExit;

    private boolean isPass;
    private boolean mComponentMode = true;

    private Timer mTimer = new Timer();
    private TimerTask mTimeout;
    private int mTimes = 0;
    private int TimeOut = 60;
    private boolean isPCBStage = false;

    private int mTestType = GPS_TYPE_CN;
    private SimpleAdapter mAdapter;
    private ArrayList<Map<String, Object>> mSatelliteList;
    private int mTotalSatellite = 10, mPassSatellite = 6, mPassSNR = 40;
    private boolean isRegisteredBroadcast;

    // common tool kit
    private WisToolKit mToolKit;
    private Intent mGpsServiceIntent;

    private IGPSService iGpsService;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.gps);

        mToolKit = new WisToolKit(this);

        findView();
        getTestArguments();
        setViewByLanguage();
        if (!hasGPSDevice()){
            Toast.makeText(this,"Device not Support GPS!",Toast.LENGTH_SHORT).show();
            mToolKit.returnWithResult(false);
        }
        Intent intent = new Intent();
        intent.setAction(WisGPS_Service.ACTION_GPS_SERVICE);
        final Intent eintent = new Intent(createExplicitFromImplicitIntent(this, intent));
        boolean bindResult = this.bindService(eintent, mServiceConnection, Service.BIND_AUTO_CREATE);

        if (!bindResult) {
            mToolKit.returnWithResult(false);
        }

		/*bindService(new Intent(WisGPS_Service.ACTION_GPS_SERVICE), mServiceConnection,
                Context.BIND_AUTO_CREATE);*/
    }

    // 20170322 Payne Add judge support GPS device
    private boolean hasGPSDevice(){
        final LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (manager == null){
            return false;
        }
        final List<String> providers = manager.getAllProviders();
        if (providers == null){
            return false;
        }
        return providers.contains(LocationManager.GPS_PROVIDER);
    }
    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);

        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }

        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);

        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);

        // Set the component to be explicit
        explicitIntent.setComponent(component);

        return explicitIntent;
    }

    private void initial() {
        // TODO Auto-generated method stub
        mGpsServiceIntent = new Intent(this, GPSService.class);
        if (isPCBStage) {
            boolean isHasModule = false;
            try {
                isHasModule = iGpsService.isHasGPSModule();
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (isHasModule) {
                isPass = true;
                tvLocationInformation.setText(mToolKit.getStringResource(R.string.gps_ok));
            } else {
                isPass = false;
                tvLocationInformation.setText(mToolKit.getStringResource(R.string.gps_none));
            }
            new Thread(waitQuit).start();
        } else {
            registerReceiver(gpsReceiver, new IntentFilter(WisGPS_Service.ACTION_GPS_STATE_CHANGED));
            isRegisteredBroadcast = true;

            pbSearchingBar.setVisibility(View.VISIBLE);
            btnStart.performClick();
        }
    }

    private void findView() {
        // TODO Auto-generated method stub
        pbSearchingBar = (ProgressBar) findViewById(R.id.gps_progress);
        tvLocationInformation = (TextView) findViewById(R.id.gps_location);
        lvSatellite = (ListView) findViewById(R.id.gps_satellite);
        btnStart = (Button) findViewById(R.id.gps_start);
        btnExit = (Button) findViewById(R.id.gps_exit);
        btnStart.setOnClickListener(this);
        btnExit.setOnClickListener(this);

        pbSearchingBar.setVisibility(View.GONE);
    }

    private void setViewByLanguage() {
        // TODO Auto-generated method stub
        ((TextView) findViewById(R.id.item_title)).setText(mToolKit
                .getStringResource(R.string.gps_test_title));
        btnStart.setText(mToolKit.getStringResource(R.string.button_start));
        btnExit.setText(mToolKit.getStringResource(R.string.button_exit));
    }

    private Runnable waitQuit = new Runnable() {

        public void run() {
            // TODO Auto-generated method stub
            try {
                Thread.sleep(QUITTIMEOUT * 1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                mHandler.sendEmptyMessage(MSG_EXIT);
            }
        }
    };

    private void getTestArguments() {
        // TODO Auto-generated method stub
        String mTestStyle = mToolKit.getCurrentTestType();
        if (mTestStyle != null) {
            if (mTestStyle.equals(WisCommonConst.TESTSTYLE_SAVED_CONFIG)) {
                mComponentMode = false;
                isPCBStage = mToolKit.isPCBATestStage();
                WisParseValue mParse = new WisParseValue(this, mToolKit.getCurrentItem(),
                        mToolKit.getCurrentDatabaseAuthorities());
                int mTestTime = Integer.parseInt(mParse.getArg1());
                // mTestType = Integer.parseInt(mParse.getArg2());
                int tempTotalSatellite = Integer.parseInt(mParse.getArg2());
                int tempPassSatellite = Integer.parseInt(mParse.getArg3());
                int tempPassSnr = Integer.parseInt(mParse.getArg4());
                if (mTestTime > 0) {
                    TimeOut = mTestTime;
                }
                if (tempTotalSatellite > 0) {
                    mTotalSatellite = tempTotalSatellite;
                }
                if (tempPassSatellite > 0) {
                    mPassSatellite = tempPassSatellite;
                }
                if (tempPassSnr > 0) {
                    mPassSNR = tempPassSnr;
                }
            }
        }
    }

    private void setTimer() {
        // TODO Auto-generated method stub
        mTimeout = new TimerTask() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                mTimes++;
                mHandler.sendEmptyMessage(MSG_REFRESHTIME);
                if (mTimes >= TimeOut) {
                    isPass = false;
                    mHandler.sendEmptyMessage(MSG_EXIT);
                }
            }
        };
        mTimer.schedule(mTimeout, 1000, 1000);
    }

    private void killTimer() {
        if (mTimeout != null) {
            mTimeout.cancel();
            mTimeout = null;
        }
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    private void setSearchContent() {
        // TODO Auto-generated method stub
        if (mTestType == GPS_TYPE_CN) {
            tvLocationInformation.setText(mToolKit.getStringResource(R.string.gps_search_satellite)
                    + (TimeOut - mTimes));
        } else if (mTestType == GPS_TYPE_LOCATION) {
            tvLocationInformation.setText(mToolKit.getStringResource(R.string.gps_search_location)
                    + (TimeOut - mTimes));
        }
    }

    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            if (msg.what == MSG_EXIT) {
                if (!isPCBStage) {
                    pbSearchingBar.setVisibility(View.GONE);
                    killTimer();

                    mGpsServiceIntent.putExtra(WisGPS_Service.EXTRA_GPS_SERVICE_DO_ACTION,
                            WisGPS_Service.GPS_SERVICE_CLOSE_GPS);
                    startService(mGpsServiceIntent);
                }
                btnExit.performClick();
            } else if (msg.what == MSG_REFRESHTIME) {
                setSearchContent();
            } else if (msg.what == MSG_SEARCHFOUND) {
                pbSearchingBar.setVisibility(View.GONE);
                killTimer();
            }
        }

    };
    private final Runnable mExitRunnable = new Runnable() {

        public void run() {
            // TODO Auto-generated method stub
            try {
                mHandler.sendEmptyMessage(MSG_SEARCHFOUND);
                Thread.sleep(2000);
                isPass = true;
                mHandler.sendEmptyMessage(MSG_EXIT);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    };

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        return true;
    }

    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v == btnStart) {
            mGpsServiceIntent.putExtra(WisGPS_Service.EXTRA_GPS_SERVICE_DO_ACTION,
                    WisGPS_Service.GPS_SERVICE_OPEN_GPS);
            startService(mGpsServiceIntent);

            if (mTestType == GPS_TYPE_CN) {
                tvLocationInformation.setText(mToolKit
                        .getStringResource(R.string.gps_search_satellite));
                lvSatellite.setVisibility(View.VISIBLE);

                mSatelliteList = new ArrayList<Map<String, Object>>();
                mAdapter = new SimpleAdapter(this, mSatelliteList, R.layout.gps_adapter,
                        new String[]{GPS_ADAPTER_INDEX, GPS_ADAPTER_SNR}, new int[]{
                        R.id.gps_index, R.id.gps_snr});
                lvSatellite.setAdapter(mAdapter);

                mGpsServiceIntent.putExtra(WisGPS_Service.EXTRA_GPS_SERVICE_DO_ACTION,
                        WisGPS_Service.GPS_SERVICE_SEARCH_SATELLITE);
                startService(mGpsServiceIntent);
            } else if (mTestType == GPS_TYPE_LOCATION) {
                tvLocationInformation.setText(mToolKit
                        .getStringResource(R.string.gps_search_location));
                mGpsServiceIntent.putExtra(WisGPS_Service.EXTRA_GPS_SERVICE_DO_ACTION,
                        WisGPS_Service.GPS_SERVICE_SEARCH_LOCATION);
                startService(mGpsServiceIntent);
            }
            setTimer();
            setSearchContent();
        } else if (v == btnExit) {
            if (mComponentMode) {
                displayResult();
            } else {
                backToPQAA();
            }
        } else if (v == mResultButton) {
            backToPQAA();
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

    private void backToPQAA() {
        mToolKit.returnWithResult(isPass);
    }

    private BroadcastReceiver gpsReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (intent.getAction().equals(WisGPS_Service.ACTION_GPS_STATE_CHANGED)) {
                int state = intent.getIntExtra(WisGPS_Service.EXTRA_GPS_STATE,
                        WisGPS_Service.GPS_STATE_SATELLITE_CHANGED);
                if (state == WisGPS_Service.GPS_STATE_SATELLITE_CHANGED) {
                    if (mTestType == GPS_TYPE_CN) {
                        mSatelliteList.clear();
                        ArrayList<Float> result = (ArrayList<Float>) intent
                                .getSerializableExtra(WisGPS_Service.EXTRA_GPS_SATELLITE_LIST);
                        for (int i = 0; i < result.size(); i++) {
                            HashMap<String, Object> map = new HashMap<String, Object>();
                            map.put(GPS_ADAPTER_INDEX,
                                    String.format(mToolKit
                                            .getStringResource(R.string.gps_satellite_index), i + 1));
                            map.put(GPS_ADAPTER_SNR, String.format(
                                    mToolKit.getStringResource(R.string.gps_satellite_snr),
                                    result.get(i)));
                            map.put(GPS_ADAPTER_VALUE, result.get(i));
                            mSatelliteList.add(map);
                        }
                        mAdapter.notifyDataSetChanged();
                        detectIfPass();
                    }
                } else if (state == WisGPS_Service.GPS_STATE_LOCATION_CHANGED) {
                    if (mTestType == GPS_TYPE_LOCATION) {
                        String[] locationValue = intent.getStringExtra(
                                WisGPS_Service.EXTRA_GPS_LOCATION_DATA).split(":");
                        StringBuffer mLocationInfo = new StringBuffer();
                        mLocationInfo.append(mToolKit
                                .getStringResource(R.string.gps_location_value));
                        mLocationInfo.append(String.format(
                                mToolKit.getStringResource(R.string.gps_location_longitude_value),
                                locationValue[0]));
                        mLocationInfo.append(String.format(
                                mToolKit.getStringResource(R.string.gps_location_latitude_value),
                                locationValue[1]));
                        tvLocationInformation.setText(mLocationInfo);

                        new Thread(mExitRunnable).start();
                    }
                }
            }
        }
    };

    protected void detectIfPass() {
        // TODO Auto-generated method stub
        if (mSatelliteList.size() >= mTotalSatellite) {
            int mPassCount = 0;
            for (Map<String, Object> item : mSatelliteList) {
                if (Float.parseFloat(item.get(GPS_ADAPTER_VALUE).toString()) >= mPassSNR) {
                    mPassCount++;
                    if (mPassCount >= mPassSatellite) {
                        isPass = true;
                        mHandler.sendEmptyMessage(MSG_EXIT);
                        break;
                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        unbindService(mServiceConnection);
        if (mGpsServiceIntent != null) {
            stopService(mGpsServiceIntent);
        }
        if (isRegisteredBroadcast) {
            unregisterReceiver(gpsReceiver);
            isRegisteredBroadcast = false;
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            iGpsService = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            iGpsService = IGPSService.Stub.asInterface(service);
            initial();
        }

    };
}