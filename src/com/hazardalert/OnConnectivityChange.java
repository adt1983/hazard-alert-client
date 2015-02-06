package com.hazardalert;

import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class OnConnectivityChange extends BroadcastReceiver {
	@Override
	public void onReceive(final Context context, final Intent intent) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (null != ni && NetworkInfo.State.CONNECTED == ni.getState()) {
			//new Assert(U.isNetworkAvailable(context)); can change back?
			if (OnUpdateSubscription.isRunning()) {
				OnUpdateSubscription.resetBackoff(context);
			}
			else {
				long now = new Date().getTime();
				long lastSync = HazardAlert.getPreference(context, C.SP_SUBSCRIPTION_LAST_SYNC, now);
				if (lastSync - now > 7 * C.ONE_HOUR_MS) {
					context.startService(new Intent(context, OnUpdateSubscription.class));
				}
			}
		}
	}
}
