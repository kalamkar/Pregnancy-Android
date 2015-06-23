package care.dovetail.api;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpRequestBase;

import android.util.Log;
import android.util.Pair;
import android.widget.Toast;
import care.dovetail.App;
import care.dovetail.Config;
import care.dovetail.common.ApiResponseTask;
import care.dovetail.common.model.ApiResponse;

@SuppressWarnings("deprecation")
public class AppointmentDelete extends ApiResponseTask {
	private static final String TAG = "AppointmentDelete";

	private final App app;
	private final String appointmentId;

	public AppointmentDelete(App app, String appointmentId) {
		super(app.getUserUUID(), app.getUserAuth());
		this.app = app;
		this.appointmentId = appointmentId;
	}

	@Override
	protected HttpRequestBase makeRequest(Pair<String, String>... params) {
		return new HttpDelete(String.format("%s?appointment_id=%s", Config.APPOINTMENT_URL,
				appointmentId));
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