package care.dovetail.messaging;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class GCMUtils {
	private static final String TAG = "GCMUtils";

	public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    public static boolean checkPlayServices(Activity activity) {
	    int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
	    if (resultCode != ConnectionResult.SUCCESS) {
	        if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
	            GooglePlayServicesUtil.getErrorDialog(resultCode, activity,
	            		PLAY_SERVICES_RESOLUTION_REQUEST).show();
	        } else {
	            Log.i(TAG, "This device is not supported.");
	        }
	        return false;
	    }
	    return true;
	}

	public static String getRegistrationId(Application app) {
	    final SharedPreferences prefs = getGCMPreferences(app);
	    String registrationId = prefs.getString(PROPERTY_REG_ID, null);
	    if (registrationId == null) {
	        Log.i(TAG, "Registration not found.");
	        return null;
	    }
	    // Check if app was updated; if so, it must clear the registration ID
	    // since the existing regID is not guaranteed to work with the new app version.
	    int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
	    int currentVersion = getAppVersion(app);
	    if (registeredVersion != currentVersion) {
	        Log.i(TAG, "App version changed.");
	        return null;
	    }
	    return registrationId;
	}

	private static SharedPreferences getGCMPreferences(Application app) {
	    return app.getSharedPreferences(app.getClass().getPackage().getName(),
	            Context.MODE_PRIVATE);
	}

	private static int getAppVersion(Context context) {
	    try {
	        PackageInfo packageInfo = context.getPackageManager()
	                .getPackageInfo(context.getPackageName(), 0);
	        return packageInfo.versionCode;
	    } catch (NameNotFoundException e) {
	        // should never happen
	        throw new RuntimeException("Could not get package name: " + e);
	    }
	}

	public static void storeRegistrationId(Application app, String regId) {
	    final SharedPreferences prefs = getGCMPreferences(app);
	    int appVersion = getAppVersion(app);
	    Log.i(TAG, "Saving regId on app version " + appVersion);
	    SharedPreferences.Editor editor = prefs.edit();
	    editor.putString(PROPERTY_REG_ID, regId);
	    editor.putInt(PROPERTY_APP_VERSION, appVersion);
	    editor.commit();
	}
}
