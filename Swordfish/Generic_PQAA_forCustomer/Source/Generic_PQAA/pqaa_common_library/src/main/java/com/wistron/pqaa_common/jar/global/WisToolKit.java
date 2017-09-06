package com.wistron.pqaa_common.jar.global;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.Instrumentation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.WindowManager;

import com.wistron.pqaa_common.jar.wcis.WisWCISCommonConst;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class WisToolKit {
	private Context context;
	private PackageManager mPackageManager;
	private int mCurrentLanguage;
	private String mCurrentTestType;
	private String mCurrentItem;
	private String mCurrentDatabaseAuthorities;
	private boolean isPCBATestStage;
	private onManualSelectedListener mManualSelectedListener;

	// single instance mode
	private static WisToolKit mToolsKit;

	/**
	 * WisToolKit default constructor
	 * 
	 * @param context
	 * 
	 */
	public WisToolKit(Context activity) {
		super();
		this.context = activity;
		if (context != null && context instanceof Activity) {
			Activity curActivity = ((Activity) context);
			mPackageManager = context.getPackageManager();
			mCurrentLanguage = curActivity.getIntent().getIntExtra(WisCommonConst.EXTRA_LANGUAGE,WisCommonConst.DEFAULT_LANGUAGE);
			mCurrentTestType = curActivity.getIntent().getStringExtra(WisCommonConst.EXTRA_TESTSTYLE);
			mCurrentItem = curActivity.getIntent().getStringExtra(WisCommonConst.EXTRA_ITEM);
			mCurrentDatabaseAuthorities = curActivity.getIntent().getStringExtra(WisCommonConst.EXTRA_AUTHORITIES);
			isPCBATestStage = curActivity.getIntent().getBooleanExtra(WisCommonConst.EXTRA_STAGE,false);
			
			// for current activity, to dismiss the keyguard.
			/*curActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
			curActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
			curActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);*/
		}else {
			mCurrentLanguage = WisCommonConst.DEFAULT_LANGUAGE;
		}
	}

	/**
	 * Single design mode
	 * 
	 * @param context
	 * @return
	 * Single design mode
	 */
	public static WisToolKit getToolsKitInstance(Context context) {
		if (mToolsKit == null) {
			mToolsKit = new WisToolKit(context);
		}
		return mToolsKit;
	}

	/**
	 * get current app version
	 * 
	 * @return 
	 * return the current app version, return null if can't get app version
	 */
	public String getAppVersion() {
		// TODO Auto-generated method stub
		return getAppVersion(context.getPackageName());
	}

	/**
	 * get the App version by specified package name
	 * 
	 * @param 
	 * packageName
	 * @return 
	 * return the app version public if the app has installed,return null if can't get app version
	 */
	public String getAppVersion(String packageName) {
		try {
			PackageInfo packinfo = mPackageManager.getPackageInfo(packageName,0);
			return packinfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * get current app version code
	 * 
	 * @return 
	 * return the current app version code, return 0 if can't get app version code
	 */
	public int getAppVersionCode() {
		// TODO Auto-generated method stub
		return getAppVersionCode(context.getPackageName());
	}
	
	/**
	 * get the app version code by specified package name
	 * @param 
	 * packageName
	 * 
	 * @return
	 *  return the app version code in Androidmanifest.xml, return 0 if can't get app version code
	 */
	public int getAppVersionCode(String packageName){
		try {
			PackageInfo packinfo = mPackageManager.getPackageInfo(packageName,0);
			return packinfo.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * set the current test language
	 * 
	 * @param language
	 *            language set: 0 - English,1 - Chinese simple
	 */
	public void setCurrentLanguage(int language) {
		mCurrentLanguage = language;
	}

	/**
	 * @return 
	 * current language setting
	 */
	public int getCurrentLanguage() {
		return mCurrentLanguage;
	}

	/**
	 * 
	 * @return 
	 * return the current test type.
	 */
	public String getCurrentTestType() {
		return mCurrentTestType;
	}

	/**
	 * set the current test type,either component or savedConfig
	 * 
	 * @param mCurrentTestType
	 *            test type
	 */
	public void setCurrentTestType(String mCurrentTestType) {
		this.mCurrentTestType = mCurrentTestType;
	}

	/**
	 * return the current test item
	 * 
	 * @return
	 * return the current test item
	 */
	public String getCurrentItem() {
		return mCurrentItem;
	}

	/**
	 * set the current test item
	 * 
	 * @param mCurrentItem
	 */
	public void setCurrentItem(String mCurrentItem) {
		this.mCurrentItem = mCurrentItem;
	}

	/**
	 * Return the current database authorities
	 * The arguments is very import for a sub test item
	 * @return
	 * the current database authorities
	 */
	public String getCurrentDatabaseAuthorities() {
		return mCurrentDatabaseAuthorities;
	}
	
	/**
	 * Return the test state for current test item.
	 * PCBA or FA test
	 * @return
	 * Return the test state for current test item.
	 */
	public boolean isPCBATestStage() {
		return isPCBATestStage;
	}
	
	/**
	 * Return the test time by database value setting
	 * @param value
	 * the value of configure file, such as 10 h,or 10 m, or 10 s
	 * @return
	 * Return the test time for current test item
	 */
	public int parseTestTimeByValue(String value){
		int mTestTime=0;
		if (value.contains("h")) {
			value = value.substring(0, value.indexOf("h")).trim();
			mTestTime = Integer.parseInt(value) * 60 * 60;
		} else if (value.contains("m")) {
			value = value.substring(0, value.indexOf("m")).trim();
			mTestTime = Integer.parseInt(value) * 60;
		} else if (value.contains("s")) {
			value = value.substring(0, value.indexOf("s")).trim();
			mTestTime = Integer.parseInt(value);
		} else {
			mTestTime = 0;
		}
		return mTestTime;
	}

	/**
	 * get the all parameters for current item
	 * @param mConfigPath
	 * the config path
	 * @return
	 * return the parameters for current item.if can't find config file,the size of map is equal to 0
	 */
	public Map<String, String> getSingleParameters(String mConfigPath) {
		// TODO Auto-generated method stub
		Map<String, String> mParametersList = new LinkedHashMap<String, String>();
		String mReadLine;
		String mKey = "";
		String mValue = "";
		try {
			FileReader mFileReader = new FileReader(mConfigPath);
			BufferedReader mReader = new BufferedReader(mFileReader);
			while ((mReadLine = mReader.readLine()) != null) {
				if (!mReadLine.startsWith("#") && mReadLine.trim().length() > 0) {
					String[] mKeyValuePair = mReadLine.split("=");
					mKey = mKeyValuePair[0].trim();
					mValue = mKeyValuePair[1].trim();
					mParametersList.put(mKey, mValue);
				}
			}
			mReader.close();
			mFileReader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return mParametersList;
	}

	/**
	 * get the group parameters by config file path,
	 * It means that has multi parameters for one item,
	 * <p>
	 * such as 
	 * <p>
	 * #######################################################
	 * <p>item = TouchPanel
	 * <p>subitem = 7
	 * <p>timeout = 90
	 * <p>#######################################################
	 * <p>item = Display
	 * <p>subitem = 3
	 * <p>interval = 2
	 * <p>#######################################################
	 * 
	 * @param mConfigPath
	 *            indicate the config file path
	 * @return 
	 * return the parameters of config file,if can't find config file,the size of arraylist is equal to 0
	 */
	public ArrayList<Map<String, String>> getGroupParameters(String mConfigPath) {
		// TODO Auto-generated method stub
		ArrayList<Map<String, String>> mParametersList = new ArrayList<Map<String, String>>();
		String mReadLine;
		String mKey = "";
		String mValue = "";
		try {
			FileReader mFileReader = new FileReader(mConfigPath);
			BufferedReader mReader = new BufferedReader(mFileReader);
			Map<String, String> map = new LinkedHashMap<String, String>();
			while ((mReadLine = mReader.readLine()) != null) {
				if (mReadLine.trim().length() > 0) {
					if (mReadLine.startsWith("#")) {
						if (map.size() > 0) {
							mParametersList.add(map);
							map = new LinkedHashMap<String, String>();
						}
					} else {
						String[] mKeyValuePair = mReadLine.split("=");
						mKey = mKeyValuePair[0].trim();
						mValue = mKeyValuePair[1].trim();
						map.put(mKey, mValue);
					}
				}
			}
			mReader.close();
			mFileReader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return mParametersList;
	}
	
	/**
	 * Read content from the specified file, put each line to arraylist
	 * @param path
	 * the file path
	 * @return
	 * return the content of file
	 */
	public ArrayList<String> readContentFromFile(String path){
		ArrayList<String> mLineList = new ArrayList<String>();
		File mDesFile = new File(path);
		if (mDesFile.exists()) {
			String mReadLine;
			FileReader mFileReader = null;
			BufferedReader mReader = null;
			try {
				mFileReader = new FileReader(path);
				mReader = new BufferedReader(mFileReader);
				while ((mReadLine = mReader.readLine()) != null) {
					mLineList.add(mReadLine);
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO: handle exception
				e.printStackTrace();
			}finally{
				try {
					if (mReader != null) {
						mReader.close();
					}
					if (mFileReader != null) {
						mFileReader.close();
					}
				} catch (IOException e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		}
		return mLineList;
	}

	/**
	 * get the string with resource id by current language setting
	 * 
	 * @param id
	 *            resource id
	 * @return the string with resource id
	 */
	public String getStringResource(int id) {
		switch (mCurrentLanguage) {
		case WisCommonConst.LANGUAGE_ENGLISH:

			break;
		case WisCommonConst.LANGUAGE_CHINESE_SIMPLE:
			id++;
			break;
		default:
			break;
		}
		return context.getResources().getString(id);
	}
	
	/**
	 * format the second time to hh:mm:ss
	 * @param totalTime
	 * the total time for seconds
	 * @param curTime
	 * the cur time for seconds
	 * @return
	 * reture the format value
	 */
	public String formatCountDownTime(int totalTime,int curTime){
		String mTime = "";
		int hour = 0, minute = 0, seconds;
		seconds = totalTime - curTime;
		if (seconds >= 60) {
			minute = seconds / 60;
			seconds = seconds % 60;
		}
		if (minute >= 60) {
			hour = minute / 60;
			minute = minute % 60;
		}
		mTime = format(hour) + ":" + format(minute) + ":" + format(seconds);
		return mTime;
	}
	
	/**
	 * for the time to two digits
	 * @param time
	 * @return
	 * for the time to two digits
	 */
	private String format(int time) {
		// TODO Auto-generated method stub
		return String.format("%1$02d", time);
	}

	/**
	 * return the test result to main app
	 * 
	 * @param isPass
	 *            pass status
	 */
	public void returnWithResult(boolean isPass) {
		Activity curActivity = (Activity) context;
		Intent intent = new Intent();
		intent.putExtra(WisCommonConst.EXTRA_PASS, isPass);
		curActivity.setResult(Activity.RESULT_OK, intent);
		curActivity.finish();
	}
	
	/**
	 * return the test result and comment to main app
	 * @param isPass
	 * the test result of current test item
	 * @param remark
	 * the comment for current item
	 */
	public void returnWithResultAndRemark(boolean isPass,String remark) {
		Activity curActivity = (Activity) context;
		Intent intent = new Intent();
		intent.putExtra(WisCommonConst.EXTRA_PASS, isPass);
		intent.putExtra(WisCommonConst.EXTRA_REMARK, remark);
		curActivity.setResult(Activity.RESULT_OK, intent);
		curActivity.finish();
	}
	
	/**
	 * return the test result to RunIn
	 * @param isPass
	 * test result is Pass or Fail
	 * @param isReboot
	 * indicate if the reboot test
	 */
	public void returnToRuninWithResult(boolean isPass,boolean isReboot){
		Activity curActivity = (Activity) context;
		Intent intent = new Intent();
		intent.setClassName("com.wistron.runin", "com.wistron.runin.TestItemsList");
		intent.putExtra(WisCommonConst.EXTRA_REBOOT, isReboot);
		intent.putExtra(WisCommonConst.EXTRA_PASS, isPass);
		if (isReboot) {
			context.startActivity(intent);
		}else {
			curActivity.setResult(Activity.RESULT_OK, intent);
		}
		curActivity.finish();
	}
	
	/**
	 * Reserved
	 * If current item need selected manually by operator,you should implement this method to listen the select result
	 * @param listener
	 * Manual select listener
	 */
	public void setOnManualSelectedListener(onManualSelectedListener listener){
		this.mManualSelectedListener=listener;
	}
	
	/**
	 * shows a result dialog on UI, operator should select the result by manual
	 * @param message
	 * the message of dialog
	 * @param messageSize
	 * message font size
	 * @param positiveButtonTitle
	 * the title of positive button
	 * @param negativeButtonTitle
	 * the title of negative button
	 * @param buttonSize
	 * button font size
	 */
	public void selectTestResultByManual(String message,float messageSize,String positiveButtonTitle,String negativeButtonTitle,float buttonSize){
		new WisAlertDialog(context)
			.setMessage(message, messageSize)
			.setPositiveButton(positiveButtonTitle,buttonSize, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					returnWithResult(true);
				}
			}).setNegativeButton(negativeButtonTitle,buttonSize, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					returnWithResult(false);
				}
			}).showDialog();
	}
	
	/**
	 * detect the device's lock key if match the "Wistron", if match, PQAA can run on the device
	 * or can't run on the device
	 * @return
	 * the detect result
	 */
	public boolean isWistronLockKey(){
		boolean isMatchLockKey=false;
		WisShellCommandHelper mShellCommandHelper = new WisShellCommandHelper();
		ArrayList<String> mLockString = mShellCommandHelper.exec(WisCommonConst.INTERFACE_LOCK_FLAG);
		if (mLockString.size() > 0 && mLockString.get(0).equals("Wistron")) {
			isMatchLockKey=true;
		}
		return isMatchLockKey;
	}
	
	/**
	 * detect the device's lock key if match the specified key with command: 'getprop ro.product.model'
	 * @param key
	 * specify the match key value
	 * @return
	 * the detect result
	 */
	public boolean isDestinationPlatformLockKey(String key){
		return isDestinationPlatformLockKey("getprop ro.product.model", key);
	}
	
	/**
	 * detect the device's lock key if match the specified key with the specified command
	 * @param command
	 * specify the command that to get lock key
	 * @param key
	 * specify the match key value
	 * @return
	 * the detect result
	 */
	public boolean isDestinationPlatformLockKey(String command,String key){
		boolean isMatchLockKey=false;
		WisShellCommandHelper mShellCommandHelper = new WisShellCommandHelper();
		ArrayList<String> mLockString = mShellCommandHelper.exec(command);
		if (mLockString.size() > 0 && mLockString.get(0).equals(key)) {
			isMatchLockKey=true;
		}
		return isMatchLockKey;
	}
	
	/**
	 * push the files under assets to device, will overwrite if the destination file exist
	 * @param assetsFolder
	 * assets folder path if you multi structure, or just set it to null.
	 * @param assetsFileName
	 * the file name of push file
	 * @param desPath
	 * the destination path of device
	 */
	public void pushAssetsFileToDevice(String assetsFolder,String assetsFileName,String desPath) {
		pushAssetsFileToDevice(assetsFolder, assetsFileName, desPath, true);
	}
	
	/**
	 * push the files under assets to device
	 * @param assetsFolder
	 * assets folder path if you multi structure, or just set it to null.
	 * @param assetsFileName
	 * the file name of push file
	 * @param desPath
	 * the destination path of device
	 * @param overwriteIfExist
	 * whether to overwrite if the destination file exist
	 */
	public void pushAssetsFileToDevice(String assetsFolder,String assetsFileName,String desPath,boolean overwriteIfExist) {
		File mDesFile = new File(String.format("%1$s/%2$s", desPath, assetsFileName));
		if (!overwriteIfExist && mDesFile.exists()) {
			return;
		}else {
			try {
				if (overwriteIfExist && mDesFile.exists()) {
					mDesFile.delete();
				}
				
				mDesFile.createNewFile();
				mDesFile.setExecutable(true, false);
				mDesFile.setWritable(true, false);
				mDesFile.setReadable(true, false);
				
				InputStream mInputStream = context.getAssets().open(assetsFolder == null ? assetsFileName: assetsFolder+File.separator+assetsFileName);
				OutputStream mOutputStream = new FileOutputStream(mDesFile);
				byte[] result = new byte[1024];
				do {
					int readSize = mInputStream.read(result);
					if (readSize == -1) {
						break;
					}
					mOutputStream.write(result, 0, readSize);
				} while (true);
				mInputStream.close();
				mOutputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Generate the log file with content
	 * The file will be truncated if it exists, and created if it doesn't exist.
	 * @param path
	 * The path of log file
	 * @param content
	 * file content to write
	 * @param append
	 * If append is true and the file already exists, it will be appended to; otherwise it will be truncated. 
	 */
	public void generateLogFile(String path,String content,boolean append){
		File mLogFile=new File(path);
		if (mLogFile != null) {
			if (!mLogFile.getParentFile().exists()) {
				mLogFile.getParentFile().mkdirs();
			}
			try {
				FileOutputStream outStream = new FileOutputStream(path,append);
				outStream.write(content.getBytes());
				outStream.flush();
				outStream.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * To delete the specified file or folder
	 * @param file
	 * the file which need to delete
	 */
	public void deleteFile(File file){
		if (file != null && file.exists()) {
			File[] fileList = file.listFiles();
			if (fileList != null && fileList.length > 0) {
				for(File tempFile:fileList){
					if (tempFile.isDirectory()) {
						deleteFile(tempFile);
					}else {
						File newFile = new File(tempFile.getAbsolutePath() + System.currentTimeMillis());
						tempFile.renameTo(newFile);
						newFile.delete();
					}
				}
			}
			File newFile = new File(file.getAbsolutePath() + System.currentTimeMillis());
			file.renameTo(newFile);
			newFile.delete();
		}
	}
	
	/**
	 * Set screen brightness, this method only valid for current Activity.
	 * @param iValue
	 * The screen backlight brightness between 0 and 255.
	 */
	public void setBrightness(int brightnessValue) {
		if (context instanceof Activity) {
			Activity curActivity = (Activity)context;
			Settings.System.putString(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, String.valueOf(brightnessValue));
			WindowManager.LayoutParams lp = curActivity.getWindow().getAttributes();
			Float tmpFloat = (float) brightnessValue / 255;
			if (tmpFloat < 0.1f) {
				tmpFloat = 0.1f;
			}
			lp.screenBrightness = tmpFloat;
			curActivity.getWindow().setAttributes(lp);
		}
	}
	
	/**
	 * Get current screen brightness value, the value is between 0 and 255.
	 * @return
	 * Current screen brightness value, default value is -1 and if any exceptions has happens.
	 */
	public int getCurrentBrightness(){
		int brightness = -1;
		try {
			if (context instanceof Activity) {
				brightness = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
			}
		} catch (SettingNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return brightness;
	}
	
	/**
	 * Get the available RAM size, the unit is MB
	 * @return Get the available RAM size, the unit is MB
	 */
	public int getFreeRAM(){
		ActivityManager manger = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo info = new MemoryInfo();
		manger.getMemoryInfo(info);
		return (int)(info.availMem/(1024 * 1024));
	}
	
	/**
	 * Get the total RAM size, the unit is MB
	 * @return Get the total RAM size, the unit is MB
	 */
	public int getTotalRAM(){
		int mTotalRam = 0;
		String line = "";
		try {
			FileReader mFileReader = new FileReader("/proc/meminfo");
			BufferedReader mReader = new BufferedReader(mFileReader);
			while((line = mReader.readLine())!=null){
				break;
			}
			mReader.close();
			mFileReader.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		if (line.length() > 0) {
			String[] values = line.split(" ");
			int index = 0;
			for(String key : values){
				if (key.trim().length() > 0) {
					index ++;
					if (index == 2) {
						mTotalRam = Integer.parseInt(key.trim())/1024;
						break;
					}
				}
			}
		}
		return mTotalRam;
	}
	
	/**
	 * To simulate a Key action
	 * @param keyCode  The keyCode of Key
	 */
	public void simulateKeyOperate(int keyCode){
		simulateKeyOperate(keyCode, 0);
	}
	
	/**
	 * To simulate a Key action, and you can specified the wait time in milliseconds
	 * @param keyCode  The keyCode of Key
	 * @param waitTime Sleep time in milliseconds.
	 */
	public void simulateKeyOperate(final int keyCode,final int waitTime){
		new Thread(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				try {
					if (waitTime > 0) {
						Thread.sleep(waitTime);
					}
					Instrumentation action = new Instrumentation();
					action.sendKeyDownUpSync(keyCode);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
			
		}.start();
	}
	
	/**
	 * To generate the log during testing.
	 * @param remark
	 */
	public void writeTempHtmlLog(String remark) {
		Intent intent = new Intent();
		intent.setAction(WisWCISCommonConst.ACTION_WRITE_TEMP_HTML_LOG);
		intent.putExtra("data", remark);
		context.sendBroadcast(intent);
	}
	
	/**
	 * Reserved
	 * @author dragon
	 * To listen the selected result for Manual select dialog
	 */
	public abstract interface onManualSelectedListener{
		/**
		 * This method will be invoked when a button in the dialog is clicked.
		 * @param dialog
		 * The dialog that received the click.
		 * @param which
		 * The button that was clicked (DialogInterface.BUTTON_POSITIVE or DialogInterface.BUTTON_NEGATIVE)
		 */
		public abstract void onManualSelected(DialogInterface dialog, int which);
	}
}
