package com.hazardalert;

import java.text.DateFormat;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.google.android.gms.maps.model.LatLngBounds;
import com.google.publicalerts.cap.Alert;
import com.google.publicalerts.cap.Area;
import com.google.publicalerts.cap.Info;
import com.google.publicalerts.cap.Info.Certainty;
import com.google.publicalerts.cap.Info.Severity;
import com.google.publicalerts.cap.Info.Urgency;
import com.hazardalert.common.Assert;
import com.hazardalert.common.Bounds;
import com.hazardalert.common.CommonUtil;
import com.hazardalert.common.Point;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;

public class Hazard {
	private String id;

	public long db_id;

	public Date expires;

	private Date effective;

	private String fullName; // extended message identifier in form "<sender>,<identifier>,<sent>"

	public Alert alert;

	public Point bbNE;

	public Point bbSW;

	public Point centroid;

	public boolean shown; // has the alert been shown to the user

	public boolean visible; // currently affects device

	private GeometryCollection area;

	public String getId() {
		return Long.toString(db_id);
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

	public Hazard() {
		alert = null;
	}

	public Alert getAlert() {
		if (null == alert) {
			Hazard h = Database.getInstance().safeGetByHazardId(getId());
			alert = h.alert;
		}
		return alert;
	}

	public Hazard(Alert alert) {
		new Assert(1 == alert.getInfoCount());
		this.alert = alert;
		initFromAlert();
	}

	public Hazard(Alert alert, int infoIndex) {
		new Assert(infoIndex >= 0);
		new Assert(infoIndex < alert.getInfoCount());
		Alert.Builder b = Alert.newBuilder(alert);
		Info info = alert.getInfo(infoIndex);
		b.clearInfo();
		b.addInfo(info);
		this.alert = b.build();
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
			setEffective(Util.parse3339(getInfo().getEffective()));
		}
		else {
			setEffective(Util.parse3339(alert.getSent()));
		}
		setExpires(Util.parse3339(getInfo().getExpires()));
		area = CommonUtil.cap_to_jts(alert); //TODO: switch to multi-polygon?
		computeBoundingBox(); // TODO only need to do when saving to DB - does this belong here?
		visible = false;
		centroid = new Point(area.getCentroid());
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
		Coordinate ne = new Point(bounds.northeast.latitude, bounds.northeast.longitude).toCoordinate();
		Coordinate sw = new Point(bounds.southwest.latitude, bounds.southwest.longitude).toCoordinate();
		Geometry g = CommonUtil.createBoundingBox(ne, sw);
		return area.intersects(g);
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

	//TODO: literally an accident waiting to happen, unit test?
	public boolean exceedsThreshold(Urgency u, Severity s, Certainty c) {
		Info info0 = getAlert().getInfo(0);
		return u.getNumber() >= info0.getUrgency().getNumber() && //
				s.getNumber() >= info0.getSeverity().getNumber() && //
				c.getNumber() >= info0.getCertainty().getNumber();
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
		Location loc = Util.getLocation(ctx);
		if (contains(Util.toPoint(loc))) {
			onEnter(ctx);
		}
	}

	public void onEnter(Context ctx) {
		Log.v("OnHazardEnter: " + getFullName());
		Database db = Database.getInstance(ctx);
		visible = true;
		db.updateHazard(this);
		Intent hazardVisible = new Intent(ctx, OnHazardVisible.class);
		hazardVisible.putExtra("id", getId());
		ctx.startService(hazardVisible);
	}

	public void onExit(Context ctx) {
		Log.v("OnHazardExit: " + getFullName());
		Database db = Database.getInstance(ctx);
		visible = false;
		db.updateHazard(this);
	}
}
