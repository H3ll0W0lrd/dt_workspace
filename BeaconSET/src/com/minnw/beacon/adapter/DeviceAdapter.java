package com.minnw.beacon.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.minnw.beacon.R;
import com.minnw.beacon.data.BluetoothDeviceAndRssi;

public class DeviceAdapter   extends BaseAdapter {
		Context context;
		List<BluetoothDeviceAndRssi> devices;
		LayoutInflater inflater;

		public DeviceAdapter(Context context,
				List<BluetoothDeviceAndRssi> devices) {
			this.context = context;
			inflater = LayoutInflater.from(context);
			this.devices = devices;
		}

		@Override
		public int getCount() {
			return devices.size();
		}

		@Override
		public Object getItem(int position) {
			return devices.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewGroup vg = null;
			if (convertView != null) {
				vg = (ViewGroup) convertView;
			} else {
				vg = (ViewGroup) inflater
						.inflate(R.layout.device_item, null);
			}

			BluetoothDeviceAndRssi bluetoothdeviceandrssi = devices
					.get(position);
			TextView tvadd = ((TextView) vg.findViewById(R.id.address));
			TextView tvname = ((TextView) vg.findViewById(R.id.name));
			TextView tvmajor = (TextView) vg.findViewById(R.id.major);
			TextView tvminor = (TextView) vg.findViewById(R.id.minor);
			TextView tvdistance = (TextView) vg.findViewById(R.id.distance);
			TextView tvconn = (TextView) vg.findViewById(R.id.conn);
			ImageView image_rssi = (ImageView) vg.findViewById(R.id.image_rssi);
			ImageView image_battery = (ImageView) vg.findViewById(R.id.battery);

			byte rssival = (byte) bluetoothdeviceandrssi.getRssi();
			if (rssival > (-60)) {
				image_rssi.setImageResource(R.drawable.icon_rssi6);
			} else if (rssival > (-75)) {
				image_rssi.setImageResource(R.drawable.icon_rssi5);
			} else if (rssival > (-90)) {
				image_rssi.setImageResource(R.drawable.icon_rssi4);
			} else if (rssival > (-100)) {
				image_rssi.setImageResource(R.drawable.icon_rssi3);
			} else if (rssival > (-110)) {
				image_rssi.setImageResource(R.drawable.icon_rssi2);
			} else {
				image_rssi.setImageResource(R.drawable.icon_rssi1);
			}

			if (bluetoothdeviceandrssi.getBattery() > 90) {
				image_battery.setImageResource(R.drawable.ic_battery4);
			} else if (bluetoothdeviceandrssi.getBattery() > 70) {
				image_battery.setImageResource(R.drawable.ic_battery3);
			} else if (bluetoothdeviceandrssi.getBattery() > 50) {
				image_battery.setImageResource(R.drawable.ic_battery2);
			} else if (bluetoothdeviceandrssi.getBattery() > 10) {
				image_battery.setImageResource(R.drawable.ic_battery1);
			} else {
				image_battery.setImageResource(R.drawable.ic_battery0);
			}

			if (bluetoothdeviceandrssi.getBluetoothdevice().getName() == null) {
				tvname.setText("Unknow Name");
			} else {
				tvname.setText(bluetoothdeviceandrssi.getBluetoothdevice()
						.getName());
			}
			tvadd.setText(context.getString(R.string.mac)
					+ bluetoothdeviceandrssi.getBluetoothdevice().getAddress());
			tvdistance.setText(bluetoothdeviceandrssi.getDistance() + "m");
			tvmajor.setText(Integer.parseInt(bluetoothdeviceandrssi.getMajor(),16)+"");
			tvminor.setText(Integer.parseInt(bluetoothdeviceandrssi.getMinor(),16)+"");
			tvconn.setText((bluetoothdeviceandrssi.isCONN() == true) ? "YES": "NO");
			return vg;
		}
	
}
