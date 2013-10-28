package com.hazardalert;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.appspot.hazard_alert.alertendpoint.Alertendpoint;
import com.appspot.hazard_alert.alertendpoint.model.AlertTransport;
import com.appspot.hazard_alert.alertendpoint.model.Subscription;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.Base64;
import com.hazardalert.common.AlertFilter;
import com.hazardalert.common.Bounds;
import com.vividsolutions.jts.geom.Envelope;

public class AlertAPI {
	private final com.appspot.hazard_alert.alertendpoint.Alertendpoint service;

	public AlertAPI() {
		final HttpTransport transport = com.google.api.client.extensions.android.http.AndroidHttp.newCompatibleTransport();
		final JsonFactory jsonFactory = new com.google.api.client.extensions.android.json.AndroidJsonFactory();
		service = new Alertendpoint(transport, jsonFactory, null);
	}

	com.google.publicalerts.cap.Alert alertFind(String fullName) throws IOException {
		return fromTransport(service.alert().find().setFullName(fullName).execute());
	}

	List<com.google.publicalerts.cap.Alert> list(AlertFilter filter) throws IOException {
		List<AlertTransport> results = service.alert().list(toModel(filter)).execute().getItems();
		return fromTransportList(results);
	}

	public Subscription createSubscription(String gcm, Bounds bounds, Long expires) throws IOException {
		return service.subscription().create(gcm, expires, toModel(bounds)).execute();
	}

	public Subscription subscriptionGet(long id) throws IOException {
		return service.subscription().get(id).execute();
	}

	public Subscription updateExpires(Subscription s, Long expires) throws IOException {
		return service.subscription().updateExpires(s.getId(), expires).execute();
	}

	List<com.google.publicalerts.cap.Alert> updateSubscription(Subscription s, Envelope env) throws IOException {
		return fromTransportList(service.alert().updateSubscription(s.getId(), s.getGcm(), toModel(new Bounds(env))).execute().getItems());
	}

	private static com.google.publicalerts.cap.Alert fromTransport(AlertTransport at) throws IOException {
		if (null == at) {
			return null;
		}
		byte[] bytes = Base64.decodeBase64(at.getPayload());
		return com.google.publicalerts.cap.Alert.parseFrom(bytes);
	}

	private static List<com.google.publicalerts.cap.Alert> fromTransportList(List<AlertTransport> transportList) throws IOException {
		List<com.google.publicalerts.cap.Alert> l = new LinkedList<com.google.publicalerts.cap.Alert>();
		if (null == transportList) {
			return l;
		}
		for (AlertTransport at : transportList) {
			if (null != at) {
				l.add(fromTransport(at));
			}
		}
		return l;
	}

	private static com.appspot.hazard_alert.alertendpoint.model.Bounds toModel(Bounds bounds) {
		return new com.appspot.hazard_alert.alertendpoint.model.Bounds().setNeLat(bounds.getNe_lat())
																		.setNeLng(bounds.getNe_lng())
																		.setSwLat(bounds.getSw_lat())
																		.setSwLng(bounds.getSw_lng());
	}

	private static com.appspot.hazard_alert.alertendpoint.model.AlertFilter toModel(AlertFilter alertFilter) {
		com.appspot.hazard_alert.alertendpoint.model.AlertFilter model = new com.appspot.hazard_alert.alertendpoint.model.AlertFilter();
		if (null != alertFilter.getInclude()) {
			model.setInclude(toModel(alertFilter.getInclude()));
		}
		if (null != alertFilter.getExclude()) {
			model.setExclude(toModel(alertFilter.getExclude()));
		}
		if (null != alertFilter.getLimit()) {
			model.setLimit(alertFilter.getLimit());
		}
		return model;
	}
}