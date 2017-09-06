package com.wistron.generic.nfc;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.wistron.generic.pqaa.R;
import com.wistron.pqaa_common.jar.global.WisCommonConst;
import com.wistron.pqaa_common.jar.global.WisNFC;
import com.wistron.pqaa_common.jar.global.WisParseValue;
import com.wistron.pqaa_common.jar.global.WisToolKit;

import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class NFC_Old extends Activity {
    private static final String TAG = "Wistron_NFC";

    private static final int TEST_MODE_READ = 1;
    private static final int TEST_MODE_WRITE_AND_READ = 2;

    private final int MSG_TIMEREFRESH = 0;
    private final int MSG_TIMEOUT = 1;

    private PendingIntent mPendingIntent;
    private IntentFilter[] mWriteFilters;
    private TextView tvNFCSupport;
    private TextView tvRemainTime, tvTotalTime;
    private TextView tvNFCReadContent, tvNFCWriteContent;
    private boolean isPCBStage = false;

    private String mReadContent, mWriteContent;
    private boolean isWriteFail;

    private MediaPlayer mPlayer;

    private Timer mTimer = new Timer();
    private TimerTask mTask;
    private int mTimes = 0;
    private int TIMEOUT = 20; // seconds

    private boolean isWriteMode;

    private int mTestMode = 2;

    // common tool kit
    private WisToolKit mToolKit;
    private WisNFC mNfcHandler;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.nfc);

        mToolKit = new WisToolKit(this);

        findView();
        getTestArguments();
        setViewByLanguage();
        initial();
    }

    private void findView() {
        // TODO Auto-generated method stub
        tvNFCSupport = (TextView) findViewById(R.id.nfc_support);
        tvRemainTime = (TextView) findViewById(R.id.remaintime);
        tvTotalTime = (TextView) findViewById(R.id.totaltime);
        tvNFCWriteContent = (TextView) findViewById(R.id.nfc_write_content);
        tvNFCReadContent = (TextView) findViewById(R.id.nfc_read_content);
    }

    private void initial() {
        // TODO Auto-generated method stub
        if (isPCBStage) {
            findViewById(R.id.nfc_fa_section).setVisibility(View.GONE);
        } else {
            findViewById(R.id.nfc_pcba_section).setVisibility(View.GONE);
            if ((mTestMode & TEST_MODE_WRITE_AND_READ) != 0) {
                isWriteMode = true;
                findViewById(R.id.nfc_read_section).setVisibility(View.GONE);
            } else {
                isWriteMode = false;
                findViewById(R.id.nfc_write_section).setVisibility(View.GONE);
            }
        }

        mReadContent = "";
        mWriteContent = "";

        mNfcHandler = new WisNFC(this);
        if (mNfcHandler.isNFCAdapterExist() && mNfcHandler.isSupportsNFC()) {
            //mNfcHandler.setNFCEnabled(true);
            initializeTimerTask();
            if (!isPCBStage) {
                mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

                IntentFilter writeFilter = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
                mWriteFilters = new IntentFilter[]{writeFilter};
            }
        } else {
            Toast.makeText(this, mToolKit.getStringResource(R.string.nfc_not_support), Toast.LENGTH_SHORT).show();
            backResult(false);
        }
    }

    private void setViewByLanguage() {
        // TODO Auto-generated method stub
        TextView mItemTitle = (TextView) findViewById(R.id.item_title);
        mItemTitle.setText(mToolKit.getStringResource(R.string.nfc_test_title));
        tvTotalTime.setText(mToolKit.getStringResource(R.string.total_time) + mToolKit.formatCountDownTime(TIMEOUT, mTimes));
        tvRemainTime.setText(mToolKit.getStringResource(R.string.remain_time) + mToolKit.formatCountDownTime(TIMEOUT, mTimes));
        tvNFCSupport.setText(mToolKit.getStringResource(R.string.nfc_do_detect));
    }

    private void getTestArguments() {
        // TODO Auto-generated method stub
        String mTestStyle = mToolKit.getCurrentTestType();
        if (mTestStyle != null) {
            if (mTestStyle.equals(WisCommonConst.TESTSTYLE_SAVED_CONFIG)) {
                isPCBStage = mToolKit.isPCBATestStage();
                WisParseValue mParse = new WisParseValue(this, mToolKit.getCurrentItem(), mToolKit.getCurrentDatabaseAuthorities());
                int mTestTime = Integer.parseInt(mParse.getArg1());
                if (mTestTime > 0) {
                    TIMEOUT = mTestTime;
                }
                mTestMode = Integer.parseInt(mParse.getArg2());
            }
        }
    }

    private void initializeTimerTask() {
        // TODO Auto-generated method stub
        mTask = new TimerTask() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                mTimes++;
                if (mTimes >= TIMEOUT) {
                    handler.sendEmptyMessage(MSG_TIMEOUT);
                } else {
                    handler.sendEmptyMessage(MSG_TIMEREFRESH);
                }
            }
        };
        mTimer.schedule(mTask, 1000, 1000);
    }

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_TIMEREFRESH:
                    tvRemainTime.setText(mToolKit.getStringResource(R.string.remain_time) + mToolKit.formatCountDownTime(TIMEOUT, mTimes));
                    if (isPCBStage) {
                        if (mNfcHandler.isEnabled()) {
                            tvNFCSupport.setText(mToolKit.getStringResource(R.string.nfc_havedevice));
                            backResult(true);
                        } else {
                            //mNfcHandler.setNFCEnabled(true);
                        }
                    }
                    break;
                case MSG_TIMEOUT:
                    backResult(false);
                    break;
                default:
                    break;
            }
        }

    };

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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void enterTagWriteMode() {
        // 先停止接收任何的Intent，準備寫入資料至tag；
        disableNdefExchangeMode();
        // 啟動寫入Tag模式，監測是否有Tag進入
        enableTagWriteMode();
    }

    private void enterTagReadMode() {
        findViewById(R.id.nfc_read_section).setVisibility(View.VISIBLE);
        // 在取消模式下，先關閉監偵有Tag準備寫入的模式，再啟動等待資料交換的模式。
        // 停止寫入Tag模式，代表已有Tag進入
        disableTagWriteMode();
        // 啟動資料交換
        enableNdefExchangeMode();
    }

    /**
     * 啟動Ndef交換資料模式。
     */
    private void enableNdefExchangeMode() {
        isWriteMode = false;
        // 讓NfcAdatper啟動前景Push資料至Tag或應用程式。
        mNfcHandler.enableForegroundNdefPush(NFC_Old.this, getNoteAsNdef());

        // 讓NfcAdapter啟動能夠在前景模式下進行intent filter的dispatch。
        mNfcHandler.enableForegroundDispatch(this, mPendingIntent, null, null);
    }

    private void disableNdefExchangeMode() {
        mNfcHandler.disableForegroundNdefPush(this);
        mNfcHandler.disableForegroundDispatch(this);
    }

    /**
     * 啟動Tag寫入模式，註冊對應的Intent Filter來前景模式監聽是否有Tag進入的訊息。
     */
    private void enableTagWriteMode() {
        isWriteMode = true;
        mNfcHandler.enableForegroundDispatch(this, mPendingIntent, mWriteFilters, null);
    }

    /**
     * 停止Tag寫入模式，取消前景模式的監測。
     */
    private void disableTagWriteMode() {
        isWriteMode = false;
        mNfcHandler.disableForegroundDispatch(this);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Log.i(TAG, "onResume: " + getIntent().getAction());
        if (mNfcHandler.isSupportsNFC() && mNfcHandler.isNFCAdapterExist() && !isPCBStage) {
            if (isWriteMode) {
                enterTagWriteMode();
            } else {
                enterTagReadMode();
            }
        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if (mNfcHandler.isSupportsNFC() && mNfcHandler.isNFCAdapterExist() && !isPCBStage) {
            disableNdefExchangeMode();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent);
        Log.i(TAG, "onNewIntent: " + intent.getAction());
        playSound();
        // 覆寫該Intent用於補捉如果有新的Intent進入時，可以觸發的事件任務。
        // NDEF exchange mode
        if (!isWriteMode && (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()) ||
                NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction()))) {
            NdefMessage[] msgs = getNdefMessages(intent);
            if (msgs != null) {
                mReadContent = new String(msgs[0].getRecords()[0].getPayload());
                tvNFCReadContent.setText(mReadContent);

                if ((mTestMode & TEST_MODE_WRITE_AND_READ) != 0) {
                    if (mWriteContent.equals(mWriteContent)) {
                        tvNFCReadContent.setTextColor(Color.GREEN);
                        backResult(true);
                    } else {
                        backResult(false);
                    }
                } else {
                    if (mReadContent != null) {
                        tvNFCReadContent.setTextColor(Color.GREEN);
                        backResult(true);
                    } else {
                        backResult(false);
                    }
                }
            }
        }

        // 監測到有指定ACTION進入，代表要寫入資料至Tag中。
        // Tag writing mode
        if (isWriteMode && NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Log.i(TAG, "nfc enabled: " + mNfcHandler.isEnabled());
            Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            boolean result = writeTag(getNoteAsNdef(), detectedTag);
            Log.i(TAG, "write result: " + result);
            if (result) {
                tvNFCWriteContent.setTextColor(Color.GREEN);
                isWriteMode = false;
            } else if (isWriteFail) {

            } else {
                backResult(false);
            }
        }
    }

    private NdefMessage getNoteAsNdef() {
        Timestamp mTimestamp = new Timestamp(System.currentTimeMillis());
        String mTime = mTimestamp.toString();
        String content = "Wistron: " + mTime;

        byte[] textBytes = content.toString().getBytes();
        NdefRecord textRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, "text/plain".getBytes(), new byte[]{}, textBytes);
        return new NdefMessage(new NdefRecord[]{textRecord});
    }

    private void playSound() {
        // TODO Auto-generated method stub
        mPlayer = new MediaPlayer();
        try {
            AssetFileDescriptor mDescriptor = getAssets().openFd("chord.wav");
            mPlayer.setDataSource(mDescriptor.getFileDescriptor(), mDescriptor.getStartOffset(), mDescriptor.getLength());
            mPlayer.prepare();
            mPlayer.start();
            mPlayer.setOnCompletionListener(new OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    // TODO Auto-generated method stub
                    mp.release();
                    mp = null;
                }
            });
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private NdefMessage[] getNdefMessages(Intent intent) {
        // Parse the intent
        NdefMessage[] msgs = null;
        String action = intent.getAction();
        // 識別目前的action為何
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            // 取得parcelabelarrry的資料
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            // 取出的內容如果不為null，將parcelable轉成ndefmessage
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            } else {
                // Unknown tag type
                byte[] empty = new byte[]{};
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
                NdefMessage msg = new NdefMessage(new NdefRecord[]{
                        record
                });
                msgs = new NdefMessage[]{
                        msg
                };
            }
        } else {
            Log.i(TAG, "Unknown intent.");
        }
        return msgs;

    }

    private boolean writeTag(NdefMessage message, Tag tag) {
        isWriteFail = false;
        int size = message.toByteArray().length;
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();
                if (!ndef.isWritable()) {
                    tvNFCWriteContent.setText(mToolKit.getStringResource(R.string.nfc_write_error_read_only));
                } else {
                    if (ndef.getMaxSize() < size) {
                        tvNFCWriteContent.setText(String.format(mToolKit.getStringResource(R.string.nfc_write_error_over_capacity), ndef.getMaxSize(), size));
                    } else {
                        // write message to a pre-formatted tag
                        ndef.writeNdefMessage(message);
                        mWriteContent = new String(message.getRecords()[0].getPayload());
                        tvNFCWriteContent.setText(mWriteContent);
                        return true;
                    }
                }
            } else {
                NdefFormatable format = NdefFormatable.get(tag);
                if (format != null) {
                    try {
                        format.connect();
                        format.format(message);  // Formatted tag and wrote message
                        mWriteContent = new String(message.getRecords()[0].getPayload());
                        tvNFCWriteContent.setText(mWriteContent);
                        return true;
                    } catch (IOException e) {
                        e.printStackTrace();
                        tvNFCWriteContent.setText(mToolKit.getStringResource(R.string.nfc_write_error_format_fail));
                    }
                } else {
                    tvNFCWriteContent.setText(mToolKit.getStringResource(R.string.nfc_write_error_not_support_ndef));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            isWriteFail = true;
            tvNFCWriteContent.setText(mToolKit.getStringResource(R.string.nfc_write_error_write_fail));
        }
        return false;
    }

    public NdefRecord createTextRecord(String payload, Locale locale, boolean encodeInUtf8) {
        // 取得預設的編碼格式
        byte[] langBytes = locale.getLanguage().getBytes(Charset.forName("US-ASCII"));
        // 準備轉換成UTF-8的編碼
        Charset utfEncoding = encodeInUtf8 ? Charset.forName("UTF-8") : Charset.forName("UTF-16");

        // 將內容依預設編碼轉成byte[]
        byte[] textBytes = payload.getBytes(utfEncoding);

        // 往下做字元轉換的位移
        int utfBit = encodeInUtf8 ? 0 : (1 << 7);
        char status = (char) (utfBit + langBytes.length);
        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        data[0] = (byte) status;
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);

        // 建立TNF_WELL_KNOWN的Ndef record
        NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                NdefRecord.RTD_TEXT, new byte[0], data);

        return record;
    }

    private void backResult(boolean finish) {
        try {
            if (mPlayer != null && mPlayer.isPlaying()) {
                mPlayer.release();
                mPlayer = null;
            }
        } catch (IllegalStateException e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        cancelTimer();
        //mNfcHandler.setNFCEnabled(false);
        mToolKit.returnWithResult(finish);
    }
}