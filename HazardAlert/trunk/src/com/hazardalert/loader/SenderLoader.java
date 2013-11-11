package com.hazardalert.loader;

import java.sql.SQLException;
import java.util.List;

import android.content.Context;

import com.hazardalert.AlertAPI;
import com.hazardalert.Log;
import com.hazardalert.Sender;
import com.hazardalert.SimpleResultLoader;
import com.hazardalert.U;
import com.j256.ormlite.dao.Dao;

public class SenderLoader extends SimpleResultLoader<List<Sender>> {
	private final Boolean localOnly;

	public SenderLoader(Context context, boolean localOnly) {
		super(context);
		this.localOnly = localOnly;
	}

	@Override
	public List<Sender> loadInBackground() {
		List<Sender> senders = null;
		if (!localOnly) {
			loadRemote();
		}
		try {
			Dao<Sender, Long> dao = Sender.getDao(getContext());
			senders = U.toNonNull(dao.queryForAll());
			if (0 == senders.size()) {
				loadRemote();
				senders = U.toNonNull(dao.queryForAll());
			}
		}
		catch (SQLException e) {
			Log.e("Failed to load local senders!", e);
		}
		return senders;
	}

	private void loadRemote() {
		try {
			Dao<Sender, Long> dao = Sender.getDao(getContext());
			for (Sender s : new AlertAPI().senderList()) {
				dao.createIfNotExists(s);
			}
		}
		catch (Exception e) {
			Log.d("Failed to load remote senders.", e);
			// no big deal? likely haven't changed?
		}
	}
}
