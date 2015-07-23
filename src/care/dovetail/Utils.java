package care.dovetail;

import java.util.Calendar;
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

	public static long getMidnightMillis() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
				cal.get(Calendar.DAY_OF_MONTH), 0, 0);
		return cal.getTimeInMillis();
	}
}
