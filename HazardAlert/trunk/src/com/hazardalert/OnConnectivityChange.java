package com.hazardalert;

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
			OnUpdateSubscription.resetBackoff(context);
			//new Assert(U.isNetworkAvailable(context)); can change back?
			//context.startService(new Intent(context, OnUpdateSubscription.class));
		}
	}
}
