package com.hazardalert;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class ORM extends OrmLiteSqliteOpenHelper {
	// name of the database file for your application -- change to something appropriate for your app
	private static final String DATABASE_NAME = "hazardAlert.db";

	// any time you make changes to your database objects, you may have to increase the database version
	private static final int DATABASE_VERSION = 2;

	private final Context ctx;

	private static ORM instance = null;

	@SuppressWarnings("unused")
	private static SQLiteDatabase db = null; // do we need this? prevent GC?

	synchronized public static ORM getInstance(Context ctx) {
		if (null == instance) {
			instance = new ORM(ctx.getApplicationContext());
			db = instance.getWritableDatabase();
		}
		return instance;
	}

	private ORM(Context ctx) {
		super(ctx, DATABASE_NAME, null, DATABASE_VERSION/*, R.raw.ormlite_config*/);
		this.ctx = ctx;
		Log.v();
	}

	/**
	 * This is called when the database is first created. Usually you should
	 * call createTable statements here to create the tables that will store
	 * your data.
	 */
	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			Log.v();
			TableUtils.createTable(connectionSource, Sender.class);
		}
		catch (SQLException e) {
			Log.e("Can't create database", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * This is called when your application is upgraded and it has a higher
	 * version number. This allows you to adjust the various data to match the
	 * new version number.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		try {
			Log.v();
			List<Sender> senders = new LinkedList<Sender>();
			Cursor c = null;
			try {
				c = db.rawQuery("SELECT * FROM sender", null);
				c.moveToFirst();
				while (!c.isAfterLast()) {
					// TODO wrap in try/catch and log a nasty exception
					Sender s = new Sender(c);
					senders.add(s);
					c.moveToNext();
				}
			}
			finally {
				if (null != c) {
					c.close();
				}
			}
			TableUtils.dropTable(connectionSource, Sender.class, true);
			onCreate(db, connectionSource); // after we drop the old databases, we create the new ones
			Dao<Sender, Long> senderDao = Sender.getDao(ctx); // must be called after onCreate
			for (Sender s : senders) {
				senderDao.create(s); // reload old data
			}
		}
		catch (SQLException e) {
			Log.e("Can't drop databases", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Close the database connections and clear any cached DAOs.
	 */
	@Override
	public void close() {
		super.close();
	}
}
