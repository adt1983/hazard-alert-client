package com.hazardalert.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ListAdapter;

import com.google.analytics.tracking.android.EasyTracker;
import com.hazardalert.Language;
import com.hazardalert.R;
import com.hazardalert.common.AlertFilter;
import com.hazardalert.fragment.LanguageFilterListFragment;

public class LanguageFilter extends FilterAbstract {
	LanguageFilterListFragment fragment;

	public static void startForResult(Activity activity, AlertFilter filter, int requestCode) {
		startForResult(activity, LanguageFilter.class, filter, requestCode);
	}

	public void onOk(View view) {
		ListAdapter adapter = fragment.getListAdapter();
		filter.setLanguages(null);
		boolean filterNothing = true;
		for (int i = 0; i < adapter.getCount(); i++) {
			Language l = (Language) adapter.getItem(i);
			if (l.getSuppress()) {
				filterNothing = false;
				break; // need to explicitly set allowed languages
			}
		}
		if (!filterNothing) {
			for (int i = 0; i < adapter.getCount(); i++) {
				Language l = (Language) adapter.getItem(i);
				if (!l.getSuppress()) {
					filter.addLanguage(l.getLanguage());
				}
			}
		}
		returnFilter(filter);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Display the fragment as the main content.
		this.setContentView(R.layout.activity_language_filter);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		fragment = LanguageFilterListFragment.newInstance(filter);
		ft.replace(R.id.language_filter_fragment_container, fragment);
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