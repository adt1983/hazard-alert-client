package com.hazardalert;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;
import android.preference.RingtonePreference;

public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		updateRingtoneSummaries();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		updateRingtoneSummaries(); // http://stackoverflow.com/questions/6725105/ringtonepreference-not-firing-onsharedpreferencechanged
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
		if (key.equals(C.PREF_NOTIF_ALLOW)) {
			if (!HazardAlert.getPreferenceBoolean(sp, C.PREF_NOTIF_ALLOW)) {
				CheckBoxPreference cbp = (CheckBoxPreference) findPreference(C.PREF_NOTIF_SOUND_ALLOW);
				cbp.setChecked(false);
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause() {
		getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}

	private final String sounds[] = { C.PREF_NOTIF_SOUND_EXTREME, C.PREF_NOTIF_SOUND_SEVERE, C.PREF_NOTIF_SOUND_MODERATE,
			C.PREF_NOTIF_SOUND_MINOR, C.PREF_NOTIF_SOUND_UNKNOWN };

	private void updateRingtoneSummaries() {
		for (int i = 0; i < sounds.length; i++) {
			RingtonePreference rtp = (RingtonePreference) findPreference(sounds[i]);
			String uri = HazardAlert.getPreference(getActivity(), sounds[i], "");
			rtp.setSummary(parseRingtoneTitle(uri));
		}
	}

	private String parseRingtoneTitle(String ringtoneUri) {
		if (ringtoneUri.equals(""))
			return "None";
		Uri uri = Uri.parse(ringtoneUri);
		Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), uri);
		if (ringtone == null)
			return "None";
		return ringtone.getTitle(getActivity());
	}
}
