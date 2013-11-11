package com.hazardalert;

import java.util.Map;

import android.support.v4.app.LoaderManager;

import com.hazardalert.common.AlertFilter;

public interface DataManager extends LoaderManager.LoaderCallbacks<Map<String, Hazard>> {
	public interface Subscriber {
		public void setDataManager(DataManager manager);

		public DataManager getDataManager();

		public abstract void updateResults(Map<String, Hazard> results);
	}

	public AlertFilter getFilter();

	public void setFilter(AlertFilter filter);

	public void reload();

	public void subscribe(Subscriber subscriber);

	public void unsubscribe(Subscriber subscriber);
}
