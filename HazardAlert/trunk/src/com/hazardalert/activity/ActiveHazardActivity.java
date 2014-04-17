package com.hazardalert.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.hazardalert.ActiveHazardFragment;
import com.hazardalert.DataManagerFragment;
import com.hazardalert.Log;

public class ActiveHazardActivity extends HazardAlertFragmentActivity {
	private DataManagerFragment dataFragment = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v();
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
}
