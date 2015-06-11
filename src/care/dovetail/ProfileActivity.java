package care.dovetail;

import java.util.Date;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import care.dovetail.api.AppointmentsGet;
import care.dovetail.api.GroupUpdate;
import care.dovetail.common.model.ApiResponse;
import care.dovetail.common.model.Appointment;
import care.dovetail.common.model.Group;
import care.dovetail.common.model.User;
import care.dovetail.fragments.NameEmailFragment;

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

			@SuppressWarnings("deprecation")
			Drawable edit = this.getResources().getDrawable(android.R.drawable.ic_menu_edit);
			((TextView) findViewById(R.id.name)).setCompoundDrawables(null, null, edit, null);
			((TextView) findViewById(R.id.roles)).setCompoundDrawables(null, null, edit, null);

			findViewById(R.id.name).setOnClickListener(this);
			findViewById(R.id.roles).setOnClickListener(this);
		}
		findViewById(R.id.message).setOnClickListener(this);
		((ListView) findViewById(R.id.appointments)).setAdapter(new AppointmentsAdapter());

		updateUi();

		new AppointmentsGet(app) {
			@Override
			protected void onPostExecute(ApiResponse result) {
				super.onPostExecute(result);
				appointments = result != null ? result.appointments : null;
			}
		}.execute();
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
		}
	};

	@SuppressWarnings("unchecked")
	@Override
	public void onClick(View view) {
		int id = view.getId();
		if (id == R.id.message) {
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
		} else if (id == R.id.name) {
			new NameEmailFragment().show(getSupportFragmentManager(), null);
		} else if (id == R.id.roles) {

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
				view = getLayoutInflater().inflate(R.layout.list_item_contact, null);
			} else {
				view = convertView;
			}

			Appointment appointment = getItem(position);
			if (appointment != null) {
				Date date = new Date(appointment.time);
				((TextView) view.findViewById(R.id.date)).setText(Config.DATE_FORMAT.format(date));
				((TextView) view.findViewById(R.id.time)).setText(Config.TIME_FORMAT.format(date));
				if (appointment.consumer != null) {
					((TextView) view.findViewById(R.id.consumer)).setText(appointment.consumer.name);
				}
			}
			view.setOnClickListener(ProfileActivity.this);
			view.setTag(appointment);
			return view;
		}
	}
}
