package com.hazardalert;

import static com.hazardalert.Util.toPoint;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.publicalerts.cap.Alert;
import com.google.publicalerts.cap.Alert.MsgType;
import com.hazardalert.common.AlertFilter;
import com.hazardalert.common.Assert;
import com.hazardalert.common.Bounds;

// TODO pathological coupling with HazardTable - needs to be a better pattern for this. ORMLite?
public class Database {
	private SQLiteDatabase db;

	private final HazardTable helper;

	private static Database instance = null;

	synchronized public static Database getInstance(Context c) {
		if (null == instance) {
			instance = new Database(c.getApplicationContext());
			instance.open();
		}
		return instance;
	}

	//bad idea?
	public static Database getInstance() {
		if (null == instance) {
			throw new RuntimeException();
		}
		return instance;
	}

	private Database(Context context) {
		helper = new HazardTable(context);
	}

	private void open() {
		db = helper.getWritableDatabase();
	}

	private void close() {
		helper.close();
	}

	public void insertAlert(Context context, com.google.publicalerts.cap.Alert alert) {
		// duplicate?
		Log.v("Got alert: " + Hazard.getFullName(alert));
		if (alreadyExistsFullName(Hazard.getFullName(alert))) {
			Log.v("Duplicate: " + Hazard.getFullName(alert));
			return;
		}
		for (int i = 0; i < alert.getInfoCount(); i++) {
			insertHazard(context, new Hazard(alert, i));
		}
		// handle updates
		if (alert.getMsgType() == MsgType.CANCEL || alert.getMsgType() == MsgType.UPDATE) {
			Log.v("Got update: " + Hazard.getFullName(alert));
			for (String s : alert.getReferences().getValueList()) {
				Log.v("Cancelling: " + s);
				List<Hazard> results = getByFullName(s);
				for (Hazard h : results) {
					deleteHazard(h);
				}
			}
		}
	}

	// http://stackoverflow.com/questions/3860008/bulk-insertion-on-android-device
	// http://stackoverflow.com/questions/5596354/insertion-of-thousands-of-contact-entries-using-applybatch-is-slow
	public void insertAlerts(Context context, List<com.google.publicalerts.cap.Alert> alerts) {
		try {
			db.beginTransaction();
			for (com.google.publicalerts.cap.Alert a : alerts) {
				insertAlert(context, a);
			}
			db.setTransactionSuccessful();
		}
		finally {
			db.endTransaction();
		}
	}

	private void insertHazard(Context context, Hazard h) {
		insertHazardInternal(h);
		h.onNew(context);
	}

	/*
	private final String[] allColumns = { HazardTable.COLUMN_ID, HazardTable.COLUMN_ALERT, HazardTable.COLUMN_EXPIRES,
			HazardTable.COLUMN_EFFECTIVE, HazardTable.COLUMN_ALERT_FULL_NAME, HazardTable.COLUMN_BB_NE_LAT, HazardTable.COLUMN_BB_NE_LNG,
			HazardTable.COLUMN_BB_SW_LAT, HazardTable.COLUMN_BB_SW_LNG, HazardTable.COLUMN_VISIBLE };*/
	// columns necessary to instantiate Hazard objects
	private final String[] builderColumns = { HazardTable.COLUMN_ID, HazardTable.COLUMN_ALERT, HazardTable.COLUMN_VISIBLE };

	private final String[] headerColumns = { HazardTable.COLUMN_ID, HazardTable.COLUMN_EXPIRES, HazardTable.COLUMN_ALERT_FULL_NAME,
			HazardTable.COLUMN_BB_NE_LAT, HazardTable.COLUMN_BB_NE_LNG, HazardTable.COLUMN_BB_SW_LAT, HazardTable.COLUMN_BB_SW_LNG,
			HazardTable.COLUMN_VISIBLE };

	public static Hazard cursorToHazard(Cursor c) {
		Hazard h = null;
		try {
			h = new Hazard(Alert.newBuilder().mergeFrom(c.getBlob(c.getColumnIndexOrThrow(HazardTable.COLUMN_ALERT))).build());
			h.db_id = c.getLong(c.getColumnIndexOrThrow(HazardTable.COLUMN_ID));
			h.visible = (c.getInt(c.getColumnIndexOrThrow(HazardTable.COLUMN_VISIBLE)) == 1) ? true : false;
		}
		catch (InvalidProtocolBufferException e) {
			throw new RuntimeException(e);
		}
		return h;
	}

	public static Hazard slimCursorToHazard(Cursor c) {
		Hazard h = new Hazard();
		h.db_id = c.getLong(c.getColumnIndexOrThrow(HazardTable.COLUMN_ID));
		h.visible = (c.getInt(c.getColumnIndexOrThrow(HazardTable.COLUMN_VISIBLE)) == 1) ? true : false;
		return h;
	}

	private ContentValues toContentValues(Hazard h) {
		ContentValues values = new ContentValues();
		values.put(HazardTable.COLUMN_ALERT, h.alert.toByteArray());
		values.put(HazardTable.COLUMN_EXPIRES, h.getExpires().getTime());
		values.put(HazardTable.COLUMN_EFFECTIVE, h.getEffective().getTime());
		values.put(HazardTable.COLUMN_ALERT_FULL_NAME, h.getFullName());
		values.put(HazardTable.COLUMN_BB_NE_LAT, h.bbNE.getLat());
		values.put(HazardTable.COLUMN_BB_NE_LNG, h.bbNE.getLng());
		values.put(HazardTable.COLUMN_BB_SW_LAT, h.bbSW.getLat());
		values.put(HazardTable.COLUMN_BB_SW_LNG, h.bbSW.getLng());
		values.put(HazardTable.COLUMN_VISIBLE, h.visible ? 1 : 0);
		values.put(HazardTable.COLUMN_STATUS, h.getAlert().getStatus().getNumber());
		values.put(HazardTable.COLUMN_URGENCY, h.getInfo().getUrgency().getNumber());
		values.put(HazardTable.COLUMN_SEVERITY, h.getInfo().getSeverity().getNumber());
		values.put(HazardTable.COLUMN_CERTAINTY, h.getInfo().getCertainty().getNumber());
		return values;
	}

	private void insertHazardInternal(Hazard h) {
		Cursor c = null;
		try {
			ContentValues values = toContentValues(h);
			h.db_id = db.insert(HazardTable.TABLE_HAZARD, null, values);
			Log.d("Inserted " + h.toString());
		}
		finally {
			if (null != c) {
				c.close();
			}
		}
	}

	public void updateHazard(Hazard h) {
		ContentValues values = toContentValues(h);
		if (1 != db.update(HazardTable.TABLE_HAZARD, values, HazardTable.COLUMN_ID + " = ?", new String[] { Long.toString(h.db_id) })) {
			throw new RuntimeException();
		}
	}

	public void deleteHazard(Hazard h) {
		Log.d("Deleting " + h.toString());
		if (1 != db.delete(HazardTable.TABLE_HAZARD, HazardTable.COLUMN_ID + " = ?", new String[] { Long.toString(h.db_id) })) {
			throw new RuntimeException();
		}
	}

	public static List<Hazard> cursorToList(Cursor c) {
		new Assert(null != c);
		try {
			List<Hazard> hazards = new ArrayList<Hazard>();
			c.moveToFirst();
			while (!c.isAfterLast()) {
				Hazard h = cursorToHazard(c);
				hazards.add(h);
				c.moveToNext();
			}
			return hazards;
		}
		finally {
			c.close();
		}
	}

	public static List<Hazard> slimCursorToList(Cursor c) {
		try {
			List<Hazard> hazards = new ArrayList<Hazard>();
			c.moveToFirst();
			while (!c.isAfterLast()) {
				Hazard h = slimCursorToHazard(c);
				hazards.add(h);
				c.moveToNext();
			}
			return hazards;
		}
		finally {
			c.close();
		}
	}

	//TODO can only load 579 Hazards at a time due to 1MB cursor limitation
	/*public List<Hazard> getAllHazards() {
		Cursor c = getAllHazardsCursor();
		return cursorToList(c);
	}
	public Cursor getAllHazardsCursor() {
		return db.rawQuery("select * from hazard", null);
	}*/
	public void deleteExpired() {
		Log.d();
		db.delete(	HazardTable.TABLE_HAZARD,
					HazardTable.COLUMN_EXPIRES + " < ?",
					new String[] { Long.toString(new Date().getTime() - (C.ONE_HOUR_MS)) }); // give alerts a one hour window
	}

	public boolean alreadyExists(String id) {
		Cursor c = null;
		try {
			c = db.rawQuery("select _id from hazard where _id = ?", new String[] { id });
			if (c.getCount() > 1) {
				throw new RuntimeException();
			}
			return c.getCount() == 1;
		}
		finally {
			if (null != c) {
				c.close();
			}
		}
	}

	public boolean alreadyExistsFullName(String alertFullName) {
		return !getByFullName(alertFullName).isEmpty();
	}

	public List<Hazard> getByFullName(String alertFullName) {
		Cursor c = null;
		try {
			return cursorToList(db.rawQuery("select * from hazard where alertFullname = ?", new String[] { alertFullName }));
		}
		finally {
			if (null != c) {
				c.close();
			}
		}
	}

	public Hazard getByHazardId(String hazardId) {
		Hazard h = null;
		Cursor c = null;
		try {
			c = db.rawQuery("select * from hazard where _id = ?", new String[] { hazardId });
			if (c.getCount() == 1) {
				c.moveToFirst();
				h = cursorToHazard(c);
			}
			return h;
		}
		finally {
			if (null != c) {
				c.close();
			}
		}
	}

	public Hazard safeGetByHazardId(String hazardId) {
		Hazard h = getByHazardId(hazardId);
		if (h == null) {
			throw new RuntimeException(hazardId);
		}
		return h;
	}

	public Cursor getByVisible(boolean visible) {
		String s = Integer.toString(visible ? 1 : 0);
		if (visible) {
			return db.rawQuery("select * from hazard where visible = ?", new String[] { s });
		}
		else {
			return db.query(HazardTable.TABLE_HAZARD, headerColumns, "visible = ?", new String[] { s }, null, null, null);
		}
	}

	public List<Hazard> getListByVisible(boolean visible) {
		if (visible) {
			return cursorToList(getByVisible(visible));
		}
		else {
			return slimCursorToList(getByVisible(visible));
		}
	}

	public List<Hazard> getHazardEntering(Location loc) {
		final String lat = Double.toString(loc.getLatitude());
		final String lng = Double.toString(loc.getLongitude());
		List<Hazard> nearby = cursorToList(db.rawQuery(	"SELECT * FROM hazard WHERE visible = ? AND bb_ne_lat > ? AND bb_ne_lng > ? AND bb_sw_lat < ? AND bb_sw_lng < ? and expires > ?",
														new String[] { "0", lat, lng, lat, lng, Long.toString(new Date().getTime()) }));
		List<Hazard> entering = new LinkedList<Hazard>();
		for (Hazard h : nearby) {
			if (h.contains(toPoint(loc))) {
				entering.add(h);
			}
		}
		return entering;
	}

	public List<Hazard> getHazardExiting(Location loc) {
		List<Hazard> active = getListByVisible(true);
		List<Hazard> exiting = new LinkedList<Hazard>();
		for (Hazard h : active) {
			if (!h.contains(toPoint(loc)) || h.isExpired()) {
				exiting.add(h);
			}
		}
		return exiting;
	}

	public List<Hazard> list(AlertFilter filter) {
		ArrayList<String> strings = new ArrayList<String>();
		String sql = "1 = 1";
		if (null != filter.getInclude()) {
			Bounds b = filter.getInclude();
			sql += " AND NOT (bb_ne_lat < ?) AND NOT (bb_ne_lng < ?) AND NOT (bb_sw_lat > ?) AND NOT (bb_sw_lng > ?)";
			strings.add(Double.toString(b.getSw_lat()));
			strings.add(Double.toString(b.getSw_lng()));
			strings.add(Double.toString(b.getNe_lat()));
			strings.add(Double.toString(b.getNe_lng()));
		}
		if (null != filter.getExclude()) {
			throw new RuntimeException();
			//sql = sql.concat(" AND NOT Intersects(area, GeomFromText(?))");
		}
		if (null != filter.getMinEffective()) {
			sql += " AND effective > ?";
			strings.add(filter.getMinEffective().toString());
		}
		if (null != filter.getMinExpires()) {
			sql += " AND expires > ?";
			strings.add(filter.getMinExpires().toString());
		}
		if (null != filter.getStatus()) {
			sql += " AND status IN (";
			for (int i = 0; i < filter.getStatus().size(); i++) {
				sql += (0 == i) ? "?" : ", ?";
				strings.add(filter.getStatus().get(i).toString());
			}
			sql += ")";
		}
		if (null != filter.getUrgency()) {
			sql += " AND urgency IN (";
			for (int i = 0; i < filter.getUrgency().size(); i++) {
				sql += (0 == i) ? "?" : ", ?";
				strings.add(filter.getUrgency().get(i).toString());
			}
			sql += ")";
		}
		if (null != filter.getSeverity()) {
			sql += " AND severity IN (";
			for (int i = 0; i < filter.getSeverity().size(); i++) {
				sql += (0 == i) ? "?" : ", ?";
				strings.add(filter.getSeverity().get(i).toString());
			}
			sql += ")";
		}
		if (null != filter.getCertainty()) {
			sql += " AND certainty IN (";
			for (int i = 0; i < filter.getCertainty().size(); i++) {
				sql += (0 == i) ? "?" : ", ?";
				strings.add(filter.getCertainty().get(i).toString());
			}
			sql += ")";
		}
		String selectionArgs[] = new String[strings.size()];
		for (int i = 0; i < strings.size(); i++) {
			selectionArgs[i] = strings.get(i);
		}
		String limit = null;
		if (null != filter.getLimit()) {
			limit = filter.getLimit().toString();
		}
		List<Hazard> results = cursorToList(db.query("hazard", builderColumns, sql, selectionArgs, null, null, null, limit));
		if (null != filter.getInclude()) {
			//eliminate <area> with mulitple <polygon> that straddle our box but don't intersect
			for (Iterator<Hazard> h = results.iterator(); h.hasNext();) {
				if (!h.next().intersects(filter.getInclude().toEnvelope())) {
					h.remove();
				}
			}
		}
		return results;
	}
}
