package com.hazardalert;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.util.Linkify;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.publicalerts.cap.Alert;
import com.google.publicalerts.cap.Area;
import com.google.publicalerts.cap.Info;
import com.google.publicalerts.cap.Info.Certainty;
import com.google.publicalerts.cap.Info.Severity;
import com.google.publicalerts.cap.Info.Urgency;
import com.hazardalert.common.CommonUtil;

public class HazardDetail extends FragmentActivity {
	public static void start(Context ctx, Hazard h) {
		Intent intent = new Intent(ctx, HazardDetail.class);
		intent.putExtra("id", h.getId());
		ctx.startActivity(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hazard_detail);
		String id = getIntent().getStringExtra("id");
		Database db = Database.getInstance(this);
		Hazard h = db.safeGetByHazardId(id);
		Log.d(h.getFullName());
		final Alert alert = h.getAlert();
		final Info info = h.getInfo();
		final Area area = h.getArea();
		this.setContentView(R.layout.hazard_detail);
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
}
