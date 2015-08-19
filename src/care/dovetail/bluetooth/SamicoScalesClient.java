package care.dovetail.bluetooth;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import care.dovetail.App;
import care.dovetail.Config;
import care.dovetail.R;
import care.dovetail.Utils;
import care.dovetail.common.model.Event;
import care.dovetail.common.model.Measurement;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class SamicoScalesClient extends BluetoothGattCallback {
	private static final String TAG = "SamicoScalesClient";

	private final App app;
	private final BluetoothAdapter adapter;

	private BluetoothGatt gatt;
	private int state = BluetoothProfile.STATE_DISCONNECTED;

	private int lastStableWeightInGrams = 0;

	public SamicoScalesClient(App app) {
		this.app = app;
		BluetoothManager bluetoothManager =
				(BluetoothManager) app.getSystemService(Context.BLUETOOTH_SERVICE);
		adapter = bluetoothManager.getAdapter();
	}

	public void connectToDevice(String address) {
		if (address == null || address.isEmpty()) {
			Log.e(TAG, "No BluetoothLE device selected.");
			return;
		}
		if (adapter != null && adapter.isEnabled()) {
			Log.i(TAG, String.format("Connecting to BluetoothLE device %s.", address));
			BluetoothDevice device = adapter.getRemoteDevice(address);
			device.connectGatt(app, true, this);
		}
	}

	@Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        this.gatt = gatt;
        this.state = newState;
        if (newState == BluetoothProfile.STATE_CONNECTED) {
        	Log.i(TAG, String.format("Connected to %s", gatt.getDevice().getName()));
        	gatt.discoverServices();
        	lastStableWeightInGrams = 0;
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
        	Log.i(TAG, String.format("Disconnected from %s.", gatt.getDevice().getName()));
        	if (lastStableWeightInGrams > 0) {
        		Measurement weight = new Measurement();
        		weight.value = lastStableWeightInGrams;
        		weight.unit = Measurement.Unit.GRAMS.name();
        		weight.endMillis = System.currentTimeMillis();
        		Log.v(TAG, String.format("Weight on %s is %d",
        				Config.MESSAGE_DATE_FORMAT.format(weight.endMillis), weight.value));
        		app.events.add(new Event(new String[] {Event.Type.WEIGHT.name()}, weight.endMillis,
        				Config.GSON.toJson(weight)));
        		int pounds = Math.round(lastStableWeightInGrams / 453.592f);
        		Utils.sendNotification(app, String.format(
        				app.getResources().getString(R.string.weight_message), pounds),
        				Config.WEIGHT_NOTIFICATION_ID);
        	}
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (status != BluetoothGatt.GATT_SUCCESS) {
        	Log.e(TAG, "onServicesDiscovered received: " + status);
        	return;
        }
    	for (BluetoothGattService service : gatt.getServices()) {
    		for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
    			if ((characteristic.getProperties()
    					& BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
    				gatt.setCharacteristicNotification(characteristic, true);
    				for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
    					descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
    					gatt.writeDescriptor(descriptor);
    				}
    			}
    		}
    	}
    }

	@Override
	public void onCharacteristicChanged(BluetoothGatt gatt,
			BluetoothGattCharacteristic characteristic) {
		try {
			byte value[] = characteristic.getValue();
			// If weighing scale is stable and value is grams
 			if ((value[0] & 0xFF) == 203 && (value[1] & 0xFF) == 0) {
 				int weight = ((value[2] & 0xFF) << 8 | (value[3] & 0xFF)) * 100;
				lastStableWeightInGrams = weight;
 				Log.v(TAG, String.format("New weight data: %d, %d, %d", weight,
 						value[0] & 0xFF, value[1] & 0xFF));
			}
		} catch(Exception ex) {
			Log.w(TAG, ex);
		}
		super.onCharacteristicChanged(gatt, characteristic);
	}

	public boolean isConnected() {
		return state == BluetoothProfile.STATE_CONNECTED;
	}

	public void disconnect() {
		if (gatt != null && isConnected()) {
			gatt.disconnect();
		}
	}
}
