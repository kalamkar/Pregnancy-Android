package care.dovetail.api;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.util.Pair;
import android.util.Log;
import android.webkit.MimeTypeMap;
import care.dovetail.App;
import care.dovetail.Config;
import care.dovetail.common.ApiResponseTask;


public class PhotoUpdate extends AsyncTask<String, Void, String> {
	private static final String TAG = "PhotoUpdate";

	private final App app;
	private final String localUrl;
	private final Uri contentUri;
	private final String contentType;

	private static final String CRLF = "\r\n";
	private static final String HYPHENS = "--";
	private static final String BOUNDARY =  "*****";

	public PhotoUpdate(App app, Uri contentUri, String contentType) {
		this.app = app;
		this.contentUri = contentUri;
		this.localUrl = null;
		this.contentType = contentType;
	}

	public PhotoUpdate(App app, String localUrl, String contentType) {
		this.app = app;
		this.localUrl = localUrl;
		this.contentUri = null;
		this.contentType = contentType;
	}

	@Override
	protected String doInBackground(String... params) {
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			if (localUrl != null) {
				URL parsedUrl = new URL(localUrl);
				URLConnection connection = parsedUrl.openConnection();
				copy(new BufferedInputStream(connection.getInputStream()), output);
			} else {
				copy(new BufferedInputStream(
						app.getContentResolver().openInputStream(contentUri)), output);
			}

			return uploadFile(contentType, output.toByteArray()).second;
		} catch (Throwable t) {
			Log.e(TAG, "", t);
			return null;
		}
	}

	private Pair<Integer, String> uploadFile(String contentType, byte data[]) throws IOException {

		HttpURLConnection conn = (HttpURLConnection) new URL(Config.PHOTO_UPLOAD_URL).openConnection();
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.setRequestMethod("POST");

		String uuid = app.getUserUUID();
		String authToken = app.getUserAuth();

		// conn.setRequestProperty("Connection", "Keep-Alive");
		conn.setRequestProperty("Authorization", ApiResponseTask.getAuthHeader(uuid, authToken));
		conn.setRequestProperty("ENCTYPE", "multipart/form-data");
		conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);

		DataOutputStream out = new DataOutputStream(conn.getOutputStream());
		out.writeBytes(HYPHENS + BOUNDARY + CRLF);
		out.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\"filename\"" + CRLF);
		out.writeBytes("Content-Type: " + contentType + CRLF);
		out.writeBytes(CRLF);
		out.write(data, 0, data.length);
		out.writeBytes(CRLF);

		out.writeBytes(HYPHENS + BOUNDARY + HYPHENS + CRLF);
		out.flush();
		out.close();

		int responseCode = conn.getResponseCode();
		StringBuilder response = new StringBuilder();

		try {
			String line;
			BufferedReader reader = new BufferedReader(
					new InputStreamReader((InputStream) conn.getContent()));
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
		} catch (IOException ex) {
			Log.w(TAG, ex);
			return Pair.create(responseCode, conn.getResponseMessage());
		} finally {
			conn.disconnect();
		}

		return Pair.create(responseCode, response.toString());
	}

	public byte[] fileToBytes(String pathToFile){
		File file = new File(pathToFile);
		int size = (int) file.length();
		byte[] bytes = new byte[size];
		try {
		    BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
		    buf.read(bytes, 0, bytes.length);
		    buf.close();
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
		}
		return bytes;
	}

	public static void copy(InputStream input, OutputStream output) throws IOException {
		byte[] buffer = new byte[512 * 1024];
		int bytesRead = 0;
		while ((bytesRead = input.read(buffer)) != -1) {
			output.write(buffer, 0, bytesRead);
		}
		output.close();
		input.close();
	}

	public static String getMimeType(String pathToFile) {
	    String type = null;
	    String extension = MimeTypeMap.getFileExtensionFromUrl(pathToFile);
	    if (extension != null) {
	        type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
	    }
	    return type;
	}
}