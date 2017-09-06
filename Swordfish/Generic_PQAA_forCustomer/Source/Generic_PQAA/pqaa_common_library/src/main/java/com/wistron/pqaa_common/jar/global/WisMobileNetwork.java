package com.wistron.pqaa_common.jar.global;

import android.content.Context;
import android.net.ConnectivityManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class WisMobileNetwork {
	private Context context;
	private ConnectivityManager mConnectivityManager;

	/**
	 * WisMobileNetwork constructed function
	 * @param context
	 * context
	 */
	public WisMobileNetwork(Context context) {
		super();
		this.context = context;
		initial();
	}

	private void initial() {
		// TODO Auto-generated method stub
		mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	}
	
	/**
	 * Sets the persisted value for enabling/disabling Mobile data.
	 * @param enable Whether the user wants the mobile data connection used or not
	 */
	public void setMobileDataEnabled(boolean enable){
		// reflect 
		try {
			Class<?> managerClass = Class.forName(mConnectivityManager.getClass().getName());
			Field iConnectivityManagerField = managerClass.getDeclaredField("mService");
			iConnectivityManagerField.setAccessible(true);
			Object iConnectivityManager = iConnectivityManagerField.get(mConnectivityManager);
			Class<?> iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
			Method setMobileDataEnabledMethod;
			Method[] methods = iConnectivityManagerClass.getDeclaredMethods();
			for(Method method : methods){
				if (method.getName().equals("setMobileDataEnabled")) {
					Class<?>[] parameterTypes = method.getParameterTypes();
					if (parameterTypes.length == 1) {
//						setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.class);
						setMobileDataEnabledMethod = method;
						setMobileDataEnabledMethod.setAccessible(true);
						setMobileDataEnabledMethod.invoke(iConnectivityManager, enable);
						break;
					}else if (parameterTypes.length == 2) {
//						setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", String.class, boolean.class);
						setMobileDataEnabledMethod = method;
						setMobileDataEnabledMethod.setAccessible(true);
						setMobileDataEnabledMethod.invoke(iConnectivityManager, context.getPackageName(), enable);
						break;
					}
				}
				
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets the value of the setting for enabling Mobile data.
	 * @return Whether mobile data is enabled.
	 * 
	 * <p>This method requires the call to hold the permission
	 * {@link android.Manifest.permission#ACCESS_NETWORK_STATE}.
	 */
	public boolean getMobileDataEnabled(){
		// reflect 
		try {
			Class<?> managerClass = Class.forName(mConnectivityManager.getClass().getName());
			Field iConnectivityManagerField = managerClass.getDeclaredField("mService");
			iConnectivityManagerField.setAccessible(true);
			Object iConnectivityManager = iConnectivityManagerField.get(mConnectivityManager);
			Class<?> iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
			Method getMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("getMobileDataEnabled");
			getMobileDataEnabledMethod.setAccessible(true);
			
			return (Boolean)getMobileDataEnabledMethod.invoke(iConnectivityManager);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return false;
	}
	
}
