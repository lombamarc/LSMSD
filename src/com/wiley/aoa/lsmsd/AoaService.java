package com.wiley.aoa.lsmsd;

import java.io.IOException;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsMessage;

import com.wiley.wroxaccessories.USBConnection12;
import com.wiley.wroxaccessories.WroxAccessory;

public class AoaService extends Service {
	private final IBinder mBinder = new AoaBinder();
	private WroxAccessory mAccessory;
	private USBConnection12 mConnection;
	public class AoaBinder extends Binder {
		AoaService getService() {
			return AoaService.this;
		}
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.provider.Telephony.SMS_RECEIVED");
		registerReceiver(smsReceiver, filter);
		if (mAccessory == null) 
			mAccessory = new WroxAccessory(this);
		UsbManager manager = (UsbManager) getSystemService(USB_SERVICE);
		if (mConnection == null) 
			mConnection = new USBConnection12(manager);
		try {
			mAccessory.connect(WroxAccessory.USB_ACCESSORY_12, mConnection);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(smsReceiver);
		try {
			mAccessory.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	private BroadcastReceiver smsReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle pudsBundle = intent.getExtras();
			Object[] pdus = (Object[]) pudsBundle.get("pdus");
			SmsMessage messages = SmsMessage.createFromPdu((byte[]) pdus[0]);
			String sms = messages.getMessageBody();
			try {
				mAccessory.publish("sms", sms.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};
}
