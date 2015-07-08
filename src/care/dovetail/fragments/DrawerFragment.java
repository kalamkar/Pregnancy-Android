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
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import care.dovetail.App;
import care.dovetail.Config;
import care.dovetail.R;
import care.dovetail.api.PhotoUpdate;
import care.dovetail.common.model.User;

import com.android.volley.toolbox.NetworkImageView;


public class DrawerFragment extends Fragment implements OnClickListener {
	private static final String TAG = "DrawerFragment";

    private static final int CAMERA_ACTIVITY = 0;
    private static final int GALLERY_ACTIVITY = 1;

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

		view.findViewById(R.id.photo).setOnClickListener(this);
		view.findViewById(R.id.name).setOnClickListener(this);
		view.findViewById(R.id.email).setOnClickListener(this);
		view.findViewById(R.id.dueDate).setOnClickListener(this);
		view.findViewById(R.id.history).setOnClickListener(this);
		view.findViewById(R.id.about).setOnClickListener(this);

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

		String photoUrl = String.format("%s%s", Config.PHOTO_URL, user.uuid);
		((NetworkImageView) view.findViewById(R.id.photo)).setImageUrl(photoUrl, app.imageLoader);
	}

	@Override
	public void onClick(View view) {
		switch(view.getId()) {
		case R.id.photo:
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	        builder.setTitle(getResources().getString(R.string.update_photo));
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
	    						getResources().getString(R.string.update_photo)), GALLERY_ACTIVITY);
	                }
	            }
	        });
	        builder.create().show();
			break;
		case R.id.name:
		case R.id.email:
			new NameEmailFragment().show(getActivity().getSupportFragmentManager(), null);
			break;
		case R.id.dueDate:
			new DueDateFragment().show(getChildFragmentManager(), null);
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
		startActivityForResult(takePictureIntent, CAMERA_ACTIVITY);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != Activity.RESULT_OK) {
			Log.w(TAG, String.format("Failed to get photo activity result."));
			return;
		}
		if (requestCode == CAMERA_ACTIVITY) {
			new PhotoUpdate(app, photoUrl, "image/jpeg") {
				@Override
				protected void onPostExecute(String result) {
					Log.v(TAG, result);
					updateUi(getView());
				}
			}.execute();
		} else if (requestCode == GALLERY_ACTIVITY) {
			new PhotoUpdate(app, data.getData(), "image/jpeg") {
				@Override
				protected void onPostExecute(String result) {
					Log.v(TAG, result);
					updateUi(getView());
				}
			}.execute();
		}
	}
}