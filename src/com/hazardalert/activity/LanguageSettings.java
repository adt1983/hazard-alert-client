package com.hazardalert.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.hazardalert.R;
import com.hazardalert.fragment.LanguageSettingsListFragment;

public class LanguageSettings extends HazardAlertFragmentActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Display the fragment as the main content.
		this.setContentView(R.layout.activity_language_settings);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.language_settings_fragment_container, new LanguageSettingsListFragment());
		ft.commit();
	}
}
