package com.wistron.carbon8_usbhost;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.util.Log;

public class ListenUsbStatus extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		if (arg1.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
			// Toast.makeText(USB.this, "1", 2000).show();
			Log.i("Tag---", "carbon8_sdcard is mounted*");
		} else if (arg1.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
			// Toast.makeText(USB.this, "2", 2000).show();
			Log.i("Tag---", "carbon8_sdcard is unmounted**");
		}
	}

}
