package com.hazardalert;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HazardTable extends SQLiteOpenHelper {
	public static final String TABLE_HAZARD = "hazard";

	public static final String COLUMN_ID = "_id";

	public static final String COLUMN_ALERT = "alert";

	public static final String COLUMN_EXPIRES = "expires";

	public static final String COLUMN_EFFECTIVE = "effective";

	public static final String COLUMN_ALERT_FULL_NAME = "alertFullName";

	public static final String COLUMN_BB_NE_LAT = "bb_ne_lat";

	public static final String COLUMN_BB_NE_LNG = "bb_ne_lng";

	public static final String COLUMN_BB_SW_LAT = "bb_sw_lat";

	public static final String COLUMN_BB_SW_LNG = "bb_sw_lng";

	public static final String COLUMN_STATUS = "status";

	public static final String COLUMN_URGENCY = "urgency";

	public static final String COLUMN_SEVERITY = "severity";

	public static final String COLUMN_CERTAINTY = "certainty";

	public static final String COLUMN_VISIBLE = "visible";

	public static final String COLUMN_SOURCE_URL = "sourceUrl";

	private static final String DATABASE_NAME = "hazard.db";

	private static final int DATABASE_VERSION = 11;

	public HazardTable(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table " + TABLE_HAZARD + "(" /**/
				+ COLUMN_ID + " integer primary key autoincrement, " /**/
				+ COLUMN_ALERT + " text not null, " /**/
				+ COLUMN_EXPIRES + " integer not null, " /**/
				+ COLUMN_EFFECTIVE + " integer not null, " /**/
				+ COLUMN_ALERT_FULL_NAME + " text not null, " /**/
				+ "sourceUrl text, " /**/
				+ "bb_ne_lat real not null, " /**/
				+ "bb_ne_lng real not null, " /**/
				+ "bb_sw_lat real not null, " /**/
				+ "bb_sw_lng real not null, " /**/
				+ "status integer not null, " /**/
				+ "urgency integer not null, " /**/
				+ "severity integer not null, " /**/
				+ "certainty integer not null, " /**/
				+ "visible integer not null);");
		db.execSQL("CREATE INDEX hazard_alert_idx ON hazard(alertFullName);");
		db.execSQL("CREATE INDEX hazard_expires_idx ON hazard(expires);");
		db.execSQL("CREATE INDEX hazard_effective_idx ON hazard(effective);");
		db.execSQL("CREATE INDEX hazard_bb_ne_lat_idx ON hazard(bb_ne_lat);");
		db.execSQL("CREATE INDEX hazard_bb_ne_lng_idx ON hazard(bb_ne_lng);");
		db.execSQL("CREATE INDEX hazard_bb_sw_lat_idx ON hazard(bb_sw_lat);");
		db.execSQL("CREATE INDEX hazard_bb_sw_lng_idx ON hazard(bb_sw_lng);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("drop table if exists " + TABLE_HAZARD);
		onCreate(db);
	}
}
