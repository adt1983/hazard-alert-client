package com.hazardalert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.hazardalert.common.AlertFilter;

public class HazardListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Map<String, Hazard>> {
	public static final String TAG = "HazardListFragment";

	private AlertFilter filter = new AlertFilter();

	public AlertFilter getFilter() {
		return filter;
	}

	public void setFilter(AlertFilter filter) {
		this.filter = filter;
		getLoaderManager().restartLoader(BaseMapFragment.LOADER_ID_BOUNDS_LOCAL, null, this);
	}

	class HazardListAdapter extends ArrayAdapter<Hazard> {
		public HazardListAdapter(Context context, int resource, int textViewResourceId, List<Hazard> objects) {
			super(context, resource, textViewResourceId, objects);
			//todo: toArray for performance?
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			View row = super.getView(position, convertView, parent);
			final Hazard alert = this.getItem(position);
			TextView tv = (TextView) row.findViewById(R.id.hazard_list_item_tv);
			tv.setText(alert.getHeadline());
			OnClickListener listener = new OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.v();
					HazardDetail.start(getContext(), alert);
				}
			};
			row.setOnClickListener(listener);
			return row;
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.v();
		this.setEmptyText("No hazards.");
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.v();
		super.onListItemClick(l, v, position, id);
	}

	@Override
	public Loader<Map<String, Hazard>> onCreateLoader(int loaderId, Bundle b) {
		switch (loaderId) {
		case BaseMapFragment.LOADER_ID_BOUNDS_LOCAL:
			return new HazardLoader(getActivity().getApplicationContext(), filter);
		default:
			throw new RuntimeException();
		}
	}

	@Override
	public void onLoadFinished(Loader<Map<String, Hazard>> arg0, Map<String, Hazard> results) {
		Log.v();
		ArrayList<Hazard> hazards = new ArrayList<Hazard>();
		for (Map.Entry<String, Hazard> e : results.entrySet()) {
			hazards.add(e.getValue());
		}
		HazardListAdapter adapter = new HazardListAdapter(getActivity(), R.layout.hazard_list_item, R.id.hazard_list_item_tv, hazards);
		this.setListAdapter(adapter);
	}

	@Override
	public void onLoaderReset(Loader<Map<String, Hazard>> arg0) {
		// TODO Auto-generated method stub
	}
}
