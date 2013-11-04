package com.hazardalert;

import com.google.android.gms.maps.model.LatLngBounds;
import com.vividsolutions.jts.geom.Envelope;

public class Bounds extends com.hazardalert.common.Bounds {
	public Bounds(LatLngBounds mapBounds) {
		setNe_lng(mapBounds.northeast.longitude);
		setNe_lat(mapBounds.northeast.latitude);
		setSw_lng(mapBounds.southwest.longitude);
		setSw_lat(mapBounds.southwest.latitude);
	}

	public Bounds(Envelope e) {
		super(e);
	}
}