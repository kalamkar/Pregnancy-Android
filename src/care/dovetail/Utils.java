package care.dovetail;

import java.util.Date;

public class Utils {

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
}
