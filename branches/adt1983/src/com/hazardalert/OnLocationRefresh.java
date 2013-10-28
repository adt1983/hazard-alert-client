package com.hazardalert;

import java.util.Date;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.google.android.gcm.GCMRegistrar;
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
		Util.setLastLocation(ctx, loc);
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
		ensureGCM();
	}

	// Restart GCM if we haven't heard from it in awhile
	private void ensureGCM() {
		final long MAX_GCM_IDLE = 3 * 60 * 60 * 1000; // 3 hours
		if (MAX_GCM_IDLE < new Date().getTime() - HazardAlert.getPreference(getApplicationContext(), "lastGCM", 0)) {
			HazardAlert.setPreference(getApplicationContext(), "lastGCM", new Date().getTime());
			GCMRegistrar.unregister(getApplicationContext());
			Intent registerGCM = new Intent(getApplicationContext(), OnRegisterGCM.class);
			startService(registerGCM);
		}
	}
}
