package com.hazardalert.fragment;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.content.Loader;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListAdapter;

import com.hazardalert.Language;
import com.hazardalert.Log;
import com.hazardalert.R;
import com.j256.ormlite.dao.Dao;

public class LanguageSettingsListFragment extends LanguageListFragment {
	class OnCheckChange implements CompoundButton.OnCheckedChangeListener {
		private final Language language;

		OnCheckChange(Language s) {
			language = s;
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean allowed) {
			Log.d("Language: " + language.getLanguage() + "\tChecked: " + allowed);
			try {
				Dao<Language, Long> dao = Language.getDao(getActivity());
				language.setSuppress(!allowed);
				dao.update(language);
			}
			catch (SQLException e) {
				Log.e("Could not update language.suppress!", e);
			}
		}
	}

	@Override
	protected void setOnCheckChangeListener(CheckBox cb, Language l) {
		cb.setOnCheckedChangeListener(new OnCheckChange(l));
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