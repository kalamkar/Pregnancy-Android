package care.dovetail.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import care.dovetail.App;
import care.dovetail.R;
import care.dovetail.api.UserUpdate;
import care.dovetail.common.model.Mother;

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
		((TextView) view.findViewById(R.id.fullName)).setText(mother.name);
		((TextView) view.findViewById(R.id.email)).setText(mother.email);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	  Dialog dialog = super.onCreateDialog(savedInstanceState);
	  dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
	  return dialog;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onDismiss(DialogInterface dialog) {
		String name = ((TextView) getView().findViewById(R.id.fullName)).getText().toString();
		String email = ((TextView) getView().findViewById(R.id.email)).getText().toString();
		new UserUpdate(app).execute(Pair.create(UserUpdate.PARAM_NAME, name),
				Pair.create(UserUpdate.PARAM_EMAIL, email));
		super.onDismiss(dialog);
	}

}