package care.dovetail.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import care.dovetail.common.Config;
import care.dovetail.common.model.User;

public class Mother extends User {
	public long dueDateMillis;
	public List<Baby> babies;

	public static class Baby {
		public String name;
		public Gender gender;

		public Baby() {}

		public Baby(String name, Gender gender) {
			this.name = name;
			this.gender = gender;
		}
	}

	public User toUser() {
		if (data == null) {
			data = new HashMap<String, String>();
		} else {
			data.clear();
		}
		data.put("DUE_DATE_MILLIS", Long.toString(dueDateMillis));
		for (int i = 0; babies != null && i < babies.size(); i++) {
			data.put("BABY_" + i, Config.GSON.toJson(babies.get(i)));
		}
		return this;
	}

	public static Mother fromUser(String user) {
		Mother mother = Config.GSON.fromJson(user, Mother.class);
		if (mother == null || mother.data == null) {
			return mother;
		}
		mother.dueDateMillis = Long.parseLong(mother.data.get("DUE_DATE_MILLIS"));
		mother.babies = new ArrayList<Baby>();
		for (String key : mother.data.keySet()) {
			if (key.startsWith("BABY_")) {
				Baby baby = Config.GSON.fromJson(mother.data.get(key), Baby.class);
				mother.babies.add(Integer.parseInt(key.replace("BABY_", "")), baby);
			}
		}
		return mother;
	}
}
