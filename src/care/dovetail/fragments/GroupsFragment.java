package care.dovetail.fragments;

import java.text.ParseException;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import care.dovetail.App;
import care.dovetail.Config;
import care.dovetail.R;
import care.dovetail.common.model.Group;
import care.dovetail.common.model.User;

public class GroupsFragment extends Fragment {
	private static final String TAG = "GroupsFragment";
	private App app;

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
			if (group.name != null) {
				((TextView) view.findViewById(R.id.title)).setText(group.name);
			} else {
				((TextView) view.findViewById(R.id.title)).setText(getMembers(group.members));
			}
			try {
				((TextView) view.findViewById(R.id.hint)).setText(Config.DATE_FORMAT.format(
						Config.JSON_DATE_FORMAT.parse(group.update_time)));
			} catch (ParseException e) {
				Log.w(TAG, e);
			}
			return view;
		}

	}

	private static String getMembers(User members[]) {
		StringBuilder builder = new StringBuilder();
		for (User member : members) {
			if (member.name != null) {
				String name = member.name.split(" ")[0];
				builder.append(name.length() > 10 ? name.substring(0, 10) : name);
			}
		}
		return builder.toString();
	}
}