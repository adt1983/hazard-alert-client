package com.hazardalert.loader;

import java.sql.SQLException;
import java.util.List;

import android.content.Context;

import com.hazardalert.Language;
import com.hazardalert.Log;
import com.hazardalert.SimpleResultLoader;
import com.hazardalert.U;
import com.j256.ormlite.dao.Dao;

public class LanguageLoader extends SimpleResultLoader<List<Language>> {
	public LanguageLoader(Context context) {
		super(context);
	}

	@Override
	public List<Language> loadInBackground() {
		List<Language> languages = null;
		try {
			Dao<Language, Long> dao = Language.getDao(getContext());
			languages = U.toNonNull(dao.queryForAll());
		}
		catch (SQLException e) {
			Log.e("Failed to load languages!", e);
		}
		return languages;
	}
}