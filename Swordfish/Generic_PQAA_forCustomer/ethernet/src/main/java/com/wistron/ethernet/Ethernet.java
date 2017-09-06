package com.wistron.ethernet;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.wistron.pqaa_common.jar.global.WisAutoScrollTextView;
import com.wistron.pqaa_common.jar.global.WisCommonConst;
import com.wistron.pqaa_common.jar.global.WisLog;
import com.wistron.pqaa_common.jar.global.WisParseValue;
import com.wistron.pqaa_common.jar.global.WisPingTool;
import com.wistron.pqaa_common.jar.global.WisPingTool.OnWisPingStateChangedListener;
import com.wistron.pqaa_common.jar.global.WisToolKit;
import com.wistron.pqaa_common.jar.wcis.WisSubHtmlBuilder;
import com.wistron.pqaa_common.jar.wcis.WisWCISCommonConst;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Ethernet extends Activity {
    private String mEthernetConfigFileName = "ethernet.cfg";
    private String ETHERNET_CONFIG_FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + mEthernetConfigFileName;

    private String mWCISEthernetGeneralLogFileName = "ethernet_log.txt";
    private String mWCISEthernetHtmlLogFileName = "ethernet_html.html";
    private String mWCISEthernetGeneralLogFilePath;
    private String mWCISEthernetHtmlLogFilePath;

    private static final String ETHERNET_LOG_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "wistron/ethernet.txt";

    private static final String ETHERNET_CONFIG_PATH = "/mnt/sdcard/pqaa_config/ethernet.cfg";
    private WisAutoScrollTextView tv_PingResult;

    private ConnectivityManager mConnectivityManager;
    private boolean isWCISTest = false;
    private boolean isComponentMode = true;
    private boolean isRegisterBroadcast = false;
    private boolean isPass = false;
    private String mEthernetLog = "";
    private String mWCISTestRemark = "";
    private int mTotalCount, mPassCount;

    private int mPingCount = 20, mNeedPass = 10;
    private int mPingInterval = 1;
    private String mPingIPAddress = "www.baidu.com";

    // common tool kit
    private WisToolKit mToolKit;
    private WisPingTool mPingTool;

    // Log class
    private WisLog mLogHandler;

    private TextView mResultContent;
    private Button mResultButton;

    //timer
    private Timer mTimer;
    private TimerTask mTask;
    private int mTimeCount = 0;
    private int TimeOut = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.ethernet);

        mToolKit = new WisToolKit(this);
        if (!mToolKit.isWistronLockKey()) {
            Toast.makeText(this, mToolKit.getStringResource(R.string.device_not_match_lock_key), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        mToolKit.setCurrentLanguage(WisCommonConst.LANGUAGE_ENGLISH);
        getTestArguments();
        findView();
        setViewByLanguage();
        if (!isWCISTest) {
            try {
                mLogHandler = new WisLog(ETHERNET_LOG_PATH);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            getConfigFile();
            doCreate();
        }

    }

    private void findView() {
        // TODO Auto-generated method stub
       /* View ethernetView = LayoutInflater.from(this).inflate(R.layout.ethernet, null);
        ((FrameLayout) findViewById(R.id.activity_middle_root)).addView(ethernetView);*/
        tv_PingResult = (WisAutoScrollTextView) findViewById(R.id.ethernet_ping_result);
        tv_PingResult.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    private void setViewByLanguage() {
        // TODO Auto-generated method stub
        TextView mItemTitle = (TextView) findViewById(R.id.item_title);
        mItemTitle.setText(mToolKit.getStringResource(R.string.ethernet_test_title));
       // ((TextView) findViewById(R.id.item_title)).setText(mToolKit.getStringResource(R.string.ethernet_test_title));
    }

    private void getConfigFile() {
        // TODO Auto-generated method stub
        Map<String, String> mParametersList = mToolKit.getSingleParameters(ETHERNET_CONFIG_PATH);
        updateTestParamsByConfig(mParametersList);
    }

    private void updateTestParamsByConfig(Map<String, String> mParametersList) {
        // TODO Auto-generated method stub
        if (mParametersList != null && mParametersList.size() > 0) {
            for (String key : mParametersList.keySet()) {
                if (key.equals("count")) {
                    int count = Integer.parseInt(mParametersList.get(key));
                    if (count > 0) {
                        mPingCount = count;
                    }
                } else if (key.equals("needPass")) {
                    int needPass = Integer.parseInt(mParametersList.get(key));
                    if (needPass > 0) {
                        mNeedPass = needPass;
                    }
                } else if (key.equals("interval")) {
                    int interval = Integer.parseInt(mParametersList.get(key));
                    if (interval > 0) {
                        mPingInterval = interval;
                    }
                } else if (key.equals("pingAddress")) {
                    String address = mParametersList.get(key);
                    if (address != null && address.trim().length() > 0) {
                        mPingIPAddress = address;
                    }
                }
            }
        }
    }

    private void doCreate() {
        // TODO Auto-generated method stub
        initial();
        startTest();
    }

    private void initial() {
        // TODO Auto-generated method stub
        mConnectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        mPingTool = new WisPingTool(this);
        mPingTool.setOnWisPingStateChangedListener(mPingStateChangedListener);

        registerReceiver(netWorkConnectStateChanged, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        isRegisterBroadcast = true;
    }
    private void initialTimer() {
        // TODO Auto-generated method stub
        mTimer=new Timer();
        mTask = new TimerTask() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                mTimeCount++;
                if (mTimeCount > TimeOut) {
                    handler.sendEmptyMessage(0);
                }else{
                    handler.sendEmptyMessage(1);
                }
            }
        };
        mTimer.schedule(mTask, 100, 1000);
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
            switch (msg.what) {
                case 0:
                    cancelTimer();
                    endTest();
                    break;
                case 1:
                    if (isEthernetConnect()) {
                        cancelTimer();
                        updateLog(String.format(mToolKit.getStringResource(R.string.ethernet_ping_start), mPingCount));
                        doPing();
                    }
                    break;

                default:
                    break;
            }
        }

    };
    private void getTestArguments() {
        // TODO Auto-generated method stub
        String mTestStyle = mToolKit.getCurrentTestType();
        if (mTestStyle != null) {
            if (mTestStyle.equals(WisCommonConst.TESTSTYLE_SAVED_CONFIG)) {
                isWCISTest = getIntent().getBooleanExtra(WisCommonConst.EXTRA_IS_WCIS_TEST, false);
                isComponentMode = false;
                if (isWCISTest) {
                    ETHERNET_CONFIG_FILE_PATH = getIntent().getStringExtra(WisWCISCommonConst.EXTRA_CONFIG_FOLDER) + mEthernetConfigFileName;
                    mWCISEthernetGeneralLogFilePath = getIntent().getStringExtra(WisWCISCommonConst.EXTRA_LOG_FOLDER) + mWCISEthernetGeneralLogFileName;
                    mWCISEthernetHtmlLogFilePath = getIntent().getStringExtra(WisWCISCommonConst.EXTRA_LOG_FOLDER) + mWCISEthernetHtmlLogFileName;

                    registerReceiver(readConfigReceiver, new IntentFilter(WisWCISCommonConst.ACTION_WCIS_FEEDBACK_CONFIG));

                    Intent getConfigIntent = new Intent(WisWCISCommonConst.ACTION_WCIS_READ_CONFIG);
                    getConfigIntent.putExtra(WisWCISCommonConst.EXTRA_CONFIG_PATH, ETHERNET_CONFIG_FILE_PATH);
                    sendBroadcast(getConfigIntent);
                } else {
                    WisParseValue mParse = new WisParseValue(this, mToolKit.getCurrentItem(), mToolKit.getCurrentDatabaseAuthorities());
                    String arg = mParse.getArg1();
                    if (arg != null) {
                        int count = Integer.parseInt(arg);
                        if (count > 0) {
                            mPingCount = count;
                        }
                    }
                    arg = mParse.getArg2();
                    if (arg != null) {
                        int needPass = Integer.parseInt(arg);
                        if (needPass > 0) {
                            mNeedPass = needPass;
                        }
                    }
                    arg = mParse.getArg3();
                    if (arg != null) {
                        int interval = Integer.parseInt(arg);
                        if (interval > 0) {
                            mPingInterval = interval;
                        }
                    }
                    arg = mParse.getArg4();
                    if (arg != null) {
                        String address = arg;
                        if ( address.trim().length() > 0) {
                            mPingIPAddress = address;
                        }
                    }
                }
            }
        }
    }

    private BroadcastReceiver readConfigReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            unregisterReceiver(this);
            Map<String, String> mParametersList = (Map<String, String>) intent.getSerializableExtra(WisWCISCommonConst.EXTRA_CONFIG_CONTENT);
            updateTestParamsByConfig(mParametersList);
            doCreate();
        }
    };

    private void updateLog(String log) {
        saveGeneralLog(log);

        mEthernetLog += log;
        tv_PingResult.addText(log);
    }

    private void startTest() {
        initialTimer();
       /* if (isEthernetConnect()) {
            updateLog(String.format(mToolKit.getStringResource(R.string.ethernet_ping_start), mPingCount));
            doPing();
        } else {
           // Toast.makeText(this,"Ethernet is disconnected!",Toast.LENGTH_SHORT).show();
            endTest();
        }*/
    }

    private void saveGeneralLog(String fileContent) {
        if (isWCISTest) {
            Intent intent = new Intent(WisWCISCommonConst.ACTION_WCIS_WRITE_LOG);
            intent.putExtra(WisWCISCommonConst.EXTRA_LOG_FILE_NAME, mWCISEthernetGeneralLogFilePath);
            intent.putExtra(WisWCISCommonConst.EXTRA_LOG_FILE_CONTENT, fileContent);
            intent.putExtra(WisWCISCommonConst.EXTRA_LOG_TYPE, WisWCISCommonConst.LOG_TYPE_GENERAL_LOG);
            sendBroadcast(intent);
        } else {
            try {
                if (mLogHandler != null) {
                    mLogHandler.write(fileContent, true);
                }
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
        mHtmlBuilder.makeTitle(getString(R.string.ethernet_test_title));
        mHtmlBuilder.date();
        mHtmlBuilder.makeString(mWCISTestRemark);
        Intent intent = new Intent(WisWCISCommonConst.ACTION_WCIS_WRITE_LOG);
        intent.putExtra(WisWCISCommonConst.EXTRA_LOG_FILE_NAME, mWCISEthernetHtmlLogFilePath);
        intent.putExtra(WisWCISCommonConst.EXTRA_LOG_FILE_CONTENT, mHtmlBuilder.getResult());
        intent.putExtra(WisWCISCommonConst.EXTRA_LOG_TYPE, WisWCISCommonConst.LOG_TYPE_HTML_LOG);
        sendBroadcast(intent);
    }

    private void doPing() {
        // TODO Auto-generated method stub
        mPingTool.doPing(mPingIPAddress, mPingCount, mPingInterval);
    }

    private boolean isEthernetConnect() {
        // TODO Auto-generated method stub
        boolean isEthernetConnect = false;
        String mTempStatus_Log = "", mTempStatus_WCIS = "";
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (mNetworkInfo == null) {
            mTempStatus_WCIS = getString(R.string.ethernet_no_network_available);
            mTempStatus_Log = mToolKit.getStringResource(R.string.ethernet_no_network_available);
        } else {
            int type = mNetworkInfo.getType();
            if (type == ConnectivityManager.TYPE_WIFI) {
                mTempStatus_WCIS = getString(R.string.ethernet_network_is_wifi);
                mTempStatus_Log = mToolKit.getStringResource(R.string.ethernet_network_is_wifi);
            } else if (type == ConnectivityManager.TYPE_MOBILE) {
                mTempStatus_WCIS = getString(R.string.ethernet_network_is_3g);
                mTempStatus_Log = mToolKit.getStringResource(R.string.ethernet_network_is_3g);
            } else if (type == ConnectivityManager.TYPE_ETHERNET) {
                isEthernetConnect = true;
            } else {
                mTempStatus_WCIS = getString(R.string.ethernet_invalid_data_connect);
                mTempStatus_Log = mToolKit.getStringResource(R.string.ethernet_invalid_data_connect);
            }
        }
        if (!isEthernetConnect) {
            addWCISRemark(mTempStatus_WCIS);
            updateLog(mTempStatus_Log);
            Toast.makeText(this, mTempStatus_Log, Toast.LENGTH_SHORT).show();
        }
        return isEthernetConnect;
    }

    private void addWCISRemark(String remark) {
        if (isWCISTest) {
            mWCISTestRemark += remark;
        }
    }

    private BroadcastReceiver netWorkConnectStateChanged = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                if (mPingTool.isDoingPing() && !isEthernetConnect()) {
                    mPingTool.stopPing();
                }
            }
        }
    };

    private OnWisPingStateChangedListener mPingStateChangedListener = new OnWisPingStateChangedListener() {

        @Override
        public void onStateIsPingStart() {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStateIsPingProgress(String result) {
            // TODO Auto-generated method stub
            String pingResult = (result + "\n");
            addWCISRemark(pingResult);
            updateLog(pingResult);
        }

        @Override
        public void onStateIsPingFinish(int[] pingResult) {
            // TODO Auto-generated method stub
            mTotalCount = pingResult[0];
            mPassCount = pingResult[1];
            if (mPassCount >= mNeedPass && mTotalCount > 0) {
                isPass = true;
            }
            endTest();
        }
    };

    private void endTest() {
        if (isWCISTest) {
            saveHtmlLog();
            mToolKit.returnWithResultAndRemark(isPass, String.format(getString(R.string.ethernet_remark_format), mTotalCount, mPassCount));
        } else {
            if (isComponentMode) {
               // mToolKit.showResultPage(this, isPass, mToolKit.getStringResource(R.string.ethernet_test_title));
                displayResult();
            } else {
                mToolKit.returnWithResult(isPass);
            }
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

        mResultButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mToolKit.returnWithResult(isPass);
            }
        });
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (mPingTool.isDoingPing()) {
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (isRegisterBroadcast) {
            unregisterReceiver(netWorkConnectStateChanged);
            isRegisterBroadcast = false;
        }
    }
}

