package com.hazardalert;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.google.analytics.tracking.android.EasyTracker;

public class ActiveHazardActivity extends FragmentActivity {
	private DataManagerFragment dataFragment = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Display the fragment as the main content.
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		dataFragment = (DataManagerFragment) getSupportFragmentManager().findFragmentByTag(DataManagerFragment.TAG);
		if (dataFragment == null) {
			dataFragment = new DataManagerFragment();
			ft.add(dataFragment, DataManagerFragment.TAG);
		}
		ActiveHazardFragment ahf = new ActiveHazardFragment();
		ahf.setDataManager(dataFragment);
		ft.replace(android.R.id.content, ahf);
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
