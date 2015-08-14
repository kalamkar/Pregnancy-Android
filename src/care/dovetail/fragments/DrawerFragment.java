package care.dovetail.fragments;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import care.dovetail.App;
import care.dovetail.Config;
import care.dovetail.FitnessPollTask;
import care.dovetail.MainActivity;
import care.dovetail.R;
import care.dovetail.Utils;
import care.dovetail.api.PhotoUpdate;
import care.dovetail.bluetooth.JellyBeanPairingActivity;
import care.dovetail.bluetooth.PairingActivity;
import care.dovetail.common.model.User;

import com.android.volley.toolbox.NetworkImageView;


public class DrawerFragment extends Fragment implements OnClickListener {
	private static final String TAG = "DrawerFragment";

	private static final int TITLES[] = new int[] {R.string.home, R.string.sharing,
		R.string.history, R.string.due_date, R.string.pair_google_fit, R.string.pair_scale,
		R.string.about};
	private static final int ICONS[] = new int[] {R.drawable.ic_home, R.drawable.ic_group,
		R.drawable.ic_history, R.drawable.ic_date, R.drawable.ic_heart, R.drawable.ic_action_pair,
		R.drawable.ic_info};

	private App app;

    private String photoUrl;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		app = (App) activity.getApplication();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_drawer, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		((ListView) view.findViewById(R.id.menu)).setAdapter(new MenuAdapter());

		view.findViewById(R.id.photo).setOnClickListener(this);
		view.findViewById(R.id.name).setOnClickListener(this);
		view.findViewById(R.id.email).setOnClickListener(this);

		updateUi(view);
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
			updateUi(getView());
		}
	};

	private void updateUi(View view) {
		User user = app.getMother();
		((TextView) view.findViewById(R.id.name)).setText(user.name);
		((TextView) view.findViewById(R.id.email)).setText(user.email);

		String photoUrl = String.format("%s%s&size=%d&rnd=%d", Config.USER_PHOTO_URL, user.uuid,
				(int) app.getResources().getDimension(R.dimen.photo_width),
				(int) (Math.random() * 10000));
		((NetworkImageView) view.findViewById(R.id.photo)).setImageUrl(photoUrl, app.imageLoader);
	}

	@Override
	public void onClick(View view) {
		switch(view.getId()) {
		case R.id.photo:
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	        builder.setItems(getResources().getStringArray(R.array.photo_options),
	        		new DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int item) {
	            	switch (item) {
	            	case 0:
	            		startCameraActivity();
	            		break;
	            	case 1:
	            		startActivityForResult(Intent.createChooser(
	    						new Intent().setType("image/jpeg").setAction(Intent.ACTION_GET_CONTENT),
	    						getResources().getString(R.string.update_photo)), Config.ACTIVITY_GALLERY);
	                }
	            }
	        });
	        AlertDialog dialog = builder.create();
	        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
	        dialog.show();
	        Utils.trackEvent(app, "Drawer", "Click",
	        		getResources().getString(R.string.update_photo));
			break;
		case R.id.name:
		case R.id.email:
			new NameEmailFragment().show(getActivity().getSupportFragmentManager(), null);
			Utils.trackEvent(app, "Drawer", "Click", "NameEmail");
			break;
		}
	}

	private void startCameraActivity() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss", Locale.ENGLISH);
	    Date date = Calendar.getInstance().getTime();
	    File photoFile;
	    try {
	    	photoFile = File.createTempFile(sdf.format(date), ".jpg", Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
	    	photoUrl = Uri.fromFile(photoFile).toString();
		} catch (IOException ex) {
			Log.w(TAG, ex);
			return;
		}
	    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) == null
	    		|| photoFile == null) {
	    	return;
	    }
		takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
		startActivityForResult(takePictureIntent, Config.ACTIVITY_CAMERA);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != Activity.RESULT_OK) {
			Log.w(TAG, String.format("Failed to get photo activity result."));
			return;
		}
		if (requestCode == Config.ACTIVITY_CAMERA) {
			new PhotoUpdate(app, photoUrl, "image/jpeg") {
				@Override
				protected void onPostExecute(String result) {
					Log.v(TAG, result);
					updateUi(getView());
				}
			}.execute();
		} else if (requestCode == Config.ACTIVITY_GALLERY) {
			new PhotoUpdate(app, data.getData(), "image/jpeg") {
				@Override
				protected void onPostExecute(String result) {
					Log.v(TAG, result);
					updateUi(getView());
				}
			}.execute();
		}
	}

	private class MenuAdapter extends BaseAdapter implements OnClickListener {

		@Override
		public int getCount() {
			return Math.min(ICONS.length, TITLES.length);
		}

		@Override
		public Pair<String, Integer> getItem(int position) {
			int titleResId = TITLES[position];
			if (titleResId == R.string.pair_google_fit && app.getGoogleFitAccount() != null) {
				titleResId = R.string.google_fit_paired;
			} else if (titleResId == R.string.pair_scale && app.getWeightScaleAddress() != null) {
				titleResId = R.string.scale_paired;
			}
			return Pair.create(getResources().getString(titleResId), ICONS[position]);
		}

		@Override
		public long getItemId(int position) {
			return getItem(position).hashCode();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			if (convertView == null) {
				view = getActivity().getLayoutInflater().inflate(R.layout.list_item_drawer, null);
			} else {
				view = convertView;
			}

			Pair<String, Integer> menu = getItem(position);
			((ImageView) view.findViewById(R.id.icon)).setImageResource(menu.second);;
			((TextView) view.findViewById(R.id.text)).setText(menu.first);
			view.setTag(TITLES[position]);
			view.setOnClickListener(this);
			return view;
		}

		@Override
		public void onClick(View view) {
			int resId = (Integer) view.getTag();
			switch(resId) {
			case R.string.due_date:
				new DueDateFragment().show(getChildFragmentManager(), null);
				break;
			case R.string.sharing:
				((MainActivity) getActivity()).setContentFragment(new GroupsFragment());
				break;
			case R.string.home:
				((MainActivity) getActivity()).setContentFragment(new HomeFragment());
				break;
			case R.string.history:
				((MainActivity) getActivity()).setContentFragment(new HistoryFragment());
				break;
			case R.string.scale_paired:
			case R.string.pair_scale:
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					startActivity(new Intent(getActivity(), PairingActivity.class));
				} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
					startActivity(new Intent(getActivity(), JellyBeanPairingActivity.class));
				} else {
					Toast.makeText(app,
							getResources().getString(R.string.weight_scale_not_supported),
							Toast.LENGTH_SHORT).show();
				}
				break;
			case R.string.google_fit_paired:
			case R.string.pair_google_fit:
				FitnessPollTask.buildFitnessClient((MainActivity) getActivity(), app);
				break;
			case R.string.about:
				break;
			}
			try {
				Utils.trackEvent(app, "Drawer", "Click", getResources().getString(resId));
			} catch(Exception ex) {
				Log.w(TAG, ex);
			}
		}
	}
}