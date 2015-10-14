package care.dovetail.common.model;

public class Group {
	public String name;
	public String uuid;
	public User admins[];
	public User members[];
	public long update_time;
	public long create_time;

	@Override
	public String toString() {
		if (name != null && !name.isEmpty()) {
			return name;
		}
		return getMemberString();
	}

	public String getMemberString() {
		StringBuilder builder = new StringBuilder();
		for (User member : members) {
			if (member.name != null && !member.name.isEmpty()) {
				String name = member.name.split(" ")[0];
				builder.append(name.length() > 10 ? name.substring(0, 10) : name).append(", ");
			}
		}
		return builder.toString().replaceFirst(", $", "");
	}

	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof Group) {
			Group group = (Group) o;
			return group.uuid != null && group.uuid.equalsIgnoreCase(uuid);
		}
		return super.equals(o);
	}
}
