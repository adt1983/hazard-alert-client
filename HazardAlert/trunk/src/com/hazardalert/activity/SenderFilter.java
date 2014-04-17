package com.hazardalert.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ListAdapter;

import com.hazardalert.R;
import com.hazardalert.Sender;
import com.hazardalert.common.AlertFilter;
import com.hazardalert.fragment.SenderFilterListFragment;

public class SenderFilter extends FilterAbstract {
	SenderFilterListFragment fragment;

	public static void startForResult(Activity activity, AlertFilter filter, int requestCode) {
		startForResult(activity, SenderFilter.class, filter, requestCode);
	}

	public void onOk(View view) {
		ListAdapter adapter = fragment.getListAdapter();
		filter.setSenders(null);
		boolean filterNothing = true;
		for (int i = 0; i < adapter.getCount(); i++) {
			Sender s = (Sender) adapter.getItem(i);
			if (s.getSuppress()) {
				filterNothing = false;
				break; // need to explicitly set allowed senders
			}
		}
		if (!filterNothing) {
			for (int i = 0; i < adapter.getCount(); i++) {
				Sender s = (Sender) adapter.getItem(i);
				if (!s.getSuppress()) {
					filter.addSender(s.getId());
				}
			}
		}
		returnFilter(filter);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Display the fragment as the main content.
		this.setContentView(R.layout.activity_sender_filter);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		fragment = SenderFilterListFragment.newInstance(filter);
		ft.replace(R.id.sender_filter_fragment_container, fragment);
		ft.commit();
	}
}
