package com.hazardalert;

import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Date;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.google.android.gms.maps.model.LatLngBounds;
import com.google.publicalerts.cap.Alert;
import com.google.publicalerts.cap.Area;
import com.google.publicalerts.cap.Info;
import com.google.publicalerts.cap.Info.Certainty;
import com.google.publicalerts.cap.Info.Severity;
import com.google.publicalerts.cap.Info.Urgency;
import com.hazardalert.activity.HazardDetail;
import com.hazardalert.common.Assert;
import com.hazardalert.common.Bounds;
import com.hazardalert.common.CommonUtil;
import com.hazardalert.common.Point;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryCollection;

public class Hazard {
	private String id;

	public long db_id;

	public Date expires;

	private Date effective;

	private String fullName; // extended message identifier in form "<sender>,<identifier>,<sent>"

	private String sourceUrl;

	public Alert alert;

	public Point bbNE;

	public Point bbSW;

	public Point centroid;

	public boolean shown; // has the alert been shown to the user?

	public boolean visible; // currently affects device

	private GeometryCollection area;

	public boolean notifyActive; // notification id

	public Hazard() {
		alert = null;
	}

	public Hazard(Alert alert, String sourceUrl) {
		new Assert(1 == alert.getInfoCount());
		this.alert = alert;
		this.sourceUrl = sourceUrl;
		initFromAlert();
	}

	public Hazard(Alert alert, int infoIndex, String sourceUrl) {
		new Assert(infoIndex >= 0);
		new Assert(infoIndex < alert.getInfoCount());
		Alert.Builder b = Alert.newBuilder(alert);
		Info info = alert.getInfo(infoIndex);
		b.clearInfo();
		b.addInfo(info);
		this.alert = b.build();
		this.sourceUrl = sourceUrl;
		initFromAlert();
	}

	private void initFromAlert() {
		fullName = getFullName(alert);
		new Assert(1 == alert.getInfoCount());
		Info info = alert.getInfo(0);
		new Assert(info.hasLanguage());
		new Assert(info.hasEffective());
		new Assert(info.hasExpires());
		new Assert(info.hasCertainty());
		new Assert(info.hasUrgency());
		new Assert(info.hasSeverity());
		new Assert(info.hasSenderName());
		new Assert(info.hasEvent());
		new Assert(1 == info.getAreaCount());
		if (getInfo().hasEffective()) {
			setEffective(U.parse3339(getInfo().getEffective()));
		}
		else {
			setEffective(U.parse3339(alert.getSent()));
		}
		setExpires(U.parse3339(getInfo().getExpires()));
		area = CommonUtil.cap_to_jts(alert); //TODO: switch to multi-polygon?
		computeBoundingBox(); // TODO only need to do when saving to DB - does this belong here?
		visible = false;
		shown = false;
		centroid = new Point(area.getCentroid());
	}

	public Alert getAlert() {
		return alert;
	}

	public String getId() {
		String s = Long.toString(db_id);
		new Assert(null != s);
		return s;
	}

	public Date getEffective() {
		return effective;
	}

	public void setEffective(Date effective) {
		this.effective = effective;
	}

	@Override
	public String toString() {
		return "Hazard [id:" + db_id + "] " + getFullName();
	}

	public boolean isExpired() {
		return expires.before(new Date());
	}

	public static String getFullName(Alert a) {
		return a.getSender() + "," + a.getIdentifier() + "," + a.getSent();
	}

	public String getFullName() {
		return fullName;
	}

	public Info getInfo() {
		new Assert(1 == getAlert().getInfoCount());
		return getAlert().getInfo(0);
	}

	public Area getArea() {
		new Assert(1 == getInfo().getAreaCount());
		return getInfo().getArea(0);
	}

	public Date getExpires() {
		return expires;
	}

	public void setExpires(Date expires) {
		this.expires = expires;
	}

	private void computeBoundingBox() {
		Envelope env = area.getEnvelopeInternal();
		bbNE = new Point(env.getMaxY(), env.getMaxX());
		bbSW = new Point(env.getMinY(), env.getMinX());
	}

	public boolean intersects(LatLngBounds bounds) {
		Point ne = new Point(bounds.northeast.latitude, bounds.northeast.longitude);
		Point sw = new Point(bounds.southwest.latitude, bounds.southwest.longitude);
		return area.intersects(new Bounds(ne, sw).toPolygon());
	}

	public String getHeadline() {
		final Info i = getAlert().getInfo(0);
		if (i.hasHeadline()) {
			return i.getHeadline();
		}
		return i.getEvent();
	}

	public Point getCentroid() {
		return this.centroid;
	}

	public String getSourceUrl() {
		return sourceUrl;
	}

	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}

	//TODO: literally an accident waiting to happen, unit test?
	public boolean exceedsThreshold(Urgency u, Severity s, Certainty c) {
		Info info0 = getAlert().getInfo(0);
		return u.getNumber() >= info0.getUrgency().getNumber() && //
				s.getNumber() >= info0.getSeverity().getNumber() && //
				c.getNumber() >= info0.getCertainty().getNumber();
	}

	public boolean exceedsThreshold(Hazard rhs) {
		Info rhsInfo = rhs.getAlert().getInfo(0);
		return exceedsThreshold(rhsInfo.getUrgency(), rhsInfo.getSeverity(), rhsInfo.getCertainty());
	}

	public boolean contains(com.hazardalert.common.Point point) {
		for (int i = 0; i < area.getNumGeometries(); i++) {
			if (area.getGeometryN(i).contains(point.toPointJTS())) {
				return true;
			}
		}
		return false;
	}

	public boolean intersects(Envelope env) {
		for (int i = 0; i < area.getNumGeometries(); i++) {
			if (area.getGeometryN(i).intersects(new Bounds(env).toPolygon())) {
				return true;
			}
		}
		return false;
	}

	public String getEffectiveString() {
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		return df.format(effective);
	}

	public String getExpiresString() {
		/*
		 * Locale.getDefault()
		SimpleDateFormat dateFormatter1 = new SimpleDateFormat("dd MMM yyyy");
		SimpleDateFormat dateFormatter2 = new SimpleDateFormat("hh:mm:ss");
		return dateFormatter1.format(expires) + " at " + dateFormatter2.format(expires);
		 */
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		return df.format(expires);
	}

	/*
	 * Events
	 */
	public void onNew(Context ctx) {
		Language.find(ctx, getInfo().getLanguage()); // create language if it is new
		if (contains(U.getLastLocation(ctx))) {
			onEnter(ctx);
		}
	}

	public void onDelete(Context ctx) {
		cancelNotify(ctx);
	}

	private String getNotificationSound(Context ctx, com.google.publicalerts.cap.Info.Severity severity) {
		switch (severity.ordinal()) {
		case Severity.EXTREME_VALUE:
			return HazardAlert.getPreference(ctx, C.PREF_NOTIF_SOUND_EXTREME, "");
		case Severity.SEVERE_VALUE:
			return HazardAlert.getPreference(ctx, C.PREF_NOTIF_SOUND_SEVERE, "");
		case Severity.MODERATE_VALUE:
			return HazardAlert.getPreference(ctx, C.PREF_NOTIF_SOUND_MODERATE, "");
		case Severity.MINOR_VALUE:
			return HazardAlert.getPreference(ctx, C.PREF_NOTIF_SOUND_MINOR, "");
		default:
			return HazardAlert.getPreference(ctx, C.PREF_NOTIF_SOUND_UNKNOWN, "");
		}
	}

	public boolean isLikelyDuplicate(Hazard rhs) {
		if (!getAlert().getSender().equals(rhs.getAlert().getSender())) {
			return false;
		}
		Info a = getAlert().getInfo(0);
		Info b = rhs.getAlert().getInfo(0);
		if (!a.getHeadline().equals(b.getHeadline())) {
			return false;
		}
		if (a.getSeverity().ordinal() < b.getSeverity().ordinal()) { // Reverse ordering
			return false;
		}
		return true;
	}

	public void onEnter(Context ctx) {
		Log.v("OnHazardEnter: " + getFullName());
		Database db = Database.getInstance(ctx);
		/**/
		final boolean allowNotif = HazardAlert.getPreferenceBoolean(ctx, C.PREF_NOTIF_ALLOW);
		final boolean allowNotifSound = HazardAlert.getPreferenceBoolean(ctx, C.PREF_NOTIF_SOUND_ALLOW);
		if (!allowNotif || shown) {
			return;
		}
		if (isSenderSuppressed(ctx)) {
			return;
		}
		Info info0 = getAlert().getInfo(0);
		if (Language.find(ctx, info0.getLanguage()).getSuppress()) {
			return;
		}
		// Reduce update spam
		if (info0.getSeverity().ordinal() != Severity.EXTREME_VALUE) {
			for (Hazard h : db.getHazardActive(U.getLastLocation(ctx))) {
				if (db_id == h.db_id) {
					continue;
				}
				if ((h.shown || h.notifyActive) && isLikelyDuplicate(h)) {
					return;
				}
			}
		}
		// http://stackoverflow.com/questions/13078230/notification-auto-cancel-does-not-call-deleteintent
		final PendingIntent deleteIntent = PendingIntent.getBroadcast(ctx, 0, OnDeleteNotification.buildIntent(ctx, this), 0);
		final NotificationManager mNM = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx).setSmallIcon(R.drawable.ic_hazardalert)
																				.setAutoCancel(true)
																				.setDeleteIntent(deleteIntent)
																				.setContentTitle(info0.getEvent())
																				.setContentText("Sev: " + info0.getSeverity() + " Urg: "
																						+ info0.getUrgency());
		builder.setPriority(NotificationCompat.PRIORITY_HIGH);
		if (allowNotifSound) {
			String soundUri = getNotificationSound(ctx, info0.getSeverity());
			if (!soundUri.isEmpty()) {
				builder.setSound(Uri.parse(soundUri));
			}
		}
		Intent resultIntent = HazardDetail.buildIntent(ctx, this);
		resultIntent.setData(new Uri.Builder().scheme("data").appendQueryParameter("unique", getId()).build()); // http://stackoverflow.com/questions/12968280/android-multiple-notifications-and-with-multiple-intents
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(ctx);
		stackBuilder.addParentStack(HazardDetail.class);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(pendingIntent);
		visible = true;
		notifyActive = true;
		db.updateHazard(this);
		mNM.notify((int) db_id, builder.build());
	}

	private boolean isSenderSuppressed(Context ctx) {
		try {
			Sender s = Sender.find(ctx, getAlert().getSender());
			return null == s ? false : s.getSuppress(); // default to not suppressed
		}
		catch (SQLException e) {
			Log.e(); //FIXME log error
			return false; // default to not suppressed
		}
	}

	public void onExit(Context ctx) {
		Log.v("OnHazardExit: " + getFullName());
		Database db = Database.getInstance(ctx);
		visible = false;
		cancelNotify(ctx);
		db.updateHazard(this);
	}

	public void onShown(Context ctx) {
		Log.v("OnHazardShown: " + getFullName());
		Database db = Database.getInstance(ctx);
		shown = true;
		db.updateHazard(this);
	}

	private void cancelNotify(Context ctx) {
		if (notifyActive) {
			final NotificationManager mNM = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
			mNM.cancel((int) db_id);
			notifyActive = false;
		}
	}
}
