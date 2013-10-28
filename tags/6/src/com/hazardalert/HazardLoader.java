package com.hazardalert;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.content.Context;

import com.hazardalert.common.AlertFilter;

public class HazardLoader extends SimpleResultLoader<Map<String, Hazard>> {
	private final Database db;
	private final AlertFilter filter;

	public HazardLoader(Context ctx, AlertFilter filter) {
		super(ctx);
		this.db = Database.getInstance(ctx.getApplicationContext());
		this.filter = filter;
	}

	@Override
	public Map<String, Hazard> loadInBackground() {
		final Map<String, Hazard> results = new TreeMap<String, Hazard>();
		List<Hazard> localAlerts = db.list(filter);
		for (Hazard h : localAlerts) {
			results.put(h.getId(), h);
		}
		return results;
	}
}
