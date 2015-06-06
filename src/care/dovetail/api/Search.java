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
import care.dovetail.common.model.ApiResponse.Result;

@SuppressWarnings("deprecation")
public abstract class Search extends ApiResponseTask {
	private static final String TAG = "Search";

	public static final String PARAM_QUERY = "q";
	public static final String PARAM_NEARBY = "nearby";

	private final App app;

	public Search(App app) {
		this.app = app;
	}

	@Override
	protected HttpRequestBase makeRequest(Pair<String, String>... params)
			throws UnsupportedEncodingException {
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("%s?user_id=%s", Config.SEARCH_URL, app.getUserId()));
		for (Pair<String, String> param : params) {
			builder.append(String.format("&%s=%s", param.first, param.second));
		}
		return new HttpGet(builder.toString());
	}

	@Override
	protected void onPostExecute(ApiResponse result) {
		super.onPostExecute(result);
		if (result != null && !"OK".equalsIgnoreCase(result.code)) {
			Log.e(TAG, result.message);
			Toast.makeText(app, result.message, Toast.LENGTH_LONG).show();
		} else if (result != null && result.results != null) {
			onResult(result.results);
		}
	}

	public abstract void onResult(Result results[]);
}