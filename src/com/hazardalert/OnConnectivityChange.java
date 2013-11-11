package com.hazardalert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.hazardalert.common.Assert;

public class OnConnectivityChange extends BroadcastReceiver {
	@Override
	public void onReceive(final Context context, final Intent intent) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (null != ni && NetworkInfo.State.CONNECTED == ni.getState()) {
			new Assert(U.isNetworkAvailable(context));
			context.startService(new Intent(context, OnUpdateSubscription.class));
		}
	}
}
