package com.example.inspiron.phonesavior.view;

import android.content.Context;
import android.view.WindowManager;

import com.example.inspiron.phonesavior.R;

/**
 * 
 * @file FloatViewManager.java
 * @package com.zp.quickaccess.view 
 * @comment 单例模式的FloatViewManager，用于管理大小窗体的创建显示移除等
 * @author zp
 * @date 2015-12-29 下午9:05:11
 */
public class FloatViewManager {

	// 小悬浮窗对象
	public SmallFloatView mSmallWindow = null;
	public static boolean isSmallWindowAdded = false;
	// 大悬浮窗对象
	public BigFloatView mBigWindow = null;
	public static boolean isBigWindowAdded = false;
	// 用于控制在屏幕上添加或移除悬浮窗
	private WindowManager mWindowManager;
	// mFloatViewManager的单例
	private static FloatViewManager mFloatViewManager;
	// 上下文对象
	private Context context;

	private FloatViewManager(Context context) {
		this.context = context;
		mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
	}

	public static FloatViewManager getInstance(Context context) {
		if (mFloatViewManager == null) {
			mFloatViewManager = new FloatViewManager(context);
		}
		return mFloatViewManager;
	}

	public void setOnClickListener(SmallFloatView.OnClickListener listener) {
		if (mSmallWindow != null) {
			mSmallWindow.setOnClickListener(listener);
		}
	}

	/*
	 * 悬浮窗创建
	 */
	private void createBigWindow(Context context) {
		if (mBigWindow == null) {
			mBigWindow = new BigFloatView(context);
		}
	}
	
	private void cteateSmallFloatWindow() {
		if (mSmallWindow == null) {
			mSmallWindow = new SmallFloatView(context, R.layout.floatview_small, R.id.floatwindow_layout);
		}
	}

	/*
	 * 将悬浮窗体添加到界面
	 */
	public void addBigFloatWindow() {
		if (mBigWindow == null) {
			createBigWindow(this.context);
			mWindowManager.addView(mBigWindow, mBigWindow.bigWindowParams);
			isBigWindowAdded = true;
		}
	}
	
	public void addSmallFloatWindow() {
		if (mSmallWindow == null) {
			cteateSmallFloatWindow();
			mWindowManager.addView(mSmallWindow, mSmallWindow.mLayoutParams);
			isSmallWindowAdded = true;
		}
	}
	
	/*
	 * 将悬浮窗从屏幕上移除
	 */
	public void removeBigWindow() {
		if (mBigWindow != null) {
			mWindowManager.removeView(mBigWindow);
			mBigWindow = null;
			isBigWindowAdded = false;
		}
	}
	
	public void removeSmallWindow() {
		if (mSmallWindow != null) {
			mWindowManager.removeView(mSmallWindow);
			mSmallWindow = null;
			isSmallWindowAdded = false;
		}
	}

	public void removeAll() {
		removeSmallWindow();
		removeBigWindow();
	}

	/**
	 * 
	 * @comment  是否有悬浮窗显示(包括小悬浮窗和大悬浮)
	 * @return boolean  有悬浮窗显示在桌面上返回true，没有的话返回false
	 */
	public boolean isWindowShowing() {
		return mSmallWindow != null || mBigWindow != null;
	}
	
	public boolean isSmallWindowShowing() {
		return mSmallWindow != null ;
	}
	
	public boolean isBigWindowShowing() {
		return mSmallWindow != null ;
	}
}

