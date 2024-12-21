package com.example.android_iot;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;

public class BleDeviceAdapter extends ArrayAdapter<String> {

    public BleDeviceAdapter(Context context, List<String> deviceList) {
        super(context, android.R.layout.simple_list_item_1, deviceList);
    }
}
