package care.dovetail;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.google.android.gms.analytics.HitBuilders;

public class Utils {
	private static final String TAG = "Utils";

	public static String getMessageDisplayTime(long messageTime) {
		long currentTime = System.currentTimeMillis();
		if (currentTime - messageTime < 24 * 60 * 60 * 1000) {
			return Config.MESSAGE_TIME_FORMAT.format(new Date(messageTime));
		} else if (currentTime - messageTime < 30 * 24 * 60 * 60 * 1000) {
			return Config.MESSAGE_DATE_FORMAT.format(new Date(messageTime));
		}
		return Config.MESSAGE_DATE_TIME_FORMAT.format(new Date(messageTime));
	}

	public static String getDisplayTime(long time) {
		long currentTime = System.currentTimeMillis();
		if (currentTime - time < 24 * 60 * 60 * 1000) {
			return Config.MESSAGE_TIME_FORMAT.format(new Date(time));
		}
		return Config.MESSAGE_DATE_FORMAT.format(new Date(time));
	}

	public static long getMidnightMillis() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
				cal.get(Calendar.DAY_OF_MONTH), 0, 0);
		return cal.getTimeInMillis();
	}

	public static void sendNotification(Context context, String text, int id) {
		sendNotification(context, text, id, null);
	}

    public static void sendNotification(Context context, String text, int id,
    		PendingIntent intent) {
    	Pair<String, String> lines =
    			splitLines(text, context.getResources().getString(R.string.app_name));
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
        		.setSmallIcon(R.drawable.ic_service)
        		.setContentTitle(lines.first)
        		.setContentText(lines.second);
        builder.setTicker(text);
		builder.setAutoCancel(true);
		builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
		if (intent != null) {
			builder.setContentIntent(intent);
		}

    	NotificationManager notificationManager = (NotificationManager)
    			context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, builder.build());
    }


    public static Pair<String, String> splitLines(String line, String defaultLine1) {
    	String[] lines = line.split("[\\p{Punct}]", 2);
		String line1 = lines.length > 1 && lines[0] != null ? lines[0] : defaultLine1;
		String line2 = lines.length > 1 && lines[1] != null ? lines[1] : line;
		return Pair.create(line1.trim(), line2.trim());
    }

    public static void trackEvent(App app, String category, String action, String label) {
    	app.tracker.send(new HitBuilders.EventBuilder()
	       .setCategory(category)
	       .setAction(action)
	       .setLabel(label)
	       .build());
    }

    public static int getRandom(int items[], long seed) {
    	return items[(int) (seed % items.length)];
    }

    public static String join(String[] tags) {
    	StringBuilder builder = new StringBuilder();
    	if (tags == null) {
    		return "";
    	}
    	for (String tag : tags) {
    		builder.append(tag).append(',');
    	}
    	return builder.toString().replaceFirst(",$", "");
    }

    public static String digest(String message) {
		StringBuffer hexString = new StringBuffer();
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			Log.w(TAG, e);
			return null;
		}
		byte[] hash = md.digest(message.getBytes());

		for (int i = 0; i < hash.length; i++) {
			if ((0xff & hash[i]) < 0x10) {
				hexString.append("0" + Integer.toHexString((0xFF & hash[i])));
			} else {
				hexString.append(Integer.toHexString(0xFF & hash[i]));
			}
		}
		return hexString.toString();
    }

    public static Bitmap getSnapshot(View view) {
        Bitmap bitmap =
        		Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(0xFFFFFFFF);
        view.draw(canvas);
        return bitmap;
    }
}
