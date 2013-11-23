package com.hazardalert;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.appspot.hazard_alert.alertendpoint.model.AlertTransport;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.hazardalert.common.AlertFilter;
import com.hazardalert.common.Assert;
import com.hazardalert.common.Point;
import com.vividsolutions.jts.geom.Envelope;

public class BaseMapFragment extends SupportMapFragment implements DataManager.Subscriber {
	public static final String TAG = "BaseMapFragment";

	public static final int LOADER_ID_BOUNDS_LOCAL = 0;

	public static final int LOADER_ID_BOUNDS_REMOTE = 1;

	private GoogleMap map = null;

	private Bounds visibleBounds;

	private final Envelope subEnv = new Envelope();

	AlertDialog rpcSpinner;

	AlertDialog loadSpinner;

	class SubscriptionManager extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (null != rpcSpinner) {
				rpcSpinner.show();
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (null != rpcSpinner) {
				rpcSpinner.dismiss();
			}
			if (result.booleanValue()) {
				if (null != loadSpinner) {
					loadSpinner.show();
				}
			}
			super.onPostExecute(result);
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			Context ctx = null == getActivity() ? null : getActivity().getApplicationContext();
			if (null == ctx) {
				return Boolean.FALSE;
			}
			if (subEnv.contains(visibleBounds.toEnvelope())) {
				return Boolean.FALSE;
			}
			AlertFilter filter = new AlertFilter().setInclude(visibleBounds).setExclude(new Bounds(subEnv));
			try {
				List<AlertTransport> newAlerts = new AlertAPI().list(filter);
				for (AlertTransport a : newAlerts) {
					try {
						Database.getInstance(ctx).insertAlert(ctx, a);
					}
					catch (Exception e) {
						// FIXME report error
						Log.e("Got invalid alert!", e);
					}
				}
				subEnv.expandToInclude(visibleBounds.toEnvelope());
				dataManager.reload();
				return Boolean.TRUE;
			}
			catch (IOException e) {
				// do nothing try again on next map change
				//ErrorReporter.getInstance().handleSilentException(e);
				return Boolean.FALSE;
			}
		}
	}

	class HazardItem {
		public final Hazard h;

		public final Marker m;

		private final List<Polygon> polygons = new LinkedList<Polygon>();

		HazardItem(Hazard _h) {
			h = _h;
			m = map.addMarker(new MarkerOptions().position(U.toLatLng(h.getCentroid())));
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
			//TextView tv = (TextView) window.findViewById(R.id.hazard_list_item_tv);
			//tv.setText(hi.h.getHeadline());
			((TextView) window.findViewById(R.id.hazard_list_item_event)).setText(hi.h.getInfo().getEvent());
			((TextView) window.findViewById(R.id.hazard_list_item_expires)).setText(hi.h.getEffectiveString() + " - "
					+ hi.h.getExpiresString());
			((TextView) window.findViewById(R.id.hazard_list_item_senderName)).setText(hi.h.getInfo().getSenderName());
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
	}

	@Override
	public void onAttach(Activity activity) {
		Log.v();
		super.onAttach(activity);
		this.rpcSpinner = createSpinner();
		this.loadSpinner = createSpinner();
	}

	@Override
	public void onDetach() {
		rpcSpinner.dismiss();
		rpcSpinner = null;
		loadSpinner.dismiss();
		loadSpinner = null;
		super.onDetach();
	}

	@Override
	public void onResume() {
		Log.v();
		super.onResume();
		if (checkPlayServices()) {
			setupMap();
		}
	}

	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(), C.RequestCode.PLAY_SERVICES_RESOLUTION.ordinal()).show();
			}
			else {
				Toast.makeText(getActivity(), "Google Play Services not found!", Toast.LENGTH_LONG).show();
				Log.e("GooglePlayServices not found! This device is not supported.");
			}
			return false;
		}
		return true;
	}

	public BaseMapFragment() {}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v();
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	// FIXME want just the spinner, not the background
	private AlertDialog createSpinner() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		ProgressBar pb = new ProgressBar(getActivity());
		pb.setLayoutParams(new LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT));
		pb.setBackgroundResource(android.R.color.transparent);
		builder.setView(pb);
		AlertDialog spinner = builder.create(); // do we need to create our own dialog class instead of using AlertDialog? shouldn't setView do the trick?
		Window w = spinner.getWindow();
		w.setGravity(Gravity.BOTTOM);
		w.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
		w.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		w.setFormat(PixelFormat.TRANSLUCENT);
		w.setBackgroundDrawableResource(android.R.color.transparent);
		w.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
		return spinner;
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
		new Assert(null != map);
		final Point loc = U.getLastLocation(getActivity());
		map.setMyLocationEnabled(true);
		CameraUpdate camera = CameraUpdateFactory.newLatLngZoom(U.toLatLng(loc), 8.0f);
		map.moveCamera(camera);
		map.setOnCameraChangeListener(new OnCameraChangeListener() {
			@Override
			public void onCameraChange(CameraPosition arg0) {
				Log.v();
				visibleBounds = new Bounds(map.getProjection().getVisibleRegion().latLngBounds); // can't access map from off UI thread
				AlertFilter filter = getDataManager().getFilter();
				filter.setInclude(visibleBounds);
				getDataManager().setFilter(filter);
				new SubscriptionManager().execute();
				if (null != loadSpinner) {
					loadSpinner.show();
				}
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

	private DataManager dataManager = null;

	@Override
	public void setDataManager(DataManager dm) {
		dataManager = dm;
		dataManager.subscribe(this);
	}

	@Override
	public void updateResults(Map<String, Hazard> results) {
		Log.v();
		loadResults(results);
		if (null != loadSpinner) {
			loadSpinner.dismiss();
		}
	}

	@Override
	public DataManager getDataManager() {
		new Assert(null != dataManager);
		return dataManager;
	}
}
