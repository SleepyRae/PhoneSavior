package com.example.inspiron.phonesavior.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import com.example.inspiron.phonesavior.R;

public class AboutAppActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_setting_aboutapp);
		
		TextView tv_common_title = (TextView) findViewById(R.id.tv_common_title);
		tv_common_title.setText("关于应用");
		TextView tv_aboutapp_version = (TextView) findViewById(R.id.tv_aboutapp_version);
		tv_aboutapp_version.setText("版本1.0");
	}
}
