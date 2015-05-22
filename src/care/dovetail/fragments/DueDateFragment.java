package care.dovetail.fragments;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import care.dovetail.App;
import care.dovetail.model.Mother;

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
		Mother mother = app.getMother();
        Calendar dueDate = Calendar.getInstance();
		dueDate.setTimeInMillis(mother.dueDateMillis);
        return new DatePickerDialog(getActivity(), this, dueDate.get(Calendar.YEAR),
        		dueDate.get(Calendar.MONTH), dueDate.get(Calendar.DAY_OF_MONTH));
    }

    @Override
	public void onDateSet(DatePicker view, int year, int month, int day) {
		Calendar dueDate = Calendar.getInstance();
		dueDate.set(Calendar.YEAR, year);
		dueDate.set(Calendar.MONTH, month);
		dueDate.set(Calendar.DAY_OF_MONTH, day);
		Mother mother = app.getMother();
		mother.dueDateMillis = dueDate.getTimeInMillis();
		app.setMother(mother);
    }
}
