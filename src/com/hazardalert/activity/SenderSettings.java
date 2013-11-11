package com.hazardalert.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.hazardalert.R;
import com.hazardalert.fragment.SenderFilterFragment;

public class SenderSettings extends FragmentActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Display the fragment as the main content.
		this.setContentView(R.layout.activity_sender_settings);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.sender_settings_fragment_container, new SenderFilterFragment());
		ft.commit();
	}
}
