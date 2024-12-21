package com.example.android_iot;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private BluetoothLeScanner bluetoothLeScanner;
    private List<String> deviceList = new ArrayList<>();
    private BleDeviceAdapter adapter;
    private ListView listView;
    private Button scanButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the ListView and Scan Button in the layout
        this.listView = findViewById(R.id.list_view);
        this.scanButton = findViewById(R.id.scan_button);

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

        // Set up scan button
        scanButton.setOnClickListener(v -> startScanning());

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String selectedItem = (String) adapterView.getItemAtPosition(i);
                Toast.makeText(MainActivity.this,"You selected "+selectedItem, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void startScanning() {
        deviceList.clear();  // Clear the device list before starting the scan
        adapter.notifyDataSetChanged();  // Notify the adapter that the data has changed
        bluetoothLeScanner.startScan(scanCallback);  // Start scanning for Bluetooth devices
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, @NonNull ScanResult result) {
            super.onScanResult(callbackType, result);
            String deviceName = result.getDevice().getName();
            if (deviceName != null && !deviceList.contains(deviceName)) {
                deviceList.add(deviceName);  // Add the new device to the list
                adapter.notifyDataSetChanged();  // Notify the adapter that the data has changed
            }
        }

        @Override
        public void onBatchScanResults(@NonNull List<ScanResult> results) {
            super.onBatchScanResults(results);
            for (ScanResult result : results) {
                String deviceName = result.getDevice().getName();
                if (deviceName != null && !deviceList.contains(deviceName)) {
                    deviceList.add(deviceName);  // Add the new device to the list
                }
            }
            adapter.notifyDataSetChanged();  // Notify the adapter after all devices are added
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
        bluetoothLeScanner.stopScan(scanCallback);  // Stop scanning when the activity is destroyed
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
