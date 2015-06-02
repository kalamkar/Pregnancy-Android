package care.dovetail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import care.dovetail.api.UserUpdate;
import care.dovetail.common.model.ApiResponse.Message;
import care.dovetail.common.model.Goal;
import care.dovetail.common.model.Goal.Aggregation;
import care.dovetail.common.model.Group;
import care.dovetail.common.model.Tip;
import care.dovetail.common.model.User;
import care.dovetail.messaging.GCMUtils;
import care.dovetail.model.Mother;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class App extends Application {
	static final String TAG = "App";

	private static final String USER_ID = "USER_ID";
	private static final String USER_PROFILE = "USER_PROFILE";
	private static final String LAST_SYNC_TIME = "LAST_SYNC_TIME";

	private String pushToken;
	private GoogleCloudMessaging gcm;

	private Mother mother;
	public List<Group> groups = new ArrayList<Group>();
	public Map<String, List<Message>> messages = new HashMap<String, List<Message>>();
	private final List<Tip> tips = new ArrayList<Tip>();

	@Override
	public void onCreate() {
		super.onCreate();
		String profile =
				getSharedPreferences(getPackageName(), MODE_PRIVATE).getString(USER_PROFILE, null);
		try {
			mother = Mother.fromUser(profile);
		} catch(Exception ex) {
			mother = new Mother();
		}
		requestPushToken();
		makeTips();
		getSharedPreferences(getPackageName(), Application.MODE_PRIVATE)
			.registerOnSharedPreferenceChangeListener(listener);
	}

	@Override
	public void onTerminate() {
		getSharedPreferences(getPackageName(), Application.MODE_PRIVATE)
			.unregisterOnSharedPreferenceChangeListener(listener);
		super.onTerminate();
	}

	public void setLastSyncTime(long timeMillis) {
		final Editor editor = getSharedPreferences(getPackageName(), MODE_PRIVATE).edit();
		new AsyncTask<Long, Void, Void>() {
			@Override
			protected Void doInBackground(Long... timeMillis) {
				if (timeMillis != null && timeMillis.length > 0) {
					editor.putLong(LAST_SYNC_TIME, timeMillis[0]);
					editor.commit();
				}
				return null;
			}
		}.execute(timeMillis);
	}

	public long getLastSyncTime() {
		return getSharedPreferences(
				getPackageName(), MODE_PRIVATE).getLong(LAST_SYNC_TIME, 0);
	}

	public String getUserId() {
		return getSharedPreferences(getPackageName(), MODE_PRIVATE).getString(USER_ID, null);
	}

	public void setUser(User user) {
		if (user != null && user.id != null) {
			setStringPref(USER_ID, user.id);
		}
		mother = Mother.fromUser(Config.GSON.toJson(user));

		// TODO(abhi): remove this once we have real goals.
		mother.goals = new ArrayList<Goal>();
		mother.goals.add(Goal.repeated("Walk 10,000 steps everyday", 1430505891000L, 86400000L)
				.aggregate(Aggregation.SUM).target(10000, "STEP"));
		setStringPref(USER_PROFILE, Config.GSON.toJson(user));
	}

	public Mother getMother() {
		return mother;
	}

	public String getPushToken() {
		return pushToken;
	}

	@SuppressWarnings("unchecked")
	public void requestPushToken() {
		if (pushToken != null) {
			Log.i(TAG, "Device already registered, registration ID = " + pushToken);
			new UserUpdate(this).execute(Pair.create(UserUpdate.PARAM_TYPE, "GOOGLE"),
					Pair.create(UserUpdate.PARAM_TOKEN, pushToken));
			return;
		}
	    new AsyncTask<Void, Void, Void>() {
			@Override
	        protected Void doInBackground(Void... params) {
	            try {
	            	if (gcm == null) {
	                    gcm = GoogleCloudMessaging.getInstance(App.this);
	                }
	                pushToken = gcm.register(Config.GCM_SENDER_ID);
	                Log.i(TAG, "Device registered, registration ID = " + pushToken);
	                new UserUpdate(App.this).execute(Pair.create(UserUpdate.PARAM_TYPE, "GOOGLE"),
	    					Pair.create(UserUpdate.PARAM_TOKEN, pushToken));
	                GCMUtils.storeRegistrationId(App.this, pushToken);
	            } catch (IOException ex) {
	                Log.w(TAG, ex);
	            }
	            return null;
	        }
	    }.execute(null, null, null);
	}

	private OnSharedPreferenceChangeListener listener = new OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
			makeTips();
		}
	};

	public List<Tip> getTips(String tag) {
		List<Tip> tips = new ArrayList<Tip>();
		for (Tip tip : this.tips) {
			List<String> tags = Arrays.asList(tip.tags);
			if (tag == null || tags.contains(tag.toLowerCase())) {
				tips.add(tip);
			}
		}
		return tips;
	}

	private void setStringPref(final String pref, String value) {
		final Editor editor = getSharedPreferences(
				getPackageName(), Application.MODE_PRIVATE).edit();
		new AsyncTask<String, Void, Void>() {
			@Override
			protected Void doInBackground(String... values) {
				if (values != null && values.length > 0) {
					editor.putString(pref, values[0]);
					editor.commit();
				}
				return null;
			}
		}.execute(value);
	}

	private void makeTips() {
		tips.clear();
		int daysToGo = (int) ((mother.dueDateMillis - new Date().getTime()) / (1000*60*60*24));
		if (daysToGo < 0) {
			tips.add(new Tip("Congratulations!", new String[] {"mother"}, 2));
		} else if (daysToGo < 7) {
			tips.add(new Tip(String.format("%d days to go!", daysToGo), new String[] {"mother"}, 2));
		} else if (daysToGo < 275) {
			tips.add(new Tip(String.format("%d weeks %d days to go!", daysToGo / 7, daysToGo % 7),
					new String[] {"mother"}, 2));
		}

		tips.add(new Tip("Your baby is A inches and B lbs now. Roughly the size of a DDDD.",
				new String[] {"image:eggplant", "mother"}, 3));
		tips.add(new Tip(String.format("Expected birthdate is %s.",
				Config.DATE_FORMAT.format(new Date(mother.dueDateMillis))),
				new String[] {"baby"}, 2));
		tips.add(new Tip("Eat 1/2 apple everyday.", new String[] {"mother"}, 1));
	}
}
