package com.example.inspiron.phonesavior.view;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.inspiron.phonesavior.domain.AppUseStatics;
import com.example.inspiron.phonesavior.ui.AppContext;
import com.example.inspiron.phonesavior.utils.LogUtil;
import com.example.inspiron.phonesavior.utils.ScreenUtils;
import com.example.inspiron.phonesavior.R;

public class BigFloatView extends LinearLayout {
	
	private static final String TAG = "BigFloatView";
	private ImageView iv_appicon_1;
	private ImageView iv_appicon_2;
	private ImageView iv_appicon_3;
	private ImageView iv_appicon_4;
	private ImageView iv_appicon_5;
	private ImageView iv_appicon_6;
	
	private List<AppUseStatics> topSixInfo;

	// 记录大悬浮窗的宽高
	public int viewWidth;
	public int viewHeight;

	public WindowManager.LayoutParams bigWindowParams;
	public FloatViewManager mFloatViewManager;

	private Context context;

	public BigFloatView(Context context) {
		super(context);
		this.context = context;

		LayoutInflater.from(context).inflate(R.layout.floatview_big, this);
		
		View bigView = findViewById(R.id.big_window_layout);
		
		viewWidth = bigView.getLayoutParams().width;
		viewHeight = bigView.getLayoutParams().height;

		bigWindowParams = new WindowManager.LayoutParams();
		// 设置显示的位置，默认的是屏幕中心
		bigWindowParams.x = ScreenUtils.getScreenWidth() / 2 - viewWidth / 2;
		bigWindowParams.y = ScreenUtils.getScreenHeight() / 2 - viewHeight / 2;
		bigWindowParams.type = WindowManager.LayoutParams.TYPE_PHONE;
		bigWindowParams.format = PixelFormat.RGBA_8888;

		// 设置交互模式
		bigWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

		bigWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
		bigWindowParams.width = viewWidth;
		bigWindowParams.height = viewHeight;
		
		mFloatViewManager = FloatViewManager.getInstance(context);

		initView();
	}

	private void initView() {
		iv_appicon_1 = (ImageView) findViewById(R.id.iv_appicon_1);
		iv_appicon_2 = (ImageView) findViewById(R.id.iv_appicon_2);
		iv_appicon_3 = (ImageView) findViewById(R.id.iv_appicon_3);
		iv_appicon_4 = (ImageView) findViewById(R.id.iv_appicon_4);
		iv_appicon_5 = (ImageView) findViewById(R.id.iv_appicon_5);
		iv_appicon_6 = (ImageView) findViewById(R.id.iv_appicon_6);
		
		topSixInfo = AppContext.mDBManager.findTopApp(6);
		// topSixInfo必须添加非空判断，否则初次使用会因为数据库为而导致错误
		if(topSixInfo != null && (topSixInfo.size() == 6)) {
			iv_appicon_1.setImageDrawable(topSixInfo.get(0).getIcon());
			iv_appicon_2.setImageDrawable(topSixInfo.get(1).getIcon());
			iv_appicon_3.setImageDrawable(topSixInfo.get(2).getIcon());
			iv_appicon_4.setImageDrawable(topSixInfo.get(3).getIcon());
			iv_appicon_5.setImageDrawable(topSixInfo.get(4).getIcon());
			iv_appicon_6.setImageDrawable(topSixInfo.get(5).getIcon());
		}
		
		iv_appicon_1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleClickAt(0);
			}
		});
		
		iv_appicon_2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleClickAt(1);
			}
		});
		
		iv_appicon_3.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleClickAt(2);
			}
		});
		
		iv_appicon_4.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleClickAt(3);
			}
		});
		
		iv_appicon_5.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleClickAt(4);
			}
		});
		
		iv_appicon_6.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleClickAt(5);
			}
		});
		
	}
	
	/**
	 * 
	 * @comment 大悬浮窗的六个ImageView的点击事件的逻辑：开启Activity并关闭自身
	 * 
	 * @param @param position   
	 * @return void  
	 * @throws
	 * @date 2015-12-27 下午7:54:44
	 */
	public void handleClickAt(int position){
		String pkgName = topSixInfo.get(position).getPkgName();
		if(topSixInfo.size() == 6 && pkgName != null){
			startActivityByPkgname(this.context, pkgName);
			FloatViewManager.getInstance(context).removeBigWindow();
			FloatViewManager.isBigWindowAdded = false;
		}else{
			Toast.makeText(context, "开启失败", 0).show();
			FloatViewManager.getInstance(context).removeBigWindow();
			FloatViewManager.isBigWindowAdded = false;
		}
	}
	
	/**
	 * 
	 * @comment 根据包名开启一个应用，代码以及逻辑和应用管理界面中的start按钮的点击事件的逻辑完全相同
	 * 			区别仅仅在去此处在activity上下文之外开启actiivty所以给Intent设置了 FLAG_ACTIVITY_NEW_TASK
	 * 
	 * 			对于无法找到和无法开启的应用弹出土司提示
	 * 
	 * @param @param context 开启Activity所需要的上下文
	 * @param @param pkgName 要开启的应用的包名
	 * @return void  
	 * @throws
	 * @date 2015-12-27 下午7:52:46
	 */
	public void startActivityByPkgname(Context context, String pkgName){
		PackageInfo pkgInfo;
		try {
			pkgInfo = context.getPackageManager().getPackageInfo(pkgName,
					PackageManager.GET_UNINSTALLED_PACKAGES | PackageManager.GET_ACTIVITIES);
			ActivityInfo[] activityInfos = pkgInfo.activities;
			if (activityInfos.length > 0) {
				ActivityInfo startActivity = activityInfos[0];
				Intent intent = new Intent();
				// 在activity上下文之外的环境中开启一个Activity，需要设置FLAG_ACTIVITY_NEW_TASK
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setClassName(pkgName, startActivity.name);
				context.startActivity(intent);
			} else {
				Toast.makeText(context, "应用程序无法启动", 0).show();
			}
		} catch (Exception e) {
			Toast.makeText(context, "应用程序无法找到或者不允许被开启", 0).show();
			e.printStackTrace();
			LogUtil.i(TAG, e.toString());
		}
	}
}

