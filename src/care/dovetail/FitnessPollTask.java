package care.dovetail;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import care.dovetail.common.model.Event;
import care.dovetail.common.model.Measurement;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;

public class FitnessPollTask extends TimerTask {
	private static final String TAG = "FitnessPollTask";

	public static final String AUTH_PENDING = "auth_state_pending";

	private final App app;

	public FitnessPollTask(App app) {
		this.app = app;
	}

	@Override
	public void run() {
		long midnightMillis = getMidnightMillis();

		long lastPollTime = app.getFitnessPollTime();
		if (lastPollTime == 0) {
			// if not polled ever, set poll time to 24 hours back
			// TODO(abhi): Switch this to 90 days before launch.
			long backlogMillis = 24L * 60L * 60L * 1000L;
			lastPollTime = midnightMillis - backlogMillis;
		}

		if (lastPollTime > midnightMillis) {
			// If already polled today then skip this task.
			Log.i(TAG, String.format("Skipping poll as polled recently at %s",
					Config.MESSAGE_DATE_TIME_FORMAT.format(lastPollTime)));
			return;
		}

		DataReadRequest readRequest = makeDailyReadRequest(lastPollTime, midnightMillis);
		DataReadResult result =
		        Fitness.HistoryApi.readData(app.apiClient, readRequest).await(1, TimeUnit.MINUTES);
		if (result == null || !result.getStatus().isSuccess() || result.getBuckets() == null) {
			Log.w(TAG, result.getStatus().getStatusMessage());
			return;
		}

		for (Bucket bucket : result.getBuckets()) {
			try {
				Event steps = getStepEvent(bucket);
				if (steps != null) {
					app.events.add(steps);
				}
			} catch (Exception ex) {
				Log.w(TAG, ex);
			}
		}

		readRequest = makeWeightRequest(lastPollTime, midnightMillis);
		result = Fitness.HistoryApi.readData(app.apiClient, readRequest).await(1, TimeUnit.MINUTES);
		if (result == null || !result.getStatus().isSuccess()) {
			Log.w(TAG, result.getStatus().getStatusMessage());
			return;
		}
		try {
			Event weight = getWeightEvent(result.getDataSet(DataType.TYPE_WEIGHT));
			if (weight != null) {
				app.events.add(weight);
			}
		} catch (Exception ex) {
			Log.w(TAG, ex);
		}
		app.setFitnessPollTime(midnightMillis);
	}

	private static DataReadRequest makeDailyReadRequest(long startTime, long endTime) {
		SimpleDateFormat dateFormat = Config.MESSAGE_DATE_TIME_FORMAT;
		Log.i(TAG, String.format("Start: %s, End: %s",
				dateFormat.format(startTime), dateFormat.format(endTime)));

		return new DataReadRequest.Builder()
        		.setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
        		.bucketByTime(1, TimeUnit.DAYS)
		        .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
		        .build();
	}

	private static DataReadRequest makeWeightRequest(long startTime, long endTime) {
		SimpleDateFormat dateFormat = Config.MESSAGE_DATE_TIME_FORMAT;
		Log.i(TAG, String.format("Start: %s, End: %s",
				dateFormat.format(startTime), dateFormat.format(endTime)));

		return new DataReadRequest.Builder()
        		.setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
		        .read(DataType.TYPE_WEIGHT)
		        .build();
	}

	private static long getMidnightMillis() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
				cal.get(Calendar.DAY_OF_MONTH), 0, 0);
		return cal.getTimeInMillis();
	}

	private static Event getStepEvent(Bucket bucket) throws Exception {
		DataPoint data =
				bucket.getDataSet(DataType.TYPE_STEP_COUNT_DELTA).getDataPoints().get(0);
		Measurement steps = new Measurement();
		steps.value = data.getValue(data.getDataType().getFields().get(0)).asInt();
		steps.unit = Measurement.Unit.STEPS.name();
		steps.startMillis = data.getStartTime(TimeUnit.MILLISECONDS);
		steps.endMillis = data.getEndTime(TimeUnit.MILLISECONDS);
		Log.v(TAG, String.format("Steps on %s is %d",
				Config.MESSAGE_DATE_FORMAT.format(steps.endMillis), steps.value));
		return new Event(Event.Type.STEPS.name(), steps.endMillis, Config.GSON.toJson(steps));
	}

	private static Event getWeightEvent(DataSet dataSet) throws Exception {
		DataPoint data = dataSet.getDataPoints().get(0);
		Measurement weight = new Measurement();
		weight.value = (long) (data.getValue(data.getDataType().getFields().get(0)).asFloat() * 1000);
		weight.unit = Measurement.Unit.GRAMS.name();
		weight.startMillis = data.getStartTime(TimeUnit.MILLISECONDS);
		weight.endMillis = data.getEndTime(TimeUnit.MILLISECONDS);
		Log.v(TAG, String.format("Weight on %s is %d",
				Config.MESSAGE_DATE_FORMAT.format(weight.endMillis), weight.value));
		return new Event(Event.Type.WEIGHT.name(), weight.endMillis, Config.GSON.toJson(weight));
	}

	@SuppressWarnings("unused")
	private static void dumpDataSet(DataSet dataSet) {
	    Log.i(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
	    SimpleDateFormat dateFormat = Config.MESSAGE_DATE_FORMAT;

	    for (DataPoint dp : dataSet.getDataPoints()) {
	        Log.i(TAG, "Data point:");
	        Log.i(TAG, "\tType: " + dp.getDataType().getName());
	        Log.i(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
	        Log.i(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
	        for(Field field : dp.getDataType().getFields()) {
	            Log.i(TAG, "\tField: " + field.getName() +
	                    " Value: " + dp.getValue(field));
	        }
	    }
	}

	/**
	 *  Build a {@link GoogleApiClient} that will authenticate the user and allow the application
	 *  to connect to Fitness APIs. The scopes included should match the scopes your app needs
	 *  (see documentation for details). Authentication will occasionally fail intentionally,
	 *  and in those cases, there will be a known resolution, which the OnConnectionFailedListener()
	 *  can address. Examples of this include the user never having signed in before, or having
	 *  multiple accounts on the device and needing to specify which account to use, etc.
	 */
	public static void buildFitnessClient(final MainActivity activity, final App app) {
		// Create the Google API Client
		app.apiClient = new GoogleApiClient.Builder(app)
			.addApi(Fitness.HISTORY_API)
			.addScope(Fitness.SCOPE_ACTIVITY_READ)
			.addScope(Fitness.SCOPE_BODY_READ)
			.addConnectionCallbacks(
				new GoogleApiClient.ConnectionCallbacks() {

					@Override
					public void onConnected(Bundle bundle) {
						Log.i(TAG, "Google API Client Connected!!!");
						// Now you can make calls to the Fitness APIs.
						// Put application specific code here.
						new Timer().schedule(new FitnessPollTask(app), 10);
					}

					@Override
					public void onConnectionSuspended(int i) {
						// If your connection to the sensor gets lost at some point,
						// you'll be able to determine the reason and react to it here.
						if (i == ConnectionCallbacks.CAUSE_NETWORK_LOST) {
							Log.i(TAG, "Connection lost.  Cause: Network Lost.");
						} else if (i == ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
							Log.i(TAG, "Connection lost.  Reason: Service Disconnected");
						}
					}
				}).addOnConnectionFailedListener(
					new GoogleApiClient.OnConnectionFailedListener() {
						// Called whenever the API client fails to connect.
						@Override
						public void onConnectionFailed(ConnectionResult result) {
							Log.i(TAG, "Connection failed. Cause: " + result.toString());
							if (!result.hasResolution()) {
								// Show the localized error dialog
								GooglePlayServicesUtil.getErrorDialog(
										result.getErrorCode(), activity, 0).show();
								return;
							}
							// The failure has a resolution. Resolve it.
							// Called typically when the app is not yet authorized, and an
							// authorization dialog is displayed to the user.
							if (!app.authInProgress) {
								try {
									Log.i(TAG, "Attempting to resolve failed connection");
									app.authInProgress = true;
									result.startResolutionForResult(activity,
											Config.ACTIVITY_REQUEST_OAUTH);
								} catch (IntentSender.SendIntentException e) {
									Log.e(TAG, "Exception while starting resolution activity", e);
								}
							}
						}
					}).build();
		Log.i(TAG, "Created Google API Client.");
	}
}
