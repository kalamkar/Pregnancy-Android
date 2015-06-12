package care.dovetail.fragments;

import java.util.Calendar;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TimePicker;
import care.dovetail.App;
import care.dovetail.R;
import care.dovetail.api.AppointmentUpdate;

public class NewAppointmentlFragment extends DialogFragment implements OnClickListener {
	private App app;

	private DatePicker date;
	private TimePicker time;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		app = (App) activity.getApplication();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_new_appointment, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		view.findViewById(R.id.create).setOnClickListener(this);
		date = (DatePicker) view.findViewById(R.id.date);
		time = (TimePicker) view.findViewById(R.id.time);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onClick(View view) {
		long millis = 0;
		Calendar calendar = Calendar.getInstance();
		calendar.set(date.getYear(), date.getMonth(), date.getDayOfMonth(),
				time.getCurrentHour(), time.getCurrentMinute());
		millis = calendar.getTimeInMillis();
		new AppointmentUpdate(app, null)
			.execute(Pair.create(AppointmentUpdate.PARAM_TIME, Long.toString(millis)));
		dismiss();
	}
}