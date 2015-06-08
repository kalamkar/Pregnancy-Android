package care.dovetail.fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import care.dovetail.App;
import care.dovetail.R;
import care.dovetail.api.UserUpdate;
import care.dovetail.common.Config;
import care.dovetail.common.model.User.Gender;
import care.dovetail.model.Mother;
import care.dovetail.model.Mother.Baby;

public class BabyEditFragment extends DialogFragment {
	public static final String BABY_NUMBER = "BABY_NUMBER";

	private App app;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		app = (App) activity.getApplication();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.baby_item, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Mother mother = app.getMother();
		int babyNumber = getArguments().getInt(BABY_NUMBER);
		if (mother.babies == null || mother.babies.size() < babyNumber - 1) {
			return;
		}
		Baby baby = mother.babies.get(babyNumber);
		((TextView) view.findViewById(R.id.babyName)).setText(baby.name);
		Spinner gender = (Spinner) view.findViewById(R.id.gender);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(app,
		        R.array.baby_genders, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		gender.setAdapter(adapter);
		gender.setSelection(
				baby.gender == Gender.FEMALE ? 1 : baby.gender == Gender.MALE ? 2 : 0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onDismiss(DialogInterface dialog) {
		int babyNumber = getArguments().getInt(BABY_NUMBER);
		Baby baby = new Baby();
		Spinner gender = (Spinner) getView().findViewById(R.id.gender);
		baby.name = ((TextView) getView().findViewById(R.id.babyName)).getText().toString();
		baby.gender = Gender.values()[gender.getSelectedItemPosition()];
		new UserUpdate(app).execute(Pair.create(UserUpdate.PARAM_FEATURE,
				String.format("BABY_%d=%s", babyNumber, Config.GSON.toJson(baby))));
		super.onDismiss(dialog);
	}
}