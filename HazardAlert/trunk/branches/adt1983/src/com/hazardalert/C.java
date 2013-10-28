package com.hazardalert;

public class C {
	public static final String TAG = "HazardAlert";

	public static final String SP_KEY_LOC_SRV_STARTED = "SP_KEY_LOC_SRV_STARTED";

	public static final String SHARED_PREFERENCE_FILE = "SHARED_PREFERENCE_FILE";

	/**
	 * Base URL of the Demo Server (such as http://my_host:8080/gcm-demo)
	 */
	public static final String SERVER_URL = "http://hazard-alert.appspot.com";

	/**
	 * Google API project id registered to use GCM.
	 */
	public static final String SENDER_ID = "620040926051";

	public static final String PREF_NOTIF_ALLOW = "pref_notif_allow";

	public static final String PREF_NOTIF_SOUND_ALLOW = "pref_notif_sound_allow";

	public static final String SP_LAST_LAT = "SP_LAST_LAT";

	public static final String SP_LAST_LNG = "SP_LAST_LNG";

	public static final String SP_GCM_REG_ID = "SP_GCM_REG_ID";

	public static final String SP_SUBSCRIPTION_ID = "SP_SUBSCRIPTION_ID";

	/**
	 * Subscription
	 */
	public static final float SUB_RECENTER_RADIUS_METERS = 20000.0f;

	public static final double SUB_RADIUS_KM = 25.0;

	/**
	 * Common time values
	 */
	public static final long ONE_SECOND_MS = 1000;

	public static final long ONE_MINUTE_MS = 60 * ONE_SECOND_MS;

	public static final long FIFTEEN_MINUTES_MS = 15 * ONE_MINUTE_MS;

	public static final long ONE_HOUR_MS = 60 * ONE_MINUTE_MS;

	public static final long ONE_DAY_MS = 24 * ONE_HOUR_MS;
}
