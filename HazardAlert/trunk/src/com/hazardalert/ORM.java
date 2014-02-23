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
	private static final int DATABASE_VERSION = 4;

	private final Context ctx;

	private static ORM instance = null;

	@SuppressWarnings("unused")
	private static SQLiteDatabase dbInstance = null; // do we need this? prevent GC?

	synchronized public static ORM getInstance(Context ctx) {
		if (null == instance) {
			instance = new ORM(ctx.getApplicationContext());
			dbInstance = instance.getWritableDatabase();
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
			TableUtils.createTable(connectionSource, Language.class);
			TableUtils.createTable(connectionSource, Sender.class);
			TableUtils.createTable(connectionSource, SupercededBy.class);
		}
		catch (SQLException e) {
			Log.e("Can't create database", e);
			throw new RuntimeException(e);
		}
	}

	private List<Language> getLanguages(SQLiteDatabase db, int oldVersion, int newVersion) {
		List<Language> languages = new LinkedList<Language>();
		if (oldVersion < 4) {
			return languages;
		}
		Cursor c = null;
		try {
			c = db.rawQuery("SELECT * FROM language", null);
			c.moveToFirst();
			while (!c.isAfterLast()) {
				// TODO wrap in try/catch and log a nasty exception
				Language l = new Language(c);
				languages.add(l);
				c.moveToNext();
			}
		}
		finally {
			if (null != c) {
				c.close();
			}
		}
		return languages;
	}

	private List<Sender> getSenders(SQLiteDatabase db, int oldVersion, int newVersion) {
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
		return senders;
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
			List<Language> languages = getLanguages(db, oldVersion, newVersion);
			List<Sender> senders = getSenders(db, oldVersion, newVersion);
			TableUtils.dropTable(connectionSource, Language.class, true);
			TableUtils.dropTable(connectionSource, Sender.class, true);
			TableUtils.dropTable(connectionSource, SupercededBy.class, true);
			onCreate(db, connectionSource); // after we drop the old databases, we create the new ones
			// must be called after onCreate
			Dao<Language, Long> languageDao = Language.getDao(ctx);
			for (Language l : languages) {
				languageDao.create(l); // reload old data
			}
			Dao<Sender, Long> senderDao = Sender.getDao(ctx);
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
