package com.wistron.generic.pqaa;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.wistron.generic.pqaa.utility.AboutWindow;
import com.wistron.generic.pqaa.utility.CommonParams;
import com.wistron.generic.pqaa.utility.DataContentProvider;
import com.wistron.generic.pqaa.utility.DataContentProvider.RunInfo;
import com.wistron.generic.pqaa.utility.TestItem;
import com.wistron.generic.pqaa.utility.XMLParse;
import com.wistron.pqaa_common.jar.global.WisAlertDialog;
import com.wistron.pqaa_common.jar.global.WisCommonConst;
import com.wistron.pqaa_common.jar.global.WisLog;
import com.wistron.pqaa_common.jar.global.WisResultFile;
import com.wistron.pqaa_common.jar.global.WisToolKit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

public class TestItemsList extends Activity implements OnClickListener {
    private final int ITEM_DISPLAY_MODE_GONE = -1;
    private final int ITEM_DISPLAY_MODE_DISABLE = 0;
    private final int ITEM_DISPLAY_MODE_ENABLE = 1;

    private static final int REQUEST_CODE = 0;

    private static final int MSG_DISPLAY_TEST_LIST = 0;
    private static final int MSG_WAIT_AUTO_TEST = 1;
    private static final int MSG_START_AUTO_TEST = 2;

    private static final int INDEX_ITEM = 0;
    private static final int INDEX_ARG1 = 1;
    private String[] mDatabaseColumns;

    private AlertDialog mWaitDialog;
    private WisLog mLogHandler, mWisLog;

    private ListView mTestItemListView;
    private Spinner mTestStyleSpinner;
    private Button mStartTestButton, mSettingButton;
    private CheckBox mSelectAllBox;
    private ImageButton mMoveUpButton, mMoveDownButton;
    private TestAdapter mAdapter;
    private Button mAboutButton;

    private String[] mTestStyle;

    private TestItem mTestItem;
    private ArrayList<TestItem> mCurrentList;
    private ContentResolver mResolver;
    private ContentValues values;
    public static int mCurrentSelectedItemIndex = 0; // move up or move next
    private int mCurrentTestItemIndex = 0;
    private String mCurrentTestStyle;

    private boolean isAutoStart = false;
    private boolean isConfirmRestart = false;
    public static boolean isHideCmdLine = true;
    private boolean is3GPCBA = true;
    private boolean isTouchPanelPCBA = false;
    private boolean isGPSPCBA = false;
    private boolean isWifiPCBA = false;
    private boolean isNFCPCBA = false;
    private boolean isSensorPCBA = false;
    private boolean isBlueToothPCBA = false;
    private boolean isNGShowWarning = false;
    private boolean isNGContinue = true;
    private int mItemDisplayMode = ITEM_DISPLAY_MODE_ENABLE;

    private boolean isStartTest = false;

    // auto start parameters
    private ProgressBar mAutoStartBar;
    private AlertDialog mAutoStartDialog;
    private int mAutoStartCurProgress;
    private int mAutoStartWaitTime = 5;

    // restart parameters
    private WisAlertDialog mRestartDialog;

    // Tool kit instance
    private WisToolKit mToolsKit;
    private AboutWindow mAboutDialog;

    // test
    private WisLog mTempLog;
    private String Security_lock = "wortsin";
    private String Security_codes = null;
    private String mSecurityPath = Environment.getExternalStorageDirectory().getPath() + File.separator;
    private String mCodeName = "md5.txt";
    private String mLockName = "computer.txt";

    private String station = "111", tester_id = "222", machine_sn = "333", cso = "444";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD, WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.pqaa_tab_test);

        mToolsKit = WisToolKit.getToolsKitInstance(this);
        mToolsKit.setCurrentLanguage(WisCommonConst.LANGUAGE_ENGLISH);
        if (!checkMode()) {
            Toast.makeText(this,
                    mToolsKit.getStringResource(R.string.device_not_match_lock_key),
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        getSettingParameter();
        initialize();
        getDatabaseObject();
        forTestSet();
        mWaitDialog.show();
        new Thread(mWaitRunnable).start();
        updateViewByLanguage();

    }
    private boolean checkMode(){
        String result = "";
        boolean flag = false;
        try {
            java.lang.Process mRunCmd = Runtime.getRuntime().exec("getprop ro.hardware");
            InputStream in = mRunCmd.getInputStream();
            BufferedReader mReader = new BufferedReader(new InputStreamReader(in));
            while ((result = mReader.readLine()) != null) {
                System.out.println(result);
                if(result.contains("swordfish")){
                    flag = true;
                    break;
                }
            }
            mRunCmd = Runtime.getRuntime().exec("getprop ro.product.name");
            in = mRunCmd.getInputStream();
            mReader = new BufferedReader(new InputStreamReader(in));
            while ((result = mReader.readLine()) != null) {
                System.out.println(result);
                if(result.contains("Carbon_10")){
                    flag = true;
                    break;
                }
            }
            if(!flag){
                return false;
            }
            mReader.close();
            in.close();
            mRunCmd.waitFor();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
    }

    private void getLogInfoConfig() {
        Map<String, String> mParametersList = mToolsKit.getSingleParameters(CommonParams.PQAA_GENERIC_LOG_DATA);
        if (mParametersList != null && mParametersList.size() > 0) {
            for (String key : mParametersList.keySet()) {
                String value = mParametersList.get(key);
                if (key.equals("station")) {
                    station = value;
                } else if (key.equals("tester_id")) {
                    tester_id = value;
                } else if (key.equals("machine_sn")) {
                    machine_sn = value;
                } else if (key.equals("cso")) {
                    cso = value;
                }
            }
        }
        writeLog("[Header]");
        writeLog(String.format(getResources().getString(R.string.log_station), station));
        writeLog(String.format(getResources().getString(R.string.log_tester_id), tester_id));
        writeLog(String.format(getResources().getString(R.string.log_machine_sn), machine_sn));
        writeLog(String.format(getResources().getString(R.string.log_cso_rma), cso));

    }

    private void writeLog(String content) {
        try {
            mWisLog.write(content, false, true);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String MD5(String string) {
        byte[] hash = null;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException("Huh,MD5 should be supported", e);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException("Huh,UTF-8 should be supported", e);
        }

        StringBuilder hexBuilder = new StringBuilder(hash.length * 2);
        if (hash != null) {
            for (byte b : hash) {
                if ((b & 0xFF) < 0x10) {
                    hexBuilder.append("0");
                }
                hexBuilder.append(Integer.toHexString(b & 0xFF));
            }
        }
        return hexBuilder.toString();
    }

    public boolean getSecurityFile() {
        File mCodeFile = new File(mSecurityPath + mCodeName);
        if (!mCodeFile.exists()) {
            return false;
        }
        File mLockFile = new File(mSecurityPath + mLockName);
        if (!mLockFile.exists()) {
            return false;
        }
        // String result = null;
        // try {
        // FileReader mFileReader = new FileReader(mLockFile);
        // BufferedReader mReader = new BufferedReader(mFileReader);
        // while ((result = mReader.readLine()) != null) {
        // System.out.println(result);
        // if (result != null && !result.equals("")) {
        // Security_lock = result.trim();
        // mTempLog.write(Security_lock, true);
        // break;
        // }
        // }
        // mReader.close();
        // mFileReader.close();
        // } catch (FileNotFoundException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // return false;
        // } catch (IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // return false;
        // }

        String line = null;
        try {
            FileReader mFileReader = new FileReader(mCodeFile);
            BufferedReader mReader = new BufferedReader(mFileReader);
            while ((line = mReader.readLine()) != null) {
                if (line != null && !line.equals("")) {
                    Security_codes = line.trim();
                    break;
                }
            }
            mReader.close();
            mFileReader.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        mLockFile.delete();
        mCodeFile.delete();
        if (Security_codes != null && Security_lock != null) {
            if (MD5(Security_lock).equals(Security_codes)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private void updateViewByLanguage() {
        // TODO Auto-generated method stub
        ((TextView) findViewById(R.id.item_title)).setText(mToolsKit.getStringResource(R.string.pqaa_test_title));
        ((TextView) findViewById(R.id.title_test)).setText(mToolsKit.getStringResource(R.string.Title_TestItem));
        ((TextView) findViewById(R.id.title_cmd)).setText(mToolsKit.getStringResource(R.string.Title_CMDLine));
        ((TextView) findViewById(R.id.title_result)).setText(mToolsKit.getStringResource(R.string.Title_Result));
        ((TextView) findViewById(R.id.tip_flow)).setText(mToolsKit.getStringResource(R.string.Tip_Flow));
        mSelectAllBox.setText(mToolsKit.getStringResource(R.string.checkbox_selectall));
        mStartTestButton.setText(mToolsKit.getStringResource(R.string.Button_Start));
        mAboutButton.setText(mToolsKit.getStringResource(R.string.Indicator_about));
    }

    private void getSettingParameter() {
        // TODO Auto-generated method stub
        SharedPreferences mStagePreferences = getSharedPreferences("start", MODE_PRIVATE);
        Map<String, String> mParametersList = mToolsKit.getSingleParameters(CommonParams.PQAA_START_PATH);
        if (mParametersList != null && mParametersList.size() > 0) {
            for (String key : mParametersList.keySet()) {
                if (key.equals("is3GPCBA")) {
                    is3GPCBA = parseBoolean(mParametersList.get(key));
                } else if (key.equals("isTouchPanelPCBA")) {
                    isTouchPanelPCBA = parseBoolean(mParametersList.get(key));
                } else if (key.equals("isGPSPCBA")) {
                    isGPSPCBA = parseBoolean(mParametersList.get(key));
                } else if (key.equals("isWifiPCBA")) {
                    isWifiPCBA = parseBoolean(mParametersList.get(key));
                } else if (key.equals("isNFCPCBA")) {
                    isNFCPCBA = parseBoolean(mParametersList.get(key));
                } else if (key.equals("isSensorPCBA")) {
                    isSensorPCBA = parseBoolean(mParametersList.get(key));
                } else if (key.equals("isBlueToothPCBA")) {
                    isBlueToothPCBA = parseBoolean(mParametersList.get(key));
                } else if (key.equals("isNGShowWarning")) {
                    isNGShowWarning = parseBoolean(mParametersList.get(key));
                } else if (key.equals("isNGContinue")) {
                    isNGContinue = parseBoolean(mParametersList.get(key));
                } else if (key.equals("isAutoStart")) {
                    isAutoStart = parseBoolean(mParametersList.get(key));
                } else if (key.equals("autoStartWaitTime")) {
                    mAutoStartWaitTime = Integer.parseInt(mParametersList.get(key));
                } else if (key.equals("isConfirmRestart")) {
                    isConfirmRestart = parseBoolean(mParametersList.get(key));
                } else if (key.equals("isHideCmdLine")) {
                    isHideCmdLine = parseBoolean(mParametersList.get(key));
                } else if (key.equals("language")) {
                    mToolsKit.setCurrentLanguage(Integer.parseInt(mParametersList.get(key)));
                } else if (key.equals("itemdisplaymode")) {
                    mItemDisplayMode = Integer.parseInt(mParametersList.get(key));
                }
            }
            Editor mEditor = mStagePreferences.edit();
            mEditor.putBoolean("isAutoStart", isAutoStart);
            mEditor.putInt("autostartwaittime", mAutoStartWaitTime);
            mEditor.putBoolean("isconfirmrestart", isConfirmRestart);
            mEditor.putBoolean("is3gpcba", is3GPCBA);
            mEditor.putBoolean("istouchpanelpcba", isTouchPanelPCBA);
            mEditor.putBoolean("isgpspcba", isGPSPCBA);
            mEditor.putBoolean("iswifipcba", isWifiPCBA);
            mEditor.putBoolean("isnfcpcba", isNFCPCBA);
            mEditor.putBoolean("issensorpcba", isSensorPCBA);
            mEditor.putBoolean("isbluetoothpcba", isBlueToothPCBA);
            mEditor.putBoolean("isngshowwarning", isNGShowWarning);
            mEditor.putBoolean("isngcontinue", isNGContinue);
            mEditor.putBoolean("isHideCmdLine", isHideCmdLine);
            mEditor.putInt("language", mToolsKit.getCurrentLanguage());
            mEditor.putInt("itemdisplaymode", mItemDisplayMode);
            mEditor.commit();
        } else {
            isAutoStart = mStagePreferences.getBoolean("isAutoStart", isAutoStart);
            mAutoStartWaitTime = mStagePreferences.getInt("autostartwaittime", 5);
            isConfirmRestart = mStagePreferences.getBoolean("isconfirmrestart", isConfirmRestart);
            is3GPCBA = mStagePreferences.getBoolean("is3gpcba", is3GPCBA);
            isTouchPanelPCBA = mStagePreferences.getBoolean("istouchpanelpcba", isTouchPanelPCBA);
            isGPSPCBA = mStagePreferences.getBoolean("isgpspcba", isGPSPCBA);
            isWifiPCBA = mStagePreferences.getBoolean("iswifipcba", isWifiPCBA);
            isNFCPCBA = mStagePreferences.getBoolean("isnfcpcba", isNFCPCBA);
            isSensorPCBA = mStagePreferences.getBoolean("issensorpcba", isSensorPCBA);
            isBlueToothPCBA = mStagePreferences.getBoolean("isbluetoothpcba", isBlueToothPCBA);
            isNGShowWarning = mStagePreferences.getBoolean("isngshowwarning", isNGShowWarning);
            isNGContinue = mStagePreferences.getBoolean("isngcontinue", isNGContinue);
            isHideCmdLine = mStagePreferences.getBoolean("isHideCmdLine", isHideCmdLine);
            mToolsKit.setCurrentLanguage(mStagePreferences.getInt("language", WisCommonConst.LANGUAGE_ENGLISH));
            mItemDisplayMode = mStagePreferences.getInt("itemdisplaymode", ITEM_DISPLAY_MODE_ENABLE);
        }
    }

    public int getItemDisplayMode() {
        return mItemDisplayMode;
    }

    private boolean parseBoolean(String mValue) {
        // TODO Auto-generated method stub
        return Integer.parseInt(mValue) == 1 ? true : false;
    }

    private void deleteLastLog() {
        // TODO Auto-generated method stub
        File mLogFile = new File(CommonParams.PQAA_RESULT_PASS_FILE_PATH);
        if (mLogFile.exists()) {
            mLogFile.delete();
        }
        mLogFile = new File(CommonParams.PQAA_RESULT_FALI_FILE_PATH);
        if (mLogFile.exists()) {
            mLogFile.delete();
        }
        mLogFile = new File(CommonParams.PQAA_SYSTEMINFO_FILE_PATH);
        if (mLogFile.exists()) {
            mLogFile.delete();
        }
        mLogHandler.deleteLogFile();
    }

    private void forTestSet() {
        // TODO Auto-generated method stub
        mTestStyleSpinner.setSelection(1);
        mTestStyleSpinner.setEnabled(false);
        mMoveUpButton.setVisibility(View.GONE);
        mMoveDownButton.setVisibility(View.GONE);
        mSettingButton.setVisibility(View.GONE);
    }

    private void getDatabaseObject() {
        // TODO Auto-generated method stub
        mResolver = getContentResolver();
    }

    protected void detectIfAutoStart() {
        // TODO Auto-generated method stub
        if (isAutoStart) {
            mAutoStartBar.setMax(mAutoStartWaitTime);
            mAutoStartDialog.show();
            new Thread(mAutoStartRunnable).start();
        }
    }

    protected boolean passFileExit() {
        // TODO Auto-generated method stub
        WisResultFile mResultFile = new WisResultFile();
        File mPassResultFile = new File(CommonParams.PQAA_RESULT_PASS_FILE_PATH);
        if (mPassResultFile.exists()) {
            String mLastResultContent = mResultFile.readFromResultFile(CommonParams.PQAA_RESULT_PASS_FILE_PATH);
            if (mLastResultContent != null) {
                mRestartDialog.setMessage(
                        String.format(mToolsKit.getStringResource(R.string.restart_msg), mCurrentList.size(), mLastResultContent),
                        getResources().getDimension(R.dimen.alert_dialog_message_size));
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * get the version of test item app
     */
    protected void getTestItemVersion() {
        // TODO Auto-generated method stub
        String mAppVersion;
        for (TestItem item : mCurrentList) {
            mAppVersion = mToolsKit.getAppVersion(item.getTestItemPackageName());
            if (mAppVersion != null && item.getTestItemName().equals("NFC")) {
                String mNFCEnablerVersion = mToolsKit.getAppVersion("com.wistron.nfc.enabler");
                if (mNFCEnablerVersion == null) {
                    mAppVersion = null;
                }
            }

            if (mAppVersion == null) {
                item.setTestItemVersion(mToolsKit.getStringResource(R.string.pqaa_package_not_found));
                item.setChecked(false);
                item.setInstalled(false);
            } else {
                item.setTestItemVersion(mAppVersion);
                item.setChecked(true);
                item.setInstalled(true);
            }
        }
        mAboutDialog.setCurTestItems(mCurrentList);
    }

    private final Handler mWaitHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_DISPLAY_TEST_LIST:
                    getDisplayList();
                    // getTestItemVersion();
                    mAdapter.notifyDataSetChanged();
                    mWaitDialog.dismiss();
                    if (isConfirmRestart && passFileExit()) {
                        mRestartDialog.showDialog();
                    } else {
                        detectIfAutoStart();
                    }
                    break;
                case MSG_WAIT_AUTO_TEST:
                    mAutoStartBar.setProgress(mAutoStartCurProgress);
                    break;
                case MSG_START_AUTO_TEST:
                    if (mAutoStartDialog != null && mAutoStartDialog.isShowing()) {
                        mAutoStartDialog.dismiss();
                    }
                    mStartTestButton.performClick();
                    break;
                default:
                    break;
            }
        }

    };

    private Runnable mAutoStartRunnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            while (mAutoStartCurProgress < mAutoStartWaitTime) {
                mWaitHandler.sendEmptyMessage(MSG_WAIT_AUTO_TEST);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                mAutoStartCurProgress++;
            }
            mWaitHandler.sendEmptyMessage(MSG_START_AUTO_TEST);
        }
    };
    private final Runnable mWaitRunnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            if (configFileExist()) {
                getConfigSetting(false);
            } else {
                getTestItemsArray(false);
            }

            // if (automaticFileExist()) {
            // getConfigSetting(true);
            // } else {
            // getTestItemsArray(true);
            // }
            mWaitHandler.sendEmptyMessage(MSG_DISPLAY_TEST_LIST);
        }
    };

    private void insertData(ContentValues values) {
        mResolver.insert(RunInfo.CONTENT_URI_TEST, values);
    }

    private Cursor queryData(String selection, String[] selectionArgs, String sortOrder) {
        return mResolver.query(RunInfo.CONTENT_URI_TEST, null, selection, selectionArgs, sortOrder);
    }

    private void deleteData(String where, String[] selectionArgs) {
        mResolver.delete(RunInfo.CONTENT_URI_TEST, where, selectionArgs);
    }

    private void updateData(ContentValues values, String where, String[] selectionArgs) {
        mResolver.update(RunInfo.CONTENT_URI_TEST, values, where, selectionArgs);
    }

    private void insertAutomaticData(ContentValues values) {
        mResolver.insert(RunInfo.CONTENT_URI_TEST_AUTOMATIC, values);
    }

    private Cursor queryAutomaticData(String selection, String[] selectionArgs, String sortOrder) {
        return mResolver.query(RunInfo.CONTENT_URI_TEST_AUTOMATIC, null, selection, selectionArgs, sortOrder);
    }

    private void deleteAutomaticData(String where, String[] selectionArgs) {
        mResolver.delete(RunInfo.CONTENT_URI_TEST_AUTOMATIC, where, selectionArgs);
    }

    private void updateAutomaticData(ContentValues values, String where, String[] selectionArgs) {
        mResolver.update(RunInfo.CONTENT_URI_TEST_AUTOMATIC, values, where, selectionArgs);
    }

    protected boolean automaticFileExist() {
        // TODO Auto-generated method stub
        File mFile = null;
        // if (isPCBAStage) {
        // mFile = new File(PCBA_FILE_PATH);
        // } else {
        // mFile = new File(FA_FILE_PATH);
        // }
        mFile = new File(CommonParams.AUTOMATIC_FILE_PATH);
        return mFile.exists();
    }

    protected boolean configFileExist() {
        // TODO Auto-generated method stub
        File mFile = null;
        // if (isPCBAStage) {
        // mFile = new File(PCBA_FILE_PATH);
        // } else {
        // mFile = new File(FA_FILE_PATH);
        // }
        mFile = new File(CommonParams.CONFIG_FILE_PATH);
        return mFile.exists();
    }

    protected void getConfigSetting(boolean isAutomatic) {
        // TODO Auto-generated method stub
        File mConfigFile = null;
        if (isAutomatic) {
            deleteAutomaticData(null, null);
            Cursor cursor = queryAutomaticData(null, null, null);
            if (cursor != null) {
                int mDataCount = cursor.getCount();
                System.out.println("**************--->" + mDataCount);
            }
            cursor.close();
            mConfigFile = new File(CommonParams.AUTOMATIC_FILE_PATH);
        } else {
            deleteData(null, null);
            Cursor cursor = queryData(null, null, null);
            if (cursor != null) {
                int mDataCount = cursor.getCount();
                System.out.println("**************--->" + mDataCount);
            }
            cursor.close();
            mConfigFile = new File(CommonParams.CONFIG_FILE_PATH);
        }

        values.clear();
        // if (isPCBAStage) {
        // mConfigFile = new File(PCBA_FILE_PATH);
        // } else {
        // mConfigFile = new File(FA_FILE_PATH);
        // }
        if (!mConfigFile.exists()) {
            return;
        }

        int index = INDEX_ITEM;
        ArrayList<Map<String, String>> mParametersList = mToolsKit.getGroupParameters(mConfigFile.getAbsolutePath());
        if (mParametersList != null && mParametersList.size() > 0) {
            for (Map<String, String> mParameters : mParametersList) {
                values.clear();
                index = INDEX_ITEM;
                for (String key : mParameters.keySet()) {
                    values.put(mDatabaseColumns[index], mParameters.get(key));
                    index++;
                }
                if (mParameters.size() > 0) {
                    if (isAutomatic) {
                        insertAutomaticData(values);
                    } else {
                        insertData(values);
                    }
                }
            }
        }
    }

    private void getTestItemsArray(boolean isAutomatic) {
        // TODO Auto-generated method stub
        int mDataCount = 0;
        if (isAutomatic) {
            Cursor cursor = queryAutomaticData(null, null, null);
            mDataCount = cursor.getCount();
            cursor.close();
        } else {
            Cursor cursor = queryData(null, null, null);
            mDataCount = cursor.getCount();
            cursor.close();
        }
        System.out.println("**************--->***" + mDataCount);
        if (mDataCount > 0) {
            return;
        }

        ArrayList<String> mTempList = new ArrayList<String>();
        XMLParse mParse = new XMLParse(this);
        LinkedHashMap<String, String> mItemsList = mParse.getItemListFromXML(isAutomatic);
        Iterator<Entry<String, String>> iterator = mItemsList.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, String> entry = iterator.next();
            if (!itemExistInDatabase(entry.getKey(), isAutomatic)) {
                values.clear();
                values.put(RunInfo.ITEM, entry.getKey());
                int index = INDEX_ARG1;
                StringTokenizer mTokenizer = new StringTokenizer(entry.getValue(), ":");
                while (mTokenizer.hasMoreElements()) {
                    String mCurValue = (String) mTokenizer.nextElement();
                    if (!mCurValue.equals("null")) {
                        values.put(mDatabaseColumns[index], mCurValue);
                    }
                    index++;
                }
                if (isAutomatic) {
                    insertAutomaticData(values);
                } else {
                    insertData(values);
                }
                mDataCount++;
            }
            mTempList.add(entry.getKey());
        }
        deleteUnlessData(mTempList, isAutomatic);
        // deleteData(RunInfo.ITEM, new String[] { " NOT EXISTS ( "
        // + mTempList.toString() +" )"});
    }

    private void deleteUnlessData(ArrayList<String> mTempList, boolean isAutomatic) {
        // TODO Auto-generated method stub
        Cursor cursor = null;
        if (isAutomatic) {
            cursor = queryAutomaticData(null, null, null);
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String mTestItem = cursor.getString(cursor.getColumnIndexOrThrow(RunInfo.ITEM));
                    if (!mTempList.contains(mTestItem)) {
                        deleteAutomaticData(RunInfo.ITEM + "=?", new String[]{mTestItem});
                    }
                }
            }
        } else {
            cursor = queryData(null, null, null);
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String mTestItem = cursor.getString(cursor.getColumnIndexOrThrow(RunInfo.ITEM));
                    if (!mTempList.contains(mTestItem)) {
                        deleteData(RunInfo.ITEM + "=?", new String[]{mTestItem});
                    }
                }
            }
        }
        cursor.close();
    }

    private boolean itemExistInDatabase(String mItem, boolean isAutomatic) {
        boolean isExist = false;
        if (isAutomatic) {
            Cursor mCursor = queryAutomaticData(RunInfo.ITEM + "=?", new String[]{mItem}, null);
            isExist = mCursor.getCount() > 0 ? true : false;
            mCursor.close();
        } else {
            Cursor mCursor = queryData(RunInfo.ITEM + "=?", new String[]{mItem}, null);
            isExist = mCursor.getCount() > 0 ? true : false;
            mCursor.close();
        }
        return isExist;
    }

    private void initialize() {
        // TODO Auto-generated method stub
        values = new ContentValues();
        mDatabaseColumns = new String[]{RunInfo.ITEM, RunInfo.ARG1, RunInfo.ARG2, RunInfo.ARG3, RunInfo.ARG4, RunInfo.ARG5, RunInfo.ARG6,
                RunInfo.ARG7, RunInfo.ARG8, RunInfo.ARG9};

        mWaitDialog = new AlertDialog.Builder(this).setMessage(mToolsKit.getStringResource(R.string.wait)).setCancelable(false).create();
        mWaitDialog.setCancelable(false);
        mWaitDialog.setCanceledOnTouchOutside(false);

        mAboutDialog = new AboutWindow(this);

        try {
            mLogHandler = new WisLog(CommonParams.PQAA_LOG_FILE_PATH);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(this,
                    String.format(mToolsKit.getStringResource(R.string.pqaa_log_error_not_create), CommonParams.PQAA_LOG_FILE_PATH),
                    Toast.LENGTH_SHORT).show();
        }

        try {
            mWisLog = new WisLog(CommonParams.PQAA_GENERIC_LOG_FILE_PATH);
            mWisLog.deleteLogFile();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            Toast.makeText(this,
                    String.format(mToolsKit.getStringResource(R.string.pqaa_log_error_not_create), CommonParams.PQAA_LOG_FILE_PATH),
                    Toast.LENGTH_SHORT).show();
        }

        mCurrentList = new ArrayList<TestItem>();
        mAdapter = new TestAdapter(this, mCurrentList, mHandler);

        mTestItemListView = (ListView) findViewById(R.id.test_list);
        mTestStyleSpinner = (Spinner) findViewById(R.id.test_spinner);
        mStartTestButton = (Button) findViewById(R.id.test_start);
        mSelectAllBox = (CheckBox) findViewById(R.id.test_selectall);
        mSettingButton = (Button) findViewById(R.id.test_listSetting);
        mMoveUpButton = (ImageButton) findViewById(R.id.test_up);
        mMoveDownButton = (ImageButton) findViewById(R.id.test_down);
        mAboutButton = (Button) findViewById(R.id.item_about);

        mStartTestButton.setOnClickListener(this);
        mSettingButton.setOnClickListener(this);
        mMoveUpButton.setOnClickListener(this);
        mMoveDownButton.setOnClickListener(this);
        mAboutButton.setOnClickListener(this);

        mTestItemListView.setAdapter(mAdapter);

        if (isHideCmdLine) {
            findViewById(R.id.title_cmd).setVisibility(View.GONE);
        }

        switch (mItemDisplayMode) {
            case ITEM_DISPLAY_MODE_GONE:
                mSelectAllBox.setVisibility(View.INVISIBLE);
                mSelectAllBox.setEnabled(false);
                break;
            case ITEM_DISPLAY_MODE_DISABLE:
                mSelectAllBox.setEnabled(false);
                break;
            case ITEM_DISPLAY_MODE_ENABLE:
                break;
            default:
                break;
        }

        mSelectAllBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                for (int i = 0; i < mCurrentList.size(); i++) {
                    if (mCurrentList.get(i).isInstalled()) {
                        mCurrentList.get(i).setChecked(isChecked);
                    }
                }
                mAdapter.notifyDataSetChanged();
            }
        });

        mTestItemListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub
                mCurrentSelectedItemIndex = arg2;
            }
        });
        mTestItemListView.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub
                mCurrentSelectedItemIndex = arg2;
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });
        mTestStyle = getResources().getStringArray(R.array.Test_Style);
        ArrayAdapter<String> mStyleAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mTestStyle);
        mStyleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTestStyleSpinner.setAdapter(mStyleAdapter);

        initialDialog();
    }

    private void initialDialog() {
        // TODO Auto-generated method stub
        AlertDialog.Builder mAutoStartBuilder = new AlertDialog.Builder(this);
        mAutoStartBuilder.setTitle(mToolsKit.getStringResource(R.string.autoStart_Title));
        LayoutInflater mInflater = LayoutInflater.from(this);
        mAutoStartBar = (ProgressBar) mInflater.inflate(R.layout.auto_start_progress, null);
        mAutoStartBuilder.setView(mAutoStartBar);
        mAutoStartDialog = mAutoStartBuilder.create();
        mAutoStartDialog.setCancelable(false);
        mAutoStartDialog.setCanceledOnTouchOutside(false);

        mRestartDialog = new WisAlertDialog(this)
                .setTitle(mToolsKit.getStringResource(R.string.restart_title))
                .setPositiveButton(mToolsKit.getStringResource(R.string.restart_ok),
                        getResources().getDimension(R.dimen.alert_dialog_button_size), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                detectIfAutoStart();
                            }
                        })
                .setNegativeButton(mToolsKit.getStringResource(R.string.restart_cancel),
                        getResources().getDimension(R.dimen.alert_dialog_button_size), new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                finish();
                            }
                        }).createDialog();
    }

    private void getDisplayList() {
        mCurrentList.clear();
        Cursor cursor = queryData(RunInfo.REMANENT + "=0", null, RunInfo.INDEX);
        String cmdline = "";
        int index;
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                cmdline = "";
                String mItemTitle = cursor.getString(cursor.getColumnIndexOrThrow(RunInfo.ITEM));
                index = INDEX_ARG1;
                for (; index < mDatabaseColumns.length; index++) {
                    String arg = cursor.getString(cursor.getColumnIndexOrThrow(mDatabaseColumns[index]));
                    if (!arg.equals("")) {
                        cmdline += (mDatabaseColumns[index] + ":" + arg + "  ");
                    } else {
                        break;
                    }
                }
                mTestItem = new TestItem(this, mItemTitle);
                mTestItem.setTestItemCmdLine(cmdline);
                mCurrentList.add(mTestItem);
            }
        }
        cursor.close();
        setTestStage();
    }

    private void setTestStage() {
        // TODO Auto-generated method stub
        String mItem;
        for (int i = 0; i < mCurrentList.size(); i++) {
            mItem = mCurrentList.get(i).getTestItemName();
            if (mItem.equals("3G")) {
                mCurrentList.get(i).setPCBATestStage(is3GPCBA);
            } else if (mItem.equals("TouchPanel")) {
                mCurrentList.get(i).setPCBATestStage(isTouchPanelPCBA);
            } else if (mItem.equals("GPS")) {
                mCurrentList.get(i).setPCBATestStage(isGPSPCBA);
            } else if (mItem.equals("Wifi")) {
                mCurrentList.get(i).setPCBATestStage(isWifiPCBA);
            } else if (mItem.equals("NFC")) {
                mCurrentList.get(i).setPCBATestStage(isNFCPCBA);
            } else if (mItem.equals("BlueTooth")) {
                mCurrentList.get(i).setPCBATestStage(isBlueToothPCBA);
            } else if (mItem.equals("GSensor") || mItem.equals("LightSensor") || mItem.equals("ProximitySensor")
                    || mItem.equals("GyroSensor") || mItem.equals("ECompass")) {
                mCurrentList.get(i).setPCBATestStage(isSensorPCBA);
            }
        }
    }

    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            mCurrentSelectedItemIndex = msg.what;
        }

    };

    private StringBuffer mLogInformation = new StringBuffer();

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v == mStartTestButton) {
            v.setEnabled(false);
            if (!isStartTest) {
                isStartTest = true;
                deleteLastLog();

                if (mLogLinesList.size() > 0) {
                    mLogLinesList.clear();
                }
                mWisLog.deleteLogFile();
                getLogInfoConfig();

                // start test format date
                SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String date = sDateFormat.format(new java.util.Date());
                String[] content = date.split(" ");
                String[] data = content[0].split("-");
                date = data[1] + ":" + data[2] + ":" + data[0].substring(data[0].length() - 2) + " " + content[1];
                writeLog(String.format(getResources().getString(R.string.log_test_start_time), date));
            }

            Intent intent = new Intent();
            int i;
            for (i = mCurrentTestItemIndex; i < mCurrentList.size(); i++) {
                if (mCurrentList.get(i).isChecked()) {
                    mCurrentTestItemIndex = i;
                    try {
                        mLogHandler.write(String.format(getString(R.string.pqaa_log_item_start), mCurrentList.get(i).getTestItemName()),
                                true);
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    intent.setClassName(this, mCurrentList.get(i).getTestItemActivityName());
                    if (mTestStyleSpinner.getSelectedItemPosition() == 0) {
                        mCurrentTestStyle = WisCommonConst.TESTSTYLE_COMPONENT;
                    } else if (mTestStyleSpinner.getSelectedItemPosition() == 1) {
                        mCurrentTestStyle = WisCommonConst.TESTSTYLE_SAVED_CONFIG;
                    }
                    intent.putExtra(WisCommonConst.EXTRA_TESTSTYLE, mCurrentTestStyle);
                    intent.putExtra(WisCommonConst.EXTRA_STAGE, mCurrentList.get(i).isPCBATestStage());
                    intent.putExtra(WisCommonConst.EXTRA_ITEM, mCurrentList.get(i).getTestItemName());
                    intent.putExtra(WisCommonConst.EXTRA_LANGUAGE, mToolsKit.getCurrentLanguage());
                    intent.putExtra(WisCommonConst.EXTRA_AUTHORITIES, DataContentProvider.AUTHORITY);
                    startActivityForResult(intent, REQUEST_CODE);
                    break;
                }
            }
            if (i >= mCurrentList.size()) {
                mCurrentTestItemIndex = 0;
                v.setEnabled(true);
                boolean result = writeResultFile();
                if (!isNGContinue) {
                    showResultInterface(true);
                }

                // test end time format
                SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String date = sDateFormat.format(new java.util.Date());
                String[] content = date.split(" ");
                String[] data = content[0].split("-");
                date = data[1] + ":" + data[2] + ":" + data[0].substring(data[0].length() - 2) + " " + content[1];
                writeLog(String.format(getResources().getString(R.string.log_test_end_time), date));
                writeLog(String.format(getResources().getString(R.string.log_result), result + ""));

                String BuildNumber = Build.DISPLAY;
                writeLog(String.format(getResources().getString(R.string.log_bios_version), BuildNumber));

                String ModelNumber = Build.MODEL;
                writeLog(String.format(getResources().getString(R.string.log_product_name), ModelNumber));

                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                String imei = telephonyManager.getDeviceId();
                writeLog(String.format(getResources().getString(R.string.log_imei), imei));
                writeLog("[Test items]");

                if (mLogLinesList.size() > 0) {
                    for (LogLine mLine : mLogLinesList) {
//						writeLog(String.format(getResources().getString(R.string.log_line), mLine.item," ", mLine.result));
                        writeLog(String.format(getResources().getString(R.string.log_line_ini), mLine.item, " ", " ", mLine.result ? "PASS" : "FAIL"));
                    }
                }

            }
        } else if (v == mSettingButton) {
            DisplayConfiguration mConfiguration = new DisplayConfiguration(this);
            mConfiguration.show();
        } else if (v == mMoveUpButton) {
            if (mCurrentSelectedItemIndex > 0) {
                TestItem mTempItem = mCurrentList.get(mCurrentSelectedItemIndex);
                mCurrentList.set(mCurrentSelectedItemIndex, mCurrentList.get(mCurrentSelectedItemIndex - 1));
                mCurrentList.set(mCurrentSelectedItemIndex - 1, mTempItem);

                updateDatabaseList();

                mCurrentSelectedItemIndex--;
            }
            mTestItemListView.requestFocusFromTouch();
            mTestItemListView.setSelection(mCurrentSelectedItemIndex);
        } else if (v == mMoveDownButton) {
            if (mCurrentSelectedItemIndex < mCurrentList.size() - 1) {
                TestItem mTempItem = mCurrentList.get(mCurrentSelectedItemIndex);
                mCurrentList.set(mCurrentSelectedItemIndex, mCurrentList.get(mCurrentSelectedItemIndex + 1));
                mCurrentList.set(mCurrentSelectedItemIndex + 1, mTempItem);

                updateDatabaseList();
                mCurrentSelectedItemIndex++;
            }
            mTestItemListView.requestFocusFromTouch();
            mTestItemListView.setSelection(mCurrentSelectedItemIndex);
        } else if (v == mAboutButton) {
            mAboutDialog.popupUpAboutWindow(v);
        }
    }

    private void updateDatabaseList() {
        // TODO Auto-generated method stub
        ArrayList<String> mTemp = new ArrayList<String>();
        for (TestItem mItem : mCurrentList) {
            mTemp.add(mItem.getTestItemName());
        }
        Cursor cursor = queryData(null, null, null);
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String mTestItem = cursor.getString(cursor.getColumnIndexOrThrow(RunInfo.ITEM));
                if (mTemp.contains(mTestItem)) {
                    values.clear();
                    int index = mTemp.indexOf(mTestItem);
                    values.put(RunInfo.INDEX, index);
                    updateData(values, RunInfo.ITEM + "=?", new String[]{mTestItem});
                }
            }
            getDisplayList();
            mAdapter.notifyDataSetChanged();
        }
        cursor.close();
    }

    public class DisplayConfiguration {
        private final Context context;
        private View mConfigurationView;
        private ListView lvRemanent;
        private ListView lvCurrent;
        private ImageButton ibAdd;
        private ImageButton ibRemove;
        private ImageButton ibUp;
        private ImageButton ibDown;
        private ArrayAdapter<String> mRemanentAdapter;
        private ArrayAdapter<String> mCurrentAdapter;

        private final List<String> mRemanentTitleList, mCurrentTitleList;
        private int mRemanentIndex = -1;
        private int mCurrentIndex = -1;

        private DisplayConfiguration(Context context) {
            // TODO Auto-generated method stub
            this.context = context;
            getLayoutParameters();

            mRemanentTitleList = new ArrayList<String>();
            getRemanentList();
            setRemanentAdapter();

            mCurrentTitleList = new ArrayList<String>();
            getCurrentList();
            setCurrentAdapter();

            lvRemanent.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    // TODO Auto-generated method stub
                    mRemanentIndex = arg2;
                    lvRemanent.requestFocusFromTouch();
                    lvRemanent.setSelection(mRemanentIndex);
                }
            });
            lvRemanent.setOnItemSelectedListener(new OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    // TODO Auto-generated method stub
                    mRemanentIndex = arg2;
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                    // TODO Auto-generated method stub

                }
            });
            lvCurrent.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    // TODO Auto-generated method stub
                    mCurrentIndex = arg2;
                    lvCurrent.requestFocusFromTouch();
                    lvCurrent.setSelection(mCurrentIndex);
                }
            });
            lvCurrent.setOnItemSelectedListener(new OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    // TODO Auto-generated method stub
                    mCurrentIndex = arg2;
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                    // TODO Auto-generated method stub

                }
            });
            ibAdd.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    if (mRemanentIndex >= 0 && mRemanentIndex < mRemanentTitleList.size()) {
                        String mCurrentItem = lvRemanent.getItemAtPosition(mRemanentIndex).toString();

                        addToCurrentTitleList(mCurrentItem);
                        removeFromRemanentList(mRemanentIndex);

                        lvCurrent.requestFocusFromTouch();
                        lvCurrent.setSelection(mCurrentTitleList.size() - 1);
                    }
                }

                private void removeFromRemanentList(int mRemanentIndex) {
                    // TODO Auto-generated method stub
                    mRemanentTitleList.remove(mRemanentIndex);
                    mRemanentAdapter.notifyDataSetChanged();
                }

                private void addToCurrentTitleList(String mCurrentItem) {
                    // TODO Auto-generated method stub
                    mCurrentTitleList.add(mCurrentItem);
                    mCurrentAdapter.notifyDataSetChanged();
                }
            });
            ibRemove.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    if (mCurrentIndex >= 0 && mCurrentIndex < mCurrentTitleList.size()) {
                        String mCurrentItem = lvCurrent.getItemAtPosition(mCurrentIndex).toString();

                        addToRemanentList(mCurrentItem);
                        removeFromCurrentList(mCurrentIndex);

                        lvRemanent.requestFocusFromTouch();
                        lvRemanent.setSelection(mRemanentTitleList.size() - 1);
                    }
                }

                private void removeFromCurrentList(int mCurrentIndex) {
                    // TODO Auto-generated method stub
                    mCurrentTitleList.remove(mCurrentIndex);
                    mCurrentAdapter.notifyDataSetChanged();
                }

                private void addToRemanentList(String mCurrentItem) {
                    // TODO Auto-generated method stub
                    mRemanentTitleList.add(mCurrentItem);
                    mRemanentAdapter.notifyDataSetChanged();
                }
            });
            ibUp.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    if (mCurrentIndex > 0) {
                        String mTempItem = mCurrentTitleList.get(mCurrentIndex);
                        mCurrentTitleList.set(mCurrentIndex, mCurrentTitleList.get(mCurrentIndex - 1));
                        mCurrentTitleList.set(mCurrentIndex - 1, mTempItem);
                        mCurrentAdapter.notifyDataSetChanged();
                        mCurrentIndex--;
                    }
                    lvCurrent.requestFocusFromTouch();
                    lvCurrent.setSelection(mCurrentIndex);
                }
            });
            ibDown.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    if (mCurrentIndex < mCurrentTitleList.size() - 1) {
                        String mTempItem = mCurrentTitleList.get(mCurrentIndex);
                        mCurrentTitleList.set(mCurrentIndex, mCurrentTitleList.get(mCurrentIndex + 1));
                        mCurrentTitleList.set(mCurrentIndex + 1, mTempItem);
                        mCurrentAdapter.notifyDataSetChanged();
                        mCurrentIndex++;
                    }
                    lvCurrent.requestFocusFromTouch();
                    lvCurrent.setSelection(mCurrentIndex);
                }
            });
        }

        private void getLayoutParameters() {
            // TODO Auto-generated method stub
            LayoutInflater mInflater = LayoutInflater.from(context);
            mConfigurationView = mInflater.inflate(R.layout.pqaa_setting, null);
            lvRemanent = (ListView) mConfigurationView.findViewById(R.id.setting_remanentItem);
            lvCurrent = (ListView) mConfigurationView.findViewById(R.id.setting_currentItem);
            ibAdd = (ImageButton) mConfigurationView.findViewById(R.id.setting_add);
            ibRemove = (ImageButton) mConfigurationView.findViewById(R.id.setting_remove);
            ibUp = (ImageButton) mConfigurationView.findViewById(R.id.setting_toUp);
            ibDown = (ImageButton) mConfigurationView.findViewById(R.id.setting_toDown);
        }

        private void getCurrentList() {
            // TODO Auto-generated method stub
            mCurrentTitleList.clear();
            for (TestItem mItem : mCurrentList) {
                mCurrentTitleList.add(mItem.getTestItemName());
            }
        }

        private void getRemanentList() {
            // TODO Auto-generated method stub
            mRemanentTitleList.clear();
            Cursor cursor = queryData(RunInfo.REMANENT + "=1", null, RunInfo.INDEX);
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String mItemTitle = cursor.getString(cursor.getColumnIndexOrThrow(RunInfo.ITEM));
                    mRemanentTitleList.add(mItemTitle);
                }
            }
            cursor.close();
        }

        public void show() {
            // TODO Auto-generated method stub
            new AlertDialog.Builder(context).setTitle("Configuration").setView(mConfigurationView)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            updateDisplayList();
                        }

                        private void updateDisplayList() {
                            // TODO Auto-generated method stub
                            Cursor cursor = queryData(null, null, null);
                            if (cursor.getCount() > 0) {
                                while (cursor.moveToNext()) {
                                    String mTestItem = cursor.getString(cursor.getColumnIndexOrThrow(RunInfo.ITEM));
                                    if (mRemanentTitleList.contains(mTestItem)) {
                                        values.clear();
                                        values.put(RunInfo.REMANENT, 1);
                                        values.put(RunInfo.INDEX, 0);
                                        updateData(values, RunInfo.ITEM + "=?", new String[]{mTestItem});
                                    } else if (mCurrentTitleList.contains(mTestItem)) {
                                        values.clear();
                                        values.put(RunInfo.REMANENT, 0);
                                        int index = mCurrentTitleList.indexOf(mTestItem);
                                        values.put(RunInfo.INDEX, index);
                                        updateData(values, RunInfo.ITEM + "=?", new String[]{mTestItem});
                                    }
                                }
                                getDisplayList();
                                mAdapter.notifyDataSetChanged();
                                mCurrentTestItemIndex = 0;
                                mCurrentSelectedItemIndex = 0;
                            }
                            cursor.close();
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub

                }
            }).show();
        }

        private void setRemanentAdapter() {
            // TODO Auto-generated method stub
            mRemanentAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, mRemanentTitleList);
            lvRemanent.setAdapter(mRemanentAdapter);
        }

        private void setCurrentAdapter() {
            // TODO Auto-generated method stub
            mCurrentAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, mCurrentTitleList);
            lvCurrent.setAdapter(mCurrentAdapter);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("Tag", "onActivityResult---");
        boolean mReturnResult = data.getBooleanExtra(WisCommonConst.EXTRA_PASS, false);
        if (requestCode == REQUEST_CODE) {
            if (mReturnResult) {
                mCurrentList.get(mCurrentTestItemIndex).setTestItemResult(TestItem.RESULT_PASS);
            } else {
                mCurrentList.get(mCurrentTestItemIndex).setTestItemResult(TestItem.RESULT_FAIL);
            }
            mAdapter.notifyDataSetChanged();
            mTestItemListView.setSelection(mCurrentTestItemIndex);

            LogLine mLine = new LogLine();
            mLine.item = mCurrentList.get(mCurrentTestItemIndex).getTestItemName();
            mLine.result = mReturnResult;
            mLogLinesList.add(mLine);

            try {
                mLogHandler.write(String.format(getString(R.string.pqaa_log_item_result), mCurrentList.get(mCurrentTestItemIndex)
                        .getTestItemName(), mReturnResult), true);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (isNGContinue) {
                if (mCurrentTestStyle.equals(mTestStyle[1])) {
                    if (!mReturnResult && isNGShowWarning) {
                        new WisAlertDialog(this)
                                .setMessage(
                                        String.format(
                                                mToolsKit.getStringResource(R.string.pqaa_back_dialog_msg),
                                                mToolsKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? mCurrentList.get(
                                                        mCurrentTestItemIndex).getTestItemName() : mCurrentList.get(mCurrentTestItemIndex)
                                                        .getTestItemCNName()),
                                        getResources().getDimension(R.dimen.alert_dialog_message_size))
                                .setPositiveButton(mToolsKit.getStringResource(R.string.pqaa_back_dialog_continue),
                                        getResources().getDimension(R.dimen.alert_dialog_button_size),
                                        new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // TODO Auto-generated method
                                                // stub
                                                mCurrentTestItemIndex++;
                                                mStartTestButton.performClick();
                                            }
                                        })
                                .setNegativeButton(mToolsKit.getStringResource(R.string.pqaa_back_dialog_retry),
                                        getResources().getDimension(R.dimen.alert_dialog_button_size),
                                        new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // TODO Auto-generated method
                                                // stub
                                                mStartTestButton.performClick();
                                            }
                                        }).setCancelable(false).showDialog();
                    } else {
                        mCurrentTestItemIndex++;
                        mStartTestButton.performClick();
                    }
                } else {
                    mCurrentTestItemIndex = 0;
                    mStartTestButton.setEnabled(true);
                }
            } else {
                if (!mReturnResult) {
                    writeResultFile();
                    showResultInterface(false);
                } else {
                    mCurrentTestItemIndex++;
                    mStartTestButton.performClick();
                }
            }
        }
    }

    private void showResultInterface(boolean result) {
        // TODO Auto-generated method stub
        setContentView(R.layout.result);
        LinearLayout mBackLayout = (LinearLayout) findViewById(R.id.result_layout);
        TextView mResultView = (TextView) findViewById(R.id.result_result);
        Button mBackButton = (Button) findViewById(R.id.result_back);
        mBackButton.setText(mToolsKit.getStringResource(R.string.ok));
        if (result) {
            mResultView.setText(mToolsKit.getStringResource(R.string.pqaa_result_pass));
            mBackLayout.setBackgroundColor(Color.GREEN);
        } else {
            mResultView.setText(mToolsKit.getCurrentLanguage() == WisCommonConst.LANGUAGE_ENGLISH ? mCurrentList.get(mCurrentTestItemIndex)
                    .getTestItemName() : mCurrentList.get(mCurrentTestItemIndex).getTestItemCNName()
                    + mToolsKit.getStringResource(R.string.pqaa_result_fail));
            mBackLayout.setBackgroundColor(Color.RED);
        }
        mBackButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                finish();
            }
        });
    }

    private boolean writeResultFile() {
        // TODO Auto-generated method stub
        boolean result = false;
        int mTotalCount = 0, mPassCount = 0, mFailCount = 0;
        WisResultFile mResultFile = new WisResultFile();
        isStartTest = false;
        for (int i = 0; i < mCurrentList.size(); i++) {
            if (mCurrentList.get(i).isChecked()) {
                mTotalCount++;
                if (mCurrentList.get(i).getTestItemResult() == TestItem.RESULT_PASS) {
                    mPassCount++;
                } else if (mCurrentList.get(i).getTestItemResult() == TestItem.RESULT_FAIL) {
                    mFailCount++;
                }
            }
        }
        if (mFailCount <= 0) {
            result = true;
            mResultFile.writeToResultFile(CommonParams.PQAA_RESULT_PASS_FILE_PATH, String.valueOf(mPassCount));
        } else {
            mResultFile.writeToResultFile(CommonParams.PQAA_RESULT_FALI_FILE_PATH,
                    String.format(getString(R.string.pqaa_fail_result), mTotalCount, mPassCount, mFailCount));
        }
        return result;
    }

    private ArrayList<LogLine> mLogLinesList = new ArrayList<TestItemsList.LogLine>();

    public class LogLine {
        String item;
        Boolean result;
    }
}
