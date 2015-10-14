package care.dovetail.common.model;

public class Event {
	public enum Type {
		KICK,
		JAB,
		SWISH,
		ROLL,
		STEPS,
		WEIGHT,
		HEART_RATE,
		KICK_RECORDED,
		VOTE,
		SENSOR_CONNECTED,
		SENSOR_DISCONNECTED,
		SERVICE_STARTED,
		SERVICE_STOPPED
	}

	public String[] tags;
	public long time;
	public String data;

	public Event(String[] tags, long time) {
		this(tags, time, null);
	}

	public Event(String[] tags, long time, String data) {
		this.tags = tags;
		this.time = time;
		this.data = data;
	}
}
