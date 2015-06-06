package care.dovetail;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import care.dovetail.api.GroupUpdate;
import care.dovetail.api.MessagesGet;
import care.dovetail.api.Search;
import care.dovetail.common.model.ApiResponse;
import care.dovetail.common.model.ApiResponse.Result;
import care.dovetail.common.model.Group;
import care.dovetail.common.model.User;

public class ContactsActivity extends FragmentActivity implements OnClickListener,
		OnEditorActionListener {
	private static final String TAG = "ContactsActivity";

	public static final String GROUP_ID = "GROUP_ID";

	private App app;
	private final List<Result> results = new ArrayList<Result>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contacts);

		app = (App) getApplication();
		reloadContacts();
		((ListView) findViewById(R.id.messages)).setAdapter(new UsersAdapter());
		((TextView) findViewById(R.id.search)).setOnEditorActionListener(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onClick(View view) {
		final Result result = (Result) view.getTag();
		if (result.user != null) {
			if (!findUserGroupAndOpen(result.user)) {
				new GroupUpdate(app, null) {
					@Override
					protected void onPostExecute(ApiResponse response) {
						super.onPostExecute(response);
						findUserGroupAndOpen(result.user);
					}
				}.execute(Pair.create(GroupUpdate.PARAM_MEMBER, result.user.uuid));
			}
		} else if (result.group != null) {
			new MessagesGet(app, result.group.uuid).execute();
			startActivity(new Intent(app, MessagingActivity.class)
					.putExtra(MessagingActivity.GROUP_ID, result.group.uuid));
			finish();
		}
	}

	@SuppressWarnings("unchecked")
	private boolean findUserGroupAndOpen(User user) {
		for (Group group : app.groups) {
			if (group != null && group.members != null && group.members.length == 2 &&
					(user.equals(group.members[0]) || user.equals(group.members[1]))) {
				new MessagesGet(app, group.uuid).execute();
				startActivity(new Intent(app, MessagingActivity.class)
						.putExtra(MessagingActivity.GROUP_ID, group.uuid));
				finish();
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		new Search(app) {
			@Override
			public void onResult(Result[] results) {
				ContactsActivity.this.results.clear();
				for (Result result : results) {
					if (result != null && (result.user != null || result.group != null)) {
						ContactsActivity.this.results.add(result);
					}
				}
				((BaseAdapter) ((ListView) findViewById(R.id.messages)).getAdapter()).notifyDataSetChanged();
			}
		}.execute(Pair.create(Search.PARAM_QUERY, v.getText().toString()));

		v.setText("");
		((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
				.hideSoftInputFromWindow(v.getWindowToken(), 0);
		return true;
	}

	private void reloadContacts() {
		List<User> contacts = new ArrayList<User>();
		User user = app.getMother();
		for (Group group : app.groups) {
			for (User member : group.members) {
				if (!contacts.contains(member) && !member.equals(user)) {
					contacts.add(member);
					Result result = new Result();
					result.user = member;
					results.add(result);
				}
			}
			for (User admin : group.admins) {
				if (!contacts.contains(admin) && !admin.equals(user)) {
					contacts.add(admin);
					Result result = new Result();
					result.user = admin;
					results.add(result);
				}
			}
		}
	}

	private class UsersAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return results.size();
		}

		@Override
		public Result getItem(int position) {
			return results.get(position);
		}

		@Override
		public long getItemId(int position) {
			Result result = getItem(position);
			return result == null ? 0 : result.hashCode();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			if (convertView == null) {
				view = getLayoutInflater().inflate(R.layout.list_item_contact, null);
			} else {
				view = convertView;
			}

			Result result = getItem(position);
			if (result != null && result.user != null) {
				((TextView) view.findViewById(R.id.title)).setText(result.user.name);
			} else if (result != null && result.group != null) {
				((TextView) view.findViewById(R.id.title)).setText(result.group.toString());
			} else {
				((TextView) view.findViewById(R.id.title)).setText("");
			}
			((TextView) view.findViewById(R.id.hint)).setText("");
			view.setOnClickListener(ContactsActivity.this);
			view.setTag(result);
			return view;
		}
	}
}
