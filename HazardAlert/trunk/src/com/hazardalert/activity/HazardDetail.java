package com.hazardalert.activity;

import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Html;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.publicalerts.cap.Alert;
import com.google.publicalerts.cap.Area;
import com.google.publicalerts.cap.Info;
import com.google.publicalerts.cap.Info.Certainty;
import com.google.publicalerts.cap.Info.Severity;
import com.google.publicalerts.cap.Info.Urgency;
import com.hazardalert.Database;
import com.hazardalert.Hazard;
import com.hazardalert.Log;
import com.hazardalert.R;
import com.hazardalert.U;
import com.hazardalert.common.Assert;
import com.hazardalert.common.CommonUtil;

public class HazardDetail extends HazardAlertFragmentActivity {
	private Hazard h = null;

	private Alert alert = null;

	private Info info = null;

	private Area area = null;

	// http://stackoverflow.com/questions/18153730/in-activity-oncreate-why-does-intent-getextras-sometimes-return-null
	private static final String ID_EXTRA = "com.hazardalert.HazardDetail.id";

	public static void startActivity(Context ctx, Hazard h) {
		ctx.startActivity(buildIntent(ctx, h));
	}

	public static Intent buildIntent(Context ctx, Hazard h) {
		Intent intent = new Intent(ctx, HazardDetail.class);
		intent.putExtra(ID_EXTRA, h.getId());
		new Assert(null != intent.getStringExtra(ID_EXTRA));
		return intent;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hazard_detail);
		String id = getIntent().getStringExtra(ID_EXTRA);
		new Assert(null != id);
		Database db = Database.getInstance(this);
		h = db.safeGetByHazardId(id);
		Log.d(h.getFullName());
		alert = h.getAlert();
		info = h.getInfo();
		area = h.getArea();
		this.setContentView(R.layout.hazard_detail);
		// Look up the AdView as a resource and load a request.
		AdView adView = (AdView) this.findViewById(R.id.adView);
		//AdRequest adRequest = new AdRequest.Builder().addTestDevice("181E681670510D8753AEF9ACE8CFC544").build();
		AdRequest.Builder arb = new AdRequest.Builder();
		try {
			String contentUrl = h.getSourceUrl();
			if (info.hasWeb()) {
				contentUrl = info.getWeb();
			}
			arb.setContentUrl(contentUrl);
		}
		catch (Exception e) {
			// do nothing
		}
		try {
			arb.setLocation(U.getLastKnownLocation(this));
		}
		catch (Exception e) {
			// do nothing
		}
		adView.loadAd(arb.build());
		//
		if (Alert.Scope.PUBLIC != alert.getScope() || Alert.Status.ACTUAL != alert.getStatus()) {
			((LinearLayout) this.findViewById(R.id.alert_test_scope)).setVisibility(View.VISIBLE);
		}
		((TextView) this.findViewById(R.id.alert_desc)).setText(CommonUtil.lowercaseLinks(info.getDescription()));
		if (info.hasInstruction()) {
			TextView instruction = ((TextView) this.findViewById(R.id.alert_instruction));
			instruction.setText("\nInstruction:\n" + CommonUtil.lowercaseLinks(info.getInstruction()));
			instruction.setVisibility(View.VISIBLE);
		}
		if (info.hasWeb()) {
			TextView web = ((TextView) findViewById(R.id.alert_web));
			web.setText("\nMore Info: " + info.getWeb());
			web.setVisibility(View.VISIBLE);
		}
		if (info.hasContact()) {
			TextView contact = ((TextView) this.findViewById(R.id.alert_contact));
			contact.setText("\nContact: " + info.getContact());
			contact.setVisibility(View.VISIBLE);
		}
		((TextView) this.findViewById(R.id.alert_event)).setText("\nEvent: " + info.getEvent());
		TextView sender = ((TextView) this.findViewById(R.id.alert_sender));
		if (!info.hasContact()) {
			sender.setAutoLinkMask(Linkify.ALL); // only linkify sender if we have no other contact
		}
		sender.setText("Sender: " + alert.getSender());
		if (info.hasSenderName() && !info.getSenderName().equals(alert.getSender())) {
			TextView senderName = ((TextView) this.findViewById(R.id.alert_senderName));
			senderName.setText("Sender Name: " + info.getSenderName());
			senderName.setVisibility(View.VISIBLE);
		}
		setTextView(R.id.alert_urgency, info.getUrgency());
		setTextView(R.id.alert_severity, info.getSeverity());
		setTextView(R.id.alert_certainty, info.getCertainty());
		((TextView) this.findViewById(R.id.alert_effective)).setText("Effective: " + h.getEffectiveString());
		((TextView) this.findViewById(R.id.alert_expires)).setText("Expires: " + h.getExpiresString());
		((TextView) this.findViewById(R.id.alert_areadesc)).setText("Affected Area: " + area.getAreaDesc());
		((TextView) this.findViewById(R.id.alert_sourceUrl)).setText("Original Source: " + h.getSourceUrl());
		h.onShown(this);
	}

	private void setTextView(int id, Urgency urgency) {
		TextView tv = (TextView) this.findViewById(id);
		tv.setText(urgency.name());
		tv.setTextColor(getResources().getColor(getColorId(urgency)));
	}

	private void setTextView(int id, Severity severity) {
		TextView tv = (TextView) this.findViewById(id);
		tv.setText(severity.name());
		tv.setTextColor(getResources().getColor(getColorId(severity)));
	}

	private void setTextView(int id, Certainty certainty) {
		TextView tv = (TextView) this.findViewById(id);
		tv.setText(certainty.name());
		tv.setTextColor(getResources().getColor(getColorId(certainty)));
	}

	private int getColorId(com.google.publicalerts.cap.Info.Urgency urgency) {
		switch (urgency.ordinal()) {
		case Urgency.IMMEDIATE_VALUE:
			return R.color.Red;
		case Urgency.EXPECTED_VALUE:
			return R.color.Orange;
		case Urgency.FUTURE_VALUE:
			return R.color.Yellow;
		case Urgency.PAST_VALUE:
			return R.color.Green;
		default:
			return R.color.Purple;
		}
	}

	private int getColorId(com.google.publicalerts.cap.Info.Severity severity) {
		switch (severity.ordinal()) {
		case Severity.EXTREME_VALUE:
			return R.color.Red;
		case Severity.SEVERE_VALUE:
			return R.color.Orange;
		case Severity.MODERATE_VALUE:
			return R.color.Yellow;
		case Severity.MINOR_VALUE:
			return R.color.Green;
		default:
			return R.color.Purple;
		}
	}

	private int getColorId(com.google.publicalerts.cap.Info.Certainty certainty) {
		switch (certainty.ordinal()) {
		case Certainty.OBSERVED_VALUE:
			return R.color.Red;
		case Certainty.VERY_LIKELY_VALUE: // fall through
		case Certainty.LIKELY_VALUE:
			return R.color.Orange;
		case Certainty.POSSIBLE_VALUE:
			return R.color.Yellow;
		case Certainty.UNLIKELY_VALUE:
			return R.color.Green;
		default:
			return R.color.Purple;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d();
		getMenuInflater().inflate(R.menu.activity_hazard_detail, menu);
		return true;
	}

	public boolean onShare(MenuItem menuItem) {
		final NavigableMap<String, Intent> intentMap = new TreeMap<String, Intent>(); // packageName -> Intent
		final Intent sendIntent = new Intent(android.content.Intent.ACTION_SEND).setType("text/plain");
		List<ResolveInfo> handlers = getPackageManager().queryIntentActivities(sendIntent, 0);
		final String hazardLink = h.getInfo().hasWeb() ? h.getInfo().getWeb() : h.getSourceUrl();
		final String appLink = "https://play.google.com/store/apps/details?id=com.hazardalert";
		final String linkText = info.getEvent() + " (" + hazardLink + ") via Hazard Alert (" + appLink + ")";
		final String linkHtml = "<a href=\"" + hazardLink + "\">" + h.getInfo().getEvent() + "</a> via <a href=\"" + appLink
				+ "\">Hazard Alert</a>";
		for (ResolveInfo ri : handlers) {
			final String packageName = ri.activityInfo.packageName;
			Log.d("Found handler: " + packageName);
			Intent targetedIntent = new Intent(android.content.Intent.ACTION_SEND);
			targetedIntent.setType("text/plain");
			if (packageName.startsWith("com.facebook")) {
				//http://stackoverflow.com/questions/8771333/android-share-intent-for-facebook-share-text-and-link
				targetedIntent.setType("text/plain");
				targetedIntent.putExtra(Intent.EXTRA_TEXT, hazardLink);
			}
			else if (packageName.startsWith("com.twitter")) {
				targetedIntent.setType("text/plain");
				targetedIntent.putExtra(Intent.EXTRA_TEXT, linkText);
			}
			else if (packageName.startsWith("com.google.android.gm")) {
				//targetedIntent.setType("message/rfc822");
				targetedIntent.putExtra(Intent.EXTRA_SUBJECT, h.getInfo().getEvent());
				targetedIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(linkHtml));
			}
			else {
				targetedIntent.setType("text/plain");
				targetedIntent.putExtra(Intent.EXTRA_TEXT, linkText);
			}
			targetedIntent.setPackage(packageName);
			addIntent(intentMap, packageName, targetedIntent);
		}
		Map.Entry<String, Intent> firstEntry = intentMap.firstEntry();
		Intent first = firstEntry.getValue();
		intentMap.remove(firstEntry.getKey());
		Intent chooser = Intent.createChooser(first, "Share");
		chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentMap.values().toArray(new Parcelable[] {}));
		startActivity(chooser);
		// track
		EasyTracker easyTracker = EasyTracker.getInstance(this);
		//WISHLIST Would be nice to see the channel (FB/Twitter/etc) but requires a custom chooser?
		easyTracker.send(MapBuilder.createEvent("ui_action", // Event category (required)
												"button_press", // Event action (required)
												"share_button", // Event label
												null) // Event value
									.build());
		return true;
	}

	private void addIntent(Map<String, Intent> map, String packageName, Intent intent) {
		if (!map.containsKey(packageName)) {
			map.put(packageName, intent);
		}
	}
}
