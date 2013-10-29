package com.hazardalert;

import java.util.Date;
import java.util.Map;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.publicalerts.cap.Alert;
import com.google.publicalerts.cap.Info;
import com.hazardalert.RangeSeekBar.OnRangeSeekBarChangeListener;
import com.hazardalert.common.AlertFilter;
import com.hazardalert.common.Assert;
import com.hazardalert.common.Longs;

public class EditFilterFragment extends Fragment implements DataSubscriber {
	private TextView effectiveTextView;

	private TextView urgencyTextView;

	private TextView severityTextView;

	private TextView certaintyTextView;

	private RangeSeekBar<Integer> effectiveSeek;

	private RangeSeekBar<Integer> urgencySeek;

	private RangeSeekBar<Integer> severitySeek;

	private RangeSeekBar<Integer> certaintySeek;

	private CheckBox allowTest;

	private Button okBtn;

	private Button resetBtn;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		//return super.onCreateView(inflater, container, savedInstanceState);
		View v = inflater.inflate(R.layout.edit_filter, null);
		allowTest = (CheckBox) v.findViewById(R.id.edit_filter_status);
		resetBtn = (Button) v.findViewById(R.id.edit_filter_reset);
		resetBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onReset();
			}
		});
		okBtn = (Button) v.findViewById(R.id.edit_filter_ok);
		okBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onOK();
			}
		});
		LinearLayout ll = (LinearLayout) v.findViewById(R.id.edit_filter_ll);
		buildSliders(ll);
		return v;
	}

	public static final String TAG = "EditFilterFragment";

	private DataManager dataManager = null;

	@Override
	public void setDataManager(DataManager manager) {
		dataManager = manager;
	}

	@Override
	public DataManager getDataManager() {
		new Assert(null != dataManager);
		return dataManager;
	}

	@Override
	public void updateResults(Map<String, Hazard> results) {
		// ignore
	}

	private void buildSliders(LinearLayout ll) {
		effectiveTextView = new TextView(getActivity());
		urgencyTextView = new TextView(getActivity());
		severityTextView = new TextView(getActivity());
		certaintyTextView = new TextView(getActivity());
		effectiveSeek = buildSlider(ll,
									Integer.valueOf((int) -C.ONE_WEEK_MS),
									Integer.valueOf((int) C.ONE_DAY_MS),
									effectiveTextView,
									new OnRangeSeekBarChangeListener<Integer>() {
										@Override
										public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
											long time = new Date().getTime();
											effectiveTextView.setText("Effective: " + Util.timeToString(time + minValue) + " - "
													+ Util.timeToString(time + maxValue));
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
		onReset();
	}

	private <T extends Number> RangeSeekBar<T> buildSlider(ViewGroup parent, T min, T max, TextView tv,
			OnRangeSeekBarChangeListener<T> listener) {
		RangeSeekBar<T> rsb = new RangeSeekBar<T>(min, max, getActivity());
		rsb.setOnRangeSeekBarChangeListener(listener);
		listener.onRangeSeekBarValuesChanged(rsb, rsb.getSelectedMinValue(), rsb.getSelectedMaxValue()); // show initial values
		parent.addView(tv);
		parent.addView(rsb);
		return rsb;
	}

	private void onReset() {
		allowTest.setChecked(false);
		allowTest.invalidate();
		effectiveSeek.setSelectedMinValue(Integer.valueOf((int) -C.ONE_DAY_MS));
		effectiveSeek.setSelectedMaxValue(Integer.valueOf((int) C.ONE_DAY_MS));
		effectiveSeek.invalidate();
		urgencySeek.setSelectedMinValue(Info.Urgency.FUTURE_VALUE);
		urgencySeek.setSelectedMaxValue(urgencySeek.getAbsoluteMaxValue());
		urgencySeek.invalidate();
		severitySeek.setSelectedMinValue(Info.Severity.MINOR_VALUE);
		severitySeek.setSelectedMaxValue(severitySeek.getAbsoluteMaxValue());
		severitySeek.invalidate();
		certaintySeek.setSelectedMinValue(Info.Certainty.UNLIKELY_VALUE);
		certaintySeek.setSelectedMaxValue(certaintySeek.getAbsoluteMaxValue());
		certaintySeek.invalidate();
	}

	private void onOK() {
		DataManager dm = getDataManager();
		AlertFilter af = dm.getFilter();
		af.setStatus(new Longs());
		af.addStatus(Alert.Status.ACTUAL);
		if (allowTest.isChecked()) {
			af.addStatus(Alert.Status.TEST);
		}
		af.setMinEffective(new Date().getTime() + effectiveSeek.getSelectedMinValue());
		Longs urgency = af.getUrgency() == null ? new Longs() : af.getUrgency();
		urgency.clear();
		for (int i = urgencySeek.getSelectedMaxValue().intValue(); i <= urgencySeek.getSelectedMinValue().intValue(); i++) { //reverse order
			urgency.add(i);
		}
		af.setUrgency(urgency);
		Longs severity = af.getSeverity() == null ? new Longs() : af.getSeverity();
		severity.clear();
		for (int i = severitySeek.getSelectedMaxValue().intValue(); i <= severitySeek.getSelectedMinValue().intValue(); i++) { //reverse order
			severity.add(i);
		}
		af.setSeverity(severity);
		Longs certainty = af.getCertainty() == null ? new Longs() : af.getCertainty();
		certainty.clear();
		for (int i = certaintySeek.getSelectedMaxValue().intValue(); i <= certaintySeek.getSelectedMinValue().intValue(); i++) { //reverse order
			certainty.add(i);
		}
		af.setCertainty(certainty);
		dm.setFilter(af);
	}
}
