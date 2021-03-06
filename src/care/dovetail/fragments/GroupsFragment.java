package care.dovetail.fragments;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

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
import care.dovetail.Utils;
import care.dovetail.api.MessagesGet;
import care.dovetail.common.model.ApiResponse.Message;
import care.dovetail.common.model.Group;

import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.analytics.HitBuilders;

public class GroupsFragment extends Fragment implements OnClickListener {
	private static final String TAG = "GroupsFragment";
	private App app;

	private Group groups[] = new Group[0];

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
		app.tracker.setScreenName(TAG);
		app.tracker.send(new HitBuilders.ScreenViewBuilder().build());
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
				return (int) (rhs.update_time - lhs.update_time);
			}
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onClick(View v) {
		Object tag = v.getTag();
		if (tag != null && tag instanceof String) {
			new MessagesGet(app, (String) tag).execute();
			startActivity(new Intent(app, MessagingActivity.class)
					.putExtra(Config.GROUP_ID, (String) tag));
		}
	}

	private class GroupsAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return groups.length;
		}

		@Override
		public Group getItem(int position) {
			return groups[position];
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
				view.setOnClickListener(GroupsFragment.this);
			} else {
				view = convertView;
			}

			Group group = getItem(position);
			view.findViewById(R.id.hint).setVisibility(View.VISIBLE);
			view.findViewById(R.id.time).setVisibility(View.VISIBLE);
			String photoUrl = String.format("%s%s&size=%d", Config.GROUP_PHOTO_URL, group.uuid,
					(int) app.getResources().getDimension(R.dimen.icon_width));
			((NetworkImageView) view.findViewById(R.id.icon)).setImageUrl(
					photoUrl, app.imageLoader);
			view.setTag(group.uuid);
			if (group.name != null && !group.name.isEmpty()) {
				((TextView) view.findViewById(R.id.title)).setText(group.name);
			} else {
				((TextView) view.findViewById(R.id.title)).setText(group.toString());
			}
			List<Message> messages = app.messages.get(group.uuid);
			if (messages != null && messages.size() > 0) {
				((TextView) view.findViewById(R.id.hint)).setText(
						messages.get(messages.size() - 1).text);
			} else {
				((TextView) view.findViewById(R.id.hint)).setText("");
			}
			((TextView) view.findViewById(R.id.time)).setText(
					Utils.getDisplayTime(group.update_time));
			return view;
		}
	}
}