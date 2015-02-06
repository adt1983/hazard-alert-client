package com.hazardalert;

import com.hazardalert.common.CommonUtil;

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

	public static final String PREF_NOTIF_SOUND_EXTREME = "pref_notif_sound_extreme";

	public static final String PREF_NOTIF_SOUND_SEVERE = "pref_notif_sound_severe";

	public static final String PREF_NOTIF_SOUND_MODERATE = "pref_notif_sound_moderate";

	public static final String PREF_NOTIF_SOUND_MINOR = "pref_notif_sound_minor";

	public static final String PREF_NOTIF_SOUND_UNKNOWN = "pref_notif_sound_unknown";

	public static final String SP_SUPPRESSED_SENDERS = "SP_SUPPRESSED_SENDERS";

	public static final String SP_LAST_LAT = "SP_LAST_LAT";

	public static final String SP_LAST_LNG = "SP_LAST_LNG";

	public static final String SP_GCM_REG_ID = "SP_GCM_REG_ID";

	public static final String SP_SUBSCRIPTION_ID = "SP_SUBSCRIPTION_ID";

	public static final String SP_SUBSCRIPTION_LAST_SYNC = "SP_SUBSCRIPTION_LAST_SYNC";

	/**
	 * Common time values - WTF? Why aren't these inherited?
	 */
	public static final long ONE_SECOND_MS = CommonUtil.ONE_SECOND_MS;

	public static final long ONE_MINUTE_MS = 60 * CommonUtil.ONE_SECOND_MS;

	public static final long FIFTEEN_MINUTES_MS = 15 * CommonUtil.ONE_MINUTE_MS;

	public static final long ONE_HOUR_MS = 60 * CommonUtil.ONE_MINUTE_MS;

	public static final long ONE_DAY_MS = 24 * CommonUtil.ONE_HOUR_MS;

	public static final long ONE_WEEK_MS = 7 * CommonUtil.ONE_DAY_MS;

	/**
	 * Subscription
	 */
	public static final float SUB_RECENTER_RADIUS_METERS = 20000.0f;

	public static final double SUB_RADIUS_KM = 25.0;

	public enum RequestCode {
		FILTER, FILTER_SENDER, FILTER_LANGUAGE, PLAY_SERVICES_RESOLUTION
	}
}
