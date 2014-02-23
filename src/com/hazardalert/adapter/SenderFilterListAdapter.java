package com.hazardalert.adapter;

/*
public class SenderFilterListAdapter extends SenderListAdapter {
	class OnCheckChange implements CompoundButton.OnCheckedChangeListener {
		private final Sender sender;

		OnCheckChange(Sender s) {
			sender = s;
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean allowed) {
			Log.d("Sender: " + sender.getName() + "\tChecked: " + allowed);
			sender.setSuppress(!allowed);
		}
	}

	public SenderFilterListAdapter(Context context, int resource, int textViewResourceId, List<Sender> objects) {
		super(context, resource, textViewResourceId, objects);
		//todo: toArray for performance?
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final View row = super.getView(position, convertView, parent);
		final Sender s = getItem(position);
		((TextView) row.findViewById(R.id.sender_list_item_name)).setText(s.getName());
		((TextView) row.findViewById(R.id.sender_list_item_url)).setText(s.getUrl().replace("'", ""));
		CheckBox allowed = (CheckBox) row.findViewById(R.id.sender_list_allowed);
		allowed.setChecked(true);
		allowed.setOnCheckedChangeListener(new OnCheckChange(s));
		return row;
	}
}
*/
