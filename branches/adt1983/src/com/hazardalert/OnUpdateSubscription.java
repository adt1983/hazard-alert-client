package com.hazardalert;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.appspot.hazard_alert.alertendpoint.model.Subscription;
import com.google.publicalerts.cap.Alert;
import com.hazardalert.common.AlertFilter;
import com.hazardalert.common.Bounds;
import com.hazardalert.common.CommonUtil;
import com.hazardalert.common.Point;
import com.vividsolutions.jts.geom.Envelope;

public class OnUpdateSubscription extends IntentService {
	private static final int BACKOFF_MILLI_SECONDS = 2000;

	public OnUpdateSubscription() {
		super("OnUpdateSubscription");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.v();
		final Context ctx = getApplicationContext();
		String regId = HazardAlert.getPreference(ctx, C.SP_GCM_REG_ID, null);
		if (null == regId) {
			setupRetry(intent);
			return;
		}
		registerOnServer(regId);
	}

	private void setupRetry(Intent intent) {
		Intent i = new Intent(this, OnUpdateSubscription.class);
		//i.putExtra(ATTEMPTS, attempts + 1);
		PendingIntent pi = PendingIntent.getService(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + BACKOFF_MILLI_SECONDS, pi);
	}

	private void registerOnServer(String regId) {
		final Context ctx = getApplicationContext();
		AlertAPI alertAPI = new AlertAPI();
		Point lastLocation = Util.getLastLocation(ctx);
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
				List<Alert> existingAlerts = alertAPI.list(filter);
				db.insertAlerts(ctx, existingAlerts);
			}
			else {
				Log.v("Updating subscription. {gcm: " + regId + "}");
				s = new Subscription().setId(subId).setGcm(regId);
				List<Alert> newAlerts = alertAPI.updateSubscription(s, env);
				alertAPI.updateExpires(s, expires);
				db.insertAlerts(ctx, newAlerts);
			}
		}
		catch (IOException e) {
			Log.e("Unable to create/update subscription.", e);
		}
	}
}
