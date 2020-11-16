package upb.airdocs;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;

public class BLEScan {
    Context mContext;
    private static final String LOG_TAG = "BLEScan";
    private BluetoothLeScanner bluetoothLeScanner =
            BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
    private boolean mScanning = false;
    private Handler handler = new Handler();

    public BLEScan(Context context){
        mContext = context;
    }

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 180000;

    public void startScan() {
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
            bluetoothLeScanner.startScan(leScanCallback);
        }
    }

    public void stopScan(){
        if (mScanning == true){
            mScanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
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

                    ScanItem bleItem = new ScanItem(2, result.getTimestampNanos(),
                            device.getName(), device.getAddress(), 0, result.getRssi());

                    ArrayList<ScanItem> scanItemArrayList = ScanService.currentFingerprint.get(device.getAddress());

                    if (scanItemArrayList == null){
                        scanItemArrayList = new ArrayList<ScanItem>();
                    }

                    scanItemArrayList.add(bleItem);

                    ScanService.currentFingerprint.put(device.getAddress(), scanItemArrayList);
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
