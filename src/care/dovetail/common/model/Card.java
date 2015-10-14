package care.dovetail.common.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Card {
	private static final Pattern TITLE = Pattern.compile("^([^\\.\\?\\!]+[\\.\\?\\!]+).*");

	public enum TAGS {
		INSIGHT,
		ARCHIVED,
		LIKED
	}

	public enum Action {
		DUE_DATE,
		CONNECT_SCALE,
		CONNECT_HEALTH_DATA,
		TO_DO,
		NONE
	}

	public enum Type {
		SIZE,
		TIP,
		POLL,
		SYMPTOM,
		VOTE,
		CARE,
		TODO,
		MILESTONE,
		INSIGHT,
		UNKNOWN
	}

	public String id;
	public String text;
	public String icon;
	public String image;
	public String url;
	public String options[];
	public String tags[];
	public int priority;
	public long expire_time;

	public String getTitle() {
		try {
			Matcher matcher = TITLE.matcher(text);
			return matcher.matches() ? matcher.group(1) : text;
		} catch (Exception ex) {
			return null;
		}
	}

	public String getText() {
		if (text == null) {
			return null;
		}
		String title = getTitle();
		return title == null ? text : text.replace(title, "").trim();
	}

	public Type getType() {
		if (tags == null) {
			return Type.UNKNOWN;
		}
		for (String tag : tags) {
			try {
				return Type.valueOf(tag.toUpperCase());
			} catch (Exception ex) {
				continue;
			}
		}
		return Type.UNKNOWN;
	}

	public Action getAction() {
		if (tags == null) {
			return Action.NONE;
		}
		for (String tag : tags) {
			try {
				if (tag.toLowerCase().startsWith("action:")) {
					return Action.valueOf(tag.replaceFirst("action:", "").toUpperCase());
				}
			} catch (Exception ex) {
				continue;
			}
		}
		if (getType() == Type.CARE) {
			return Action.TO_DO;
		}
		return Action.NONE;
	}
}