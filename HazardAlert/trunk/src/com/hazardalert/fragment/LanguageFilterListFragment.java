package com.hazardalert.fragment;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.content.Loader;
import android.widget.ListAdapter;

import com.hazardalert.Language;
import com.hazardalert.Log;
import com.hazardalert.R;
import com.hazardalert.common.AlertFilter;
import com.hazardalert.common.Assert;

public class LanguageFilterListFragment extends LanguageListFragment {
	private AlertFilter filter;

	public static LanguageFilterListFragment newInstance(AlertFilter filter) {
		Log.v();
		LanguageFilterListFragment f = new LanguageFilterListFragment();
		Bundle b = new Bundle();
		b.putSerializable("FILTER", filter);
		f.setArguments(b);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v();
		Bundle b = getArguments();
		new Assert(null != b);
		filter = (AlertFilter) b.getSerializable("FILTER");
		new Assert(null != filter);
		getLoaderManager().restartLoader(0 /* ignored */, null, this);
	}

	@Override
	public void onLoadFinished(Loader<List<Language>> arg0, List<Language> results) {
		Log.v();
		ArrayList<Language> ar = new ArrayList<Language>(results);
		if (null != filter.getLanguages()) {
			for (Language l : ar) {
				l.setSuppress(filter.hasLanguage(l.getLanguage()) ? false : true);
			}
		}
		ListAdapter adapter = new LanguageListAdapter(getActivity(), R.layout.language_list_item, R.id.language_list_item_name, ar);
		setListAdapter(adapter);
	}

	@Override
	protected void onSetSuppress(Language l) {
		// do nothing
	}
}