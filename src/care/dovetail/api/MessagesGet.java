package care.dovetail.api;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;

import android.util.Log;
import android.util.Pair;
import android.widget.Toast;
import care.dovetail.App;
import care.dovetail.Config;
import care.dovetail.common.ApiResponseTask;
import care.dovetail.common.model.ApiResponse;
import care.dovetail.common.model.ApiResponse.Message;

@SuppressWarnings("deprecation")
public class MessagesGet extends ApiResponseTask {
	private static final String TAG = "MessagesGet";

	private final App app;
	private final String groupId;

	public MessagesGet(App app, String groupId) {
		super(app.getUserUUID(), app.getUserAuth());
		this.app = app;
		this.groupId = groupId;
	}

	@Override
	protected HttpRequestBase makeRequest(Pair<String, String>... params)
			throws UnsupportedEncodingException {
		return new HttpGet(String.format("%s?group_uuid=%s", Config.MESSAGE_URL, groupId));
	}

	@Override
	protected void onPostExecute(ApiResponse result) {
		super.onPostExecute(result);
		if (result != null && !"OK".equalsIgnoreCase(result.code)) {
			Log.e(TAG, result.message);
			Toast.makeText(app, result.message, Toast.LENGTH_LONG).show();
		} else if (result != null && result.messages != null) {
			List<Message> messages = new ArrayList<Message>();
			for (Message message : result.messages) {
				messages.add(0, message);
			}
			app.messages.put(groupId, messages);
			app.setMessageSyncTime(System.currentTimeMillis());
		}
	}
}