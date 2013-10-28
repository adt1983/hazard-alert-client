package com.hazardalert;

import java.util.Map;

import android.support.v4.app.LoaderManager;

import com.hazardalert.common.AlertFilter;

public interface DataManager extends LoaderManager.LoaderCallbacks<Map<String, Hazard>> {
	public AlertFilter getFilter();

	public void setFilter(AlertFilter filter);

	public void reload();

	public void subscribe(DataSubscriber subscriber);

	public void unsubscribe(DataSubscriber subscriber);
}
