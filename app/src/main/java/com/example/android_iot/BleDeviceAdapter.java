package com.example.android_iot;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import android.bluetooth.BluetoothDevice;

import java.util.List;

public class BleDeviceAdapter extends BaseAdapter {
    private Context context;
    private List<BluetoothDevice> deviceList;

    public BleDeviceAdapter(Context context, List<BluetoothDevice> deviceList) {
        this.context = context;
        this.deviceList = deviceList;
    }

    @Override
    public int getCount() {
        return deviceList.size();
    }

    @Override
    public Object getItem(int position) {
        return deviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        BluetoothDevice device = deviceList.get(position);
        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(device.getName() != null ? device.getName() : "Unknown Device");

        return convertView;
    }
}
