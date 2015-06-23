package care.dovetail.api;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;
import android.util.Pair;
import android.widget.Toast;
import care.dovetail.App;
import care.dovetail.Config;
import care.dovetail.common.ApiResponseTask;
import care.dovetail.common.model.ApiResponse;

@SuppressWarnings("deprecation")
public class MessagePost extends ApiResponseTask {
	private static final String TAG = "MessagePost";

	public static final String PARAM_TEXT = "text";
	public static final String PARAM_GROUP_ID = "group_uuid";

	private final App app;

	public MessagePost(App app) {
		super(app.getUserUUID(), app.getUserAuth());
		this.app = app;
	}

	@Override
	protected HttpRequestBase makeRequest(Pair<String, String>... params)
			throws UnsupportedEncodingException {
		List<NameValuePair> queryParams = new ArrayList<NameValuePair>();
		HttpEntityEnclosingRequestBase request = new HttpPost(Config.MESSAGE_URL);
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
		}
	}
}