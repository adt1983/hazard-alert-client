package com.hazardalert.fragment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.content.Loader;
import android.widget.ListAdapter;

import com.hazardalert.Language;
import com.hazardalert.Log;
import com.hazardalert.R;
import com.j256.ormlite.dao.Dao;

public class LanguageSettingsListFragment extends LanguageListFragment {
	@Override
	protected void onSetSuppress(Language l) {
		try {
			Dao<Language, Long> dao = Language.getDao(getActivity());
			dao.update(l);
		}
		catch (SQLException e) {
			Log.e("Could not update language.suppress!", e);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v();
		getLoaderManager().restartLoader(0 /* ignored */, null, this);
	}

	@Override
	public void onLoadFinished(Loader<List<Language>> arg0, List<Language> results) {
		Log.v();
		ArrayList<Language> ar = new ArrayList<Language>(results);
		ListAdapter adapter = new LanguageListAdapter(getActivity(), R.layout.language_list_item, R.id.language_list_item_name, ar);
		setListAdapter(adapter);
	}
}