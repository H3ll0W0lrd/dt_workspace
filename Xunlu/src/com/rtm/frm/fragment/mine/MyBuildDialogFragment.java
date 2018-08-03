package com.rtm.frm.fragment.mine;
//package com.rtm.frm.dialogfragment.mine;
//
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//
//import com.rtm.frm.R;
//import com.rtm.frm.dialogfragment.BaseDialogFragment;
///***
// * @author hukunge
// * @date 2014.08.28 10:22
// */
//public class MyBuildDialogFragment extends BaseDialogFragment implements View.OnClickListener{
//	Button bt_logout;
//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup container,
//			Bundle savedInstanceState) {
//		View contentView = inflater.inflate(R.layout.fragment_mybuild, container,
//				false);
//		initView(contentView);
//		return contentView;
//	}
//
//	private void initView(View contentView) {
//		bt_logout = (Button)contentView.findViewById(R.id.button_right);
//		bt_logout.setOnClickListener(this);
//	}
//
//	@Override
//	public void onDestroy() {
//		super.onDestroy();
//	}
//
//	@Override
//	public void onClick(View v) {
//		switch (v.getId()) {
//		case R.id.button_right://退出登陆状态
//			//TODO
//			MineFragment.isLogin = false;
//			break;
//
//		default:
//			break;
//		}
//	}
//
//}
