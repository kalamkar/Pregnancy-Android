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
public class AppointmentUpdate extends ApiResponseTask {
	private static final String TAG = "AppointmentUpdate";

	public static final String PARAM_TIME = "time";
	public static final String PARAM_MINUTES = "minutes";
	public static final String PARAM_CONSUMER = "consumer_uuid";

	private final App app;
	private final String appointmentId;

	public AppointmentUpdate(App app, String appointmentId) {
		this.app = app;
		this.appointmentId = appointmentId;
	}

	@Override
	protected HttpRequestBase makeRequest(Pair<String, String>... params)
			throws UnsupportedEncodingException {
		List<NameValuePair> queryParams = new ArrayList<NameValuePair>();
		queryParams.add(new BasicNameValuePair("user_id", app.getUserId()));
		HttpEntityEnclosingRequestBase request;
		if (appointmentId == null) {
			request = new HttpPost(Config.APPOINTMENT_URL);
		} else {
			request = new HttpPut(Config.APPOINTMENT_URL);
			queryParams.add(new BasicNameValuePair("appointment_id", appointmentId));
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
		}
	}
}