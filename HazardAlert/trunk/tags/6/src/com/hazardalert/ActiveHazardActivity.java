package com.hazardalert;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class ActiveHazardActivity extends FragmentActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Display the fragment as the main content.
		getSupportFragmentManager().beginTransaction().replace(android.R.id.content, new ActiveHazardFragment()).commit();
	}
}
