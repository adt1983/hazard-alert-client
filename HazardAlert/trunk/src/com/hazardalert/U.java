package com.hazardalert;

import java.text.DateFormat;
import java.util.Date;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.format.Time;

import com.google.android.gms.maps.model.LatLng;
import com.hazardalert.common.CommonUtil;
import com.hazardalert.common.Point;

public final class U extends CommonUtil {
	private static final Time sTime = new Time();

	private static Point lastLocation = null;

	public static Date parse3339(String time) {
		try {
			sTime.parse3339(time);
			return new Date(sTime.toMillis(false));
		}
		catch (RuntimeException e) {
			throw e; //just here for debugging
		}
	}

	public static boolean hasLastLocation(Context ctx) {
		return HazardAlert.containsPreference(ctx.getApplicationContext(), C.SP_LAST_LAT)
				&& HazardAlert.containsPreference(ctx.getApplicationContext(), C.SP_LAST_LNG);
	}

	public static Point getLastLocation(Context ctx) {
		if (null != lastLocation) {
			Log.d("Lng: " + lastLocation.getLng() + " Lat: " + lastLocation.getLat() + " Source: Cache");
			return lastLocation;
		}
		LocationManager lm = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		criteria.setCostAllowed(false);
		String provider = lm.getBestProvider(criteria, true);
		Location loc = null == provider ? null : lm.getLastKnownLocation(provider);
		if (null != loc) {
			Log.d("Lng: " + loc.getLongitude() + " Lat: " + loc.getLatitude() + " Source: LocationManager Provider: " + provider);
			lastLocation = U.toPoint(loc);
			return lastLocation;
		}
		if (!hasLastLocation(ctx)) {
			// No cache. No providers. No database. Default to 0.0, 0.0?
			Log.e("No clue where we are.");
		}
		lastLocation = new Point(//
									HazardAlert.getPreference(ctx.getApplicationContext(), C.SP_LAST_LAT, 0.0f),
									HazardAlert.getPreference(ctx.getApplicationContext(), C.SP_LAST_LNG, 0.0f));
		Log.d("Lng: " + lastLocation.getLng() + " Lat: " + lastLocation.getLat() + " Source: Disk");
		return getLastLocation(ctx);
	}

	public static void setLastLocation(Context ctx, Location loc) {
		if (null == loc) {
			Log.e("null == loc");
			return; // TODO: Assert false?
		}
		Log.d("Lng: " + loc.getLongitude() + " Lat: " + loc.getLatitude() + " Provider: " + loc.getProvider());
		lastLocation = U.toPoint(loc);
		HazardAlert.setPreference(ctx.getApplicationContext(), C.SP_LAST_LAT, (float) loc.getLatitude());
		HazardAlert.setPreference(ctx.getApplicationContext(), C.SP_LAST_LNG, (float) loc.getLongitude());
	}

	public static Point toPoint(android.location.Location location) {
		return new Point(location.getLatitude(), location.getLongitude());
	}

	public static com.google.android.gms.maps.model.LatLng toLatLng(Point point) {
		return new LatLng(point.getLat(), point.getLng());
	}

	public static com.google.android.gms.maps.model.LatLng toLatLng(android.location.Location location) {
		return new LatLng(location.getLatitude(), location.getLongitude());
	}

	public static boolean isNetworkAvailable(Context ctx) {
		ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		return null != ni && ni.isConnected();
	}

	public static String timeToString(Long time) {
		Date d = new Date(time);
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		return df.format(d);
	}
}
