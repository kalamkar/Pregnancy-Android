package care.dovetail;

import java.util.Date;

import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import care.dovetail.api.AppointmentDelete;
import care.dovetail.api.AppointmentUpdate;
import care.dovetail.api.AppointmentsGet;
import care.dovetail.api.GroupUpdate;
import care.dovetail.common.model.ApiResponse;
import care.dovetail.common.model.Appointment;
import care.dovetail.common.model.Group;
import care.dovetail.common.model.User;
import care.dovetail.fragments.NameEmailFragment;
import care.dovetail.fragments.NewAppointmentlFragment;

public class ProfileActivity extends FragmentActivity implements OnClickListener {
	private static final String TAG = "ProfileActivity";

	private App app;
	private User user;
	private boolean isOwner = false;
	private Appointment appointments[];

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);

		app = (App) getApplication();
		user = app.getUser(getIntent().getStringExtra(Config.USER_ID));
		if (user == null) {
			user = app.getMother();
			isOwner = true;
			findViewById(R.id.name).setOnClickListener(this);
			findViewById(R.id.roles).setOnClickListener(this);
			findViewById(R.id.newAppointment).setOnClickListener(this);
		} else {
			((TextView) findViewById(R.id.name)).setCompoundDrawablesWithIntrinsicBounds(
					null, null, null, null);
			((TextView) findViewById(R.id.roles)).setCompoundDrawablesWithIntrinsicBounds(
					null, null, null, null);
			findViewById(R.id.newAppointment).setVisibility(View.GONE);
		}
		((ListView) findViewById(R.id.appointments)).setAdapter(new AppointmentsAdapter());

		updateUi();

		new AppointmentsGet(app, isOwner ? null : user.uuid) {
			@Override
			protected void onPostExecute(ApiResponse result) {
				super.onPostExecute(result);
				appointments = result != null ? result.appointments : null;
				app.setAppointmentSyncTime(System.currentTimeMillis());
			}
		}.execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.profile, menu);
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.action_message:
			Group group = app.findUserGroup(user);
			if (group == null) {
				new GroupUpdate(app, null) {
					@Override
					protected void onPostExecute(ApiResponse response) {
						super.onPostExecute(response);
						Group group = app.findUserGroup(user);
						if (group != null) {
							startActivity(new Intent(ProfileActivity.this, MessagingActivity.class)
									.putExtra(Config.GROUP_ID, group.uuid));
							finish();
						}
					}
				}.execute(Pair.create(GroupUpdate.PARAM_MEMBER, user.uuid));
			} else {
				startActivity(new Intent(ProfileActivity.this, MessagingActivity.class)
					.putExtra(Config.GROUP_ID, group.uuid));
			}
			break;
		}
		return super.onOptionsItemSelected(item);
	}


	private void updateUi() {
		((TextView) findViewById(R.id.name)).setText(user.name);
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
			updateUi();
			((BaseAdapter) ((ListView) findViewById(R.id.appointments)).getAdapter()).notifyDataSetChanged();
		}
	};

	@Override
	public void onClick(View view) {
		int id = view.getId();
		Object tag = view.getTag();
		if (id == R.id.name) {
			new NameEmailFragment().show(getSupportFragmentManager(), null);
		} else if (id == R.id.roles) {
		} else if (id == R.id.newAppointment) {
			new NewAppointmentlFragment().show(getSupportFragmentManager(), null);
		} else if (id == R.id.delete && tag != null && tag instanceof Appointment) {
			final Appointment appointment = (Appointment) tag;
			new AlertDialog.Builder(this)
        		.setIcon(android.R.drawable.ic_dialog_info)
        		.setMessage(R.string.continue_remove_appointment)
        		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
        			@Override
        			public void onClick(DialogInterface dialog, int which) {
        				User user = app.getMother();
        				if (user.equals(appointment.consumer)) {
        					new AppointmentUpdate(app, appointment.id)
        						.execute(Pair.create(AppointmentUpdate.PARAM_CONSUMER, "-"));
        				} else if (user.equals(appointment.provider)) {
        					new AppointmentDelete(app, appointment.id).execute();
        				}
        			}
        		})
        		.setNegativeButton(android.R.string.cancel, null)
        		.show();
		} else if (tag != null && tag instanceof Appointment) {
			final Appointment appointment = (Appointment) tag;
			if (appointment.consumer == null && !appointment.provider.equals(app.getMother())) {
				// Pop up a dialog to confirm appointment booking.
				new AlertDialog.Builder(this)
		        	.setIcon(android.R.drawable.ic_dialog_info)
		        	.setMessage(R.string.continue_booking_appointment)
		        	.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
		        		@Override
		        		public void onClick(DialogInterface dialog, int which) {
		        			new AppointmentUpdate(app, appointment.id)
		        				.execute(Pair.create(AppointmentUpdate.PARAM_CONSUMER,
		        						app.getMother().uuid));
		        		}
		        	})
		        	.setNegativeButton(android.R.string.cancel, null)
		        	.show();
			}
		}
	}

	private class AppointmentsAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return appointments != null ? appointments.length : 0;
		}

		@Override
		public Appointment getItem(int position) {
			return appointments[position];
		}

		@Override
		public long getItemId(int position) {
			return getItem(position).hashCode();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			if (convertView == null) {
				view = getLayoutInflater().inflate(R.layout.list_item_appointment, null);
			} else {
				view = convertView;
			}

			Appointment appointment = getItem(position);
			if (appointment == null) {
				return view;
			}
			Date date = new Date(appointment.time);
			((TextView) view.findViewById(R.id.date)).setText(Config.DATE_FORMAT.format(date));
			((TextView) view.findViewById(R.id.time)).setText(Config.TIME_FORMAT.format(date));
			((TextView) view.findViewById(R.id.provider)).setText(appointment.provider.name);
			((TextView) view.findViewById(R.id.consumer)).setText(
					appointment.consumer != null ? appointment.consumer.name : null);
			view.setTag(appointment);
			if (appointment.consumer == null && !appointment.provider.equals(app.getMother())) {
				view.setOnClickListener(ProfileActivity.this);
			}
			view.findViewById(R.id.delete).setOnClickListener(ProfileActivity.this);
			view.findViewById(R.id.delete).setTag(appointment);
			view.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					View deleteBtn = v.findViewById(R.id.delete);
					deleteBtn.setVisibility(
							deleteBtn.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
					return true;
				}
			});
			return view;
		}
	}
}
