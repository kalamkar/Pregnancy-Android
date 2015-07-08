package care.dovetail.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import care.dovetail.App;
import care.dovetail.R;
import care.dovetail.common.model.User;


public class DrawerFragment extends Fragment implements OnClickListener {
	private static final String TAG = "DrawerFragment";

	private App app;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		app = (App) activity.getApplication();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_drawer, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		view.findViewById(R.id.name).setOnClickListener(this);
		view.findViewById(R.id.email).setOnClickListener(this);
		view.findViewById(R.id.dueDate).setOnClickListener(this);
		view.findViewById(R.id.history).setOnClickListener(this);
		view.findViewById(R.id.about).setOnClickListener(this);

		User user = app.getMother();
		((TextView) view.findViewById(R.id.name)).setText(user.name);
		((TextView) view.findViewById(R.id.email)).setText(user.email);
	}

	@Override
	public void onClick(View view) {
		switch(view.getId()) {
		case R.id.name:
		case R.id.email:
			new NameEmailFragment().show(getActivity().getSupportFragmentManager(), null);
			break;
		case R.id.dueDate:
			new DueDateFragment().show(getChildFragmentManager(), null);
			break;
		}
	}
}