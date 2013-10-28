package com.hazardalert;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.google.publicalerts.cap.Info;
import com.google.publicalerts.cap.Info.Certainty;
import com.google.publicalerts.cap.Info.Severity;
import com.google.publicalerts.cap.Info.Urgency;

public class OnHazardVisible extends IntentService {
	private static int mId = 1;

	public OnHazardVisible() {
		super("OnHazardVisible");
	}

	//TODO sometimes showing up in HazardDetail with the wrong Alert, notifications getting overwritten?
	@Override
	protected void onHandleIntent(Intent intent) {
		final Context context = this.getApplicationContext();
		String id = intent.getStringExtra("id");
		Database db = Database.getInstance(context);
		Hazard h = db.safeGetByHazardId(id);
		doTaskbar(context, h);
	}

	private void doTaskbar(Context context, Hazard h) {
		final boolean allowNotif = HazardAlert.getPreferenceBoolean(context, C.PREF_NOTIF_ALLOW);
		final boolean allowNotifSound = HazardAlert.getPreferenceBoolean(context, C.PREF_NOTIF_SOUND_ALLOW);
		if (!allowNotif) {
			return;
		}
		final NotificationManager mNM = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Info info0 = h.getAlert().getInfo(0);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_hazardalert)
																					.setAutoCancel(true)
																					.setContentTitle(info0.getEvent())
																					.setContentText("Sev: " + info0.getSeverity()
																							+ " Urg: " + info0.getUrgency());
		builder.setPriority(NotificationCompat.PRIORITY_HIGH);
		if (h.exceedsThreshold(Urgency.FUTURE, Severity.MODERATE, Certainty.POSSIBLE) && allowNotifSound) {
			builder.setDefaults(Notification.DEFAULT_SOUND);
		}
		Intent resultIntent = new Intent(context, HazardDetail.class);
		resultIntent.putExtra("id", h.getId());
		resultIntent.setData(new Uri.Builder().scheme("data").appendQueryParameter("unique", h.getId()).build()); // http://stackoverflow.com/questions/12968280/android-multiple-notifications-and-with-multiple-intents
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		stackBuilder.addParentStack(HazardDetail.class);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(pendingIntent);
		mNM.notify(mId++, builder.build());
	}
}
