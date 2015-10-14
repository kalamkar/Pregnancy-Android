package care.dovetail.common.model;

public class ApiResponse {
	public String code;
	public String message;

	public User users[];
	public Group groups[];
	public Message messages[];
	public Event events[];
	public Result results[];
	public Card cards[];
	public Appointment appointments[];

	public static class Message {
		public User sender;
		public String text;
		public String group_uuid;
		public long create_time;
	}

	public static class Result {
		public User user;
		public Group group;
		public Message message;
		public Card card;
	}
}
