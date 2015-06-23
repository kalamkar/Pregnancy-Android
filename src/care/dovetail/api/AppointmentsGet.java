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

@SuppressWarnings("deprecation")
public class AppointmentsGet extends ApiResponseTask {
	private static final String TAG = "AppointmentsGet";

	private final App app;
	private final String uuid;

	public AppointmentsGet(App app, String uuid) {
		super(app.getUserUUID(), app.getUserAuth());
		this.app = app;
		this.uuid = uuid;
	}

	@Override
	protected HttpRequestBase makeRequest(Pair<String, String>... params)
			throws UnsupportedEncodingException {
		if (uuid != null) {
			return new HttpGet(String.format("%s?uuid=%s", Config.APPOINTMENT_URL, uuid));
		} else {
			return new HttpGet(Config.APPOINTMENT_URL);
		}
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