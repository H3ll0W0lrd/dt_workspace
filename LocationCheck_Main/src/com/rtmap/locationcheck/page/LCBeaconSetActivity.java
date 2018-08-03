package com.rtmap.locationcheck.page;

import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;

import com.google.gson.internal.bind.MapTypeAdapterFactory;
import com.rtmap.locationcheck.R;
import com.rtmap.locationcheck.core.LCActivity;
import com.rtmap.locationcheck.core.LCApplication;

public class LCBeaconSetActivity extends LCActivity implements
		OnSeekBarChangeListener {
	private TextView mThreText;
	private Switch mThreSwitch, mMajorSwitch, mMinorSwitch, mInt16Swith;
	private SeekBar mThreSeek;
	private int mValue;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lc_beacon_set);
		mThreText = (TextView) findViewById(R.id.threshold_text);
		mThreSeek = (SeekBar) findViewById(R.id.threshold_seek);
		mThreSwitch = (Switch) findViewById(R.id.threshold_switch);
		mMajorSwitch = (Switch) findViewById(R.id.major_switch);
		mMinorSwitch = (Switch) findViewById(R.id.minor_switch);
		mInt16Swith = (Switch) findViewById(R.id.int16_switch);
		mValue = LCApplication.getInstance().getShare()
				.getInt("threshold", -99);
		mThreSeek.setProgress((int) ((mValue + 99) / 74.0f * 100));
		mThreText.setText(getString(R.string.threshold_value, mValue));
		mThreSeek.setOnSeekBarChangeListener(this);
		mThreSwitch.setChecked(LCApplication.getInstance().getShare()
				.getBoolean("threshold_switch", false));
		mMajorSwitch.setChecked(LCApplication.getInstance().getShare()
				.getBoolean("major_switch", false));
		mMinorSwitch.setChecked(LCApplication.getInstance().getShare()
				.getBoolean("minor_switch", false));
		mInt16Swith.setChecked(LCApplication.getInstance().getShare()
				.getBoolean("int16_switch", false));
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		mValue = progress * 74 / 100 - 99;
		mThreText.setText(getString(R.string.threshold_value, mValue));
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {

	}

	@Override
	protected void onPause() {
		super.onPause();
		LCApplication.getInstance().getShare().edit()
				.putInt("threshold", mValue).commit();
		LCApplication.getInstance().getShare().edit()
				.putBoolean("threshold_switch", mThreSwitch.isChecked())
				.commit();
		LCApplication.getInstance().getShare().edit()
				.putBoolean("major_switch", mMajorSwitch.isChecked()).commit();
		LCApplication.getInstance().getShare().edit()
				.putBoolean("minor_switch", mMinorSwitch.isChecked()).commit();
		LCApplication.getInstance().getShare().edit()
				.putBoolean("int16_switch", mInt16Swith.isChecked()).commit();
	}

}
