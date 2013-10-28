package com.hazardalert;

import java.util.Calendar;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.hazardalert.common.AlertFilter;

public class MainActivity extends FragmentActivity {
	private static final String KEY_VISIBLE_FRAGMENT_TAG = "KEY_VISIBLE_FRAGMENT_TAG";

	private DataManagerFragment dataFragment;

	private BaseMapFragment mapFragment;

	private HazardListFragment listFragment;

	private Fragment visibleFragment = null;

	private int mYear, mMonth, mDay;

	public static String DATE_PICKER_DIALOG = "datePicker";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d();
		setContentView(R.layout.activity_main);
		setupFragments();
		showFragment(getVisibleFragment(savedInstanceState));
	}

	private Fragment getVisibleFragment(Bundle savedInstanceState) {
		if (null == savedInstanceState) {
			return mapFragment;
		}
		String visibleFragmentTag = savedInstanceState.getString(KEY_VISIBLE_FRAGMENT_TAG);
		if (mapFragment.getTag().equals(visibleFragmentTag)) {
			return mapFragment;
		}
		else if (listFragment.getTag().equals(visibleFragmentTag)) {
			return listFragment;
		}
		else {
			throw new RuntimeException();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d();
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.d();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putString(KEY_VISIBLE_FRAGMENT_TAG, this.visibleFragment.getTag());
	}

	private void setupFragments() {
		final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		// If the activity is killed while in BG, it's possible that the
		// fragment still remains in the FragmentManager, so, we don't need to
		// add it again.
		dataFragment = (DataManagerFragment) getSupportFragmentManager().findFragmentByTag(DataManagerFragment.TAG);
		if (dataFragment == null) {
			dataFragment = new DataManagerFragment();
			ft.add(dataFragment, DataManagerFragment.TAG);
		}
		mapFragment = (BaseMapFragment) getSupportFragmentManager().findFragmentByTag(BaseMapFragment.TAG);
		if (mapFragment == null) {
			mapFragment = new BaseMapFragment();
			mapFragment.setDataManager(dataFragment);
			ft.add(R.id.fragment_container, mapFragment, BaseMapFragment.TAG);
		}
		ft.hide(mapFragment);
		listFragment = (HazardListFragment) getSupportFragmentManager().findFragmentByTag(HazardListFragment.TAG);
		if (listFragment == null) {
			listFragment = new HazardListFragment();
			listFragment.setDataManager(dataFragment);
			ft.add(R.id.fragment_container, listFragment, HazardListFragment.TAG);
		}
		ft.hide(listFragment);
		ft.commit();
	}

	private void showFragment(Fragment f) {
		if (f == null) {
			throw new RuntimeException();
		}
		final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
		if (visibleFragment != null)
			ft.hide(visibleFragment);
		ft.show(f).commit();
		visibleFragment = f;
		invalidateOptionsMenu();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d();
		getMenuInflater().inflate(R.menu.activity_main, menu);
		if (this.visibleFragment == this.mapFragment) {
			menu.findItem(R.id.action_list).setVisible(true);
			menu.findItem(R.id.action_map).setVisible(false);
		}
		else if (this.visibleFragment == this.listFragment) {
			menu.findItem(R.id.action_list).setVisible(false);
			menu.findItem(R.id.action_map).setVisible(true);
		}
		else {
			throw new RuntimeException();
		}
		return true;
	}

	public boolean onMenuSettings(MenuItem menuItem) {
		startActivity(new Intent(this, SettingsActivity.class));
		return true;
	}

	public boolean onShowList(MenuItem view) {
		Log.d();
		showFragment(listFragment);
		return true;
	}

	public boolean onShowMap(MenuItem view) {
		Log.d();
		showFragment(mapFragment);
		return true;
	}

	public boolean onActiveHazards(MenuItem menuItem) {
		startActivity(new Intent(this, ActiveHazardActivity.class));
		return true;
	}

	public void onLegalNotices(MenuItem munuItem) {
		showDialog(2); //TODO working on to replace this deprecated method
	}

	public void filterByEffectiveDate(MenuItem menuItem) {
		final Calendar calendar = Calendar.getInstance();
		mYear = calendar.get(Calendar.YEAR);
		mMonth = calendar.get(Calendar.MONTH);
		mDay = calendar.get(Calendar.DAY_OF_MONTH);
		showDialog(1); //TODO working on to replace this deprecated method
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case 1:
			return new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
				@Override
				public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
					Calendar cal = Calendar.getInstance();
					cal.set(selectedYear, selectedMonth, selectedDay);
					if (visibleFragment == mapFragment) {
						AlertFilter filter = mapFragment.getDataManager().getFilter();
						filter.setMinEffective(cal.getTimeInMillis());
						mapFragment.getDataManager().setFilter(filter);
					}
					else if (visibleFragment == listFragment) {
						AlertFilter filter = listFragment.getDataManager().getFilter();
						filter.setMinEffective(cal.getTimeInMillis());
						listFragment.getDataManager().setFilter(filter);
					}
					else {
						throw new RuntimeException();
					}
				}
			}, mYear, mMonth, mDay);
		case 2:
			String LicenseInfo = GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(getApplicationContext());
			AlertDialog.Builder LicenseDialog = new AlertDialog.Builder(MainActivity.this);
			LicenseDialog.setTitle("Legal Notices");
			LicenseDialog.setMessage(LicenseInfo);
			LicenseDialog.show();
		}
		return null;
	}
}
