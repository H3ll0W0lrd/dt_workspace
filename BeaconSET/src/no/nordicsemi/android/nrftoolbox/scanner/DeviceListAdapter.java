/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package no.nordicsemi.android.nrftoolbox.scanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import com.minnw.beacon.R;


/**
 * DeviceListAdapter class is list adapter for showing scanned Devices name, address and RSSI image based on RSSI values.
 */
public class DeviceListAdapter extends BaseAdapter {
	private static final int TYPE_ITEM = 1;
	private static final int TYPE_EMPTY = 2;

	private final ArrayList<ExtendedBluetoothDevice> mListValues = new ArrayList<>();
	private final Context mContext;
	private final ExtendedBluetoothDevice.AddressComparator comparator = new ExtendedBluetoothDevice.AddressComparator();

	public DeviceListAdapter(Context context) {
		mContext = context;
	}

	public void addBondedDevice(ExtendedBluetoothDevice device) {
		notifyDataSetChanged();
	}

	/**
	 * Looks for the device with the same address as given one in the list of bonded devices. If the device has been found it updates its RSSI value.
	 * 
	 * @param address
	 *            the device address
	 * @param rssi ExtendedBluetoothDevice
	 *            the RSSI of the scanned device
	 */
	public void updateRssiOfBondedDevice(String address, int rssi) {
		comparator.address = address;
	 
	}

	/**
	 * If such device exists on the bonded device list, this method does nothing. If not then the device is updated (rssi value) or added.
	 * 
	 * @param device
	 *            the device to be added or updated
	 */
	public void addOrUpdateDevice(ExtendedBluetoothDevice device) {
		boolean   iscon=false;
		for(int i=0;i<mListValues.size();i++){
			if(mListValues.get(i).device.getAddress().equals(device.device.getAddress())){
				iscon=true;
			}
		}
		if(!iscon)
		    mListValues.add(device);
		notifyDataSetChanged();
	}

	public void clearDevices() {
		mListValues.clear();
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mListValues.size();
	}

	@Override
	public Object getItem(int position) {
		 return mListValues.get(position  );
	}

	@Override
	public int getViewTypeCount() {
		return 3;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		return getItemViewType(position) == TYPE_ITEM;
	}

	@Override
	public int getItemViewType(int position) {
		return TYPE_ITEM;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View oldView, ViewGroup parent) {
		final LayoutInflater inflater = LayoutInflater.from(mContext);
		final int type = getItemViewType(position);
		View view = oldView;
		switch (type) {
		case TYPE_EMPTY:
			if (view == null) {
				view = inflater.inflate(R.layout.device_list_empty, parent, false);
			}
			break;
	 
		default:
			if (view == null) {
				view = inflater.inflate(R.layout.device_list_row, parent, false);
				final ViewHolder holder = new ViewHolder();
				holder.name = (TextView) view.findViewById(R.id.name);
				holder.address = (TextView) view.findViewById(R.id.address);
				holder.rssi = (ImageView) view.findViewById(R.id.rssi);
				view.setTag(holder);
			}

			final ExtendedBluetoothDevice device = (ExtendedBluetoothDevice) getItem(position);
			final ViewHolder holder = (ViewHolder) view.getTag();
			final String name = device.name;
			holder.name.setText(name != null ? name : mContext.getString(R.string.not_available));
			holder.address.setText(device.device.getAddress());
			if (!device.isBonded || device.rssi != ScannerFragment.NO_RSSI) {
				final int rssiPercent = (int) (100.0f * (127.0f + device.rssi) / (127.0f + 20.0f));
				holder.rssi.setImageLevel(rssiPercent);
				holder.rssi.setVisibility(View.VISIBLE);
			} else {
				holder.rssi.setVisibility(View.GONE);
			}
			break;
		}

		return view;
	}

	private class ViewHolder {
		private TextView name;
		private TextView address;
		private ImageView rssi;
	}
}
