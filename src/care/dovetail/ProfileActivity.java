package care.dovetail;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;
import care.dovetail.common.Config;
import care.dovetail.model.Mother;

public class ProfileActivity extends FragmentActivity {

	private App app;

	private Timer motherEditTimer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);

		app = (App) getApplication();
		Mother mother = app.getMother();

		TextView fullName = (TextView) findViewById(R.id.fullName);
		TextView email = (TextView) findViewById(R.id.email);

		fullName.setText(mother.fullName);
		email.setText(mother.email);

		fullName.addTextChangedListener(new ProfileTextWatcher(fullName));
		email.addTextChangedListener(new ProfileTextWatcher(email));
	}

	private class ProfileTextWatcher  implements TextWatcher {
		private final TextView view;

		ProfileTextWatcher(TextView view) {
			this.view = view;
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			final String text = s.toString();
			if (motherEditTimer != null) {
				motherEditTimer.cancel();
			}
			motherEditTimer = new Timer();
			motherEditTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					Mother mother = app.getMother();
					if (view.getId() == R.id.fullName) {
						mother.fullName = text;
					} else if (view.getId() == R.id.email) {
						mother.email = text;
					}
					app.setMother(mother);
				}
			}, Config.TEXTBOX_EDIT_DELAY_MS);
		}
	}
}
