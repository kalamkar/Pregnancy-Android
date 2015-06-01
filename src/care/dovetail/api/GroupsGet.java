package care.dovetail.api;

import java.io.UnsupportedEncodingException;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;

import android.util.Log;
import android.util.Pair;
import android.widget.Toast;
import care.dovetail.App;
import care.dovetail.Config;
import care.dovetail.common.ApiResponseTask;
import care.dovetail.common.model.ApiResponse;
import care.dovetail.common.model.Group;

@SuppressWarnings("deprecation")
public class GroupsGet extends ApiResponseTask {
	private static final String TAG = "UserGet";

	private final App app;

	public GroupsGet(App app) {
		this.app = app;
	}

	@Override
	protected HttpRequestBase makeRequest(Pair<String, String>... params)
			throws UnsupportedEncodingException {
		return new HttpGet(String.format("%s?user_id=%s", Config.GROUP_URL, app.getUserId()));
	}

	@Override
	protected void onPostExecute(ApiResponse result) {
		super.onPostExecute(result);
		if (result != null && !"OK".equalsIgnoreCase(result.code)) {
			Log.e(TAG, result.message);
			Toast.makeText(app, result.message, Toast.LENGTH_LONG).show();
		} else if (result != null && result.groups != null) {
			for (Group group : result.groups) {
				if (group != null && group.uuid != null && !app.groups.contains(group)) {
					app.groups.add(group);
				}
			}
		}
	}
}