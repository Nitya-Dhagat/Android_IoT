package com.example.android_iot;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private BluetoothLeScanner bluetoothLeScanner;
    private List<BluetoothDevice> deviceList = new ArrayList<>();
    private BleDeviceAdapter adapter;
    private ListView listView;
    private Button scanButton, disconnectButton;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic ledControlCharacteristic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the ListView and Scan/Disconnect Buttons in the layout
        listView = findViewById(R.id.list_view);
        scanButton = findViewById(R.id.scan_button);
        disconnectButton = findViewById(R.id.disconnect_button);

        // Set up BleDeviceAdapter for ListView
        adapter = new BleDeviceAdapter(this, deviceList);
        listView.setAdapter(adapter);

        // Initialize Bluetooth
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        // Request location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }

        // Set up scan button to start scanning
        scanButton.setOnClickListener(v -> startScanning());

        // Set up disconnect button to disconnect from the device
        disconnectButton.setOnClickListener(v -> disconnectDevice());

        // Handle item clicks in ListView
        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            BluetoothDevice selectedDevice = deviceList.get(i);
            connectToDevice(selectedDevice);
            Toast.makeText(MainActivity.this, "You selected " + selectedDevice.getName(), Toast.LENGTH_SHORT).show();
        });
    }

    // Start scanning for Bluetooth devices
    @SuppressLint("MissingPermission")
    private void startScanning() {
        deviceList.clear();  // Clear the device list before starting the scan
        adapter.notifyDataSetChanged();  // Notify the adapter that the data has changed
        bluetoothLeScanner.startScan(scanCallback);  // Start scanning for Bluetooth devices
    }

    // Connect to the selected Bluetooth device
    private void connectToDevice(BluetoothDevice device) {
        bluetoothGatt = device.connectGatt(this, false, gattCallback);
        disconnectButton.setVisibility(View.VISIBLE);  // Show the Disconnect button when a device is connected
    }

    // GATT callback for connection state changes
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                // Successfully connected to the device
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Connected to " + gatt.getDevice().getName(), Toast.LENGTH_SHORT).show();
                    bluetoothGatt.discoverServices();  // Discover services
                });
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                // Disconnected from the device
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Disconnected from " + gatt.getDevice().getName(), Toast.LENGTH_SHORT).show();
                    disconnectButton.setVisibility(View.GONE);  // Hide the Disconnect button after disconnect
                });
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                ledControlCharacteristic = gatt.getService(UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb"))
                        .getCharacteristic(UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb"));
            }
        }
    };

    // Send the command to turn the LED ON or OFF
    private void sendCommand(String command) {
        if (ledControlCharacteristic != null) {
            ledControlCharacteristic.setValue(command);
            bluetoothGatt.writeCharacteristic(ledControlCharacteristic);  // Write the command to the characteristic
        }
    }

    // Disconnect from the device
    private void disconnectDevice() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
        }
    }

    // Scan callback to handle detected devices
    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, @NonNull ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            String deviceName = device.getName();
            if (deviceName != null && !deviceList.contains(device)) {
                deviceList.add(device);  // Add the new device to the list
                adapter.notifyDataSetChanged();  // Notify the adapter that the data has changed
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            // Handle scan failure (you could show an error message here)
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothGatt != null) {
            bluetoothGatt.close();  // Close the connection when the activity is destroyed
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanning();  // Start scanning if permission is granted
            }
        }
    }
}
