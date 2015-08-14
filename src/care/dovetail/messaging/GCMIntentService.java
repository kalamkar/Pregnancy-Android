package care.dovetail.messaging;

import java.util.ArrayList;
import java.util.List;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import care.dovetail.App;
import care.dovetail.Config;
import care.dovetail.MessagingActivity;
import care.dovetail.Utils;
import care.dovetail.common.model.ApiResponse.Message;
import care.dovetail.common.model.User;

import com.google.android.gms.gcm.GcmListenerService;

public class GCMIntentService extends GcmListenerService {
	public static final String TAG = "GCMIntentService";

    NotificationCompat.Builder builder;

    @Override
	public void onMessageReceived(String from, Bundle data) {
        if (!data.isEmpty()) {
        	Log.i(TAG, "Received: " + data.toString());
        	if (data.containsKey("message") || data.containsKey(Config.GROUP_ID)) {
        		processMessage(data);
    		} else if (data.containsKey("user")) {
    			processUser(data);
    		}
        }
		super.onMessageReceived(from, data);
	}

    private void processUser(Bundle data) {
    	User user = Config.GSON.fromJson(data.getString("user"), User.class);
    	if (user == null || user.uuid == null || user.auth == null) {
    		Log.w(TAG, String.format("Invalid push message: %s", data.getString("user")));
    	}

    	App app = (App) getApplication();
    	if (app != null) {
    		app.setUser(user);
    	}
    }

    private void processMessage(Bundle data) {
    	Message message = Config.GSON.fromJson(data.getString("message"), Message.class);
    	if (message == null || message.text == null) {
    		Log.w(TAG, String.format("Invalid push message: %s", data.getString("message")));
    	}

    	App app = (App) getApplication();
    	if (app != null) {
    		List<Message> messages = app.messages.get(data.getString(Config.GROUP_ID));
    		if (messages == null) {
    			messages = new ArrayList<Message>(1);
    		}
    		messages.add(message);
    		app.messages.put(data.getString(Config.GROUP_ID), messages);
    		app.setMessageSyncTime(System.currentTimeMillis());
    	}

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MessagingActivity.class).replaceExtras(data), 0);
		Utils.sendNotification(this, message.text, Config.MESSAGE_NOTIFICATION_ID, contentIntent);
    }
}
