package com.hazardalert;

import java.sql.SQLException;
import java.util.List;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

// TODO merge into Hazard ORM
@DatabaseTable
public class SupercededBy {
	synchronized public static com.j256.ormlite.dao.Dao<SupercededBy, Long> getDao(Context ctx) throws SQLException {
		return ORM.getInstance(ctx).getDao(SupercededBy.class);
	}

	synchronized public static RuntimeExceptionDao<SupercededBy, Long> getRuntimeDao(Context ctx) {
		return ORM.getInstance(ctx).getRuntimeExceptionDao(SupercededBy.class);
	}

	static public void add(Context ctx, String supercededBy, String superceded) throws SQLException {
		Dao<SupercededBy, Long> dao = getDao(ctx);
		SupercededBy sb = new SupercededBy(supercededBy, superceded);
		dao.create(sb);
	}

	static public void remove(Context ctx, String supercededBy) throws SQLException {
		Dao<SupercededBy, Long> dao = getDao(ctx);
		List<SupercededBy> results = dao.queryForEq("supercededBy", supercededBy);
		for (SupercededBy sb : results) {
			dao.delete(sb);
		}
	}

	static public boolean isSuperceded(Context ctx, String superceded) {
		RuntimeExceptionDao<SupercededBy, Long> dao = getRuntimeDao(ctx);
		List<SupercededBy> results = dao.queryForEq("superceded", superceded);
		return !results.isEmpty();
	}

	@DatabaseField(generatedId = true)
	private Long id;

	@DatabaseField(index = true, canBeNull = false, uniqueCombo = true)
	private String supercededBy;

	@DatabaseField(index = true, canBeNull = false, uniqueCombo = true)
	private String superceded;

	public SupercededBy() {
		//ORM
	}

	public SupercededBy(String supercededBy, String superceded) {
		this.supercededBy = supercededBy;
		this.superceded = superceded;
	}

	public String getSupercededBy() {
		return supercededBy;
	}

	public void setSupercededBy(String supercededBy) {
		this.supercededBy = supercededBy;
	}

	public String getSuperceded() {
		return superceded;
	}

	public void setSuperceded(String superceded) {
		this.superceded = superceded;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
