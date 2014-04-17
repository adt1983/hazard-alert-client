package com.hazardalert.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.hazardalert.BaseMapFragment;
import com.hazardalert.C;
import com.hazardalert.DataManagerFragment;
import com.hazardalert.HazardListFragment;
import com.hazardalert.Log;
import com.hazardalert.R;

public class MainActivity extends HazardAlertFragmentActivity {
	private static final String KEY_VISIBLE_FRAGMENT_TAG = "KEY_VISIBLE_FRAGMENT_TAG";

	private DataManagerFragment dataFragment;

	private BaseMapFragment mapFragment;

	private HazardListFragment listFragment;

	private Fragment visibleFragment = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v();
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
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putString(KEY_VISIBLE_FRAGMENT_TAG, this.visibleFragment.getTag());
	}

	private void setupFragments() {
		final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		// If the activity is killed while in BG, it's possible that the
		// fragment still remains in the FragmentManager, so, we don't need to
		// add it again.
		mapFragment = (BaseMapFragment) getSupportFragmentManager().findFragmentByTag(BaseMapFragment.TAG);
		if (mapFragment == null) {
			mapFragment = new BaseMapFragment();
			ft.add(R.id.fragment_container, mapFragment, BaseMapFragment.TAG);
		}
		ft.hide(mapFragment);
		listFragment = (HazardListFragment) getSupportFragmentManager().findFragmentByTag(HazardListFragment.TAG);
		if (listFragment == null) {
			listFragment = new HazardListFragment();
			ft.add(R.id.fragment_container, listFragment, HazardListFragment.TAG);
		}
		ft.hide(listFragment);
		dataFragment = (DataManagerFragment) getSupportFragmentManager().findFragmentByTag(DataManagerFragment.TAG);
		if (dataFragment == null) {
			dataFragment = new DataManagerFragment();
			ft.add(dataFragment, DataManagerFragment.TAG);
		}
		mapFragment.setDataManager(dataFragment); // possible DM was killed but mapFrag remained alive
		listFragment.setDataManager(dataFragment);
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
		Log.v();
		getMenuInflater().inflate(R.menu.activity_main, menu);
		if (this.visibleFragment == this.mapFragment) {
			menu.findItem(R.id.action_map).setVisible(false);
			menu.findItem(R.id.action_list).setVisible(true);
			menu.findItem(R.id.action_search).setVisible(true);
		}
		else if (this.visibleFragment == this.listFragment) {
			menu.findItem(R.id.action_list).setVisible(false);
			menu.findItem(R.id.action_map).setVisible(true);
			menu.findItem(R.id.action_search).setVisible(true);
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
		Log.v();
		showFragment(listFragment);
		return true;
	}

	public boolean onShowMap(MenuItem view) {
		Log.v();
		showFragment(mapFragment);
		return true;
	}

	public boolean onShowEditFilter(MenuItem view) {
		Log.v();
		Filter.startForResult(this, dataFragment.getFilter(), C.RequestCode.FILTER.ordinal());
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == C.RequestCode.FILTER.ordinal()) {
			if (null != data) {
				dataFragment.setFilter(Filter.parseResult(data));
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public boolean onActiveHazards(MenuItem menuItem) {
		startActivity(new Intent(this, ActiveHazardActivity.class));
		return true;
	}

	public boolean onLegalNotices(MenuItem munuItem) {
		String licenseInfo = GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(getApplicationContext());
		AlertDialog.Builder licenseDialog = new AlertDialog.Builder(this);
		licenseDialog.setTitle("Legal Notices");
		licenseDialog.setMessage(licenseInfo);
		licenseDialog.show();
		return true;
	}
}
