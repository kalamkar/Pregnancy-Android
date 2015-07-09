package care.dovetail;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Pair;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import care.dovetail.api.MessagePost;
import care.dovetail.common.model.ApiResponse.Message;
import care.dovetail.common.model.Group;
import care.dovetail.fragments.GroupNameFragment;

public class MessagingActivity extends FragmentActivity implements OnClickListener {

	private static final String TAG = "MessagingActivity";

	private static final int SHORT_TIME_MILLIS = 10 * 60 * 1000;

	private App app;
	private String groupId;
	private List<Message> messages;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_messaging);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

		app = (App) getApplication();
		groupId = this.getIntent().getStringExtra(Config.GROUP_ID);
		messages = app.messages.get(groupId);
		if (messages == null) {
			messages = new ArrayList<Message>();
		}

		Group group = findGroup(groupId);
		MessagingActivity.this.setTitle(group != null ? group.toString() :
			getResources().getString(R.string.app_name));

		((ListView) findViewById(R.id.messages)).setAdapter(new MessagesAdapter());
		findViewById(R.id.send).setOnClickListener(this);
	}

	@Override
	public void onResume() {
		app.getSharedPreferences(app.getPackageName(), Application.MODE_PRIVATE)
				.registerOnSharedPreferenceChangeListener(listener);
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
			if (App.MESSAGE_SYNC_TIME.equalsIgnoreCase(key)) {
				messages = app.messages.get(groupId);
				if (messages == null) {
					messages = new ArrayList<Message>();
				}
				((BaseAdapter) ((ListView) findViewById(R.id.messages)).getAdapter())
						.notifyDataSetChanged();
			} else if (App.GROUP_SYNC_TIME.equalsIgnoreCase(key)) {
				Group group = findGroup(groupId);
				MessagingActivity.this.setTitle(group != null ? group.toString() :
					getResources().getString(R.string.app_name));
			}
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.group, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.action_add_user:
			startActivity(
					new Intent(this, SearchActivity.class).putExtra(Config.GROUP_ID, groupId));
			break;
		case R.id.action_edit_name:
			DialogFragment fragment = new GroupNameFragment();
			Bundle args = new Bundle();
			args.putString(GroupNameFragment.GROUP_ID, groupId);
			fragment.setArguments(args);
			fragment.show(getSupportFragmentManager(), null);
			break;
		case android.R.id.home:
	        NavUtils.navigateUpFromSameTask(this);
	        return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onClick(View view) {
		TextView messageBox = (TextView) findViewById(R.id.message);
		Message message = new Message();
		message.text = messageBox.getText().toString();
		message.sender = app.getMother();
		message.create_time = System.currentTimeMillis();
		messages.add(message);
		new MessagePost(app).execute(Pair.create(MessagePost.PARAM_TEXT, message.text),
				Pair.create(MessagePost.PARAM_GROUP_ID, groupId));
		((BaseAdapter) ((ListView) findViewById(R.id.messages)).getAdapter()).notifyDataSetChanged();

		messageBox.setText("");
		((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
				.hideSoftInputFromWindow(messageBox.getWindowToken(), 0);
	}

	private class MessagesAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return messages.size();
		}

		@Override
		public Message getItem(int position) {
			return position < 0 ? null : messages.get(position);
		}

		@Override
		public long getItemId(int position) {
			Message message = getItem(position);
			return message == null ? 0 : message.hashCode();
		}

		@SuppressWarnings("deprecation")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			if (convertView == null) {
				view = getLayoutInflater().inflate(R.layout.list_item_message, null);
			} else {
				view = convertView;
			}

			Message message = getItem(position);
			Message prevMessage = getItem(position - 1);
			((TextView) view.findViewById(R.id.text)).setText(message.text);
			((TextView) view.findViewById(R.id.sender)).setText(message.sender.name);
			((TextView) view.findViewById(R.id.time)).setText(
					Utils.getDisplayTime(message.create_time));

			boolean sameSender = prevMessage != null && prevMessage.sender.equals(message.sender);
			boolean continuedMessage = prevMessage != null
					&& message.create_time - prevMessage.create_time < SHORT_TIME_MILLIS;
			view.findViewById(R.id.sender).setVisibility(sameSender ? View.GONE : View.VISIBLE);
			view.findViewById(R.id.time).setVisibility(
					continuedMessage && sameSender? View.GONE : View.VISIBLE);

			View bubble = view.findViewById(R.id.bubble);
			FrameLayout.LayoutParams params =
					(FrameLayout.LayoutParams) bubble.getLayoutParams();
			if (params == null) {
				params = new FrameLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			}
			if (app.getMother().equals(message.sender)) {
				params.leftMargin = getResources().getDimensionPixelOffset(R.dimen.message_indent);
				params.rightMargin = getResources().getDimensionPixelOffset(R.dimen.small_margin);
				params.gravity = Gravity.RIGHT;
				bubble.setBackground(getResources().getDrawable(R.drawable.my_message));
			} else {
				params.leftMargin = getResources().getDimensionPixelOffset(R.dimen.small_margin);
				params.rightMargin = getResources().getDimensionPixelOffset(R.dimen.message_indent);
				params.gravity = Gravity.LEFT;
				bubble.setBackground(getResources().getDrawable(R.drawable.message));
			}
			if (sameSender && continuedMessage) {
				params.topMargin = getResources().getDimensionPixelOffset(R.dimen.small_margin);
			} else {
				params.topMargin =
						getResources().getDimensionPixelOffset(R.dimen.activity_vertical_margin);
			}
			bubble.setLayoutParams(params);

			return view;
		}
	}

	private Group findGroup(String groupId) {
		if (groupId == null) {
			return null;
		}
		for (Group group : app.groups) {
			if (group != null && groupId.equalsIgnoreCase(group.uuid)) {
				return group;
			}
		}
		return null;
	}
}
