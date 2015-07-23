package care.dovetail.api;

import java.io.UnsupportedEncodingException;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;

import android.net.Uri;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;
import care.dovetail.App;
import care.dovetail.Config;
import care.dovetail.common.ApiResponseTask;
import care.dovetail.common.model.ApiResponse;

@SuppressWarnings("deprecation")
public class EventsGet extends ApiResponseTask {
	private static final String TAG = "EventsGet";

	private final App app;
	private final long startTime;
	private final long endTime;
	private final String type;

	public EventsGet(App app, String type) {
		this(app, type, -1, -1);
	}

	public EventsGet(App app, String type, long startTime, long endTime) {
		super(app.getUserUUID(), app.getUserAuth());
		this.app = app;
		this.type = type;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	@Override
	protected HttpRequestBase makeRequest(Pair<String, String>... params)
			throws UnsupportedEncodingException {
		Uri.Builder builder = Uri.parse(Config.EVENT_URL).buildUpon();
		for (Pair<String, String> param : params) {
			builder.appendQueryParameter(param.first, param.second);
		}
		if (startTime >= 0) {
			builder.appendQueryParameter("start_time", Long.toString(startTime));
		}
		if (endTime >= 0) {
			builder.appendQueryParameter("end_time", Long.toString(endTime));
		}
		if (type != null) {
			builder.appendQueryParameter("type", type);
		}
		return new HttpGet(builder.build().toString());
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