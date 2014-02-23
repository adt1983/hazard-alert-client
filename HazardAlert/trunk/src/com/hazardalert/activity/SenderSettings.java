package com.hazardalert.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.google.analytics.tracking.android.EasyTracker;
import com.hazardalert.R;
import com.hazardalert.fragment.SenderSettingsFragment;

public class SenderSettings extends FragmentActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Display the fragment as the main content.
		this.setContentView(R.layout.activity_sender_settings);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.sender_settings_fragment_container, new SenderSettingsFragment());
		ft.commit();
	}

	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);
	}
}