package upb.airdocs;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import java.util.ArrayList;

public class BLEScan {
    Context mContext;
    private static final String LOG_TAG = "BLEScan";
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothLeScanner bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
    private boolean mScanning = false;
    private Handler handler = new Handler();

    public BLEScan(Context context){
        mContext = context;
    }

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 180000;

    public void startScan() {
        if (mBluetoothAdapter.isEnabled()) {
            if (mScanning == false) {
                // Stops scanning after a pre-defined scan period.
            /*handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    bluetoothLeScanner.stopScan(leScanCallback);
                }
            }, SCAN_PERIOD);*/

                mScanning = true;

                if (leScanCallback != null) {
                    bluetoothLeScanner.startScan(leScanCallback);
                }
            }
        }
        else{
            Toast.makeText(mContext,
                    "Bluetooth is not enabled. BLE will be excluded from the scan.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void stopScan(){
        if (mScanning == true){
            mScanning = false;
            if (leScanCallback != null) {
                bluetoothLeScanner.stopScan(leScanCallback);
            }
        }

    }

    // Device scan callback.
    private ScanCallback leScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    BluetoothDevice device = result.getDevice();
                    //Log.d(LOG_TAG, "Timestamp: "+result.getTimestampNanos()+" Name: "+device.getName()+
                    //        " MAC: "+device.getAddress()+ " RSSI: "+result.getRssi());

                    BLEFingerprint bleItem = new BLEFingerprint(
                            device.getName(), result.getRssi());

                    ScanService.currentFingerprint.addBLEFingerprint(device.getAddress(), bleItem);
                }
            };

    /*public void startScan(){
        if (mContext.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE)) {
            final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            Log.d(LOG_TAG, "Start BLE Scan");
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address

                    Log.d(LOG_TAG, "Name: " + deviceName + " MAC: "  + deviceHardwareAddress + " RSSI: " + rssi);
                }
            };

    public void stopScan(){

    }*/

    /*private void btScan(){
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        mContext.registerReceiver(mReceiver, filter);
        final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.startDiscovery();

    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                int  rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);

                Log.d(LOG_TAG, "device: " + deviceName + " hardwareAdd: "  + deviceHardwareAddress + " RSSI: " + rssi);

            }
        }
    };*/
}
