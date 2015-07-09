package care.dovetail;

import java.util.Date;

public class Utils {
	
	public static String getDisplayTime(long messageTime) {
		long currentTime = System.currentTimeMillis();
		if (currentTime - messageTime < 24 * 60 * 60 * 1000) {
			return Config.MESSAGE_TIME_FORMAT.format(new Date(messageTime));
		} else if (currentTime - messageTime < 30 * 24 * 60 * 60 * 1000) {
			return Config.MESSAGE_DATE_FORMAT.format(new Date(messageTime));
		}
		return Config.MESSAGE_DATE_TIME_FORMAT.format(new Date(messageTime));
	}
}
