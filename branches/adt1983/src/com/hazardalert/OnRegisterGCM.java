package com.hazardalert;

import java.io.IOException;
import java.util.Date;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class OnRegisterGCM extends IntentService {
	private static final int MAX_ATTEMPTS = 5;
	private static final int BACKOFF_MILLI_SECONDS = 2000;
	private static final String ATTEMPTS = "attempts";

	public OnRegisterGCM() {
		super("OnRegisterGCM");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		final Context ctx = getApplicationContext();
		long attempts = intent.getLongExtra(ATTEMPTS, 0);
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(ctx);
		try {
			String regId = gcm.register(C.SENDER_ID);
			HazardAlert.setPreference(ctx, C.SP_GCM_REG_ID, regId);
			HazardAlert.setPreference(ctx, "lastGCM", new Date().getTime());
			startService(new Intent(this, OnUpdateSubscription.class));
			Log.d("GCM Registration successful.");
		}
		catch (IOException e) {
			if (attempts < MAX_ATTEMPTS) {
				//TODO
				Log.e("GCM Registration failed. Retrying...", e);
				Intent i = new Intent(this, OnRegisterGCM.class);
				i.putExtra(ATTEMPTS, attempts + 1);
				PendingIntent pi = PendingIntent.getService(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
				AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
				am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + BACKOFF_MILLI_SECONDS, pi);
			}
			else {
				throw new RuntimeException(e);
			}
		}
	}
}
