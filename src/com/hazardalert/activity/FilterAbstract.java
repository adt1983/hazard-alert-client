package com.hazardalert.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.hazardalert.common.AlertFilter;

public abstract class FilterAbstract extends HazardAlertFragmentActivity {
	private final static String KEY_FILTER = "KEY_FILTER";

	protected AlertFilter filter;

	public static AlertFilter parseResult(Intent intent) {
		return (AlertFilter) intent.getExtras().getSerializable(KEY_FILTER);
	}

	public static void startForResult(Activity activity, Class<? extends FilterAbstract> clazz, AlertFilter filter, int requestCode) {
		Intent intent = new Intent(activity, clazz);
		Bundle b = new Bundle();
		b.putSerializable(KEY_FILTER, filter);
		intent.putExtras(b);
		activity.startActivityForResult(intent, requestCode);
	}

	public void returnFilter(AlertFilter filter) {
		Intent intent = new Intent();
		Bundle b = new Bundle();
		b.putSerializable(KEY_FILTER, filter);
		intent.putExtras(b);
		setResult(RESULT_OK, intent);
		finish();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		filter = (AlertFilter) getIntent().getSerializableExtra(KEY_FILTER);
		if (null == filter) {
			filter = new AlertFilter();
		}
	}
}
