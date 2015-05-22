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
import care.dovetail.common.model.Tip;

public class HelloFragment extends Fragment {
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
			fillNameEmail(getView());
			fillTips(getView());
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_hello, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		view.findViewById(R.id.edit).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new NameEmailFragment().show(getChildFragmentManager(), null);
			}
		});
		view.findViewById(R.id.dueDate).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new DueDateFragment().show(getChildFragmentManager(), null);
			}
		});

		fillNameEmail(view);
		fillTips(view);
	}

	private void fillNameEmail(View view) {
		String firstName = app.getMother().fullName != null
				? app.getMother().fullName.split(" ")[0] : "";
		((TextView) view.findViewById(R.id.hello)).setText(String.format(
				getResources().getString(R.string.hello_text), firstName));
	}

	private void fillTips(View view) {
		Tip sortedTips[] = app.getTips("mother").toArray(new Tip[0]);
		Arrays.sort(sortedTips, new Comparator<Tip>() {
			@Override
			public int compare(Tip lhs, Tip rhs) {
				return lhs.priority - rhs.priority;
			}
		});
		int tipIds[] = new int[] { R.id.tip1, R.id.tip2, R.id.tip3 };
		for (int i = 0; i < sortedTips.length && i < tipIds.length; i++) {
			((TextView) view.findViewById(tipIds[i])).setText(sortedTips[i].title);
		}
	}
}