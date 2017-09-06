package com.wistron.generic.autotest.internal.wifi;

interface IWirelessService{
	boolean isWifiOn();
	boolean isWifiCanUse();
	String getMacAddress();
	void disCurrentConnect();
	void openWifi();
	int getWifiConnectState();
	boolean isNeedReConnectAp();
	void connectAp(String ssid,int security,String password);
	int getWifiEnableState();
	String getConnectApSSID();
	void ping(String address,int count,int interval);
	int getWifiRssi();
	void closeWifi();
}