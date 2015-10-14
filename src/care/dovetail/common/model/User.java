package care.dovetail.common.model;

import java.util.List;
import java.util.Map;


public class User {
	public String id;
	public String name;
	public String uuid;
	public String auth;
	public String email;
	public long update_time;
	public long create_time;

	public Map<String, String> features;
	public Card cards[];
	public List<Goal> goals;

	public enum Gender {
		Unknown,
		FEMALE,
		MALE;
	}

	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof User) {
			User user = (User) o;
			if (user.uuid != null && user.uuid.equalsIgnoreCase(uuid)) {
				return true;
			} else if (user.id != null && user.id.equalsIgnoreCase(id)) {
				return true;
			}
		}
		return super.equals(o);
	}
}
