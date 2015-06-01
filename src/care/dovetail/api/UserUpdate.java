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

@SuppressWarnings("deprecation")
public class UserUpdate extends ApiResponseTask {
	private static final String TAG = "UserCreateUpdate";

	public static final String PARAM_TYPE = "type";
	public static final String PARAM_TOKEN = "token";
	public static final String PARAM_NAME = "name";
	public static final String PARAM_EMAIL = "email";
	public static final String PARAM_FEATURE = "feature";

	private final App app;

	public UserUpdate(App app) {
		this.app = app;
	}

	@Override
	protected HttpRequestBase makeRequest(Pair<String, String>... params)
			throws UnsupportedEncodingException {
		List<NameValuePair> queryParams = new ArrayList<NameValuePair>();
		HttpEntityEnclosingRequestBase request;
		if (app.getUserId() == null) {
			request = new HttpPost(Config.USER_URL);
		} else {
			request = new HttpPut(Config.USER_URL);
			queryParams.add(new BasicNameValuePair("user_id", app.getUserId()));
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
		} else if (result != null && result.users != null && result.users.length > 0) {
			app.setUser(result.users[0]);
		}
	}
}