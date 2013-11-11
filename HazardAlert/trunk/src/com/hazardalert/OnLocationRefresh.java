package com.hazardalert;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.google.android.gms.location.LocationClient;

public class OnLocationRefresh extends IntentService {
	public OnLocationRefresh() {
		super("OnLocationRefresh");
	}

	//private static final String SP_LOC_INIT = "SP_LOC_INIT";
	/*
	 Check all alerts to new Loc, generate a OnHazardVisible 
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		final Location loc = intent.getParcelableExtra(LocationClient.KEY_LOCATION_CHANGED);
		if (null == loc) {
			throw new RuntimeException();
		}
		final Context ctx = getApplicationContext();
		U.setLastLocation(ctx, loc);
		/*
		final Context context = getApplicationContext();
		
		HazardAlert.getPreference(context, SP_LOC_INIT, false);
		if (!initialized) {
			initialize(loc);
			initialized = true;
		}*/
		Log.v("Provider: " + loc.getProvider() + " Lat: " + loc.getLatitude() + " Lng: " + loc.getLongitude());
		Database db = Database.getInstance(ctx);
		for (Hazard h : db.getHazardEntering(loc)) {
			h.onEnter(ctx);
		}
		for (Hazard h : db.getHazardExiting(loc)) {
			h.onExit(ctx);
		}
	}
}
