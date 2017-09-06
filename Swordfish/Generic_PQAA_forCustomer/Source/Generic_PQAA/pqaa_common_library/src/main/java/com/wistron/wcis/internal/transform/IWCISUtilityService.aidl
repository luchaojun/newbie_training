package com.wistron.wcis.internal.transform;

import java.util.Map;

interface IWCISUtilityService{
	Map readConfigFile(String path);
	List readGroupConfigFile(String path);
	void writeLog(int type,String path,String content);
	void deleteLog(String path);
	void eraseSDCard(String path);
}