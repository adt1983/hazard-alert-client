package com.hazardalert;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.appspot.hazard_alert.alertendpoint.model.AlertTransport;
import com.appspot.hazard_alert.alertendpoint.model.Subscription;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.hazardalert.common.AlertFilter;
import com.hazardalert.common.Assert;
import com.hazardalert.common.Bounds;
import com.hazardalert.common.Point;

/*
 * Establish/Verify connection with server. Called upon application start, periodic heartbeats,
 * and upon establishing a network connection
 */
public class OnUpdateSubscription extends IntentService {
	private static final int BACKOFF_START_MS = 10000;

	private static long backoff = BACKOFF_START_MS;

	private static boolean running = false;

	private String regId = null;

	public OnUpdateSubscription() {
		super("OnUpdateSubscription");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.v();
		final Context ctx = getApplicationContext();
		clearExpiredNotifications();
		if (!running) {
			backoff = BACKOFF_START_MS;
			running = true;
		}
		if (!U.isNetworkAvailable(ctx)) {
			// Log a message if we have been disconnected for "too" long?
			setupRetry(this);
			return;
		}
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(ctx);
		try {
			regId = gcm.register(C.SENDER_ID);
			HazardAlert.setPreference(ctx, C.SP_GCM_REG_ID, regId);
			Log.d("GCM Registration successful.");
		}
		catch (IOException e) {
			Log.e("GCM Registration failed. Retrying...", e);
			setupRetry(this);
			return;
		}
		if (null == regId) {
			Log.e("regId: null");
			setupRetry(this);
			return;
		}
		try {
			updateSubscription();
			running = false;
		}
		catch (Exception e) {
			setupRetry(this);
		}
	}

	private static void setupRetry(Context ctx) {
		Log.d();
		new Assert(running);
		backoff *= 2;
		Intent i = new Intent(ctx, OnUpdateSubscription.class);
		PendingIntent pi = PendingIntent.getService(ctx, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + backoff, pi);
	}

	public static void resetBackoff(Context ctx) {
		Log.d();
		if (!running)
			return;
		backoff = BACKOFF_START_MS;
		setupRetry(ctx.getApplicationContext());
	}

	public static boolean isRunning() {
		return running;
	}

	//FIXME: Server Subscription count != Install Base - need error checking?
	/*
	 * *#*#426#*#*
	 * https://productforums.google.com/forum/#!topic/nexus/fslYqYrULto%5B1-25-false%5D
	 */
	private void updateSubscription() {
		Log.d();
		final Context ctx = getApplicationContext();
		AlertAPI alertAPI = new AlertAPI();
		Point lastLocation = U.getLastLocation(ctx);
		Bounds bounds = new Bounds(lastLocation, C.SUB_RADIUS_KM);
		Database db = Database.getInstance(ctx);
		long l = HazardAlert.getPreference(ctx, C.SP_SUBSCRIPTION_ID, 0);
		Long subId = Long.valueOf(l);
		Long expires = new Date().getTime() + C.ONE_DAY_MS;
		Subscription s = null;
		try {
			if (0 != subId) {
				s = alertAPI.subscriptionGet(subId);
				if (null == s || null == s.getId()) {
					HazardAlert.logException(ctx, new Exception("Server unable to find subscription. id: " + subId));
				}
				else {
					if (0 != regId.compareTo(s.getGcm())) {
						HazardAlert.logException(ctx, new Exception("Client GCM: " + regId + "\nServer GCM: " + s.getGcm()));
					}
				}
			}
			if (s == null || null == s.getId()) {
				Log.v("Creating new subscription. {gcm: " + regId + "}");
				s = alertAPI.createSubscription(regId, bounds, expires);
				HazardAlert.setPreference(ctx, C.SP_SUBSCRIPTION_ID, s.getId());
				AlertFilter filter = new AlertFilter().setInclude(bounds);
				List<AlertTransport> existingAlerts = alertAPI.list(filter);
				db.insertAlerts(ctx, existingAlerts);
			}
			else {
				Log.v("Updating subscription. {gcm: " + regId + "}");
				s = new Subscription().setId(subId).setGcm(regId);
				List<AlertTransport> newAlerts = alertAPI.updateSubscription(s, bounds.toEnvelope());
				//alertAPI.updateExpires(s, expires);
				db.insertAlerts(ctx, newAlerts);
			}
			HazardAlert.setPreference(ctx, C.SP_SUBSCRIPTION_LAST_SYNC, new Date().getTime());
		}
		catch (IOException e) {
			Log.e("Unable to create/update subscription.", e);
			HazardAlert.logException(ctx, e);
			throw new RuntimeException(e);
		}
	}

	private void clearExpiredNotifications() {
		for (Hazard h : Database.getInstance(this).getActiveNotifications()) {
			if (h.isExpired()) {
				h.onExit(this);
			}
		}
	}
}
