package com.hazardalert;

import android.app.IntentService;
import android.content.Intent;

public class OnRecenterSubscription extends IntentService {
	public OnRecenterSubscription() {
		super("OnRecenterSubscription");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		startService(new Intent(this, OnUpdateSubscription.class));
	}
}
