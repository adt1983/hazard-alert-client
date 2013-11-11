package com.hazardalert;

import java.sql.SQLException;
import java.util.List;

import android.content.Context;
import android.database.Cursor;

import com.hazardalert.common.Assert;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Sender {
	/*
	private static com.j256.ormlite.dao.Dao<Sender, Long> dao = null;

	synchronized public static com.j256.ormlite.dao.Dao<Sender, Long> getDao(Context ctx) {
		if (null == dao) {
			try {
				dao = ORM.getInstance(ctx).getDao(Sender.class);
			}
			catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
		return dao;
	}
	*/
	synchronized public static com.j256.ormlite.dao.Dao<Sender, Long> getDao(Context ctx) throws SQLException {
		return ORM.getInstance(ctx).getDao(Sender.class);
	}

	synchronized public static RuntimeExceptionDao<Sender, Long> getRuntimeDao(Context ctx) {
		return ORM.getInstance(ctx).getRuntimeExceptionDao(Sender.class);
	}

	static public Sender find(Context ctx, String sender) throws SQLException {
		Dao<Sender, Long> dao = getDao(ctx);
		List<Sender> results = dao.queryForEq("sender", sender);
		new Assert(results.size() < 2);
		return results.isEmpty() ? null : results.get(0);
	}

	@DatabaseField(id = true, index = true, unique = true, canBeNull = false)
	private Long id;

	@DatabaseField(uniqueIndex = true, canBeNull = false)
	private String sender;

	@DatabaseField(canBeNull = false)
	private String name;

	@DatabaseField(canBeNull = false)
	private String url;

	@DatabaseField(canBeNull = false)
	private Boolean suppress;

	public Sender() {
		//ORM
	}

	public Sender(Cursor c) {
		// for schema upgrade
		id = c.getLong(c.getColumnIndexOrThrow("id"));
		sender = c.getString(c.getColumnIndexOrThrow("sender"));
		name = c.getString(c.getColumnIndexOrThrow("name"));
		url = c.getString(c.getColumnIndexOrThrow("url"));
		if (-1 == c.getColumnIndex("suppress")) {
			suppress = false;
		}
		else {
			suppress = c.getInt(c.getColumnIndexOrThrow("suppress")) == 1 ? true : false;
		}
	}

	public Sender(Long id, String sender, String name, String url, Boolean suppress) {
		this.id = id;
		this.sender = sender;
		this.name = name;
		this.url = url;
		this.suppress = suppress;
	}

	public Long getId() {
		return id;
	}

	public String getSender() {
		return sender;
	}

	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}

	public Boolean getSuppress() {
		return suppress;
	}

	public void setSuppress(Boolean suppress) {
		this.suppress = suppress;
	}
}
