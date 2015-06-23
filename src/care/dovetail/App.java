package care.dovetail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Application;
import android.content.SharedPreferences.Editor;
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

	public static final String USER_UUID = "USER_UUID";
	public static final String USER_AUTH = "USER_AUTH";
	public static final String USER_PROFILE = "USER_PROFILE";
	public static final String EVENT_SYNC_TIME = "EVENT_SYNC_TIME";
	public static final String MESSAGE_SYNC_TIME = "MESSAGE_SYNC_TIME";
	public static final String GROUP_SYNC_TIME = "GROUP_SYNC_TIME";
	public static final String APPOINTMENT_SYNC_TIME = "APPOINTMENT_SYNC_TIME";

	private String pushToken;
	private GoogleCloudMessaging gcm;

	private Mother mother;
	public List<Group> groups = new ArrayList<Group>();
	public Map<String, List<Message>> messages = new HashMap<String, List<Message>>();
	public List<User> contacts = new ArrayList<User>();

	@Override
	public void onCreate() {
		super.onCreate();
		String profile =
				getSharedPreferences(getPackageName(), MODE_PRIVATE).getString(USER_PROFILE, null);
		mother = profile != null ? Mother.fromUser(profile) : new Mother();
	}

	public void setEventSyncTime(long timeMillis) {
		setLongPref(EVENT_SYNC_TIME, timeMillis);
	}

	public long getEventSyncTime() {
		return getSharedPreferences(
				getPackageName(), MODE_PRIVATE).getLong(EVENT_SYNC_TIME, 0);
	}

	public void setMessageSyncTime(long timeMillis) {
		setLongPref(MESSAGE_SYNC_TIME, timeMillis);
	}

	public long getMessageSyncTime() {
		return getSharedPreferences(
				getPackageName(), MODE_PRIVATE).getLong(MESSAGE_SYNC_TIME, 0);
	}

	public void setGroupSyncTime(long timeMillis) {
		setLongPref(GROUP_SYNC_TIME, timeMillis);
		updateContacts();
	}

	public long getGroupSyncTime() {
		return getSharedPreferences(
				getPackageName(), MODE_PRIVATE).getLong(GROUP_SYNC_TIME, 0);
	}

	public void setAppointmentSyncTime(long timeMillis) {
		setLongPref(APPOINTMENT_SYNC_TIME, timeMillis);
	}

	public long getAppointmentSyncTime() {
		return getSharedPreferences(
				getPackageName(), MODE_PRIVATE).getLong(APPOINTMENT_SYNC_TIME, 0);
	}

	public String getUserUUID() {
		return getSharedPreferences(getPackageName(), MODE_PRIVATE).getString(USER_UUID, null);
	}

	public String getUserAuth() {
		return getSharedPreferences(getPackageName(), MODE_PRIVATE).getString(USER_AUTH, null);
	}

	public void setUser(User user) {
		if (user != null) {
			if (user.uuid != null) {
				setStringPref(USER_UUID, user.uuid);
			}
			if (user.auth != null) {
				setStringPref(USER_AUTH, user.auth);
			}
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

	public User getUser(String userId) {
		if (userId == null) {
			return null;
		}
		for (User user : contacts) {
			if (userId.equalsIgnoreCase(user.uuid)) {
				return user;
			}
		}
		return null;
	}

	public Group findUserGroup(User user) {
		for (Group group : groups) {
			if (group != null && group.members != null && group.members.length == 2 &&
					(user.equals(group.members[0]) || user.equals(group.members[1]))) {
				return group;
			}
		}
		return null;
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

	public List<Tip> getTips(String tag) {
		List<Tip> tips = new ArrayList<Tip>();
		if (mother == null || mother.insights == null) {
			return tips;
		}
		for (Tip tip : mother.insights) {
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

	private void setLongPref(final String pref, long value) {
		final Editor editor = getSharedPreferences(
				getPackageName(), Application.MODE_PRIVATE).edit();
		new AsyncTask<Long, Void, Void>() {
			@Override
			protected Void doInBackground(Long... values) {
				if (values != null && values.length > 0) {
					editor.putLong(pref, values[0]);
					editor.commit();
				}
				return null;
			}
		}.execute(value);
	}

	public void updateContacts() {
		for (Group group : groups) {
			for (User member : group.members) {
				if (!contacts.contains(member) && !member.equals(mother)) {
					contacts.add(member);
				}
			}
			for (User admin : group.admins) {
				if (!contacts.contains(admin) && !admin.equals(mother)) {
					contacts.add(admin);
				}
			}
		}
	}
}
