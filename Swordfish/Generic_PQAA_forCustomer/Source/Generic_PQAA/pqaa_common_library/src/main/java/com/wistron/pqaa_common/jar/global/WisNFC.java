package com.wistron.pqaa_common.jar.global;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;

import java.lang.reflect.Method;

public class WisNFC {
	private Context context;
	private NfcAdapter mNfcAdapter;
	
	/**
	 * Initial WisNFC object
	 * <p> It need {@link #(android.permission.NFC)} permission
	 * @param context
	 */
	public WisNFC(Context context) {
		super();
		this.context = context;
		initial();
	}
	
	private void initial() {
		// TODO Auto-generated method stub
		mNfcAdapter = NfcAdapter.getDefaultAdapter(context);
	}
	
	/**
	 * Return ture if NFC adapter exists, else return false
	 * @return Return ture if NFC adapter exists, else return false
	 */
	public boolean isNFCAdapterExist(){
		return mNfcAdapter != null;
	}
	
	/**
	 * Check whether the device is supports NFC feature or not.
	 * @return Returns true if the devices supports the Device, else false.
	 */
	public boolean isSupportsNFC(){
		return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC);
	}
	
    /**
     * Return true if this NFC Adapter has any features enabled.
     * @return true if this NFC Adapter has any features enabled
     */
    public boolean isEnabled() {
        return mNfcAdapter.isEnabled();
    }
    
	/**
	 * Enable or Disable NFC hardware.
	 * <p> It need {@link #(android.permission.WRITE_SECURE_SETTINGS)} permission.
	 * <p> It need system permission to enable or disable NFC hardware.
	 * @param enable Whether the user wants to enable NFC or not
	 */
	public void setNFCEnabled(boolean enable){
		// reflect 
		try {
			Class<?> adapterClass = Class.forName(mNfcAdapter.getClass().getName());
			Method setNFCEnabledMethod = enable ? adapterClass.getDeclaredMethod("enable") : adapterClass.getDeclaredMethod("disable");
			setNFCEnabledMethod.setAccessible(true);
			setNFCEnabledMethod.invoke(mNfcAdapter);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
    /**
     * Enable NDEF message push over NFC while this Activity is in the foreground.
     *
     * <p>You must explicitly call this method every time the activity is
     * resumed, and you must call {@link #disableForegroundNdefPush} before
     * your activity completes {@link Activity#onPause}.
     *
     * <p>Strongly recommend to use the new {@link #setNdefPushMessage}
     * instead: it automatically hooks into your activity life-cycle,
     * so you do not need to call enable/disable in your onResume/onPause.
     *
     * <p>For NDEF push to function properly the other NFC device must
     * support either NFC Forum's SNEP (Simple Ndef Exchange Protocol), or
     * Android's "com.android.npp" (Ndef Push Protocol). This was optional
     * on Gingerbread level Android NFC devices, but SNEP is mandatory on
     * Ice-Cream-Sandwich and beyond.
     *
     * <p>This method must be called from the main thread.
     *
     * <p class="note">Requires the {@link android.Manifest.permission#NFC} permission.
     *
     * @param activity foreground activity
     * @param message a NDEF Message to push over NFC
     * @throws IllegalStateException if the activity is not currently in the foreground
     * @deprecated use {@link #setNdefPushMessage} instead
     */
    @Deprecated
    public void enableForegroundNdefPush(Activity activity, NdefMessage message) {
        mNfcAdapter.enableForegroundNdefPush(activity, message);
    }
    
    /**
     * Enable foreground dispatch to the given Activity.
     *
     * <p>This will give give priority to the foreground activity when
     * dispatching a discovered {@link Tag} to an application.
     *
     * <p>If any IntentFilters are provided to this method they are used to match dispatch Intents
     * for both the {@link NfcAdapter#ACTION_NDEF_DISCOVERED} and
     * {@link NfcAdapter#ACTION_TAG_DISCOVERED}. Since {@link NfcAdapter#ACTION_TECH_DISCOVERED}
     * relies on meta data outside of the IntentFilter matching for that dispatch Intent is handled
     * by passing in the tech lists separately. Each first level entry in the tech list represents
     * an array of technologies that must all be present to match. If any of the first level sets
     * match then the dispatch is routed through the given PendingIntent. In other words, the second
     * level is ANDed together and the first level entries are ORed together.
     *
     * <p>If you pass {@code null} for both the {@code filters} and {@code techLists} parameters
     * that acts a wild card and will cause the foreground activity to receive all tags via the
     * {@link NfcAdapter#ACTION_TAG_DISCOVERED} intent.
     *
     * <p>This method must be called from the main thread, and only when the activity is in the
     * foreground (resumed). Also, activities must call {@link #disableForegroundDispatch} before
     * the completion of their {@link Activity#onPause} callback to disable foreground dispatch
     * after it has been enabled.
     *
     * <p class="note">Requires the {@link android.Manifest.permission#NFC} permission.
     *
     * @param activity the Activity to dispatch to
     * @param intent the PendingIntent to start for the dispatch
     * @param filters the IntentFilters to override dispatching for, or null to always dispatch
     * @param techLists the tech lists used to perform matching for dispatching of the
     *      {@link NfcAdapter#ACTION_TECH_DISCOVERED} intent
     * @throws IllegalStateException if the Activity is not currently in the foreground
     */
    public void enableForegroundDispatch(Activity activity, PendingIntent intent,
            IntentFilter[] filters, String[][] techLists) {
        mNfcAdapter.enableForegroundDispatch(activity, intent, filters, techLists);
    }
    
    /**
     * Disable NDEF message push over P2P.
     *
     * <p>After calling {@link #enableForegroundNdefPush}, an activity
     * must call this method before its {@link Activity#onPause} callback
     * completes.
     *
     * <p>Strongly recommend to use the new {@link #setNdefPushMessage}
     * instead: it automatically hooks into your activity life-cycle,
     * so you do not need to call enable/disable in your onResume/onPause.
     *
     * <p>This method must be called from the main thread.
     *
     * <p class="note">Requires the {@link android.Manifest.permission#NFC} permission.
     *
     * @param activity the Foreground activity
     * @throws IllegalStateException if the Activity has already been paused
     * @deprecated use {@link #setNdefPushMessage} instead
     */
    @Deprecated
    public void disableForegroundNdefPush(Activity activity) {
        mNfcAdapter.disableForegroundNdefPush(activity);
    }
    
    /**
     * Disable foreground dispatch to the given activity.
     *
     * <p>After calling {@link #enableForegroundDispatch}, an activity
     * must call this method before its {@link Activity#onPause} callback
     * completes.
     *
     * <p>This method must be called from the main thread.
     *
     * <p class="note">Requires the {@link android.Manifest.permission#NFC} permission.
     *
     * @param activity the Activity to disable dispatch to
     * @throws IllegalStateException if the Activity has already been paused
     */
    public void disableForegroundDispatch(Activity activity) {
        mNfcAdapter.disableForegroundDispatch(activity);
    }
}
