package care.dovetail.fragments;

import java.util.Arrays;
import java.util.Comparator;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import care.dovetail.App;
import care.dovetail.R;
import care.dovetail.common.model.Card;
import care.dovetail.model.Mother.Baby;

public class BabyInfoFragment extends Fragment {
	private App app;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		app = (App) activity.getApplication();
	}

	@Override
	public void onResume() {
		app.getSharedPreferences(app.getPackageName(), Application.MODE_PRIVATE)
				.registerOnSharedPreferenceChangeListener(listener);
		super.onResume();
	}

	@Override
	public void onPause() {
		app.getSharedPreferences(app.getPackageName(), Application.MODE_PRIVATE)
				.unregisterOnSharedPreferenceChangeListener(listener);
		super.onPause();
	}

	private OnSharedPreferenceChangeListener listener = new OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
			updateView(getView());
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_baby_info, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		view.findViewById(R.id.edit).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				BabyEditFragment fragment = new BabyEditFragment();
				Bundle args = new Bundle();
				args.putInt(BabyEditFragment.BABY_NUMBER, 0);
				fragment.setArguments(args);
				fragment.show(getChildFragmentManager(), null);
			}
		});
		updateView(view);
	}

	private void updateView(View view) {
		if (app.getMother().babies != null && app.getMother().babies.size() > 0) {
			Baby baby = app.getMother().babies.get(0);
			if (baby.name != null) {
				((TextView) view.findViewById(R.id.hello)).setText(String.format(
						getResources().getString(R.string.baby_text), baby.name));
			}
		}

		Card sortedTips[] = app.getTips("baby").toArray(new Card[0]);
		Arrays.sort(sortedTips, new Comparator<Card>() {
			@Override
			public int compare(Card lhs, Card rhs) {
				return lhs.priority - rhs.priority;
			}
		});
		int tipIds[] = new int[] { R.id.tip1, R.id.tip2 };
		for (int i = 0; i < sortedTips.length && i < tipIds.length; i++) {
			((TextView) view.findViewById(tipIds[i])).setText(sortedTips[i].text);
		}
	}
}