package com.hazardalert;

import java.io.IOException;
import java.util.Date;

import android.content.Context;
import android.content.Intent;

import com.appspot.hazard_alert.alertendpoint.model.AlertTransport;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.google.android.gcm.GCMBaseIntentService;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hazardalert.common.Assert;

/**
 * IntentService responsible for handling GCM messages.
 * 
 * "ACT NOW AS SECONDS CAN SAVE LIVES!" -
 * NOAA-NWS-ALERTS-FL124CD5966EA8.TornadoWarning
 * .124CD5967F74FL.MLBTORMLB.8332360c041ebd86b9d65c863919d2b7
 */
/*
 * TODO 2014.2.22 GCMBaseIntentService is deprecated but I don't see a compelling reason to migrate.
 * GCMBaseIntentService wraps callbacks in a WakeLock
 * http://stackoverflow.com/questions/16619097/problems-with-android-gcm
 * http://code.google.com/p/gcm/source/browse/gcm-client-deprecated/src/com/google/android/gcm/GCMBaseIntentService.java?r=7f647288103bac2c5552af881f0e217c5b95d78a
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
		startService(new Intent(this, OnUpdateSubscription.class)); //attempt to restart gcm
		//no need to unregister old gcm on server, when it sends a message it should be a NotRegistered error
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		Log.v();
		HazardAlert.setPreference(getApplicationContext(), "lastGCM", new Date().getTime());
		for (long retryInterval = C.ONE_SECOND_MS;; retryInterval *= 2) {
			AlertTransport alert = null;
			try {
				alert = new AlertAPI().alertFind(intent.getStringExtra("fullName"));
				Database db = Database.getInstance(context);
				db.insertAlert(context, alert);
				trackDeliveryTiming(context, intent);
				db.deleteExpired();
				return;
			}
			catch (InvalidProtocolBufferException dbE) {
				HazardAlert.logException(context, dbE);
				return; // give up - local DB isn't going to change it's mind
			}
			catch (IOException e) {
				// retry on network related errors
				if (retryInterval > C.ONE_HOUR_MS) {
					throw new RuntimeException(e); // TODO handle with a pending request?
				}
				try {
					Thread.sleep(retryInterval);
				}
				catch (InterruptedException e1) {
					HazardAlert.logException(context, e1);
				}
			}
		}
	}

	private void trackDeliveryTiming(Context ctx, Intent intent) {
		try {
			Tracker easyTracker = EasyTracker.getInstance(this);
			String timeSentString = intent.getStringExtra("timeSent");
			new Assert(null != timeSentString);
			long timeSent = Long.parseLong(timeSentString);
			new Assert(0 != timeSent);
			new Assert(timeSent > 0);
			long timeNow = new AlertAPI().serverTime();
			new Assert(timeNow > timeSent);
			easyTracker.send(MapBuilder.createTiming("gcm", // Timing category (required)
														timeNow - timeSent, // Timing interval in milliseconds (required)
														"pushAlert", // Timing name
														"label") // Timing label
										.build());
			Log.v("" + (timeNow - timeSent));
		}
		catch (Exception e) {
			HazardAlert.logException(ctx, e);
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
