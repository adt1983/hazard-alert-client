package com.hazardalert;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.api.client.util.DateTime;
import com.google.publicalerts.cap.Alert;
import com.google.publicalerts.cap.Info;
import com.google.publicalerts.cap.Info.Certainty;
import com.google.publicalerts.cap.Info.Severity;
import com.google.publicalerts.cap.Info.Urgency;

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
		final Alert alert = h.getAlert();
		final Info info0 = alert.getInfo(0);
		this.setContentView(R.layout.hazard_detail);
		if (Alert.Scope.PUBLIC != alert.getScope() || Alert.Status.ACTUAL != alert.getStatus()) {
			((LinearLayout) this.findViewById(R.id.alert_test_scope)).setVisibility(View.VISIBLE);
		}
		((TextView) this.findViewById(R.id.alert_event)).setText(h.getHeadline());
		((TextView) this.findViewById(R.id.alert_sender)).setText(h.getAlert().getSender());
		setTextView(R.id.alert_urgency, info0.getUrgency());
		setTextView(R.id.alert_severity, info0.getSeverity());
		setTextView(R.id.alert_certainty, info0.getCertainty());
		
		((TextView) this.findViewById(R.id.alert_effective)).setText(localizeEffectiveDate(info0.getEffective())); 
		((TextView) this.findViewById(R.id.alert_desc)).setText(info0.getDescription());
		((TextView) this.findViewById(R.id.alert_instruction)).setText(info0.getInstruction());
		((TextView) this.findViewById(R.id.alert_areadesc)).setText(info0.getArea(0).getAreaDesc());
		
	}

	@SuppressLint("SimpleDateFormat")
	private static String localizeEffectiveDate(String effectiveDate) {
		
		DateTime dateTime = new DateTime(effectiveDate);
		SimpleDateFormat dateFormatter1 = new SimpleDateFormat("dd MMM yyyy");
		SimpleDateFormat dateFormatter2 = new SimpleDateFormat("hh:mm:ss");
		Date date = new Date(dateTime.getValue());
		return dateFormatter1.format(date) + " at " + dateFormatter2.format(date);
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
