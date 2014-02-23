package com.hazardalert.fragment;

import java.sql.SQLException;
import java.util.ArrayList;
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

import com.hazardalert.Log;
import com.hazardalert.R;
import com.hazardalert.Sender;
import com.hazardalert.loader.SenderLoader;
import com.j256.ormlite.dao.Dao;

public class SenderSettingsFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<Sender>> {
	class SenderSettingsListAdapter extends ArrayAdapter<Sender> {
		class OnCheckChange implements CompoundButton.OnCheckedChangeListener {
			private final Sender sender;

			OnCheckChange(Sender s) {
				sender = s;
			}

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean allowed) {
				Log.d("Sender: " + sender.getName() + "\tChecked: " + allowed);
				try {
					Dao<Sender, Long> dao = Sender.getDao(getActivity());
					sender.setSuppress(!allowed);
					dao.update(sender);
				}
				catch (SQLException e) {
					Log.e("Could not update sender.suppress!", e);
				}
			}
		}

		public SenderSettingsListAdapter(Context context, int resource, int textViewResourceId, List<Sender> objects) {
			super(context, resource, textViewResourceId, objects);
			//todo: toArray for performance?
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final View row = super.getView(position, convertView, parent);
			final Sender s = getItem(position);
			((TextView) row.findViewById(R.id.sender_list_item_name)).setText(s.getName());
			((TextView) row.findViewById(R.id.sender_list_item_url)).setText(s.getUrl().replace("'", ""));
			CheckBox allowed = (CheckBox) row.findViewById(R.id.sender_list_allowed);
			allowed.setChecked(!s.getSuppress());
			allowed.setOnCheckedChangeListener(new OnCheckChange(s));
			return row;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getLoaderManager().restartLoader(0 /* ignored*/, null, this);
	}

	@Override
	public Loader<List<Sender>> onCreateLoader(int arg0, Bundle arg1) {
		Log.v();
		return new SenderLoader(getActivity().getApplicationContext(), false);
	}

	@Override
	public void onLoadFinished(Loader<List<Sender>> arg0, List<Sender> results) {
		Log.v();
		ArrayList<Sender> ar = new ArrayList<Sender>(results);
		ListAdapter adapter = new SenderSettingsListAdapter(getActivity(), R.layout.sender_list_item, R.id.sender_list_item_name, ar);
		setListAdapter(adapter);
	}

	@Override
	public void onLoaderReset(Loader<List<Sender>> arg0) {
		Log.v();
	}
}
