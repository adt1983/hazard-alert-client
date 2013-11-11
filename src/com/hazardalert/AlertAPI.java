package com.hazardalert;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.appspot.hazard_alert.alertendpoint.Alertendpoint;
import com.appspot.hazard_alert.alertendpoint.model.AlertTransport;
import com.appspot.hazard_alert.alertendpoint.model.Sender;
import com.appspot.hazard_alert.alertendpoint.model.Subscription;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
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

	AlertTransport alertFind(String fullName) throws IOException {
		return service.alert().find().setFullName(fullName).execute();
	}

	List<AlertTransport> list(AlertFilter filter) throws IOException {
		return U.toNonNull(service.alert().list(toModel(filter)).execute().getItems());
	}

	public List<com.hazardalert.Sender> senderList() throws IOException {
		return fromSenderList(service.sender().list().execute().getItems());
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

	List<AlertTransport> updateSubscription(Subscription s, Envelope env) throws IOException {
		return U.toNonNull((service.alert().updateSubscription(s.getId(), s.getGcm(), toModel(new Bounds(env))).execute().getItems()));
	}

	private static List<com.hazardalert.Sender> fromSenderList(List<Sender> senderList) {
		List<com.hazardalert.Sender> l = new LinkedList<com.hazardalert.Sender>();
		if (null == senderList) {
			return l;
		}
		for (Sender s : senderList) {
			if (null != s) {
				l.add(new com.hazardalert.Sender(s.getId(), s.getSender(), s.getName(), s.getUrl(), s.getSuppress()));
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