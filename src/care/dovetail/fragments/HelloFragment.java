package care.dovetail.fragments;

import java.util.Arrays;
import java.util.Comparator;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_hello, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		String firstName = app.getMother().fullName != null
				? app.getMother().fullName.split(" ")[0] : "";
		((TextView) view.findViewById(R.id.hello)).setText(String.format(
				getResources().getString(R.string.hello_text), firstName));

		Tip sortedTips[] = app.getTips().toArray(new Tip[0]);
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