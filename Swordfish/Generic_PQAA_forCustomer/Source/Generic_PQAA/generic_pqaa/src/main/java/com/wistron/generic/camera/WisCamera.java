package com.wistron.generic.camera;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.wistron.generic.pqaa.R;
import com.wistron.pqaa_common.jar.global.WisCommonConst;
import com.wistron.pqaa_common.jar.global.WisToolKit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class WisCamera extends Activity implements Callback, OnClickListener {
    private final int MSG_DISABLE_VIEW = 0;
    private final int MSG_TACK_CAMERA = 1;

    private final int THUMBNAIL_SIZE = 100;
    // ----------------------result---------------
    private TextView mResultContent;
    private Button mResultButton;
    // --------------------------------------
    private SurfaceView mPreView;
    private RadioGroup mCameraGroup;
    private ToggleButton mFlashButton;
    private Button mTakeButton, mPassExitButton, mFailExitButton;
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private int mCameraID;
    private int mDisplayRotate = 0;

    private boolean isPass = false;
    private boolean mComponentMode = true;
    private boolean isPassButtonEnable = false;

    // common tool kit
    private WisToolKit mToolKit;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// keep screen on
        setContentView(R.layout.camera);

        mToolKit = new WisToolKit(this);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)
                && !getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)
                && !getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            Toast.makeText(this, mToolKit.getStringResource(R.string.camera_not_exist), Toast.LENGTH_LONG).show();
            backToPQAA();
        } else {
            findView();
            getTestArguments();
            setSurfaceType();
            setViewByLanguage();
        }
    }

    private void setViewByLanguage() {
        // TODO Auto-generated method stub
        TextView mItemTitle = (TextView) findViewById(R.id.item_title);
        mItemTitle.setText(mToolKit.getStringResource(R.string.camera_test_title));

        mTakeButton.setText(mToolKit.getStringResource(R.string.button_shoot));
        mFlashButton.setText(mToolKit.getStringResource(R.string.button_flash));
        mFlashButton.setTextOn(mToolKit.getStringResource(R.string.button_flash));
        mFlashButton.setTextOff(mToolKit.getStringResource(R.string.button_flash));
        mPassExitButton.setText(mToolKit.getStringResource(R.string.button_pass));
        mFailExitButton.setText(mToolKit.getStringResource(R.string.button_fail));

        ((RadioButton) findViewById(R.id.camera_frontcamera)).setText(mToolKit.getStringResource(R.string.camera_front));
        ((RadioButton) findViewById(R.id.camera_backcamera)).setText(mToolKit.getStringResource(R.string.camera_back));
    }

    private void getTestArguments() {
        // TODO Auto-generated method stub
        String mTestStyle = mToolKit.getCurrentTestType();
        if (mTestStyle != null) {
            if (mTestStyle.equals(WisCommonConst.TESTSTYLE_SAVED_CONFIG)) {
                mComponentMode = false;
            }
        }
    }

    private void findView() {
        // TODO Auto-generated method stub
        mPreView = (SurfaceView) findViewById(R.id.camera_preview);
        mCameraGroup = (RadioGroup) findViewById(R.id.camera_camera_group);
        mTakeButton = (Button) findViewById(R.id.camera_shoot);
        mFlashButton = (ToggleButton) findViewById(R.id.camera_flash);
        mPassExitButton = (Button) findViewById(R.id.camera_passExit);
        mFailExitButton = (Button) findViewById(R.id.camera_failExit);
        mCameraGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // TODO Auto-generated method stub
                switch (checkedId) {
                    case R.id.camera_frontcamera:
                        resetCamera();
                        try {
                            initCamera();
                            isPassButtonEnable = true;
                            setCameraParameters();
                        } catch (Exception e) {
                            // TODO: handle exception
                            e.printStackTrace();
                        }
                        break;
                    case R.id.camera_backcamera:
                        resetCamera();
                        try {
                            initCamera();
                            setCameraParameters();
                        } catch (Exception e) {
                            // TODO: handle exception
                            e.printStackTrace();
                        }
                        break;
                    default:
                        break;
                }
            }
        });

        mFlashButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                Parameters mParameters = mCamera.getParameters();
                if (isChecked) {
                    mParameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
                    mCamera.setParameters(mParameters);
                } else {
                    mParameters.setFlashMode(Parameters.FLASH_MODE_OFF);
                    mCamera.setParameters(mParameters);
                }
            }
        });

        mTakeButton.setOnClickListener(this);
        mPassExitButton.setOnClickListener(this);
        mFailExitButton.setOnClickListener(this);
        mPassExitButton.setEnabled(false);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            mFlashButton.setVisibility(View.GONE);
        }

        if (Camera.getNumberOfCameras() <= 1) {
            mCameraGroup.setVisibility(View.GONE);
            isPassButtonEnable = true;
            mPassExitButton.setEnabled(true);
        }
    }

    private void setSurfaceType() {
        // TODO Auto-generated method stub
        mHolder = mPreView.getHolder();
        mHolder.addCallback(this);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
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

    private void resetCamera() {
        // TODO Auto-generated method stub
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public Camera.PictureCallback takePictureCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // TODO Auto-generated method stub
            // save picture
            String mSavePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "pic.jpg";
            FileOutputStream mOutputStream;
            try {
                mOutputStream = new FileOutputStream(mSavePath);
                mOutputStream.write(data);
                mOutputStream.flush();
                mOutputStream.close();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // preview picture
            Bitmap mThumbnailBitmap = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(mSavePath), THUMBNAIL_SIZE, THUMBNAIL_SIZE, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
            int rotate = mDisplayRotate;
            if (mCameraGroup.getCheckedRadioButtonId() == R.id.camera_frontcamera
                    && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                rotate += 180;
            }
            Matrix matrix = new Matrix();
            matrix.setRotate(rotate);
            mThumbnailBitmap = Bitmap.createBitmap(mThumbnailBitmap, 0, 0, mThumbnailBitmap.getWidth(), mThumbnailBitmap.getHeight(), matrix, true);
            ((ImageView) findViewById(R.id.camera_thumbnail)).setImageBitmap(mThumbnailBitmap);
            System.gc();
            mCamera.startPreview();
            enableView();
        }
    };

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // TODO Auto-generated method stub
        if (holder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
            e.printStackTrace();
        }
        setCameraParameters();
    }

    protected void enableView() {
        // TODO Auto-generated method stub
        mCameraGroup.setEnabled(true);
        findViewById(R.id.camera_backcamera).setEnabled(true);
        findViewById(R.id.camera_frontcamera).setEnabled(true);
        mTakeButton.setEnabled(true);
        mPassExitButton.setEnabled(isPassButtonEnable);
        mFailExitButton.setEnabled(true);
    }

    protected void disableView() {
        mCameraGroup.setEnabled(false);
        findViewById(R.id.camera_backcamera).setEnabled(false);
        findViewById(R.id.camera_frontcamera).setEnabled(false);
        mTakeButton.setEnabled(false);
        mPassExitButton.setEnabled(false);
        mFailExitButton.setEnabled(false);
    }

    private void setCameraParameters() {
        // TODO Auto-generated method stub
        mDisplayRotate = 0;

        // Flash options
        mFlashButton.setEnabled(false);
        mFlashButton.setChecked(false);
        Parameters mParameters = mCamera.getParameters();
        // Flash options
        List<String> supportedFlash = mParameters.getSupportedFlashModes();
        if (supportedFlash != null && supportedFlash.contains(Parameters.FLASH_MODE_TORCH)) {
            mFlashButton.setEnabled(true);
        }

        // PictureSize options
        if (mParameters.getPictureSize().width > 1024 || mParameters.getPictureSize().height > 1024) {
            List<Size> mPictureSizes = mParameters.getSupportedPictureSizes();
            int mWidth = 0, mHeight = 0;
            for (Size mSize : mPictureSizes) {
                if ((mSize.width <= 1024 && mSize.height <= 1024) && (mWidth < mSize.width || mHeight < mSize.height)) {
                    mWidth = mSize.width;
                    mHeight = mSize.height;
                }
            }
            mParameters.setPictureSize(mWidth, mHeight);
            mCamera.setParameters(mParameters);
        }

        // display options
        mDisplayRotate = 0;
        CameraInfo mCameraInfo = new CameraInfo();
        Camera.getCameraInfo(mCameraID, mCameraInfo);
        int mScreenOrientation = getResources().getConfiguration().orientation;
        int mDisplayOrientation = getWindowManager().getDefaultDisplay().getRotation() * 90;
        int mCameraOrientation = mCameraInfo.orientation;
        Log.i("WisCamera---rotation", "CameraOrientation: " + mCameraOrientation + "screenOrientation: " + mScreenOrientation + ",displayOrientation: " + mDisplayOrientation);
        if (mCameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
            mDisplayRotate = (mCameraOrientation - mDisplayOrientation + 360) % 360;
        } else {
            mDisplayRotate = (mCameraOrientation + mDisplayOrientation) % 360;
            mDisplayRotate = (360 - mDisplayRotate) % 360;
        }
        mCamera.setDisplayOrientation(mDisplayRotate);
        try {
            mCamera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mCamera.startPreview();

        mPassExitButton.setEnabled(isPassButtonEnable);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        // mCameraNumber = Camera.getNumberOfCameras();
        Log.i("--->", "surface created......");
        initCamera();
    }

    private void initCamera() {
        try {
            if (Camera.getNumberOfCameras() <= 1) {
                mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                if (mCamera != null) {
                    mCameraID = CameraInfo.CAMERA_FACING_BACK;
                }
            } else {
                if (mCameraGroup.getCheckedRadioButtonId() == R.id.camera_backcamera) {
                    mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                    if (mCamera != null) {
                        mCameraID = CameraInfo.CAMERA_FACING_BACK;
                    }
                } else {
                    mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
                    if (mCamera != null) {
                        mCameraID = CameraInfo.CAMERA_FACING_FRONT;
                    }
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        resetCamera();
    }

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            if (msg.what == MSG_DISABLE_VIEW) {
                disableView();
            } else if (msg.what == MSG_TACK_CAMERA) {
                System.out.println("---->>" + mCamera.getParameters().getFlashMode());
                mCamera.takePicture(null, null, null, takePictureCallback);
            }
        }

    };
    private Runnable toDisable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            handler.sendEmptyMessage(MSG_DISABLE_VIEW);
        }
    };
    private Runnable waitSet = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            handler.sendEmptyMessage(MSG_TACK_CAMERA);
        }
    };

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v == mTakeButton) {
            isPassButtonEnable = mPassExitButton.isEnabled();
            disableView();
            mCamera.takePicture(null, null, null, takePictureCallback);
        } else if (v == mPassExitButton) {
            mPassExitButton.setEnabled(false);
            resetCamera();
            isPass = true;
            if (mComponentMode) {
                displayResult();
            } else {
                backToPQAA();
            }
        } else if (v == mFailExitButton) {
            mFailExitButton.setEnabled(false);
            resetCamera();
            isPass = false;
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
        System.gc();
        mToolKit.returnWithResult(isPass);
    }
}
