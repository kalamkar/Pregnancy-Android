package care.dovetail.messaging;

import java.util.List;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.Pair;
import care.dovetail.App;
import care.dovetail.Config;
import care.dovetail.MessagingActivity;
import care.dovetail.R;
import care.dovetail.common.model.ApiResponse.Message;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GCMIntentService extends IntentService {
	public static final String TAG = "GCMIntentService";

    public static final int NOTIFICATION_ID = 1;
    NotificationCompat.Builder builder;

    public GCMIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle data = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);
        if (!data.isEmpty()) {
        	Log.i(TAG, "Received: " + data.toString());
        	if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
        		processData(data);
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GCMBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void processData(Bundle data) {
    	if (!data.containsKey("message") && !data.containsKey(Config.GROUP_ID)) {
			return;
		}
    	Message message = Config.GSON.fromJson(data.getString("message"), Message.class);
    	if (message == null || message.text == null) {
    		Log.w(TAG, String.format("Invalid push message: %s", data.getString("message")));
    	}

    	App app = (App) getApplication();
    	if (app != null) {
    		List<Message> messages = app.messages.get(data.getString(Config.GROUP_ID));
    		messages.add(message);
    		app.messages.put(data.getString(Config.GROUP_ID), messages);
    	}

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MessagingActivity.class).replaceExtras(data), 0);
		sendNotification(message.text, contentIntent);
    }

    private void sendNotification(String text, PendingIntent intent) {
    	Pair<String, String> lines =
    			splitLines(text, getResources().getString(R.string.app_name));
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
        		.setSmallIcon(R.drawable.ic_service)
        		.setContentTitle(lines.first)
        		.setContentText(lines.second);
        builder.setTicker(text);
		builder.setAutoCancel(true);
        builder.setContentIntent(intent);

    	NotificationManager notificationManager = (NotificationManager)
    			this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private static Pair<String, String> splitLines(String line, String defaultLine1) {
    	String[] lines = line.split("[\\p{Punct}]", 2);
		String line1 = lines.length > 1 && lines[0] != null ? lines[0] : defaultLine1;
		String line2 = lines.length > 1 && lines[1] != null ? lines[1] : line;
		return Pair.create(line1.trim(), line2.trim());
    }
}
