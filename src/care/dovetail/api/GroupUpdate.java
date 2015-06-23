package care.dovetail.api;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;
import android.util.Pair;
import android.widget.Toast;
import care.dovetail.App;
import care.dovetail.Config;
import care.dovetail.common.ApiResponseTask;
import care.dovetail.common.model.ApiResponse;
import care.dovetail.common.model.Group;

@SuppressWarnings("deprecation")
public class GroupUpdate extends ApiResponseTask {
	private static final String TAG = "GroupUpdate";

	public static final String PARAM_NAME = "name";
	public static final String PARAM_ADMIN = "admin_uuid";
	public static final String PARAM_MEMBER = "member_uuid";

	private final App app;
	private final String groupId;

	public GroupUpdate(App app, String groupId) {
		super(app.getUserUUID(), app.getUserAuth());
		this.app = app;
		this.groupId = groupId;
	}

	@Override
	protected HttpRequestBase makeRequest(Pair<String, String>... params)
			throws UnsupportedEncodingException {
		List<NameValuePair> queryParams = new ArrayList<NameValuePair>();
		HttpEntityEnclosingRequestBase request;
		if (groupId == null) {
			request = new HttpPost(Config.GROUP_URL);
		} else {
			request = new HttpPut(Config.GROUP_URL);
			queryParams.add(new BasicNameValuePair("group_uuid", groupId));
		}
		for (Pair<String, String> param : params) {
			queryParams.add(new BasicNameValuePair(param.first, param.second));
		}
		request.setEntity(new UrlEncodedFormEntity(queryParams));
		return request;
	}

	@Override
	protected void onPostExecute(ApiResponse result) {
		super.onPostExecute(result);
		if (result != null && !"OK".equalsIgnoreCase(result.code)) {
			Log.e(TAG, result.message);
			Toast.makeText(app, result.message, Toast.LENGTH_LONG).show();
		} else if (result != null && result.groups != null) {
			for (Group group : result.groups) {
				if (group != null && group.uuid != null) {
					if (app.groups.contains(group)) {
						app.groups.remove(group);
					}
					app.groups.add(group);
				}
			}
			app.setGroupSyncTime(System.currentTimeMillis());
		}
	}
}