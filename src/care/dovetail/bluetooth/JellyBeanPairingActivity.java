package care.dovetail.bluetooth;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import care.dovetail.App;
import care.dovetail.Config;
import care.dovetail.R;

@SuppressWarnings("deprecation")
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class JellyBeanPairingActivity extends Activity {
	private static final String TAG = "JellyBeanPairingActivity";

	private App app;

	private BluetoothAdapter bluetooth;

	// Stops scanning after 10 seconds.
	private static final int REQUEST_ENABLE_BT = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pairing);
		app = (App) getApplication();

		getActionBar().hide();

		final BluetoothManager bluetoothManager =
				(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		bluetooth = bluetoothManager.getAdapter();

		if (bluetooth == null || !bluetooth.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		bluetooth.startLeScan(callback);
	}

	@Override
	protected void onDestroy() {
		bluetooth.stopLeScan(callback);
		super.onDestroy();
	}

	private BluetoothAdapter.LeScanCallback callback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
			String name = device.getName();
			if (Config.WEIGHT_SCALE_NAME.equalsIgnoreCase(name)) {
				Log.i(TAG, String.format("Found device %s", name));
				app.setWeightScaleAddress(device.getAddress());
				findViewById(R.id.progress).setVisibility(View.INVISIBLE);
				findViewById(R.id.success).setVisibility(View.VISIBLE);
				(RingtoneManager.getRingtone(app,
						RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))).play();
			}
		}
	};
}
