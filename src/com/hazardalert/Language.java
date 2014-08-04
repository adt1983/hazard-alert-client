package com.hazardalert;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;

import com.hazardalert.common.Assert;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Language {
	synchronized public static com.j256.ormlite.dao.Dao<Language, Long> getDao(Context ctx) throws SQLException {
		return ORM.getInstance(ctx).getDao(Language.class);
	}

	synchronized public static RuntimeExceptionDao<Language, Long> getRuntimeDao(Context ctx) {
		return ORM.getInstance(ctx).getRuntimeExceptionDao(Language.class);
	}

	public static Language find(Context ctx, String lang) {
		try {
			Language l = null;
			Dao<Language, Long> dao = getDao(ctx);
			List<Language> results = dao.queryForEq("language", lang);
			if (0 == results.size()) {
				// first alert with this language
				Log.d("Creating Language: " + lang);
				l = new Language(lang, false);
				dao.create(l);
			}
			else {
				l = results.get(0);
			}
			new Assert(null != l);
			return l;
		}
		catch (SQLException e) {
			Log.e("Error querying for language: " + lang);
			throw new RuntimeException(e);
		}
	}

	@DatabaseField(generatedId = true)
	private Long id;

	@DatabaseField(uniqueIndex = true, canBeNull = false)
	private String language; // RFC 3066 code

	@DatabaseField(canBeNull = false)
	private Boolean suppress; // can hazards with this language raise notifications?

	public Language() {
		//ORM
	}

	public Language(Cursor c) {
		// for schema upgrade
		id = c.getLong(c.getColumnIndexOrThrow("id"));
		language = c.getString(c.getColumnIndexOrThrow("language"));
		suppress = c.getInt(c.getColumnIndexOrThrow("suppress")) == 1 ? true : false;
	}

	public Language(String language, Boolean suppress) {
		this.language = language;
		this.suppress = suppress;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public Boolean getSuppress() {
		return suppress;
	}

	public void setSuppress(Boolean suppress) {
		this.suppress = suppress;
	}

	public Locale getLocale() {
		new Assert(language.length() == 5, language); // TODO: RFC 3066 compliant? Check on server?
		new Assert('-' == language.charAt(2));
		Locale locale = new Locale(language.substring(0, 2), language.substring(3, 5));
		return locale;
	}

	public String getDisplayLanguage() {
		Locale l = getLocale();
		return l.getDisplayLanguage(l);
	}

	public String getDisplayCountry() {
		Locale l = getLocale();
		return l.getDisplayCountry(l);
	}
}
