package com.hazardalert;

import java.io.IOException;
import java.util.Date;

import android.content.Context;
import android.content.Intent;

import com.google.android.gcm.GCMBaseIntentService;

/**
 * IntentService responsible for handling GCM messages.
 * 
 * "ACT NOW AS SECONDS CAN SAVE LIVES!" -
 * NOAA-NWS-ALERTS-FL124CD5966EA8.TornadoWarning
 * .124CD5967F74FL.MLBTORMLB.8332360c041ebd86b9d65c863919d2b7
 */
public class GCMIntentService extends GCMBaseIntentService {
	public GCMIntentService() {
		super(C.SENDER_ID);
	}

	@Override
	public void onStart(Intent intent, int flags) {
		Log.v();
		super.onStart(intent, flags);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v();
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.v();
	}

	@Override
	public void onDestroy() {
		Log.v();
		super.onDestroy();
	}

	@Override
	protected void onRegistered(Context context, String registrationId) {
		Log.v("Device registered: regId = " + registrationId);
	}

	@Override
	protected void onUnregistered(Context context, String registrationId) {
		Log.v("Device unregistered: regId = " + registrationId);
		startService(new Intent(this, OnRegisterGCM.class)); //attempt to restart gcm
		//no need to unregister old gcm on server, when it sends a message it should be a NotRegistered error
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		Log.v();
		HazardAlert.setPreference(getApplicationContext(), "lastGCM", new Date().getTime());
		for (long retryInterval = 100;; retryInterval *= 2) {
			try {
				com.google.publicalerts.cap.Alert alert = new AlertAPI().alertFind(intent.getStringExtra("fullName"));
				Database db = Database.getInstance(context);
				db.insertAlert(context, alert);
				db.deleteExpired();
				return;
			}
			catch (IOException e) {
				if (retryInterval > C.ONE_HOUR_MS) {
					throw new RuntimeException(e);
				}
				try {
					Thread.sleep(retryInterval);
				}
				catch (InterruptedException e1) {
					//
				}
			}
		}
	}

	@Override
	protected void onDeletedMessages(Context context, int total) {
		Log.v();
	}

	@Override
	public void onError(Context context, String errorId) {
		Log.v(errorId);
	}

	@Override
	protected boolean onRecoverableError(Context context, String errorId) {
		Log.v(errorId);
		return super.onRecoverableError(context, errorId);
	}
}
