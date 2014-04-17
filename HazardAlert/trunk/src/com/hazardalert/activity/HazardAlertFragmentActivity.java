package com.hazardalert.activity;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.hazardalert.HazardAlert;
import com.hazardalert.Log;
import com.hazardalert.R;

public abstract class HazardAlertFragmentActivity extends FragmentActivity {
	@Override
	public void onStart() {
		super.onStart();
		Log.v();
		EasyTracker.getInstance(this).activityStart(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.v();
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.v();
		EasyTracker.getInstance(this).activityStop(this);
	}

	public boolean onHelp(MenuItem view) {
		Log.v();
		onHelpTrack();
		Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "roanokesoftware@gmail.com", null));
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Help with Hazard Alert (" + this.getLocalClassName().replace("activity.", "") + ")");
		this.startActivity(Intent.createChooser(emailIntent, "Get Help with Hazard Alert..."));
		return true;
	}

	private void onHelpTrack() {
		try {
			EasyTracker easyTracker = EasyTracker.getInstance(this);
			easyTracker.send(MapBuilder.createEvent("ui_action", // Event category (required)
													"help_button", // Event action (required)
													this.getLocalClassName(), // Event label
													null) // Event value
										.build());
		}
		catch (Exception e) {
			HazardAlert.logException(this, e);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d();
		getMenuInflater().inflate(R.menu.help_only, menu);
		return true;
	}
}
