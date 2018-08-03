package demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.location.R;
import com.rtm.common.model.RMLocation;
import com.rtm.common.utils.RMStringUtils;
import com.rtm.location.LocationApp;
import com.rtm.location.entity.RMUser;
import com.rtm.location.utils.RMLocationListener;
import com.rtm.location.utils.RMSqlite;

/**
 * 常见问题
 * @author dingtao
 *
 */
public class FAQActivity extends Activity {


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.faq);
	}

}
