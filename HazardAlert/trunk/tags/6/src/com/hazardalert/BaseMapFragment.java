package com.hazardalert;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.publicalerts.cap.Alert;
import com.hazardalert.common.AlertFilter;
import com.hazardalert.common.Bounds;
import com.vividsolutions.jts.geom.Envelope;

public class BaseMapFragment extends SupportMapFragment implements LoaderManager.LoaderCallbacks<Map<String, Hazard>> {
	public static final String TAG = "BaseMapFragment";

	public static final int LOADER_ID_BOUNDS_LOCAL = 0;

	public static final int LOADER_ID_BOUNDS_REMOTE = 1;

	private GoogleMap map = null;

	private Envelope bounds; // can't access map from off ui thread

	private final Envelope subEnv = new Envelope();

	private AlertFilter filter = new AlertFilter();

	public AlertFilter getFilter() {
		return filter;
	}

	public void setFilter(AlertFilter filter) {
		this.filter = filter;
		getLoaderManager().restartLoader(LOADER_ID_BOUNDS_LOCAL, null, BaseMapFragment.this);
	}

	class SubscriptionManager extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			if (subEnv.contains(bounds)) {
				return null;
			}
			Context ctx = getActivity().getApplicationContext();
			AlertFilter filter = new AlertFilter().setInclude(new Bounds(bounds)).setExclude(new Bounds(subEnv));
			try {
				List<Alert> newAlerts = new AlertAPI().list(filter);
				Database.getInstance(ctx).insertAlerts(ctx, newAlerts);
				subEnv.expandToInclude(bounds);
				getLoaderManager().restartLoader(LOADER_ID_BOUNDS_LOCAL, null, BaseMapFragment.this);
			}
			catch (IOException e) {
				// do nothing try again on next map change
				//ErrorReporter.getInstance().handleSilentException(e);
			}
			return null;
		}
	}

	class HazardItem {
		public final Hazard h;

		public final Marker m;

		private final List<Polygon> polygons = new LinkedList<Polygon>();

		HazardItem(Hazard _h) {
			h = _h;
			m = map.addMarker(new MarkerOptions().position(Util.toLatLng(h.getCentroid())));
			markerToItem.put(m.getId(), this);
			for (com.google.publicalerts.cap.Info i : h.getAlert().getInfoList()) {
				for (com.google.publicalerts.cap.Area a : i.getAreaList()) {
					for (com.google.publicalerts.cap.Polygon polygon : a.getPolygonList()) {
						com.google.android.gms.maps.model.PolygonOptions mapPolygon = new com.google.android.gms.maps.model.PolygonOptions();
						/*TODO: dynamic hue?
						 * https://developers.google.com/maps/documentation/android/reference/com/google/android/gms/maps/model/BitmapDescriptorFactory
						 * http://www.worqx.com/color/color_wheel.htm
						 */
						mapPolygon.fillColor(Color.argb(75, 255, 0, 0)); // see through red
						mapPolygon.strokeColor(Color.TRANSPARENT);
						mapPolygon.visible(null == selectedHazard);
						ListIterator<com.google.publicalerts.cap.Point> pointIter = polygon.getPointList().listIterator();
						while (pointIter.hasNext()) {
							com.google.publicalerts.cap.Point point = pointIter.next(); // intentionally pop front
							mapPolygon.add(new LatLng(point.getLatitude(), point.getLongitude()));
						}
						polygons.add(map.addPolygon(mapPolygon));
					}
				}
			}
		}

		public void remove() {
			for (Polygon p : polygons) {
				p.remove();
			}
			polygons.clear();
			markerToItem.remove(m.getId());
			m.remove();
		}

		public void setVisible(boolean visible) {
			for (Polygon p : polygons) {
				p.setVisible(visible);
			}
		}
	}

	private final java.util.Map<String, HazardItem> hazardToItem = new java.util.TreeMap<String, HazardItem>();

	private final java.util.Map<String, HazardItem> markerToItem = new java.util.TreeMap<String, HazardItem>();

	private HazardItem selectedHazard = null;

	private Marker selectedMarker = null;

	class HazardInfoWindowAdapter implements InfoWindowAdapter {
		private final View window = getActivity().getLayoutInflater().inflate(R.layout.hazard_list_item, null);

		@Override
		public View getInfoWindow(Marker marker) {
			HazardItem hi = markerToItem.get(marker.getId());
			TextView tv = (TextView) window.findViewById(R.id.hazard_list_item_tv);
			tv.setText(hi.h.getHeadline());
			return window;
		}

		@Override
		public View getInfoContents(Marker marker) {
			throw new RuntimeException();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.v();
		View view = super.onCreateView(inflater, container, savedInstanceState);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.v();
		super.onActivityCreated(savedInstanceState);
		checkPlayServices();
		setupMap();
	}

	@Override
	public void onResume() {
		Log.v();
		super.onResume();
		checkPlayServices();
	}

	public BaseMapFragment() {}

	@Override
	public void onAttach(Activity activity) {
		Log.v();
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v();
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public void onDestroyView() {
		Log.v();
		super.onDestroyView();
	}

	private void setupMap() {
		if (null != map) {
			return; // already ran
		}
		map = getMap();
		final Location loc = Util.getLocation(getActivity());
		map.setMyLocationEnabled(true);
		CameraUpdate camera = CameraUpdateFactory.newLatLngZoom(Util.toLatLng(loc), 8.0f);
		map.moveCamera(camera);
		map.setOnCameraChangeListener(new OnCameraChangeListener() {
			@Override
			public void onCameraChange(CameraPosition arg0) {
				Log.v();
				bounds = Util.toEnvelope(getBounds());
				filter.setInclude(new Bounds(bounds));
				getLoaderManager().restartLoader(LOADER_ID_BOUNDS_LOCAL, null, BaseMapFragment.this);
				new SubscriptionManager().execute();
			}
		});
		map.setInfoWindowAdapter(new HazardInfoWindowAdapter());
		map.setOnMarkerClickListener(new OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker marker) {
				if (null != selectedMarker) {
					selectedMarker.hideInfoWindow();
				}
				//marker.showInfoWindow();
				selectedMarker = marker;
				HazardItem hi = markerToItem.get(marker.getId());
				setSelectedHazard(hi);
				return false; // re-center map
			}
		});
		map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
			@Override
			public void onInfoWindowClick(Marker marker) {
				HazardItem hi = markerToItem.get(marker.getId());
				HazardDetail.start(getActivity(), hi.h);
			}
		});
		map.setOnMapClickListener(new OnMapClickListener() {
			@Override
			public void onMapClick(LatLng point) {
				setSelectedHazard(null);
			}
		});
	}

	private void setVisibleAll(boolean visible) {
		for (Map.Entry<String, HazardItem> e : hazardToItem.entrySet()) {
			e.getValue().setVisible(visible);
		}
	}

	private void addHazard(Hazard h) {
		HazardItem hi = new HazardItem(h);
		hazardToItem.put(h.getId(), hi);
	}

	private void setSelectedHazard(HazardItem hi) {
		if (null == hi) {
			if (null == selectedHazard) {
				return;
			}
			selectedHazard = null;
			setVisibleAll(true);
		}
		else {
			selectedHazard = hi;
			setVisibleAll(false);
			hi.setVisible(true);
		}
	}

	private boolean hasHazard(String id) {
		return hazardToItem.containsKey(id);
	}

	public LatLngBounds getBounds() {
		return map.getProjection().getVisibleRegion().latLngBounds;
	}

	/*
	 * private void removeHazard(String id) {
		HazardItem hi = hazardToItem.get(id);
		hi.remove();
		hazardToItem.remove(id);
		
	}
	 */
	private void loadResults(Map<String, Hazard> results) {
		Iterator<Map.Entry<String, HazardItem>> i = hazardToItem.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry<String, HazardItem> e = i.next();
			final String id = e.getKey();
			if (!results.containsKey(id)) {
				final HazardItem hi = e.getValue();
				hi.remove();
				i.remove();
				if (null != selectedHazard && selectedHazard.h.getId().equals(id)) {
					setSelectedHazard(null);
				}
			}
		}
		addResults(results);
	}

	private void addResults(Map<String, Hazard> results) {
		for (Map.Entry<String, Hazard> entry : results.entrySet()) {
			if (hasHazard(entry.getKey())) {
				continue;
			}
			addHazard(entry.getValue());
		}
	}

	@Override
	public Loader<Map<String, Hazard>> onCreateLoader(int loaderId, Bundle b) {
		switch (loaderId) {
		case LOADER_ID_BOUNDS_LOCAL:
			//return new LoaderHazardLocal(getActivity().getApplicationContext(), b);
			return new HazardLoader(getActivity().getApplicationContext(), filter);
		case LOADER_ID_BOUNDS_REMOTE:
			//return new LoaderHazardRemote(getActivity().getApplicationContext(), box);
		default:
			throw new RuntimeException();
		}
	}

	@Override
	public void onLoadFinished(Loader<Map<String, Hazard>> loader, Map<String, Hazard> results) {
		Log.v();
		switch (loader.getId()) {
		case LOADER_ID_BOUNDS_LOCAL:
			loadResults(results);
			// finished local now start remote			
			//getLoaderManager().restartLoader(LOADER_ID_BOUNDS_REMOTE, ((SimpleHazardLoader) loader).getBundle(), BaseMapFragment.this);
			break;
		case LOADER_ID_BOUNDS_REMOTE:
			addResults(results);
			break;
		default:
			throw new RuntimeException();
		}
	}

	@Override
	public void onLoaderReset(Loader<Map<String, Hazard>> arg0) {
		// do nothing
	}

	private boolean checkPlayServices() {
		final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000; // http://developer.android.com/google/gcm/client.html - who the fuck thinks up these magic numbers?
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.getActivity());
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this.getActivity(), PLAY_SERVICES_RESOLUTION_REQUEST).show();
			}
			else {
				//Log.i(TAG, "This device is not supported.");
				//finish();
			}
			return false;
		}
		return true;
	}
}
