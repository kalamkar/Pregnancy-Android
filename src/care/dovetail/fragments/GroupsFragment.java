package care.dovetail.fragments;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import care.dovetail.App;
import care.dovetail.Config;
import care.dovetail.MessagingActivity;
import care.dovetail.R;
import care.dovetail.api.MessagesGet;
import care.dovetail.common.model.Group;

public class GroupsFragment extends Fragment {
	private static final String TAG = "GroupsFragment";
	private App app;

	private Group groups[];

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		app = (App) activity.getApplication();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_groups, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		((ListView) view.findViewById(R.id.groups)).setAdapter(new GroupsAdapter());
	}

	@Override
	public void onResume() {
		app.getSharedPreferences(app.getPackageName(), Application.MODE_PRIVATE)
				.registerOnSharedPreferenceChangeListener(listener);
		loadGroups();
		((BaseAdapter) ((ListView) getView().findViewById(R.id.groups)).getAdapter())
				.notifyDataSetChanged();
		super.onResume();
	}

	@Override
	public void onPause() {
		app.getSharedPreferences(app.getPackageName(), Application.MODE_PRIVATE)
				.unregisterOnSharedPreferenceChangeListener(listener);
		super.onPause();
	}

	private OnSharedPreferenceChangeListener listener = new OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
			if (App.GROUP_SYNC_TIME.equalsIgnoreCase(key)) {
				loadGroups();
				((BaseAdapter) ((ListView) getView().findViewById(R.id.groups)).getAdapter())
						.notifyDataSetChanged();
			}
		}
	};

	private void loadGroups() {
		groups = app.groups.toArray(new Group[0]);
		Arrays.sort(groups, new Comparator<Group>() {
			@Override
			public int compare(Group lhs, Group rhs) {
				return (int) (lhs.update_time - rhs.update_time);
			}
		});
	}

	private class GroupsAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return app.groups.size();
		}

		@Override
		public Group getItem(int position) {
			return app.groups.get(position);
		}

		@Override
		public long getItemId(int position) {
			return getItem(position).hashCode();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			if (convertView == null) {
				view = getActivity().getLayoutInflater().inflate(R.layout.list_item_group, null);
			} else {
				view = convertView;
			}

			Group group = getItem(position);
			view.setTag(group.uuid);
			if (group.name != null && !group.name.isEmpty()) {
				((TextView) view.findViewById(R.id.title)).setText(group.name);
			} else {
				((TextView) view.findViewById(R.id.title)).setText(group.toString());
			}
			((TextView) view.findViewById(R.id.hint)).setText(Config.DATE_FORMAT.format(
					new Date(group.update_time)));

			view.setOnClickListener(new OnClickListener() {
				@SuppressWarnings("unchecked")
				@Override
				public void onClick(View v) {
					String groupId = (String) v.getTag();
					new MessagesGet(app, groupId).execute();
					startActivity(new Intent(app, MessagingActivity.class)
							.putExtra(Config.GROUP_ID, groupId));
				}
			});
			return view;
		}

	}
}