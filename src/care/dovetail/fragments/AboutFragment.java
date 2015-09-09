package care.dovetail.fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import care.dovetail.App;
import care.dovetail.R;
import care.dovetail.Utils;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class AboutFragment extends Fragment implements OnClickListener {
	private static final String TAG = "AboutFragment";

	private App app;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		app = (App) activity.getApplication();
	}

	@Override
	public void onResume() {
		app.tracker.setScreenName(TAG);
		app.tracker.send(new HitBuilders.ScreenViewBuilder().build());

		int playServicesAvailability = GooglePlayServicesUtil.isGooglePlayServicesAvailable(app);
		switch(playServicesAvailability) {
		case ConnectionResult.SUCCESS:
			Log.i(TAG, "Phone has latest Google Play Services.");
			break;
		default:
			GooglePlayServicesUtil.getErrorDialog(playServicesAvailability, getActivity(), 0,
					new DialogInterface.OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							Utils.trackEvent(app, TAG, "Error", "Update Cancelled");
						}
					}).show();
			Utils.trackEvent(app, TAG, "Error",
					GooglePlayServicesUtil.getErrorString(playServicesAvailability));
		}
		super.onResume();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_about, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		try {
			PackageInfo info = app.getPackageManager().getPackageInfo(app.getPackageName(), 0);
			((TextView) view.findViewById(R.id.version)).setText(
					String.format("Version: %s.%d", info.versionName, info.versionCode));
		} catch (NameNotFoundException e) {
			Log.w(TAG, e);
		}
	}

	@Override
	public void onClick(View view) {
	}
}