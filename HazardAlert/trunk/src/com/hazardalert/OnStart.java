package com.hazardalert;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

public class OnStart extends IntentService implements GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {
	public static final String SP_KEY_RUNNING = "running";

	private LocationClient locationClient;

	public OnStart() {
		super("OnStart");
	}

	@Override
	public void onStart(Intent intent, int flags) {
		Log.v();
		super.onStart(intent, flags);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v();
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onCreate() {
		Log.v();
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		Log.v();
		super.onDestroy();
	}

	@Override
	protected synchronized void onHandleIntent(Intent intent) {
		if (!HazardAlert.getPreference(getApplicationContext(), SP_KEY_RUNNING, false)) {
			locationClient = new LocationClient(this, this, this);
			locationClient.connect();
			HazardAlert.setPreference(getApplicationContext(), SP_KEY_RUNNING, true);
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		Log.v();
	}

	@Override
	public void onConnected(Bundle arg0) {
		Log.v();
		setupLocationUpdates();
		setupSubscriptionUpdates();
	}

	private void setupLocationUpdates() {
		Log.v();
		Intent i = new Intent(this, OnLocationRefresh.class);
		PendingIntent pi = PendingIntent.getService(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
		LocationRequest request = LocationRequest.create();
		request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
		request.setFastestInterval(50); //50ms
		request.setSmallestDisplacement((float) 10.0); //10m
		U.setLastLocation(getApplicationContext(), locationClient.getLastLocation());
		locationClient.requestLocationUpdates(request, pi);
	}

	private void setupSubscriptionUpdates() {
		Log.v();
		Intent subIntent = new Intent(this, OnUpdateSubscription.class);
		PendingIntent subPendingIntent = PendingIntent.getService(this, 0, subIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		LocationRequest updateSubscriptionRequest = LocationRequest.create();
		updateSubscriptionRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
		updateSubscriptionRequest.setFastestInterval(C.ONE_SECOND_MS); //1s - for debugging mock locations
		updateSubscriptionRequest.setSmallestDisplacement(C.SUB_RECENTER_RADIUS_METERS);
		updateSubscriptionRequest.setInterval(C.ONE_HOUR_MS);
		locationClient.requestLocationUpdates(updateSubscriptionRequest, subPendingIntent);
	}

	@Override
	public void onDisconnected() {
		Log.v();
	}
}
