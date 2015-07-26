package care.dovetail;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class PairingActivity extends Activity {
	private static final String TAG = "PairingActivity";

	private App app;

	private BluetoothAdapter bluetooth;
	private BluetoothLeScanner scanner;

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
		scanner = bluetooth.getBluetoothLeScanner();

		if (bluetooth == null || !bluetooth.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		ScanSettings settings = new ScanSettings.Builder()
			.setScanMode(ScanSettings.SCAN_MODE_BALANCED).setReportDelay(0).build();
		scanner.startScan(null, settings, callback);
	}

	@Override
	protected void onDestroy() {
		scanner.stopScan(callback);
		super.onDestroy();
	}

	private ScanCallback callback = new ScanCallback() {
		@Override
		public void onScanFailed(int errorCode) {
			Toast.makeText(PairingActivity.this,
					String.format("Bluetooth LE scan failed with error %d", errorCode),
					Toast.LENGTH_LONG).show();
			super.onScanFailed(errorCode);
		}

		@Override
		public void onScanResult(int callbackType, ScanResult result) {
			String name = result.getDevice().getName();
			name = name == null ? result.getScanRecord().getDeviceName() : name;
			if (Config.WEIGHT_SCALE_NAME.equalsIgnoreCase(name)) {
				Log.i(TAG, String.format("Found device %s", name));
				app.setWeightScaleAddress(result.getDevice().getAddress());
				findViewById(R.id.progress).setVisibility(View.INVISIBLE);
				findViewById(R.id.success).setVisibility(View.VISIBLE);
				(RingtoneManager.getRingtone(app,
						RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))).play();
			}
			super.onScanResult(callbackType, result);
		}
	};
}
