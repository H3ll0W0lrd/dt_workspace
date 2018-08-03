package com.rtmap.locationcheck.page;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Hashtable;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.mining.app.zxing.decoding.RGBLuminanceSource;
import com.rtmap.checkpicker.R;
import com.rtmap.locationcheck.core.LCActivity;
import com.rtmap.locationcheck.core.LCApplication;
import com.rtmap.locationcheck.core.model.BeaconInfo;
import com.rtmap.locationcheck.util.DTFileUtils;
import com.rtmap.locationcheck.util.DTLog;
import com.rtmap.locationcheck.util.DTStringUtils;
import com.rtmap.locationcheck.util.DTUIUtils;

/**
 * 添加beacon页面
 * 
 * @author dingtao
 *
 */
public class LCAddBeaconActivity extends LCActivity implements OnClickListener,
		TextWatcher {

	private TextView mMac;// mac地址
	private EditText mMin, mMax, mMajor, mMinor;
	private TextView mCoord, mBeaconStatus;// 坐标文本
	private int mX, mY;// xy坐标值
	private EditText mUUID;// uuid默认是C91A
	private Button mAdd;// 添加按钮
	public static HashMap<String, BeaconInfo> mMacSet;
	private String deviceid;
	private String uuid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lc_add_beacon);
		mMac = (TextView) findViewById(R.id.mac);
		mMin = (EditText) findViewById(R.id.min);
		mMax = (EditText) findViewById(R.id.max);
		mMajor = (EditText) findViewById(R.id.major);
		mMinor = (EditText) findViewById(R.id.minor);
		mCoord = (TextView) findViewById(R.id.coord);
		mAdd = (Button) findViewById(R.id.add);
		mUUID = (EditText) findViewById(R.id.uuid);
		mBeaconStatus = (TextView) findViewById(R.id.work_status);
		findViewById(R.id.open_beacon_scanner).setOnClickListener(this);
		findViewById(R.id.scanner).setOnClickListener(this);
		findViewById(R.id.photo).setOnClickListener(this);

		mMajor.addTextChangedListener(this);

		mMin.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				String str = s.toString();
				DTLog.i(str);
				if (DTStringUtils.isEmpty(str) || "-".equals(str)
						|| "+".equals(str))
					return;
				int value = Integer.parseInt(str);
				if (value < -100 || value > 0) {
					mMin.setText(str.substring(0, str.length() - 1));
				}
			}
		});
		mMax.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				String str = s.toString();
				DTLog.i(str);
				if (DTStringUtils.isEmpty(str) || "-".equals(str)
						|| "+".equals(str))
					return;
				int value = Integer.parseInt(str);
				if (value < -100 || value > 0) {
					mMax.setText(str.substring(0, str.length() - 1));
				}
			}
		});

		mUUID.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				String value = "0000";
				String mac = mMac.getText().toString();
				String uuid = s.toString().toUpperCase();
				if (!DTStringUtils.isEmpty(s.toString())) {
					if (uuid.length() >= 4) {
						value = uuid.substring(0, 4);
					} else {
						value = value.substring(0, 4 - uuid.length()) + uuid;
					}
				}
				mMac.setText(value + (mac.substring(4)).toUpperCase());
			}
		});

		mMinor.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				String value = "0000";
				String mac = mMac.getText().toString();
				if (!DTStringUtils.isEmpty(s.toString())) {
					String minor16 = Integer.toHexString(Integer.parseInt(s
							.toString()));
					if (minor16.length() >= 4) {
						value = minor16.substring(0, 4);
					} else {
						value = value.substring(0, 4 - minor16.length())
								+ minor16;
					}
				}
				mMac.setText((mac.subSequence(0, 8) + value).toUpperCase());
			}
		});
		mUUID.setText(LCApplication.getInstance().getShare()
				.getString("uuid", "C91A"));
		// mMajor.setText(LCApplication.getInstance().getShare()
		// .getString("major", "111"));

		mY = (int) (getIntent().getFloatExtra("y", 0) * 1000);
		mX = (int) (getIntent().getFloatExtra("x", 0) * 1000);
		mCoord.setText(mX + "/" + mY);
		mAdd.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.scanner:
			Intent intent1 = new Intent();
			intent1.setClass(this, LCScannerActivity.class);
			startActivityForResult(intent1, 100);
			break;
		case R.id.open_beacon_scanner:
			Intent intent5 = new Intent();
			intent5.setClass(this, LCBeaconLIstActivity.class);
			intent5.putExtra("sign", 2);
			startActivityForResult(intent5, 500);
			break;
		case R.id.photo:
			Intent i = new Intent(
					Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(i, PHOTO);
			break;
		case R.id.add:
			String mac = mMac.getText().toString();
			String min = mMin.getText().toString();
			String max = mMax.getText().toString();
			String major = mMajor.getText().toString();
			String minor = mMinor.getText().toString();
			if (DTStringUtils.isEmpty(mac) || DTStringUtils.isEmpty(min)
					|| DTStringUtils.isEmpty(max)
					|| DTStringUtils.isEmpty(major)
					|| DTStringUtils.isEmpty(minor)) {
				DTUIUtils.showToastSafe(R.string.add_beacon_please);
				return;
			}
			if (mMacSet != null && mMacSet.containsKey(mac.toUpperCase())) {
				DTUIUtils.showToastSafe(R.string.add_beacon_mac_error);
				return;
			}
			BeaconInfo info = new BeaconInfo();
			info.setMac(mac.toUpperCase());// MAC一定要大写
			info.setThreshold_switch_min(Integer.parseInt(min));
			info.setThreshold_switch_max(Integer.parseInt(max));
			info.setMajor(major);
			info.setMinor(minor);
			String major16 = mac.substring(4, 8);
			String minor16 = mac.substring(8, 12);
			info.setMajor16(major16);
			info.setMinor16(minor16);
			info.setX(mX);
			info.setY(mY);
			info.setUuid(uuid);
			info.setBroadcast_id(deviceid);
			info.setBuildId(getIntent().getStringExtra("build"));
			info.setFloor(DTStringUtils.floorTransform(getIntent()
					.getStringExtra("floor")) + "");
			if (mBeaconStatus.getText().toString().equals("正常")) {
				info.setWork_status(0);
			} else {
				info.setWork_status(-4);
			}
			// 编辑状态：0正常，1删除，2新建，3修改
			info.setEdit_status(2);
			Bundle bundle = new Bundle();
			bundle.putSerializable("beacon", info);
			Intent intent = new Intent();
			getIntent().putExtras(bundle);
			intent.putExtras(bundle);
			setResult(Activity.RESULT_OK, intent);
			DTUIUtils.showToastSafe(R.string.add_beacon_success);
			finish();
			break;
		}
	}

	@Override
	public void afterTextChanged(Editable s) {
		String value = "0000";
		String mac = mMac.getText().toString();
		if (!DTStringUtils.isEmpty(s.toString())) {
			String major16 = Integer
					.toHexString(Integer.parseInt(s.toString()));
			if (major16.length() >= 4) {
				value = major16.substring(0, 4);
			} else {
				value = value.substring(0, 4 - major16.length()) + major16;
			}
		}
		mMac.setText((mac.subSequence(0, 4) + value + mac.substring(8, 12))
				.toString());
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK)
			return;
		if (requestCode == 100) {
			Bundle bundle = data.getExtras();
			// 显示扫描到的内容
			String result = bundle.getString("result");
			DTLog.i("beacon-info : " + result);
			if (!DTStringUtils.isEmpty(result)) {
				setBeaconInfoValue(result);
				String fileName = DTFileUtils.getImageDir()
						+ mMac.getText().toString() + "_"
						+ mMajor.getText().toString() + "_"
						+ mMinor.getText().toString() + ".jpg";
				Bitmap bitmap = (Bitmap) data.getParcelableExtra("bitmap");
				CompressFormat format = Bitmap.CompressFormat.JPEG;
				int quality = 100;
				OutputStream stream = null;
				try {
					stream = new FileOutputStream(fileName);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				bitmap.compress(format, quality, stream);
				Intent scanIntent = new Intent(
						Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
				scanIntent.setData(Uri.fromFile(new File(fileName)));
				sendBroadcast(scanIntent);
			} else {
				DTUIUtils.showToastSafe("无法识别信息");
			}
		} else if (requestCode == PHOTO) {
			String path = getFilePath(null, requestCode, data);
			if (!DTStringUtils.isEmpty(path)) {
				Result result = scanningImage(path);
				if (result == null) {
					DTUIUtils.showToastSafe("图片无法使用");
				} else {
					setBeaconInfoValue(result.toString());
				}
			} else {
				DTUIUtils.showToastSafe("图片无法使用");
			}
		} else if (requestCode == 500) {
			HashMap<String, com.rtm.location.entity.BeaconInfo> map = (HashMap<String, com.rtm.location.entity.BeaconInfo>) data
					.getExtras().getSerializable("list");
			if (map.containsKey(mMac.getText().toString().toUpperCase())
					|| map.containsKey(mMac.getText().toString().toLowerCase())) {
				mBeaconStatus.setText("正常");
			}
		}
	}

	/**
	 * 设置beacon值
	 * 
	 * @param result
	 */
	private void setBeaconInfoValue(String result) {
		DTLog.i(result);
		String[] params = result.split("_");

		// String uuid_beta = mUUID.getText().toString().toUpperCase(), value =
		// "0000";
		// if (!DTStringUtils.isEmpty(uuid_beta)) {
		// if (uuid_beta.length() >= 4) {
		// value = uuid_beta.substring(0, 4);
		// } else {
		// value = value.substring(0, 4 - uuid_beta.length()) + uuid_beta;
		// }
		// }
		String value;
		if (params.length == 3) {
			value = params[0].substring(0, 4);
			mUUID.setText(value);
			mMac.setText(value + params[1] + params[2]);
			mMajor.setText(Integer.parseInt(params[1], 16) + "");
			mMinor.setText(Integer.parseInt(params[2], 16) + "");
			uuid = params[0];
		} else if (params.length == 7) {
			value = params[1].substring(0, 4);
			mUUID.setText(value);
			mMac.setText(value + params[2] + params[3]);
			mMajor.setText(Integer.parseInt(params[2], 16) + "");
			mMinor.setText(Integer.parseInt(params[3], 16) + "");
			deviceid = params[0];
			uuid = params[1];
		} else if (params.length == 4) {
			value = params[1].substring(0, 4);
			mUUID.setText(value);
			mMac.setText(value + params[2] + params[3]);
			mMajor.setText(Integer.parseInt(params[2], 16) + "");
			mMinor.setText(Integer.parseInt(params[3], 16) + "");
			deviceid = params[0];
			uuid = params[1];
		} else {
			DTUIUtils.showToastSafe("图片无法使用");
		}
	}

	/**
	 * 读取图片二维码
	 * 
	 * @param path
	 * @return
	 */
	protected Result scanningImage(String path) {
		if (TextUtils.isEmpty(path)) {
			return null;
		}
		// DecodeHintType 和EncodeHintType
		Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
		hints.put(DecodeHintType.CHARACTER_SET, "utf-8"); // 设置二维码内容的编码
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true; // 先获取原大小
		Bitmap scanBitmap = BitmapFactory.decodeFile(path, options);
		options.inJustDecodeBounds = false; // 获取新的大小

		int sampleSize = (int) (options.outHeight / (float) 200);

		if (sampleSize <= 0)
			sampleSize = 1;
		options.inSampleSize = sampleSize;
		scanBitmap = BitmapFactory.decodeFile(path, options);

		RGBLuminanceSource source = new RGBLuminanceSource(scanBitmap);
		BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
		QRCodeReader reader = new QRCodeReader();
		try {
			return reader.decode(bitmap1, hints);
		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (ChecksumException e) {
			e.printStackTrace();
		} catch (FormatException e) {
			e.printStackTrace();
		}
		return null;

	}
}
