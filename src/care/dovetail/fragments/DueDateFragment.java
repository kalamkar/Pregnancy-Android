package care.dovetail.fragments;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Pair;
import android.widget.DatePicker;
import care.dovetail.App;
import care.dovetail.R;
import care.dovetail.api.UserUpdate;
import care.dovetail.common.model.ApiResponse;
import care.dovetail.common.model.Mother;

public class DueDateFragment extends DialogFragment
		implements DatePickerDialog.OnDateSetListener {
	private App app;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		app = (App) activity.getApplication();
	}

	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar dueDate = Calendar.getInstance();
		Mother mother = app.getMother();
		if (mother.dueDateMillis <= 0) {
			// Set default date for 2 months in future
			dueDate.setTimeInMillis(System.currentTimeMillis() + 60L * 86400L * 1000L);
		} else {
			dueDate.setTimeInMillis(mother.dueDateMillis);
		}
        return new DatePickerDialog(getActivity(), this, dueDate.get(Calendar.YEAR),
        		dueDate.get(Calendar.MONTH), dueDate.get(Calendar.DAY_OF_MONTH));
    }

    @SuppressWarnings("unchecked")
	@Override
	public void onDateSet(DatePicker view, int year, int month, int day) {
		Calendar dueDate = Calendar.getInstance();
		dueDate.set(Calendar.YEAR, year);
		dueDate.set(Calendar.MONTH, month);
		dueDate.set(Calendar.DAY_OF_MONTH, day);

		final ProgressDialog progress = ProgressDialog.show(getActivity(), null,
				getResources().getString(R.string.submitting_your_answer));
		String feature = String.format("%s=%s", Mother.FEATURE_DUE_DATE_MILLIS,
				Long.toString(dueDate.getTimeInMillis()));
		new UserUpdate(app){
			@Override
			protected void onPostExecute(ApiResponse result) {
				progress.dismiss();
				super.onPostExecute(result);
			}
		}.execute(Pair.create(UserUpdate.PARAM_FEATURE, feature));
    }
}
