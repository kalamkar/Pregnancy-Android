package care.dovetail;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import care.dovetail.api.GroupUpdate;
import care.dovetail.api.MessagesGet;
import care.dovetail.api.Search;
import care.dovetail.common.model.ApiResponse;
import care.dovetail.common.model.ApiResponse.Result;
import care.dovetail.common.model.Group;
import care.dovetail.common.model.User;
import care.dovetail.fragments.CardUtils;

import com.android.volley.toolbox.NetworkImageView;

public class SearchResultsActivity extends FragmentActivity implements OnClickListener {
	private static final String TAG = "SearchResultsActivity";

	private App app;
	private final List<Result> results = new ArrayList<Result>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_results);

		app = (App) getApplication();
		((ListView) findViewById(R.id.results)).setAdapter(new ResultsAdapter());

		handleIntent(getIntent());
	}

	@Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            search(intent.getStringExtra(SearchManager.QUERY));
        }
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
			startMessagingWithUser(result.user);
			Utils.trackEvent(app, "Search", "Click", "User");
		} else if (result.group != null) {
			Utils.trackEvent(app, "Search", "Click", "Group");
			new MessagesGet(app, result.group.uuid).execute();
			startActivity(new Intent(app, MessagingActivity.class)
					.putExtra(Config.GROUP_ID, result.group.uuid));
			finish();
		}
	}

	@SuppressWarnings("unchecked")
	private void search(String query) {
		if (query == null || query.isEmpty()) {
			return;
		}

		try {
			query = URLEncoder.encode(query, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			Log.w(TAG, e);
			query = query.replace(' ', '+');
		}
		new Search(app) {
			@Override
			public void onResult(Result[] results) {
				SearchResultsActivity.this.results.clear();
				findViewById(R.id.progress).setVisibility(View.INVISIBLE);
				if (results == null || results.length == 0) {
					findViewById(R.id.message).setVisibility(View.VISIBLE);
				}
				for (Result result : results) {
					if (result != null) {
						SearchResultsActivity.this.results.add(result);
					}
				}
				((BaseAdapter) ((ListView) findViewById(R.id.results)).getAdapter())
						.notifyDataSetChanged();
			}
		}.execute(Pair.create(Search.PARAM_QUERY, query));
		findViewById(R.id.progress).setVisibility(View.VISIBLE);
		findViewById(R.id.message).setVisibility(View.INVISIBLE);
	}

	private class ResultsAdapter extends BaseAdapter {
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
			return getItem(position).hashCode();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			int left = getResources().getDimensionPixelOffset(R.dimen.activity_horizontal_margin);
			int top = getResources().getDimensionPixelOffset(R.dimen.activity_vertical_margin);
			int right = getResources().getDimensionPixelOffset(R.dimen.activity_horizontal_margin);
			int bottom = getResources().getDimensionPixelOffset(R.dimen.activity_vertical_margin);
			View view;
			Result result = getItem(position);
			if (result.user != null) {
				view = getLayoutInflater().inflate(R.layout.list_item_contact, null);
				((TextView) view.findViewById(R.id.title)).setText(result.user.name);
				((TextView) view.findViewById(R.id.date)).setText(
						Utils.getDisplayTime(result.user.update_time));
				String photoUrl = String.format("%s%s&size=%d", Config.USER_PHOTO_URL,
						result.user.uuid, (int) app.getResources().getDimension(R.dimen.icon_width));
				((NetworkImageView) view.findViewById(R.id.icon)).setImageUrl(
						photoUrl, app.imageLoader);
			} else if (result.group != null) {
				view = getLayoutInflater().inflate(R.layout.list_item_contact, null);
				((TextView) view.findViewById(R.id.title)).setText(result.group.toString());
				((TextView) view.findViewById(R.id.date)).setText(
						Utils.getDisplayTime(result.group.update_time));
				String photoUrl = String.format("%s%s&size=%d", Config.GROUP_PHOTO_URL,
						result.group.uuid, (int) app.getResources().getDimension(R.dimen.icon_width));
				((NetworkImageView) view.findViewById(R.id.icon)).setImageUrl(
						photoUrl, app.imageLoader);
			} else if (result.message != null) {
				top = 0;
				view = getLayoutInflater().inflate(R.layout.list_item_message, null);
				((TextView) view.findViewById(R.id.text)).setText(result.message.text);
				((TextView) view.findViewById(R.id.sender)).setText(result.message.sender.name);
				((TextView) view.findViewById(R.id.time)).setText(
						Utils.getMessageDisplayTime(result.message.create_time));

				View bubble = view.findViewById(R.id.bubble);
				FrameLayout.LayoutParams params =
						(FrameLayout.LayoutParams) bubble.getLayoutParams();
				if (params == null) {
					params = new FrameLayout.LayoutParams(
							LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				}
				if (app.getMother().equals(result.message.sender)) {
					params.rightMargin = getResources().getDimensionPixelOffset(R.dimen.tiny_margin);
					params.leftMargin = getResources().getDimensionPixelOffset(R.dimen.message_indent);
					params.gravity = Gravity.RIGHT;
					bubble.setBackground(getResources().getDrawable(R.drawable.my_message));
				} else {
					params.rightMargin = getResources().getDimensionPixelOffset(R.dimen.message_indent);
					params.leftMargin = getResources().getDimensionPixelOffset(R.dimen.tiny_margin);
					params.gravity = Gravity.LEFT;
					bubble.setBackground(getResources().getDrawable(R.drawable.message));
				}
				params.topMargin =
						getResources().getDimensionPixelOffset(R.dimen.activity_vertical_margin);
				bubble.setLayoutParams(params);
			} else if (result.card != null) {
				left = top = right = bottom = 0;
				view = CardUtils.getViewForCard(result.card, null, null, getLayoutInflater(),
						null, getResources(), app.imageLoader);
			} else {
				view = new View(app);
			}
			view.setPadding(left, top, right, bottom);
			view.setOnClickListener(SearchResultsActivity.this);
			view.setTag(result);
			return view;
		}
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
								new Intent(SearchResultsActivity.this, MessagingActivity.class)
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
