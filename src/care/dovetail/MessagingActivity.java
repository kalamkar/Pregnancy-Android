package care.dovetail;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import care.dovetail.api.MessagePost;
import care.dovetail.common.model.ApiResponse.Message;

public class MessagingActivity extends Activity implements OnClickListener {

	private static final String TAG = "MainActivity";

	public static final String GROUP_ID = "GROUP_ID";

	private App app;
	private String groupId;
	private List<Message> messages;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_messaging);

		app = (App) getApplication();
		groupId = this.getIntent().getStringExtra(GROUP_ID);
		messages = app.messages.get(groupId);
		if (messages == null) {
			messages = new ArrayList<Message>();
		}

		((ListView) findViewById(R.id.messages)).setAdapter(new MessagesAdapter());
		findViewById(R.id.send).setOnClickListener(this);
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
			return messages.get(position);
		}

		@Override
		public long getItemId(int position) {
			Message message = getItem(position);
			return message == null ? 0 : message.hashCode();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			if (convertView == null) {
				view = getLayoutInflater().inflate(R.layout.list_item_message, null);
			} else {
				view = convertView;
			}

			Message message = getItem(position);
			((TextView) view.findViewById(R.id.text)).setText(message.text);
			((TextView) view.findViewById(R.id.sender)).setText(message.sender.name);
			((TextView) view.findViewById(R.id.time)).setText(getDisplayTime(message.create_time));
			return view;
		}
	}

	private String getDisplayTime(long time) {
		return Config.MESSAGE_DATE_FORMAT.format(new Date(time));
	}
}
