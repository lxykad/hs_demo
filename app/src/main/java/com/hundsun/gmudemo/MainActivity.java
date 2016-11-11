package com.hundsun.gmudemo;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ListActivity {
	public static final String TAG = MainActivity.class.getName();
	
	private H5DataCenter mDatacenter;
	private EditText accoutedt,pwdedt,hostedt,portedt,appkeyedt,appsecretedt,tokenedt,authidedt,logcatedt;
	private Button button,button2;
	private String logcatstring="";
	public Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if(msg!=null){
				if(msg.what == 6002){
					Bundle data = msg.getData();
					if(data!=null){
						logcatstring +=data.getString("log")+"\n";
						logcatedt.setText(logcatstring);
					}
				}else if(msg.what == 1215){
					logcatstring="==========Start=========\n";
					logcatedt.setText(logcatstring);
				}else if(msg.what == 126){
					Bundle data = msg.getData();
					if(data!=null){
						authidedt.setText(data.getString("authid"));
						tokenedt.setText(data.getString("token"));
					}
				}
			}
			super.handleMessage(msg);
		}
	};
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		List<String> items = getDemoList();

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);

		setListAdapter(adapter);
		mDatacenter = H5DataCenter.getInstance();
		mDatacenter.setHandler(mHandler);
		accoutedt = (EditText)findViewById(R.id.account_edt);
		pwdedt = (EditText)findViewById(R.id.pwd_edt);
		hostedt = (EditText)findViewById(R.id.host_edt);
		portedt = (EditText)findViewById(R.id.port_edt);
		appkeyedt = (EditText)findViewById(R.id.appkey_edt);
		appsecretedt = (EditText)findViewById(R.id.appsecert_edt);
		tokenedt = (EditText)findViewById(R.id.token_edt);
		authidedt = (EditText)findViewById(R.id.authid_edt);
		logcatedt = (EditText)findViewById(R.id.logcat_edt);
		button = (Button)findViewById(R.id.comfire_btn);
		button2 = (Button)findViewById(R.id.comfire_btn2);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mHandler.sendEmptyMessage(1215);
				if(!TextUtils.isEmpty(accoutedt.getText())){
					mDatacenter.setUSERNAME(accoutedt.getText().toString());
				}else{
					//logcat("用户名不能为空");
					//return;
				}
				if(!TextUtils.isEmpty(pwdedt.getText())){
					mDatacenter.setPASSWORD(pwdedt.getText().toString());
				}else{
					//logcat("密码不能为空");
					//return;
				}
				if(!TextUtils.isEmpty(appkeyedt.getText()))mDatacenter.setAPP_KEY(appkeyedt.getText().toString());
				if(!TextUtils.isEmpty(appsecretedt.getText()))mDatacenter.setAPP_SECRET(appsecretedt.getText().toString());
				if(!TextUtils.isEmpty(hostedt.getText()))mDatacenter.setSERVER_HOST(hostedt.getText().toString());
				if(!TextUtils.isEmpty(portedt.getText()))mDatacenter.setSERVER_PORT(portedt.getText().toString());
				mDatacenter.initSession(MainActivity.this,0);
			}
		});
		button2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mHandler.sendEmptyMessage(1215);
				if(!TextUtils.isEmpty(authidedt.getText())){
					mDatacenter.setAUTHID(authidedt.getText().toString());
				}else{
					/*logcat("authid不能为空");
					return;*/
				}
				if(!TextUtils.isEmpty(tokenedt.getText())){
					mDatacenter.setTOKEN(tokenedt.getText().toString());
				}else{
					/*logcat("token不能为空");
					return;*/
				}
				if(!TextUtils.isEmpty(appkeyedt.getText()))mDatacenter.setAPP_KEY(appkeyedt.getText().toString());
				if(!TextUtils.isEmpty(appsecretedt.getText()))mDatacenter.setAPP_SECRET(appsecretedt.getText().toString());
				if(!TextUtils.isEmpty(hostedt.getText()))mDatacenter.setSERVER_HOST(hostedt.getText().toString());
				if(!TextUtils.isEmpty(portedt.getText()))mDatacenter.setSERVER_PORT(portedt.getText().toString());
				mDatacenter.initSession(MainActivity.this,1);
			}
		});
	}

	private List<String> getDemoList() {
		List<String> items = new ArrayList<String>();

		items.add("初始化Session");
		items.add("竖屏视图实例");
		items.add("横屏视图实例");

		return items;

	}

	public void logcat(String str){
		if(mHandler==null)return;
		Message msg = Message.obtain();
		msg.what = 6002;
		Bundle bundle = new Bundle();
		bundle.putString("log", str);
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		System.out.println("position is : "+position);
		switch (position) {
		case 0:
			mHandler.sendEmptyMessage(1215);
			mDatacenter.initSession(this,0);
			break;
			
		case 1:
			Intent it = new Intent();
			it.setClass(this, QuoteDemoActivity.class);
			startActivity(it);
			break;
		case 2:
			it = new Intent();
			it.setClass(this, QuoteLandsacpeDemoActivity.class);
			startActivity(it);
			break;

		default:
			break;
		}
	}
	

}
