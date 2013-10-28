package com.hazardalert;

import java.util.Map;

public interface DataSubscriber {
	public void setDataManager(DataManager manager);

	public DataManager getDataManager();

	public abstract void updateResults(Map<String, Hazard> results);
}
