package com.example.inspiron.phonesavior.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.inspiron.phonesavior.domain.UpdateInfo;
import com.example.inspiron.phonesavior.engine.UpdateInfoService;
import com.example.inspiron.phonesavior.utils.LogUtil;
import com.example.inspiron.phonesavior.R;

public class SplashActivity extends Activity {

	protected static final String TAG = "SplashActivity";
	private Intent mainActivity;
	private LinearLayout ll_splash;
	private TextView tv_splashactivity_version;
	private Context context;

	private String curVersion = "1.0";
	public static final int NEED_UPDATE = 100;
	public static final int NOT_NEED_UPDATE = 101;
	public static final int DEFAULT_FINISH_SPLASH = 102;

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case NEED_UPDATE:
				LogUtil.i(TAG, "需要更新");
				startActivity(mainActivity);
				finish();
				showUpdateDialog(context);
				break;

			case NOT_NEED_UPDATE:
				LogUtil.i(TAG, "不需要更新");
				startActivity(mainActivity);
				finish();
				break;
				
			case DEFAULT_FINISH_SPLASH:
				LogUtil.i(TAG, "DEFAULT_FINISH_SPLASH");
				startActivity(mainActivity);
				finish();
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_splash);

		// 为splash界面设置动画
		ll_splash = (LinearLayout) findViewById(R.id.ll_splash);
		AlphaAnimation aa = new AlphaAnimation(0.5f, 1.0f);
		aa.setDuration(2000);
		ll_splash.setAnimation(aa);
		// 设置版本信息
		tv_splashactivity_version = (TextView) findViewById(R.id.tv_splashactivity_version);
		tv_splashactivity_version.setText(" Time Mirror ");

		curVersion = getVersion();
		context = this;
		mainActivity = new Intent();
		mainActivity.setClass(this, MainActivity.class);

		/*
		new Thread() {
			@Override
			public void run() {
				
				boolean flag = isNeedUpdate(curVersion);
				if (flag) {
					// 需要更新，那么发送消息NEED_UPDATE给主UI，通知其弹出更新对话框
					handler.sendEmptyMessage(NEED_UPDATE);
				} else {
					// 不需要更新，发NOT_NEED_UPDATE消息，直接进入主界面
					handler.sendEmptyMessage(NOT_NEED_UPDATE);
				}
				
			}
		}.start(); // 不要忘了开启线程啦……
		*/
				
		//由于一开始是先链接服务器获取新版本，后来觉得没有必要。但是为了显示splash，此处休眠4秒
		new Thread(){
			public void run() {
				try {
					sleep(4000);
					handler.sendEmptyMessage(DEFAULT_FINISH_SPLASH);
					LogUtil.i(TAG, "DEFAULT_FINISH_SPLASH");
				} catch (InterruptedException e) {
					LogUtil.e(TAG, "InterruptedException : " + e.toString());
					e.printStackTrace();
				}
			};
		}.start();
	}

	/**
	 * 显示升级对话框
	 */
	private void showUpdateDialog(Context context) {
		Builder builder = new Builder(context);
		builder.setIcon(R.drawable.ic_launcher);
		builder.setTitle("新版本升级");
		builder.setMessage("从服务器获取的更新信息……");
		builder.setCancelable(false); // 对话框不可取消

		builder.setPositiveButton("确定", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				LogUtil.i(TAG, "用户点击更新,下载apk……");
			}
		});

		builder.setNegativeButton("取消", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				LogUtil.i(TAG, "用户取消更新,进入程序主界面");
				// 如果用户点击取消更新，那么发送不需要更新并进入主界面的消息
				handler.sendEmptyMessage(NOT_NEED_UPDATE);
			}
		});

		builder.create().show();
	}

	/**
	 * 判断是否需要更新版本
	 * 
	 * 注意事项：由于4.0以后是不允许子主线程中进行耗时操作，比如此处的网络访问
	 * 因此isNeedUpdate()方法需要在一个子线程中执行。既然是在子线程，那么toast显然是不可以再显示了
	 * ，要么注释掉，要么通过handler发送消息进行更新
	 * 
	 * @param curVersion
	 *            系统当前版本号
	 */
	private boolean isNeedUpdate(String curVersion) {
		UpdateInfoService service = new UpdateInfoService(this);
		String newVersion = getVersion();
		try {
			UpdateInfo info = service.getUpdataInfo(R.string.updateurl);
			newVersion = info.getVersion();
			if (curVersion.equals(newVersion)) {
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {
			Log.e(TAG, e.toString());
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 获取当前系统的版本信息
	 * 
	 * @return 版本号
	 */
	public String getVersion() {
		try {
			PackageManager mPackageManager = getPackageManager();
			PackageInfo info = mPackageManager.getPackageInfo(getPackageName(),
					0);
			return info.versionName;
		} catch (Exception e) {
			e.printStackTrace();
			return "版本号未知";
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
