package com.wistron.pqaa_common.jar.global;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class WisResultFile {
	
	/**
	 * write the result to result file.
	 * @param filePath
	 * result file path
	 * @param result
	 * the test result
	 */
	public void writeToResultFile(String filePath,String result){
		File mResultFile=new File(filePath);
		if (!mResultFile.getParentFile().exists()) {
			mResultFile.getParentFile().mkdirs();
		}
		
		try {
			FileOutputStream mStream = new FileOutputStream(mResultFile);
			mStream.write(result.getBytes());
			mStream.flush();
			mStream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * read the last test result from result file
	 * @param filePath
	 * the path of result file
	 * @return
	 * get the last test result
	 */
	public String readFromResultFile(String filePath){
		String mFileContent=null;
		try {
			BufferedReader mReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
			mFileContent = mReader.readLine();
			mReader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mFileContent;
	}
}
