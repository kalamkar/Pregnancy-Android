package care.dovetail;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import care.dovetail.bluetooth.SamicoScalesClient;

public class BackgroundService extends Service implements OnSharedPreferenceChangeListener {
	private static final String TAG = "BackgroundService";

	private App app;
	private SharedPreferences prefs;

	private SamicoScalesClient weighingScale;

    private final IBinder binder = new LocalBinder();
    private NotificationManager notifications;

	@Override
	public void onCreate() {
		super.onCreate();
		app = (App) getApplication();
		prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);
		prefs.registerOnSharedPreferenceChangeListener(this);
		initScale();
	}

	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Received start id " + startId + ": " + intent);
        return START_STICKY;
    }

	@Override
	public void onDestroy() {
		if (notifications != null) {
			notifications.cancelAll();
		}
		weighingScale.disconnect();
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	public class LocalBinder extends Binder {
		BackgroundService getService() {
            return BackgroundService.this;
        }
    }

	private void initScale() {
		if (weighingScale == null) {
			weighingScale = new SamicoScalesClient(app);
		}
		weighingScale.connectToDevice(app.getWeightScaleAddress());
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		if (App.WEIGHT_SCALE_MAC.equals(key)) {
			initScale();
		}
	}
}
