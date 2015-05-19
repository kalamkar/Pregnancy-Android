package care.dovetail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Application;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import care.dovetail.common.Config;
import care.dovetail.common.model.Goal;
import care.dovetail.common.model.Goal.Aggregation;
import care.dovetail.common.model.Tip;
import care.dovetail.common.model.User;
import care.dovetail.model.Mother;
import care.dovetail.model.Mother.Baby;

public class App extends Application {

	public static final String USER_PROFILE = "USER_PROFILE";
	private final List<Tip> tips = new ArrayList<Tip>();

	@Override
	public void onCreate() {
		super.onCreate();
		makeTips();
	}

	public Mother getMother() {
		String profile = getSharedPreferences(getPackageName(), Application.MODE_PRIVATE)
				.getString(USER_PROFILE, null);
		return profile == null ? getFakeData() : Mother.fromUser(profile);
	}

	public void setMother(Mother mother) {
		setStringPref(USER_PROFILE, Config.GSON.toJson(mother.toUser()));
	}

	public List<Tip> getTips(String tag) {
		List<Tip> tips = new ArrayList<Tip>();
		for (Tip tip : this.tips) {
			List<String> tags = Arrays.asList(tip.tags);
			if (tag == null || tags.contains(tag.toLowerCase())) {
				tips.add(tip);
			}
		}
		return tips;
	}

	private void setStringPref(final String pref, String value) {
		final Editor editor = getSharedPreferences(
				getPackageName(), Application.MODE_PRIVATE).edit();
		new AsyncTask<String, Void, Void>() {
			@Override
			protected Void doInBackground(String... values) {
				if (values != null && values.length > 0) {
					editor.putString(pref, values[0]);
					editor.commit();
				}
				return null;
			}
		}.execute(value);
	}

	private void makeTips() {
		tips.add(new Tip("Eat 1/2 apple everyday.", new String[] {"mother"}, 1));
		tips.add(new Tip("X weeks Y days to go!", new String[] {"mother"}, 2));
		tips.add(new Tip("Your baby is A inches and B lbs now. Roughly the size of a DDDD",
				new String[] {"image:eggplant", "mother"}, 3));
		tips.add(new Tip("Expected birthdate is XXXX.", new String[] {"baby"}, 2));
	}

	private Mother getFakeData() {
		Mother mother = new Mother();
		mother.fullName = "Jennifer Doe";
		mother.email = "jennifer.doe@gmail.com";
		mother.dueDateMillis = 1436985891000L;
		mother.babies = new ArrayList<Baby>();
		mother.babies.add(new Baby("Mia", User.Gender.FEMALE));
		mother.goals = new ArrayList<Goal>();
		mother.goals.add(Goal.repeated("Walk 10,000 steps everyday", 1430505891000L, 86400000L)
				.aggregate(Aggregation.SUM).target(10000, "STEP"));
		return mother;
	}
}
