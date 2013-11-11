package com.hazardalert.activity;

import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.publicalerts.cap.Alert;
import com.google.publicalerts.cap.Info;
import com.hazardalert.C;
import com.hazardalert.R;
import com.hazardalert.RangeSeekBar;
import com.hazardalert.RangeSeekBar.OnRangeSeekBarChangeListener;
import com.hazardalert.U;
import com.hazardalert.common.AlertFilter;
import com.hazardalert.common.Assert;
import com.hazardalert.common.Bounds;

public class Filter extends FilterAbstract {
	private TextView effectiveTextView;

	private TextView urgencyTextView;

	private TextView severityTextView;

	private TextView certaintyTextView;

	private RangeSeekBar<Integer> effectiveSeek;

	private RangeSeekBar<Integer> urgencySeek;

	private RangeSeekBar<Integer> severitySeek;

	private RangeSeekBar<Integer> certaintySeek;

	private CheckBox allowTest;

	public static void startForResult(Activity activity, AlertFilter filter, int requestCode) {
		startForResult(activity, Filter.class, filter, requestCode);
	}

	public void onOk(View view) {
		filter.setStatus(null);
		filter.addStatus(Alert.Status.ACTUAL);
		if (allowTest.isChecked()) {
			filter.addStatus(Alert.Status.TEST);
		}
		filter.setMinEffective(new Date().getTime() + effectiveSeek.getSelectedMinValue());
		filter.setUrgency(null);
		for (int i = urgencySeek.getSelectedMaxValue().intValue(); i <= urgencySeek.getSelectedMinValue().intValue(); i++) { //reverse order
			filter.addUrgency(i);
		}
		filter.setSeverity(null);
		for (int i = severitySeek.getSelectedMaxValue().intValue(); i <= severitySeek.getSelectedMinValue().intValue(); i++) { //reverse order
			filter.addSeverity(i);
		}
		filter.setCertainty(null);
		for (int i = certaintySeek.getSelectedMaxValue().intValue(); i <= certaintySeek.getSelectedMinValue().intValue(); i++) { //reverse order
			filter.addCertainty(i);
		}
		returnFilter(filter);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_filter);
		allowTest = (CheckBox) findViewById(R.id.edit_filter_status);
		LinearLayout ll = (LinearLayout) findViewById(R.id.edit_filter_ll);
		buildSliders(ll);
	}

	public void onSenderFilter(View view) {
		SenderFilter.startForResult(this, filter, C.RequestCode.FILTER_SENDER.ordinal());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == C.RequestCode.FILTER_SENDER.ordinal() && resultCode == RESULT_OK) {
			filter = SenderFilter.parseResult(data);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void onReset(View view) {
		Bounds include = filter.getInclude(); // preserve bounds
		filter = AlertFilter.defaultClientFilter();
		filter.setInclude(include);
		initializeWidgets();
	}

	private void buildSliders(LinearLayout ll) {
		effectiveTextView = new TextView(this);
		urgencyTextView = new TextView(this);
		severityTextView = new TextView(this);
		certaintyTextView = new TextView(this);
		effectiveSeek = buildSlider(ll,
									Integer.valueOf((int) -C.ONE_WEEK_MS),
									Integer.valueOf((int) C.ONE_DAY_MS),
									effectiveTextView,
									new OnRangeSeekBarChangeListener<Integer>() {
										@Override
										public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
											long time = new Date().getTime();
											effectiveTextView.setText("Effective: " + U.timeToString(time + minValue) + " - "
													+ U.timeToString(time + maxValue));
										}
									});
		urgencySeek = buildSlider(	ll,
									Info.Urgency.UNKNOWN_URGENCY_VALUE,
									Info.Urgency.IMMEDIATE_VALUE,
									urgencyTextView,
									new OnRangeSeekBarChangeListener<Integer>() {
										@Override
										public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
											urgencyTextView.setText("Urgency: " + Info.Urgency.valueOf(minValue).name() + " - "
													+ Info.Urgency.valueOf(maxValue).name());
										}
									});
		severitySeek = buildSlider(	ll,
									Info.Severity.UNKNOWN_SEVERITY_VALUE,
									Info.Severity.EXTREME_VALUE,
									severityTextView,
									new OnRangeSeekBarChangeListener<Integer>() {
										@Override
										public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
											severityTextView.setText("Severity: " + Info.Severity.valueOf(minValue).name() + " - "
													+ Info.Severity.valueOf(maxValue).name());
										}
									});
		certaintySeek = buildSlider(ll,
									Info.Certainty.UNKNOWN_CERTAINTY_VALUE,
									Info.Certainty.OBSERVED_VALUE,
									certaintyTextView,
									new OnRangeSeekBarChangeListener<Integer>() {
										@Override
										public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
											certaintyTextView.setText("Certainty: " + Info.Certainty.valueOf(minValue).name() + " - "
													+ Info.Certainty.valueOf(maxValue).name());
										}
									});
		initializeWidgets();
	}

	private void initializeWidgets() {
		allowTest.setChecked(filter.hasStatus(Alert.Status.TEST) ? true : false);
		allowTest.invalidate();
		effectiveSeek.setSelectedMinValue(Integer.valueOf(-(int) (new Date().getTime() - filter.getMinEffective().longValue())));
		effectiveSeek.setSelectedMaxValue(Integer.valueOf((int) C.ONE_DAY_MS));
		effectiveSeek.invalidate();
		List<Long> urgency = filter.getUrgency();
		urgencySeek.setSelectedMinValue(getSelectedMin(urgency));
		urgencySeek.setSelectedMaxValue(getSelectedMax(urgency));
		urgencySeek.invalidate();
		List<Long> severity = filter.getSeverity();
		severitySeek.setSelectedMinValue(getSelectedMin(severity));
		severitySeek.setSelectedMaxValue(getSelectedMax(severity));
		severitySeek.invalidate();
		List<Long> certainty = filter.getCertainty();
		certaintySeek.setSelectedMinValue(getSelectedMin(certainty));
		certaintySeek.setSelectedMaxValue(getSelectedMax(certainty));
		certaintySeek.invalidate();
	}

	int getSelectedMin(List<Long> ordinals) {
		// return the highest value in the List since CAP is in reverse order
		new Assert(!ordinals.isEmpty());
		Long max = Long.valueOf(0);
		for (Long item : ordinals) {
			if (item.longValue() > max.longValue()) {
				max = item;
			}
		}
		return (int) max.longValue();
	}

	int getSelectedMax(List<Long> ordinals) {
		// return the lowest value in the List since CAP is in reverse order
		new Assert(!ordinals.isEmpty());
		Long min = Long.valueOf(100);
		for (Long item : ordinals) {
			if (item.longValue() < min.longValue()) {
				min = item;
			}
		}
		return (int) min.longValue();
	}

	private <T extends Number> RangeSeekBar<T> buildSlider(ViewGroup parent, T min, T max, TextView tv,
			OnRangeSeekBarChangeListener<T> listener) {
		RangeSeekBar<T> rsb = new RangeSeekBar<T>(min, max, this);
		rsb.setOnRangeSeekBarChangeListener(listener);
		listener.onRangeSeekBarValuesChanged(rsb, rsb.getSelectedMinValue(), rsb.getSelectedMaxValue()); // show initial values
		parent.addView(tv);
		parent.addView(rsb);
		return rsb;
	}
}
