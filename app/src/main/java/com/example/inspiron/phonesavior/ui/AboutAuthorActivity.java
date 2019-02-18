package com.example.inspiron.phonesavior.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import com.example.inspiron.phonesavior.R;

public class AboutAuthorActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_setting_aboutme);
		
		TextView tv_common_title = (TextView) findViewById(R.id.tv_common_title);
		tv_common_title.setText("关于作者");
	}
}
