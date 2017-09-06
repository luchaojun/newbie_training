package com.wistron.generic.hdmi;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.swetake.util.Qrcode;
import com.wistron.generic.pqaa.R;
import com.wistron.pqaa_common.jar.global.WisCommonConst;
import com.wistron.pqaa_common.jar.global.WisParseValue;
import com.wistron.pqaa_common.jar.global.WisToolKit;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class HDMI extends Activity implements OnClickListener, Callback {
    private static final int MSG_REFRESH_SCAN = 0;
    private static final int MSG_UPDATE_TIME = 1;
    private static final int MSG_TIMEOUT = 2;
    private static final int MSG_OPEN_CAMERA = 3;

    private static final int QRCODE_VERSION = 4;
    private static final String[] mQrcodeTestArray = new String[]{"one", "two", "three", "four",
            "five"};

    private static final int REFRESH_FREQUENCY = 200;

    private Button btn_Exit;
    private FindView mFindView;
    private SurfaceView mGenerateSurfaceView;
    private SurfaceView mDecodeSurfaceView;

    private SurfaceHolder mGenerateSurfaceHolder;
    private SurfaceHolder mDecodeSurfaceHolder;

    private boolean isPass;

    // decode QRCode
    private ImageScanner scanner;
    private Camera mCamera;
    private int mCameraID;

    private String mCurQR = mQrcodeTestArray[0];

    // common tool kit
    private WisToolKit mToolKit;

    private ScheduledExecutorService mScheduledTimer;
    private ScheduledFuture<?> mOpenCameraTask, mTimeCountDownTask;

    private int TIMEOUT = 90;
    private int mScanIncrease;

    private Handler autoFocusHandler;

    static {
        System.loadLibrary("iconv");
    }

    private String mItem = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.hdmi);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mToolKit = new WisToolKit(this);

        Log.i("WKS", "...:" + mItem);
        getTestArguments();
        findView();
        initial();
        setViewByLanguage();
    }

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            if (msg.what == MSG_REFRESH_SCAN) {
                mFindView.increaseScan();
            } else if (msg.what == MSG_UPDATE_TIME) {
                updateTimeCountUI();
            } else if (msg.what == MSG_TIMEOUT) {
                btn_Exit.performClick();
            } else if (msg.what == MSG_OPEN_CAMERA) {
                mFindView.setVisibility(View.VISIBLE);
                findViewById(R.id.time_countdown_layout).setVisibility(View.VISIBLE);
                cancelTimer();
                mTimeCountDownTask = mScheduledTimer.scheduleWithFixedDelay(toUpdateUI,
                        REFRESH_FREQUENCY, REFRESH_FREQUENCY, TimeUnit.MILLISECONDS);
                mDecodeSurfaceView.setVisibility(View.VISIBLE);
            }
        }

    };

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (mCamera != null)
                mCamera.autoFocus(mAutoFocusCallback);
        }
    };

    private void initial() {
        // TODO Auto-generated method stub
        setRandom();

        autoFocusHandler = new Handler();

		/* Instance barcode scanner */
        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);

        mScheduledTimer = Executors.newScheduledThreadPool(2);
        mOpenCameraTask = mScheduledTimer.schedule(toOpenCamera, 500, TimeUnit.MILLISECONDS);

    }

    private Runnable toOpenCamera = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            handler.sendEmptyMessage(MSG_OPEN_CAMERA);
        }
    };

    private Runnable toUpdateUI = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            mScanIncrease++;
            if (mScanIncrease % (1000 / REFRESH_FREQUENCY) == 0) {
                if (mScanIncrease / (1000 / REFRESH_FREQUENCY) == TIMEOUT) {
                    handler.sendEmptyMessage(MSG_TIMEOUT);
                } else {
                    handler.sendEmptyMessage(MSG_UPDATE_TIME);
                }
            }
            handler.sendEmptyMessage(MSG_REFRESH_SCAN);
        }
    };

    private void updateTimeCountUI() {
        // TODO Auto-generated method stub
        ((TextView) findViewById(R.id.remaintime))
                .setText(mToolKit.getStringResource(R.string.remain_time)
                        + mToolKit.formatCountDownTime(TIMEOUT, mScanIncrease
                        / (1000 / REFRESH_FREQUENCY)));
    }

    private void initCamera() {
        try {
            if (Camera.getNumberOfCameras() <= 1) {
                mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                if (mCamera != null) {
                    mCameraID = CameraInfo.CAMERA_FACING_BACK;
                }
            } else {

                if (mItem.equals("back")) {
                    mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                    if (mCamera != null) {
                        mCameraID = CameraInfo.CAMERA_FACING_BACK;
                    }
                } else {
                    mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                    if (mCamera != null) {
                        mCameraID = CameraInfo.CAMERA_FACING_BACK;
                    }
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    private void setCameraParameters() {
        // TODO Auto-generated method stub
        if (mCamera == null) {
            return;
        }

        Parameters mParameters = mCamera.getParameters();
        if (mParameters.getPreviewSize().width < 320) {
            List<Size> sizes = mParameters.getSupportedPreviewSizes();
            for (Size mSize : sizes) {
                if (mSize.width > 400) {
                    mParameters.setPreviewSize(mSize.width, mSize.height);
                    break;
                }
            }
        }
        mCamera.setParameters(mParameters);

        // display options
        int mDisplayRotate = 0;
        CameraInfo mCameraInfo = new CameraInfo();
        Camera.getCameraInfo(mCameraID, mCameraInfo);
        int mScreenOrientation = getResources().getConfiguration().orientation;
        int mDisplayOrientation = getWindowManager().getDefaultDisplay().getRotation() * 90;
        int mCameraOrientation = mCameraInfo.orientation;
        Log.i("WisCamera---rotation", "CameraOrientation: " + mCameraOrientation
                + "screenOrientation: " + mScreenOrientation + ",displayOrientation: "
                + mDisplayOrientation);
        if (mCameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
            mDisplayRotate = (mCameraOrientation - mDisplayOrientation + 360) % 360;
        } else {
            mDisplayRotate = (mCameraOrientation + mDisplayOrientation) % 360;
            mDisplayRotate = (360 - mDisplayRotate) % 360;
        }
        mCamera.setDisplayOrientation(mDisplayRotate);
        mCamera.setPreviewCallback(previewCallback);
        mCamera.startPreview();

        String mFocusMode = mCamera.getParameters().getFocusMode();
        Log.i("HDMI-FocusMode", mFocusMode);
        if (mFocusMode.equals(Camera.Parameters.FOCUS_MODE_AUTO)
                || mFocusMode.equals(Camera.Parameters.FOCUS_MODE_MACRO)) {
            mCamera.autoFocus(mAutoFocusCallback);
        }
    }

    private void findView() {
        // TODO Auto-generated method stub
        btn_Exit = (Button) findViewById(R.id.hdmi_exit);
        mGenerateSurfaceView = (SurfaceView) findViewById(R.id.hdmi_qrcode_generate);
        mDecodeSurfaceView = (SurfaceView) findViewById(R.id.hdmi_qrcode_decode);
        mDecodeSurfaceView.setVisibility(View.INVISIBLE);

        mGenerateSurfaceHolder = mGenerateSurfaceView.getHolder();
        mGenerateSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        mGenerateSurfaceView.setZOrderOnTop(true);

        mDecodeSurfaceHolder = mDecodeSurfaceView.getHolder();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            mDecodeSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        btn_Exit.setOnClickListener(this);
        mGenerateSurfaceHolder.addCallback(new Callback() {

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                // TODO Auto-generated method stub

            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                // TODO Auto-generated method stub
                generateQRCode();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                // TODO Auto-generated method stub

            }
        });
        mDecodeSurfaceHolder.addCallback(this);

        mFindView = (FindView) findViewById(R.id.hdmi_qrcode_find);

        mFindView.setVisibility(View.INVISIBLE);
        btn_Exit.setVisibility(View.INVISIBLE);
        findViewById(R.id.time_countdown_layout).setVisibility(View.INVISIBLE);

    }

    @Override
    public void onAttachedToWindow() {
        // TODO Auto-generated method stub
        super.onAttachedToWindow();
    }

    private void getTestArguments() {
        // TODO Auto-generated method stub
        String mTestStyle = mToolKit.getCurrentTestType();
        if (mTestStyle != null) {
            if (mTestStyle.equals(WisCommonConst.TESTSTYLE_SAVED_CONFIG)) {
                WisParseValue mParse = new WisParseValue(this, mToolKit.getCurrentItem(),
                        mToolKit.getCurrentDatabaseAuthorities());
                int mValue = Integer.parseInt(mParse.getArg1());
                if (mValue > 0) {
                    TIMEOUT = mValue;
                }
            }
        }
    }

    private void setViewByLanguage() {
        // TODO Auto-generated method stub
        TextView mItemTitle = (TextView) findViewById(R.id.item_title);
        mItemTitle.setText(mToolKit.getStringResource(R.string.hdmi_test_title));
        btn_Exit.setText(mToolKit.getStringResource(R.string.button_exit));
        ((TextView) findViewById(R.id.totaltime)).setText(mToolKit
                .getStringResource(R.string.total_time) + mToolKit.formatCountDownTime(TIMEOUT, 0));
        updateTimeCountUI();
    }

    private void setRandom() {
        Random mRandom = new Random(System.currentTimeMillis());
        mCurQR = mQrcodeTestArray[mRandom.nextInt(mQrcodeTestArray.length)];
    }

    private void generateQRCode() {
        // TODO Auto-generated method stub
        try {
            Qrcode mQrcodeGenerate = new Qrcode();
            // L,M,Q,H
            mQrcodeGenerate.setQrcodeErrorCorrect('M');
            // N,A or other
            mQrcodeGenerate.setQrcodeEncodeMode('B');
            // 1~20
            mQrcodeGenerate.setQrcodeVersion(QRCODE_VERSION);

            byte[] mQRBytesEncoding = mCurQR.getBytes();
            boolean[][] bEncoding = mQrcodeGenerate.calQrcode(mQRBytesEncoding);
            drawQRCode(bEncoding, Color.RED);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    private void drawQRCode(boolean[][] bEncoding, int color) {
        // TODO Auto-generated method stub
        int intPadding = 0;
        Canvas mCanvas = mGenerateSurfaceHolder.lockCanvas();
        if (mCanvas == null) {
            return;
        }

        float scaleBase = mCanvas.getWidth() / (bEncoding.length * 3 * 2);
        mCanvas.drawColor(Color.TRANSPARENT);
        mCanvas.scale(scaleBase, scaleBase);
        Paint mPaint = new Paint();
        mPaint.setStyle(Style.FILL);
        mPaint.setColor(color);
        mPaint.setStrokeWidth(5.0f);

        mPaint.setColor(Color.WHITE);
        mCanvas.drawRect(new Rect(intPadding, intPadding, intPadding + (bEncoding.length - 1) * 3
                + 10, intPadding + (bEncoding.length - 1) * 3 + 10), mPaint);

        mPaint.setColor(color);
        for (int i = 0; i < bEncoding.length; i++) {
            for (int j = 0; j < bEncoding.length; j++) {
                if (bEncoding[j][i]) {
                    mPaint.setColor(color);
                    mCanvas.drawRect(new Rect(intPadding + j * 3 + 2, intPadding + i * 3 + 2,
                            intPadding + j * 3 + 2 + 3, intPadding + i * 3 + 2 + 3), mPaint);
                }
            }
        }

        mGenerateSurfaceHolder.unlockCanvasAndPost(mCanvas);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // TODO Auto-generated method stub
        if (holder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
            e.printStackTrace();
        }
        setCameraParameters();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        initCamera();
        try {
            mCamera.setPreviewDisplay(mDecodeSurfaceHolder);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        releaseResource();
    }

    public Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            // TODO Auto-generated method stub

            if (!isPass) {
                Camera.Parameters parameters = camera.getParameters();
                Size size = parameters.getPreviewSize();

                Image barcode = new Image(size.width, size.height, "Y800");
                barcode.setData(data);

                int result = scanner.scanImage(barcode);

                if (result != 0) {
                    String mDecodeQRCode = "";
                    SymbolSet syms = scanner.getResults();
                    for (Symbol sym : syms) {
                        mDecodeQRCode = sym.getData();
                        Toast.makeText(HDMI.this, mDecodeQRCode, Toast.LENGTH_SHORT).show();
                        Log.i("HDMI_Decode", "--> " + mDecodeQRCode);
                        if (mDecodeQRCode != null && !isPass) {
                            if (mDecodeQRCode.equals(mCurQR)) {
                                releaseResource();
                                isPass = true;
                                btn_Exit.performClick();
                            }
                        }
                        break;
                    }
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    private Camera.AutoFocusCallback mAutoFocusCallback = new Camera.AutoFocusCallback() {

        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            // TODO Auto-generated method stub
            /*
			 * if (success) { if (mCamera!=null) { mCamera.setOneShotPreviewCallback(previewCallback); } }else { //
			 * Toast.makeText(HDMI.this, "can't focus!", Toast.LENGTH_SHORT).show();
			 * 
			 * }
			 */
            autoFocusHandler.postDelayed(doAutoFocus, 1000);
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        return true;
    }

    private void backToPQAA() {
        mToolKit.returnWithResult(isPass);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v == btn_Exit) {
            releaseResource();
            backToPQAA();
        }
    }

    private void releaseResource() {
        // TODO Auto-generated method stub
        cancelTimer();
        resetCamera();
    }

    private void cancelTimer() {
		/*
		 * if (mTask != null) { mTask.cancel(); mTask = null; } if (mTimer != null) { mTimer.cancel(); mTimer = null; }
		 */
        if (mOpenCameraTask != null) {
            mOpenCameraTask.cancel(true);
            mOpenCameraTask = null;
        }
        if (mTimeCountDownTask != null) {
            mTimeCountDownTask.cancel(true);
            mTimeCountDownTask = null;
        }
    }

    private void resetCamera() {
        // TODO Auto-generated method stub
        if (mCamera != null) {
            mCamera.cancelAutoFocus();
            mCamera.autoFocus(null);
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }
}
