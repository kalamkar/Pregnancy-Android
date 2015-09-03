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

public class CardsGet extends ApiResponseTask {
	private static final String TAG = "CardsGet";

	public static final String PARAM_TAGS = "tags";
	public static final String PARAM_PUBLIC = "public";

	private final App app;

	public CardsGet(App app) {
		super(app.getUserUUID(), app.getUserAuth());
		this.app = app;
	}

	@Override
	protected HttpRequestBase makeRequest(Pair<String, String>... params)
			throws UnsupportedEncodingException {
		Uri.Builder builder = Uri.parse(Config.CARD_URL).buildUpon();
		for (Pair<String, String> param : params) {
			builder.appendQueryParameter(param.first, param.second);
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