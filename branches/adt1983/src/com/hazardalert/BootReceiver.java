package com.hazardalert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v();
		HazardAlert.setPreference(context, OnStart.SP_KEY_RUNNING, false);
		Intent onStart = new Intent(context, OnStart.class);
		context.startService(onStart);
	}
}
