package care.dovetail.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import care.dovetail.Config;
import care.dovetail.common.model.ApiResponse;

import com.google.gson.JsonSyntaxException;

public abstract class ApiResponseTask extends AsyncTask<Pair<String, String>, Void, ApiResponse> {
	private final static String TAG = "ApiResponseTask";

	protected final DefaultHttpClient httpClient = new DefaultHttpClient();

	private final String userId;
	private final String authToken;

	public ApiResponseTask() {
		this(null, null);
	}

	public ApiResponseTask(String userId, String authToken) {
		HttpParams params = httpClient.getParams();
		HttpConnectionParams.setConnectionTimeout(params, 5000);
		HttpConnectionParams.setSoTimeout(params, 5000);
		httpClient.setParams(params);
		this.userId = userId;
		this.authToken = authToken;
	}

	abstract protected HttpRequestBase makeRequest(Pair<String, String>... params)
			throws UnsupportedEncodingException;

	@Override
	protected ApiResponse doInBackground(Pair<String, String>... params) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			HttpRequestBase request = makeRequest(params);
			if (userId != null && authToken != null) {
				request.addHeader("Authorization", getAuthHeader(userId, authToken));
			}
			Log.v(TAG, String.format("HTTP %s %s", request.getMethod(), request.getURI()));
			HttpResponse response = httpClient.execute(request);
			if (response != null && response.getEntity() != null) {
				response.getEntity().writeTo(output);
			}
			output.close();
			return Config.GSON.fromJson(output.toString(), ApiResponse.class);
		} catch (JsonSyntaxException e) {
			Log.e(TAG, output.toString(), e);
		} catch (IOException e) {
			Log.e(TAG, "IOException", e);
		}
		return null;
	}

	public static String getAuthHeader(String uuid, String authToken) {
		return String.format("UUID-TOKEN uuid=\"%s\", token=\"%s\"", uuid, authToken);
	}
}
