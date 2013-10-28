package com.hazardalert;

import org.acra.annotation.ReportsCrashes;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.os.StrictMode;
import android.preference.PreferenceManager;

import com.hazardalert.common.Assert;

@ReportsCrashes(formKey = "dEZaUXljeGhMdEU4c01UdThOVmxWRlE6MQ")
public class HazardAlert extends Application {
	@SuppressLint("NewApi")
	@Override
	public void onCreate() {
		Log.v();
		super.onCreate();
		initializePreferences();
		if (!isDebug()) {
			org.acra.ACRA.init(this);
		}
		else {
			new Assert(android.os.Build.VERSION.SDK_INT > 9);
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
			setPreference(getApplicationContext(), OnStart.SP_KEY_RUNNING, false);
		}
		Intent onStart = new Intent(getApplicationContext(), OnStart.class);
		startService(onStart);
	}

	private void initializePreferences() {
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
	}

	@Override
	public void onTerminate() {}

	private boolean isDebug() {
		return (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) > 0;
	}

	public static boolean containsPreference(Context context, String key) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		return sp.contains(key);
	}

	public static boolean getPreference(Context context, String key, boolean defaultValue) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		return sp.getBoolean(key, defaultValue);
	}

	public static boolean getPreferenceBoolean(Context context, String key) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		return getPreferenceBoolean(sp, key);
	}

	public static boolean getPreferenceBoolean(SharedPreferences sp, String key) {
		if (!sp.contains(key)) {
			throw new RuntimeException(key);
		}
		return sp.getBoolean(key, /*ignored*/true);
	}

	public static void setPreference(Context context, String key, boolean value) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = sp.edit();
		editor.putBoolean(key, value);
		if (!editor.commit()) {
			throw new RuntimeException(key);
		}
	}

	public static long getPreference(Context context, String key, long defaultValue) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		return sp.getLong(key, defaultValue);
	}

	public static void setPreference(Context context, String key, long value) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = sp.edit();
		editor.putLong(key, value);
		if (!editor.commit()) {
			throw new RuntimeException(key);
		}
	}

	public static float getPreference(Context context, String key, float defaultValue) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		return sp.getFloat(key, defaultValue);
	}

	public static void setPreference(Context context, String key, float value) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = sp.edit();
		editor.putFloat(key, value);
		if (!editor.commit()) {
			throw new RuntimeException(key);
		}
	}

	public static String getPreference(Context context, String key, String defaultValue) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		return sp.getString(key, defaultValue);
	}

	public static void setPreference(Context context, String key, String value) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = sp.edit();
		editor.putString(key, value);
		if (!editor.commit()) {
			throw new RuntimeException(key);
		}
	}
}
