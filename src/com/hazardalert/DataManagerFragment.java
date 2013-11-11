package com.hazardalert;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.Loader;

import com.hazardalert.common.AlertFilter;
import com.hazardalert.common.Assert;

/*
 * Not sure about this approach. Seems messy.
 * Should Map/List implement a common interface with setFilter/getFilter?
 * 
 * http://www.androiddesignpatterns.com/2013/04/retaining-objects-across-config-changes.html
 * http://stackoverflow.com/questions/16688073/synchronize-listfragment-and-supportmapfragment-selection
 * http://stackoverflow.com/questions/10045543/global-loader-loadermanager-for-reuse-in-multiple-activities-fragments
 * http://stackoverflow.com/questions/8871285/generic-class-that-extends-class-and-implements-interface 
 */
public class DataManagerFragment extends Fragment implements DataManager {
	public static final String TAG = "DataManagerFragment";

	private AlertFilter filter = AlertFilter.defaultClientFilter();

	private final List<Subscriber> subscribers = new LinkedList<Subscriber>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		// Filter defaults
	}

	@Override
	public Loader<Map<String, Hazard>> onCreateLoader(int arg0, Bundle arg1) {
		new Assert(BaseMapFragment.LOADER_ID_BOUNDS_LOCAL == arg0);
		return new HazardLoader(getActivity().getApplicationContext(), filter);
	}

	@Override
	public void onLoadFinished(Loader<Map<String, Hazard>> loader, Map<String, Hazard> results) {
		new Assert(BaseMapFragment.LOADER_ID_BOUNDS_LOCAL == loader.getId());
		for (Subscriber s : subscribers) {
			s.updateResults(results);
		}
	}

	@Override
	public void onLoaderReset(Loader<Map<String, Hazard>> arg0) {
		// do nothing?
	}

	@Override
	public AlertFilter getFilter() {
		return this.filter;
	}

	@Override
	public void setFilter(AlertFilter filter) {
		this.filter = filter;
		reload();
	}

	@Override
	public void subscribe(Subscriber subscriber) {
		if (!subscribers.contains(subscriber) && null != subscriber) {
			subscribers.add(subscriber);
		}
	}

	@Override
	public void unsubscribe(Subscriber subscriber) {
		subscribers.remove(subscriber);
	}

	@Override
	public void reload() {
		if (!isAdded()) {
			return;
		}
		getLoaderManager().restartLoader(BaseMapFragment.LOADER_ID_BOUNDS_LOCAL, null, this);
	}
}
