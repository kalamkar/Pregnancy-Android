package care.dovetail.messaging;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.Pair;
import care.dovetail.MainActivity;
import care.dovetail.R;

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
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);
        if (!extras.isEmpty()) {
        	Log.i(TAG, "Received: " + extras.toString());
        	if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                sendNotification(extras.getString("message"));
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GCMBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(String message) {
    	if (message == null) {
			return;
		}

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);
    	Pair<String, String> lines =
    			splitLines(message, getResources().getString(R.string.app_name));
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
        		.setSmallIcon(R.drawable.ic_service)
        		.setContentTitle(lines.first)
        		.setContentText(lines.second);
        builder.setTicker(message);
		builder.setAutoCancel(true);
        builder.setContentIntent(contentIntent);

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
