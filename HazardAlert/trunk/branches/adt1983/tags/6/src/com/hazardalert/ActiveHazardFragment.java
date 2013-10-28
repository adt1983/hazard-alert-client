package com.hazardalert;

import android.os.Bundle;

import com.hazardalert.common.AlertFilter;
import com.hazardalert.common.Bounds;
import com.hazardalert.common.CommonUtil;
import com.hazardalert.common.Point;
import com.vividsolutions.jts.geom.Envelope;

public class ActiveHazardFragment extends HazardListFragment {
	public static final String TAG = "ActiveHazardFragment";

	public ActiveHazardFragment() {}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.v();
		this.setEmptyText("No active hazards affecting your current location.");
		Point center = Util.getLastLocation(getActivity());
		Envelope env = CommonUtil.getBoundingBox(center.toCoordinate(), 0.25); // 0.25km //FIXME
		this.setFilter(new AlertFilter().setInclude(new Bounds(env)));
	}
}
