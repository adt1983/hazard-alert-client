package com.hazardalert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.hazardalert.common.Assert;

public class OnDeleteNotification extends BroadcastReceiver {
	private static final String ID_EXTRA = "com.hazardalert.OnDeleteNotification.id";

	public static Intent buildIntent(Context ctx, Hazard h) {
		Log.v();
		Intent intent = new Intent(ctx, OnDeleteNotification.class);
		intent.putExtra(ID_EXTRA, h.getId());
		new Assert(null != intent.getStringExtra(ID_EXTRA));
		intent.setData(new Uri.Builder().scheme("data").appendQueryParameter("unique", h.getId()).build()); // http://stackoverflow.com/questions/12968280/android-multiple-notifications-and-with-multiple-intents
		return intent;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v();
		String id = intent.getStringExtra(ID_EXTRA);
		new Assert(null != id);
		Database db = Database.getInstance(context.getApplicationContext());
		Hazard h = db.safeGetByHazardId(id);
		h.onShown(context);
	}
}
