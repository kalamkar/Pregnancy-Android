package care.dovetail.fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import care.dovetail.App;
import care.dovetail.R;
import care.dovetail.model.Mother;

public class NameEmailFragment extends DialogFragment {
	private App app;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		app = (App) activity.getApplication();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_name_email, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Mother mother = app.getMother();
		((TextView) view.findViewById(R.id.fullName)).setText(mother.fullName);
		((TextView) view.findViewById(R.id.email)).setText(mother.email);
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		Mother mother = app.getMother();
		mother.fullName = ((TextView) getView().findViewById(R.id.fullName)).getText().toString();
		mother.email = ((TextView) getView().findViewById(R.id.email)).getText().toString();
		app.setMother(mother);
		super.onDismiss(dialog);
	}

}