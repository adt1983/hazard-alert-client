package com.hazardalert.fragment;

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.hazardalert.Language;
import com.hazardalert.Log;
import com.hazardalert.R;
import com.hazardalert.loader.LanguageLoader;

public abstract class LanguageListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<Language>> {
	class LanguageListAdapter extends ArrayAdapter<Language> {
		class OnCheckChange implements CompoundButton.OnCheckedChangeListener {
			private final Language language;

			OnCheckChange(Language l) {
				language = l;
			}

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean allowed) {
				Log.d("Checked: " + allowed + "\t Language: " + language.getLanguage());
				// ensure at least one language is allowed
				int numAllowed = 0;
				final ListAdapter adapter = getListAdapter();
				for (int i = 0; i < adapter.getCount(); i++) {
					final Language l = (Language) adapter.getItem(i);
					if (!l.getSuppress()) {
						numAllowed++;
						if (numAllowed > 1 || allowed) {
							language.setSuppress(!allowed);
							onSetSuppress(language);
							return;
						}
					}
				}
				// prevent checkbox change
				((CheckBox) buttonView).setChecked(true);
			}
		}

		public LanguageListAdapter(Context context, int resource, int textViewResourceId, List<Language> objects) {
			super(context, resource, textViewResourceId, objects);
			//todo: toArray for performance?
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			Log.v();
			final View row = super.getView(position, convertView, parent);
			final Language l = getItem(position);
			String displayName = l.getDisplayLanguage();
			if (!l.getDisplayCountry().isEmpty()) {
				displayName = displayName.concat(" (" + l.getDisplayCountry() + ")");
			}
			((TextView) row.findViewById(R.id.language_list_item_name)).setText(displayName);
			CheckBox allowed = (CheckBox) row.findViewById(R.id.language_list_allowed);
			allowed.setOnCheckedChangeListener(new OnCheckChange(l));
			allowed.setChecked(!l.getSuppress());
			return row;
		}
	}

	protected abstract void onSetSuppress(Language l);

	@Override
	public Loader<List<Language>> onCreateLoader(int arg0, Bundle arg1) {
		Log.v();
		return new LanguageLoader(getActivity().getApplicationContext());
	}

	@Override
	public void onLoaderReset(Loader<List<Language>> arg0) {
		Log.v();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.v();
		this.setEmptyText("No known languages. Languages will be added as you receive alerts.");
	}
}
