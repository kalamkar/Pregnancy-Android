package care.dovetail;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
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

public class SearchActivity extends FragmentActivity implements OnClickListener,
		OnEditorActionListener {
	private static final String TAG = "ContactsActivity";

	private App app;
	private String groupId;
	private final List<Result> results = new ArrayList<Result>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contacts);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

		app = (App) getApplication();
		groupId = this.getIntent().getStringExtra(Config.GROUP_ID);
		reloadContacts();
		((ListView) findViewById(R.id.messages)).setAdapter(new UsersAdapter());
		((TextView) findViewById(R.id.search)).setOnEditorActionListener(this);
	}

	@Override
	protected void onDestroy() {
		app.updateContacts();
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case android.R.id.home:
	        NavUtils.navigateUpFromSameTask(this);
	        return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onClick(View view) {
		final Result result = (Result) view.getTag();
		if (result.user != null) {
			if (groupId != null) {
				addUserToGroup(result.user, groupId);
			} else {
				startMessagingWithUser(result.user);
			}
		} else if (result.group != null) {
			new MessagesGet(app, result.group.uuid).execute();
			startActivity(new Intent(app, MessagingActivity.class)
					.putExtra(Config.GROUP_ID, result.group.uuid));
			finish();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		String query = v.getText().toString();
		try {
			query = URLEncoder.encode(query, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			Log.w(TAG, e);
			query = query.replace(' ', '+');
		}
		new Search(app) {
			@Override
			public void onResult(Result[] results) {
				SearchActivity.this.results.clear();
				for (Result result : results) {
					if (result != null && (result.user != null || result.group != null)) {
						SearchActivity.this.results.add(result);
						if (result.user != null) {
							app.contacts.add(result.user);
						}
					}
				}
				((BaseAdapter) ((ListView) findViewById(R.id.messages)).getAdapter())
						.notifyDataSetChanged();
			}
		}.execute(Pair.create(Search.PARAM_QUERY, query));

		v.setText("");
		((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
				.hideSoftInputFromWindow(v.getWindowToken(), 0);
		return true;
	}

	private void reloadContacts() {
		for (User user : app.contacts) {
			Result result = new Result();
			result.user = user;
			results.add(result);
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
				((TextView) view.findViewById(R.id.hint)).setText(
						Utils.getDisplayTime(result.user.update_time));
				((ImageView) view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_user);
			} else if (result != null && result.group != null) {
				((TextView) view.findViewById(R.id.title)).setText(result.group.toString());
				((TextView) view.findViewById(R.id.hint)).setText(
						Utils.getDisplayTime(result.group.update_time));
				((ImageView) view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_group);
			} else {
				((TextView) view.findViewById(R.id.title)).setText("");
				((TextView) view.findViewById(R.id.hint)).setText("");
				((ImageView) view.findViewById(R.id.icon)).setImageDrawable(null);
			}
			view.setOnClickListener(SearchActivity.this);
			view.setTag(result);
			return view;
		}
	}

	@SuppressWarnings("unchecked")
	private void addUserToGroup(User user, String groupId) {
		new GroupUpdate(app, groupId) {
			@Override
			protected void onPostExecute(ApiResponse response) {
				super.onPostExecute(response);
				finish();
			}
		}.execute(Pair.create(GroupUpdate.PARAM_MEMBER, user.uuid));
	}

	@SuppressWarnings("unchecked")
	private void startMessagingWithUser(final User user) {
		Group group = app.findUserGroup(user);
		if (group == null) {
			new GroupUpdate(app, null) {
				@Override
				protected void onPostExecute(ApiResponse response) {
					super.onPostExecute(response);
					Group group = app.findUserGroup(user);
					if (group != null) {
						startActivity(
								new Intent(SearchActivity.this, MessagingActivity.class)
								.putExtra(Config.GROUP_ID, group.uuid));
						finish();
					}
				}
			}.execute(Pair.create(GroupUpdate.PARAM_MEMBER, user.uuid));
		} else {
			startActivity(new Intent(this, MessagingActivity.class)
				.putExtra(Config.GROUP_ID, group.uuid));
			finish();
		}
	}
}
