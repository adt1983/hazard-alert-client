package com.hazardalert.adapter;

import java.util.List;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.hazardalert.Sender;

public abstract class SenderListAdapter extends ArrayAdapter<Sender> {
	public SenderListAdapter(Context context, int resource, int textViewResourceId, List<Sender> objects) {
		super(context, resource, textViewResourceId, objects);
	}
}
