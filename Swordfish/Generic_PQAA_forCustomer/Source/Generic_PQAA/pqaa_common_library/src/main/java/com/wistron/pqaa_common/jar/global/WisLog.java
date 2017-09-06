package com.wistron.pqaa_common.jar.global;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;

public class WisLog {
	private File mLogFile;

	/**
	 * Construct method with filePath
	 * @param filePath
	 * 		indicate the path of log file
	 * @throws IOException  
	 * 		can't create the specified file
	 */
	public WisLog(String filePath) throws IOException{
		setLogFilePath(filePath);
	}
	
	/**
	 * Construct method with filePath
	 * @param filePath
	 * 		indicate the path of log file
	 * @param deleteIfExist
	 * 		whether to delete the file if it exist.
	 * @throws IOException  
	 * 		can't create the specified file
	 */
	public WisLog(String filePath,boolean deleteIfExist) throws IOException{
		setLogFilePath(filePath,deleteIfExist);
	}
	
	
	/**
	 * reset the path of log file with filePath
	 * @param filePath
	 * 		indicate the path of log file
	 * @throws IOException  
	 * 		can't create the specified file
	 */
	public void setLogFilePath(String filePath) throws IOException{
		mLogFile=new File(filePath);
		File mFolder=mLogFile.getParentFile();
		if (!mFolder.exists()) {
			mFolder.mkdirs();
		}
		
		if (!mLogFile.exists()) {
			mLogFile.createNewFile();
			mLogFile.setWritable(true, false);
			mLogFile.setReadable(true, false);
		}
	}
	
	/**
	 * reset the path of log file with filePath
	 * @param filePath
	 * 		indicate the path of log file
	 * @param deleteIfExist
	 * 		whether to delete the file if it exist.
	 * @throws IOException  
	 * 		can't create the specified file
	 */
	public void setLogFilePath(String filePath,boolean deleteIfExist) throws IOException{
		mLogFile=new File(filePath);
		File mFolder=mLogFile.getParentFile();
		if (!mFolder.exists()) {
			mFolder.mkdirs();
		}
		
		if (deleteIfExist && mLogFile.exists()) {
			mLogFile.delete();
		}
		
		if (!mLogFile.exists()) {
			mLogFile.createNewFile();
			mLogFile.setWritable(true, false);
			mLogFile.setReadable(true, false);
		}
	}
	
	/**
	 * Return the log file path
	 * @return
	 * Return the log file path if log file is not null, otherwise return null
	 */
	public String getLogFilePath(){
		if (mLogFile != null) {
			return mLogFile.getAbsolutePath();
		}else {
			return null;
		}
	}
	
	/**
	 * write the specified content to log file with date-time tag
	 * @param content
	 * 		write content
	 * @param append   
	 * 		whether to append the content to the file
	 * @throws FileNotFoundException  
	 * 		can't find the log file
	 * @throws IOException
	 * 		
	 */
	public void write(String content,boolean append) throws FileNotFoundException,IOException{
		write(content, true, append);
	}
	
	/**
	 * write the specified content to log file
	 * @param content
	 * 		write content
	 * @param showTime
	 * 		show time tag in log file
	 * @param append
	 * 		whether to append the content to the file
	 * @throws FileNotFoundException
	 * 		can't find the log file
	 * @throws IOException
	 */
	public void write(String content,boolean showTime,boolean append) throws FileNotFoundException,IOException{
		if (mLogFile != null) {
			if (showTime) {
				Timestamp mTimestamp = new Timestamp(System.currentTimeMillis());
				String mTime = mTimestamp.toString();
				content = mTime + " ------> " + content;
			}
			content += "\n";
			FileOutputStream mOutputStream = new FileOutputStream(mLogFile, append);
			mOutputStream.write(content.getBytes());
			mOutputStream.flush();
			mOutputStream.close();
		}
	}
	
	/**
	 * delete the log file
	 */
	public void deleteLogFile(){
		if (mLogFile.exists()) {
			File newFile = new File(mLogFile.getAbsolutePath() + System.currentTimeMillis());
			mLogFile.renameTo(newFile);
			newFile.delete();
		}
	}
}
