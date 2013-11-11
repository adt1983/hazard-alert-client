package com.hazardalert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class HazardListFragment extends ListFragment implements DataManager.Subscriber {
	public static final String TAG = "HazardListFragment";

	class HazardListAdapter extends ArrayAdapter<Hazard> {
		public HazardListAdapter(Context context, int resource, int textViewResourceId, List<Hazard> objects) {
			super(context, resource, textViewResourceId, objects);
			//todo: toArray for performance?
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			View row = super.getView(position, convertView, parent);
			final Hazard h = this.getItem(position);
			//TextView tv = (TextView) row.findViewById(R.id.hazard_list_item_tv);
			//tv.setText(alert.getHeadline());
			((TextView) row.findViewById(R.id.hazard_list_item_event)).setText(h.getInfo().getEvent());
			((TextView) row.findViewById(R.id.hazard_list_item_expires)).setText(h.getEffectiveString() + " - " + h.getExpiresString());
			((TextView) row.findViewById(R.id.hazard_list_item_senderName)).setText(h.getInfo().getSenderName());
			OnClickListener listener = new OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.v();
					HazardDetail.start(getContext(), h);
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

	private DataManager dataManager = null;

	@Override
	public void setDataManager(DataManager manager) {
		dataManager = manager;
		dataManager.subscribe(this);
	}

	@Override
	public DataManager getDataManager() {
		return dataManager;
	}

	@Override
	public void updateResults(Map<String, Hazard> results) {
		Log.v();
		ArrayList<Hazard> hazards = new ArrayList<Hazard>();
		for (Map.Entry<String, Hazard> e : results.entrySet()) {
			hazards.add(e.getValue());
		}
		HazardListAdapter adapter = new HazardListAdapter(getActivity(), R.layout.hazard_list_item, R.id.hazard_list_item_event, hazards);
		this.setListAdapter(adapter);
	}
}
