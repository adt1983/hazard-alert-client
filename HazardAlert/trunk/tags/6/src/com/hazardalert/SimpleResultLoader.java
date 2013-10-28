package com.hazardalert;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

public abstract class SimpleResultLoader<D> extends AsyncTaskLoader<D> {
	private D result;

	/**
	 * Create a new SimpleResultLoader.
	 * 
	 * @param context
	 *            A Context object.
	 */
	public SimpleResultLoader(final Context context) {
		super(context);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onStartLoading() {
		if (result != null) {
			// If a result already exists, deliver it.
			deliverResult(result);
		}
		else {
			// If a result does not exist, force a load.
			forceLoad();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onStopLoading() {
		// Attempt to cancel the current load task if possible.
		cancelLoad();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onReset() {
		super.onReset();
		onStopLoading();
		// Reset to defaults.
		result = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deliverResult(final D resultIn) {
		// If the Loader has been reset, do not deliver a result.
		if (isReset()) {
			return;
		}
		result = resultIn;
		// Deliver the result only if the Loader is in the started state.
		if (isStarted()) {
			super.deliverResult(resultIn);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public abstract D loadInBackground();
}
