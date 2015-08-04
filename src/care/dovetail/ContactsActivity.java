package care.dovetail;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import care.dovetail.api.GroupUpdate;
import care.dovetail.api.Search;
import care.dovetail.common.model.ApiResponse;
import care.dovetail.common.model.ApiResponse.Result;
import care.dovetail.common.model.User;

import com.android.volley.toolbox.NetworkImageView;

public class ContactsActivity extends FragmentActivity implements OnClickListener,
		OnEditorActionListener {
	private static final String TAG = "ContactsActivity";

	private App app;
	private String groupId;
	private final List<User> users = new ArrayList<User>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contacts);

        getActionBar().hide();

		app = (App) getApplication();
		groupId = this.getIntent().getStringExtra(Config.GROUP_ID);
		users.addAll(app.contacts);
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

	@Override
	public void onClick(View view) {
		addUserToGroup((User) view.getTag(), groupId);
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
				ContactsActivity.this.users.clear();
				for (Result result : results) {
					if (result != null && result.user != null) {
						ContactsActivity.this.users.add(result.user);
						app.contacts.add(result.user);
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

	private class UsersAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return users.size();
		}

		@Override
		public User getItem(int position) {
			return users.get(position);
		}

		@Override
		public long getItemId(int position) {
			return getItem(position).hashCode();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			if (convertView == null) {
				view = getLayoutInflater().inflate(R.layout.list_item_contact, null);
			} else {
				view = convertView;
			}

			User user = getItem(position);
			((TextView) view.findViewById(R.id.title)).setText(user.name);
			((TextView) view.findViewById(R.id.date)).setText(
					Utils.getDisplayTime(user.update_time));
			String photoUrl = String.format("%s%s&size=%d", Config.USER_PHOTO_URL,
					user.uuid, (int) app.getResources().getDimension(R.dimen.icon_width));
			((NetworkImageView) view.findViewById(R.id.icon)).setImageUrl(
					photoUrl, app.imageLoader);
			view.setOnClickListener(ContactsActivity.this);
			view.setTag(user);
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
}
