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
import com.hazardalert.common.Bounds;
import com.hazardalert.common.CommonUtil;
import com.hazardalert.common.Point;
import com.vividsolutions.jts.geom.Envelope;

/*
 * Establish/Verify connection with server. Called upon application start, periodic heartbeats,
 * and upon establishing a network connection
 */
public class OnUpdateSubscription extends IntentService {
	private static final int MAX_ATTEMPTS = 5;

	private static final int BACKOFF_MILLI_SECONDS = 10000;

	private static final String BACKOFF = "backoff";

	private String regId = null;

	public OnUpdateSubscription() {
		super("OnUpdateSubscription");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.v();
		final Context ctx = getApplicationContext();
		if (!U.isNetworkAvailable(ctx)) {
			// Log a message if we have been disconnected for "too" long?
			return;
		}
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(ctx);
		try {
			regId = gcm.register(C.SENDER_ID);
			HazardAlert.setPreference(ctx, C.SP_GCM_REG_ID, regId);
			HazardAlert.setPreference(ctx, "lastGCM", new Date().getTime());
			Log.d("GCM Registration successful.");
		}
		catch (IOException e) {
			Log.e("GCM Registration failed. Retrying...", e);
			setupRetry(intent);
			return;
		}
		if (null == regId) {
			Log.e("regId: null");
			setupRetry(intent);
			return;
		}
		try {
			updateSubscription();
		}
		catch (Exception e) {
			setupRetry(intent);
		}
	}

	private void setupRetry(Intent intent) {
		long backoff = intent.getLongExtra(BACKOFF, BACKOFF_MILLI_SECONDS);
		backoff *= 2;
		Intent i = new Intent(this, OnUpdateSubscription.class);
		i.putExtra(BACKOFF, backoff);
		PendingIntent pi = PendingIntent.getService(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + backoff, pi);
	}

	private void updateSubscription() {
		final Context ctx = getApplicationContext();
		AlertAPI alertAPI = new AlertAPI();
		Point lastLocation = U.getLastLocation(ctx);
		Envelope env = CommonUtil.getBoundingBox(lastLocation.toCoordinate(), C.SUB_RADIUS_KM);
		Database db = Database.getInstance(ctx);
		long l = HazardAlert.getPreference(ctx, C.SP_SUBSCRIPTION_ID, 0);
		Long subId = Long.valueOf(l);
		Long expires = new Date().getTime() + C.ONE_DAY_MS;
		Subscription s = null;
		try {
			if (0 != subId) {
				s = alertAPI.subscriptionGet(subId);
			}
			if (s == null || null == s.getId()) {
				Log.v("Creating new subscription. {gcm: " + regId + "}");
				s = alertAPI.createSubscription(regId, new Bounds(env), expires);
				HazardAlert.setPreference(ctx, C.SP_SUBSCRIPTION_ID, s.getId());
				AlertFilter filter = new AlertFilter().setInclude(new Bounds(env));
				List<AlertTransport> existingAlerts = alertAPI.list(filter);
				db.insertAlerts(ctx, existingAlerts);
			}
			else {
				Log.v("Updating subscription. {gcm: " + regId + "}");
				s = new Subscription().setId(subId).setGcm(regId);
				List<AlertTransport> newAlerts = alertAPI.updateSubscription(s, env);
				alertAPI.updateExpires(s, expires);
				db.insertAlerts(ctx, newAlerts);
			}
		}
		catch (IOException e) {
			Log.e("Unable to create/update subscription.", e);
			throw new RuntimeException();
		}
	}
}
/*
 * // Restart GCM if we haven't heard from it in awhile
	private void ensureGCM() {
		final long MAX_GCM_IDLE = 3 * 60 * 60 * 1000; // 3 hours
		if (MAX_GCM_IDLE < new Date().getTime() - HazardAlert.getPreference(getApplicationContext(), "lastGCM", 0)) {
			HazardAlert.setPreference(getApplicationContext(), "lastGCM", new Date().getTime());
			GCMRegistrar.unregister(getApplicationContext());
			Intent registerGCM = new Intent(getApplicationContext(), OnRegisterGCM.class);
			startService(registerGCM);
		}
	}
	*/
