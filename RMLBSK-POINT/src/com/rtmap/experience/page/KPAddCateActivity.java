package com.rtmap.experience.page;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.rtmap.experience.R;
import com.rtmap.experience.core.KPActivity;
import com.rtmap.experience.core.model.CateInfo;
import com.rtmap.experience.util.DTStringUtils;

public class KPAddCateActivity extends KPActivity implements OnClickListener {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.kp_add_cate);
		findViewById(R.id.save).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if(v.getId()==R.id.save){
			EditText text = (EditText) findViewById(R.id.sign);
			String str = text.getText().toString();
			if(!DTStringUtils.isEmpty(str)){
				CateInfo info = new CateInfo();
				info.setName(str);
				Intent intent = getIntent();
				Bundle bundle = new Bundle();
				bundle.putSerializable("cate", info);
				intent.putExtras(bundle);
				setResult(RESULT_OK, intent);
				finish();
			}
		}
	}
}
