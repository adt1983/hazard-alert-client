package com.hazardalert;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.Loader;

import com.hazardalert.common.AlertFilter;
import com.hazardalert.common.Assert;
import com.j256.ormlite.dao.Dao;

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
		try {
			boolean filterNothing = true;
			Dao<Sender, Long> dao = Sender.getDao(getActivity());
			List<Sender> senders = dao.queryForAll();
			for (Sender s : senders) {
				if (s.getSuppress()) {
					filterNothing = false;
					break; // need to explicitly set allowed senders
				}
			}
			if (!filterNothing) {
				for (Sender s : senders) {
					if (!s.getSuppress()) {
						filter.addSender(s.getId());
					}
				}
			}
		}
		catch (SQLException e) {
			filter.setSenders(null);
			HazardAlert.logException(getActivity(), e);
		}
		//Language defaults
		try {
			boolean filterNothing = true;
			Dao<Language, Long> dao = Language.getDao(getActivity());
			List<Language> languages = dao.queryForAll();
			for (Language l : languages) {
				if (l.getSuppress()) {
					filterNothing = false;
					break; // need to explicitly set allowed languages
				}
			}
			if (!filterNothing) {
				for (Language l : languages) {
					if (!l.getSuppress()) {
						filter.addLanguage(l.getLanguage());
					}
				}
			}
		}
		catch (SQLException e) {
			filter.setLanguages(null);
			HazardAlert.logException(getActivity(), e);
		}
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
	synchronized public void reload() {
		if (!isAdded()) {
			return;
		}
		getLoaderManager().restartLoader(BaseMapFragment.LOADER_ID_BOUNDS_LOCAL, null, this);
	}
}
