package care.dovetail.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import care.dovetail.App;
import care.dovetail.R;

public class HomeFragment extends Fragment {
	private App app;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		app = (App) activity.getApplication();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_home, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
		transaction.add(R.id.hello, new HelloFragment());
//		transaction.add(R.id.photos, new PhotosFragment());
//		transaction.add(R.id.tips, new TipsFragment());
//		transaction.add(R.id.mom_goals, new MomGoalsFragment());
//		transaction.add(R.id.mom_trends, new MomTrendsFragment());
		transaction.commit();
	}
}