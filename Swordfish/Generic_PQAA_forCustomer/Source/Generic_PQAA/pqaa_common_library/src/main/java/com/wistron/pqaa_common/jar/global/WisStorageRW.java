package com.wistron.pqaa_common.jar.global;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class WisStorageRW {
	private static final byte MSG_TEST_START = 0;
	private static final byte MSG_TEST_PROGRESS = 1;
	private static final byte MSG_DELETE_START = 2;
	private static final byte MSG_DELETE_PROGRESS = 3;
	private static final byte MSG_TEST_ABORT = 4;
	private static final byte MSG_RESULT_FAIL = 5;
	private static final byte MSG_TEST_DONE = 6;
	
	/**
	 * Memory test
	 */
	public static final int FLAG_MEMORY=0;
	/**
	 * Internal SDCard or EMMC test
	 */
	public static final int FLAG_INTERNAL_SDCARD=1;
	/**
	 * External SDCard test
	 */
	public static final int FLAG_EXTERNAL_SDCARD=2;
	
	private Context context;
	private OnSDCardTestStateChangedListener mSDCardListener;
	private OnMemoryTestStateChangedListener mMemoryListener;
	
	private int mCurFlag=FLAG_INTERNAL_SDCARD;
	private String mSDCardPath="";
	private int mSDCardFileSize = 1 * 1024;
	private int mSDCardFileNumber = 10;
	private int mMemorySize = 500;
	private boolean isRepeatTest = true;
	private boolean isTesting=false;
	
	/**
	 * Create a WisStorageRW object with flag
	 * @param context
	 * the context
	 * @param mFlag
	 * set the test flag: FLAG_MEMORY; FLAG_INTERNAL_SDCARD; FLAG_EXTERNAL_SDCARD.
	 */
	public WisStorageRW(Context context, int mFlag) {
		super();
		this.context = context;
		this.mCurFlag = mFlag;
	}

	/**
	 * Create a WisStorageRW object with flag and SDCard test path
	 * @param context
	 * the context
	 * @param mFlag
	 * set the test flag: FLAG_MEMORY; FLAG_INTERNAL_SDCARD; FLAG_EXTERNAL_SDCARD.
	 * @param mSDCardPath
	 * set the SDCard test path
	 */
	public WisStorageRW(Context context, int mFlag, String mSDCardPath) {
		super();
		this.context = context;
		this.mCurFlag = mFlag;
		this.mSDCardPath = mSDCardPath;
	}

	/**
	 * Start to test
	 */
	public void start(){
		isTesting=true;
		if (mCurFlag == FLAG_MEMORY) {
			new Thread(startRamTest).start();
		}else if (mCurFlag == FLAG_INTERNAL_SDCARD || mCurFlag == FLAG_EXTERNAL_SDCARD) {
			new Thread(startSDCardTest).start();
		}
	}
	
	/**
	 * Stop to test
	 */
	public void stop(){
		isTesting=false;
	}
	
	/**
	 * Set the SDCard test path
	 * @param path
	 * SDCard path
	 */
	public void setSDCardPath(String path){
		mSDCardPath=path;
	}
	
	/**
	 * Set the file size for SDCard test, test unit is KB
	 * @param mSDCardFileSize
	 * the file size for SDCard test
	 */
	public void setSDCardFileSize(int mSDCardFileSize) {
		this.mSDCardFileSize = mSDCardFileSize;
	}

	/**
	 * Set the file numbers for SDCard test
	 * @param mSDCardFileNumber
	 * the file number for SDCard test
	 */
	public void setSDCardFileNumber(int mSDCardFileNumber) {
		this.mSDCardFileNumber = mSDCardFileNumber;
	}

	/**
	 * Set the momory test size, test unit is KB
	 * @param mMemorySize
	 * the memory size
	 */
	public void setMemorySize(int mMemorySize) {
		this.mMemorySize = mMemorySize;
	}

	/**
	 * Set if the continuous test for current item
	 * @param isRepeat
	 * indicate if continuous test
	 */
	public void setRepeatTest(boolean isRepeat){
		this.isRepeatTest=isRepeat;
	}

	private Runnable startRamTest = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			isTesting=true;
			ArrayList<String> mTestList = new ArrayList<String>();
			byte[] buffer = new byte[1024];
			while (isTesting) {
				if (mTestList.size() == 0) {
					handler.sendEmptyMessage(MSG_TEST_START);
				}
				InputStream mInputStream;
				try {
					mInputStream = context.getAssets().open("sdcard_test.txt");
					mInputStream.read(buffer);
					String mTempString = new String(buffer);
					mTestList.add(mTempString);
					if (!mTestList.get(mTestList.size() - 1).equals(mTempString)) {
						isTesting = false;
						handler.sendEmptyMessage(MSG_RESULT_FAIL);
						break;
					}
					mTempString = null;
					mInputStream.close();
					Message msg = handler.obtainMessage();
					msg.what = MSG_TEST_PROGRESS;
					msg.arg1 = (mTestList.size()*100)/mMemorySize;
					handler.sendMessage(msg);
					if (mTestList.size() == mMemorySize) {
						mTestList.clear();
						System.gc();
						if (!isRepeatTest) {
							isTesting = false;
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					isTesting = false;
					Message msg=new Message();
					msg.what=MSG_TEST_ABORT;
					msg.obj=e.toString();
					handler.sendMessage(msg);
				}
			}
			mTestList.clear();
			System.gc();
			
			handler.sendEmptyMessage(MSG_TEST_DONE);
		}
	};
	
	private Runnable startSDCardTest = new Runnable() {
		private int mAddFiles = 0, mDelFiles = 0;

		@Override
		public void run() {
			// TODO Auto-generated method stub
			readAndWriteTest();
		}
		
		private void readAndWriteTest() {
			try {
				byte[] buffer_W = new byte[1024];
				InputStream mStream = context.getAssets().open("sdcard_test.txt");
				mStream.read(buffer_W);
				byte[] buffer_R = new byte[buffer_W.length];

				isTesting = true;
				while (isTesting) {
					if (mAddFiles == 0) {
						handler.sendEmptyMessage(MSG_TEST_START);
					}
						
					File mModifyFile = File.createTempFile("sdcard", ".sdt", new File(mSDCardPath));
					FileOutputStream mFileOutputStream = new FileOutputStream(mModifyFile);
					FileInputStream mFileInputStream = new FileInputStream(mModifyFile);

					for (int i = 0; i < mSDCardFileSize; i++) {
						mFileOutputStream.write(buffer_W);
						mFileOutputStream.flush();
						mFileInputStream.read(buffer_R);
						String mRead = new String(buffer_R);
						String mWrite = new String(buffer_W);
						if (!mRead.equals(mWrite)) {
							isTesting = false;
							handler.sendEmptyMessage(MSG_RESULT_FAIL);
							break;
						}
					}
					mFileInputStream.close();
					mFileOutputStream.close();
					mAddFiles++;
					
					if (isTesting) {
						Message msg = new Message();
						msg.what=MSG_TEST_PROGRESS;
						msg.arg1=(mAddFiles*100)/mSDCardFileNumber;
						handler.sendMessage(msg);
						
						if (mAddFiles >= mSDCardFileNumber) {
							handler.sendEmptyMessage(MSG_DELETE_START);
							
							deleteTempFile();
							mAddFiles = 0;
							if (!isRepeatTest) {
								isTesting=false;
							}
						}
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				isTesting = false;
				Message msg=new Message();
				msg.what=MSG_TEST_ABORT;
				msg.obj=e.toString();
				handler.sendMessage(msg);
			}
			deleteTempFile();
			handler.sendEmptyMessage(MSG_TEST_DONE);
			Log.i("sdcard finish", "********************************");
		}

		private boolean deleteTempFile() {
			// TODO Auto-generated method stub
			mDelFiles=0;
			FilenameFilter mFilter = new FilenameFilter() {

				@Override
				public boolean accept(File dir, String filename) { //
					// TODO Auto-generated method stub
					return filename.endsWith(".sdt");
				}
			};
			File file = new File(mSDCardPath);
			
			if (file != null) {
				File[] files = file.listFiles(mFilter);
				if (files != null) {
					int mTempProgress=0;
					for (File mTempFile : files) {
						mTempFile.delete();
						mDelFiles++;
						if (mTempProgress != (mDelFiles*100)/files.length) {
							mTempProgress=(mDelFiles*100)/files.length;
							Message msg=new Message();
							msg.what=MSG_DELETE_PROGRESS;
							msg.arg1=mTempProgress;
							handler.sendMessage(msg);
						}
					}
				}
			}
			return true;
		}
	};
	
	/**
	 * Register a callback to be invoked when test status changed during SDCard test
	 * @param listener
	 * 	The callback that will run
	 */
	public void setOnSDCardTestStateChangedListener(OnSDCardTestStateChangedListener listener){
		this.mSDCardListener = listener;
	}
	
	/**
	 * Register a callback to be invoked when test status changed during Memory test
	 * @param listener
	 * The callback that will run
	 */
	public void setOnMemoryTestStateChangedListener(OnMemoryTestStateChangedListener listener){
		this.mMemoryListener = listener;
	}
	
	private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if (mCurFlag == FLAG_MEMORY) {
				switch (msg.what) {
				case MSG_TEST_START:
					mMemoryListener.onStateIsTestStart(mCurFlag);
					break;
				case MSG_TEST_PROGRESS:
					mMemoryListener.onStateIsTestProgressChanged(mCurFlag, msg.arg1);
					break;
				case MSG_TEST_ABORT:
					mMemoryListener.onStateIsTestAbort(mCurFlag,msg.obj.toString());
					break;
				case MSG_RESULT_FAIL:
					mMemoryListener.onStateIsResultFail(mCurFlag);
					break;
				case MSG_TEST_DONE:
					mMemoryListener.onStateIsTestDone(mCurFlag);
					break;
				default:
					break;
				}
			}else {
				switch (msg.what) {
				case MSG_TEST_START:
					mSDCardListener.onStateIsTestStart(mCurFlag);
					break;
				case MSG_TEST_PROGRESS:
					mSDCardListener.onStateIsTestProgressChanged(mCurFlag, msg.arg1);
					break;
				case MSG_DELETE_START:
					mSDCardListener.onStateIsDeleteStart(mCurFlag);
					break;
				case MSG_DELETE_PROGRESS:
					mSDCardListener.onStateIsDeleteProgressChanged(mCurFlag, msg.arg1);
					break;
				case MSG_TEST_ABORT:
					mSDCardListener.onStateIsTestAbort(mCurFlag,msg.obj.toString());
					break;
				case MSG_RESULT_FAIL:
					mSDCardListener.onStateIsResultFail(mCurFlag);
					break;
				case MSG_TEST_DONE:
					mSDCardListener.onStateIsTestDone(mCurFlag);
					break;
				default:
					break;
				}
			}
		}
		
	};
	
	/**
	 * @author dragon
	 * Interface definition for a callback to be invoked when test status changed during SDCard test
	 */
	public abstract interface OnSDCardTestStateChangedListener{
		/**
		 * Start to test for one cycle
		 * @param flag
		 * indicate the current test
		 */
		public abstract void onStateIsTestStart(int flag);
		/**
		 * test progress changed, you should update your UI
		 * @param flag
		 * indicate the current test
		 * @param progress
		 * indicate the current progress during test
		 */
		public abstract void onStateIsTestProgressChanged(int flag, int progress);
		/**
		 * Start to delete the temporary files during test end of one cycle
		 * @param flag
		 * indicate the current test
		 */
		public abstract void onStateIsDeleteStart(int flag);
		/**
		 * delete progress changed,you should update your UI
		 * @param flag
		 * indicate the current test
		 * @param progress
		 * indicate the current progress during delete
		 */
		public abstract void onStateIsDeleteProgressChanged(int flag, int progress);
		/**
		 * has exception during test
		 * @param flag
		 * indicate the current test
		 * @param exception 
		 * exception throws during test,you can capture this exception for yourself demands;
		 * you can handle "IOException,FileNotFoundException,IllegalArgumentException"
		 */
		public abstract void onStateIsTestAbort(int flag, String exception);
		/**
		 * the read content can not match the write content
		 * @param flag
		 * indicate the current test
		 */
		public abstract void onStateIsResultFail(int flag);
		/**
		 * test done, the exit operate should be handle in this action.
		 * @param flag
		 * indicate the current test
		 */
		public abstract void onStateIsTestDone(int flag);
	}
	
	/**
	 * @author dragon
	 * Interface definition for a callback to be invoked when test status changed during Memory test.
	 */
	public abstract interface OnMemoryTestStateChangedListener{
		/**
		 * Start to test for one cycle
		 * @param flag
		 * indicate the current test
		 */
		public abstract void onStateIsTestStart(int flag);
		/**
		 * test progress changed, you should update your UI
		 * @param flag
		 * indicate the current test
		 * @param progress
		 * indicate the current progress during test
		 */
		public abstract void onStateIsTestProgressChanged(int flag, int progress);
		/**
		 * has exception during test
		 * @param flag
		 * indicate the current test
		 * @param exception 
		 * exception throws during test,you can capture this exception for yourself demands;
		 * you can handle "IOException"
		 */
		public abstract void onStateIsTestAbort(int flag, String exception);
		/**
		 * the read content can not match the write content
		 * @param flag
		 * indicate the current test
		 */
		public abstract void onStateIsResultFail(int flag);
		/**
		 * test done, the exit operate should be handle in this action.
		 * @param flag
		 * indicate the current test
		 */
		public abstract void onStateIsTestDone(int flag);
	}
}
