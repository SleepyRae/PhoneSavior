package com.example.inspiron.phonesavior.view;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.example.inspiron.phonesavior.utils.*;

import java.lang.reflect.Field;

/**
 * 悬浮窗体的管理中大窗体采用的是全局单例进行的管理，小窗体的管理使用的是本地的windowmanager进行的显示和移除
 * 有一点点混乱，还好逻辑基本正确
 * 
 */
public class SmallFloatView extends LinearLayout {

	private static final String TAG = "SmallFloatView";
	private Context context;
	// 悬浮窗的宽和高
	private int viewWidth;
	private int viewHeight;
	// 状态栏高度
	private static int statusBarHeight;
	// 更新悬浮窗
	private WindowManager mWindowManager;
	private FloatViewManager mFloatViewManager;
	public WindowManager.LayoutParams mLayoutParams;
	// 当前的横纵坐标
	private float xInScreen;
	private float yInScreen;
	// 点击屏幕的横纵坐标
	private float xTouchInScreen;
	private float yTouchInScreen;
	// 点击位置在悬浮窗上的坐标
	private float xTouchInFloatwindow;
	private float yTouchInFloatwindow;
	// 获取屏幕宽高的工具类
	private ScreenUtils mScreenUtils;
	// 自定义的事件监听器
	private OnClickListener listener;

	public SmallFloatView(Context context, int layoutResId, int rootLayoutId) {
		super(context);
		this.context = context;
		mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		mFloatViewManager = FloatViewManager.getInstance(context);
		// 填充布局
		LayoutInflater.from(context).inflate(layoutResId, this);
		// 获取布局中的view
		View view = findViewById(rootLayoutId);
		viewWidth = view.getLayoutParams().width;
		viewHeight = view.getLayoutParams().height;
		statusBarHeight = getStatusBarHeight();

		mScreenUtils = new ScreenUtils(context);

		mLayoutParams = new WindowManager.LayoutParams();
		// 设置显示类型为phone
		mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
		// 显示图片格式
		mLayoutParams.format = PixelFormat.RGBA_8888;
		// 设置交互模式
		mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		// 设置对齐方式为左上
		mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
		mLayoutParams.width = viewWidth;
		mLayoutParams.height = viewHeight;
		// 靠右侧居中位置显示view
		mLayoutParams.x = mScreenUtils.getScreenWidth();
		mLayoutParams.y = mScreenUtils.getScreenHeight() / 2;

	}

	/*
	 * 自己处理的点击事件，就没有使用框架提供的setOnClickListener()设置点击事件
	 * 
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		// 手指按下时记录必要的数据,纵坐标的值都减去状态栏的高度
		case MotionEvent.ACTION_DOWN:
			// 获取相对与小悬浮窗的坐标
			xTouchInFloatwindow = event.getX();
			yTouchInFloatwindow = event.getY();
			// 按下时的坐标位置，只记录一次
			xTouchInScreen = event.getRawX();
			yTouchInScreen = event.getRawY() - statusBarHeight;
			break;
		case MotionEvent.ACTION_MOVE:
			// 时时的更新当前手指在屏幕上的位置
			xInScreen = event.getRawX();
			yInScreen = event.getRawY() - statusBarHeight;
			// 手指移动的时候更新小悬浮窗的位置；拖动距离大于5像素的时候视为发生拖动
			if (CommonUtils.abs(xInScreen - xTouchInScreen) > 5
					&& CommonUtils.abs(yInScreen - yTouchInScreen) > 5) {
				updateViewPosition();
				LogUtil.i(TAG, "悬浮框被拖动了");
			}
			return false;
		case MotionEvent.ACTION_UP:
			 // 当移动距离在 + - 5像素之内，都视为是点击事件而不是拖动事件
			if (CommonUtils.abs(xTouchInScreen - event.getRawX()) < 5
					&& CommonUtils.abs(yTouchInScreen
							- (event.getRawY() - statusBarHeight)) < 5) {
				OnClicked();
				if (listener != null) {

				}
				return false;
			}
		}
		return true;
	}

	/**
	 * 必须使用全局的单例中的mFloatViewManager.isBigWindowAdded布尔变量进行判断
	 * 如果用本地的布尔值进行判断的时候由于存在如下场景：小悬浮窗点击将大悬浮窗加载显示，而大悬浮窗被点击之后自行关闭，
	 * 那么就没有走else的逻辑，就会造成需要点击两次才会再次显示达悬浮窗的情形
	 * 
	 * 为了于此逻辑相对应，需要在FloatWindowBigView的initView()方法
	 * 中同样使用mFloatViewManager.isBigWindowAdded进行状态判断
	 */
	@SuppressWarnings("static-access")
	public void OnClicked() {
		if (!mFloatViewManager.isBigWindowAdded) {
			// mWindowManager.addView(FloatwindowService.mFloatwindowBigView,
			// FloatwindowService.mFloatwindowBigView.bigWindowParams);
			// isBigviewAdded = true;
			
//			mFloatViewManager.createBigWindow(context);
//			mFloatViewManager.isBigWindowAdded = true;
			mFloatViewManager.addBigFloatWindow();
		} else {
			// mWindowManager.removeView(FloatwindowService.mFloatwindowBigView);
			
//			mFloatViewManager.removeBigWindow();
//			mFloatViewManager.isBigWindowAdded = false;
			
			mFloatViewManager.removeBigWindow();
		}
		LogUtil.i(TAG, "悬浮框被点击了");
	}

	public void setOnClickListener(OnClickListener listener) {
		this.listener = listener;
	}

	public interface OnClickListener {
		public void click();
	}

	/**
	 * 
	 * @comment 当拖动距离大于指定值的时候视为发生了拖动事件，更新view的位置
	 * 
	 * @param    
	 * @return void  
	 * @throws
	 * @date 2015-12-28 下午1:50:41
	 */
	private void updateViewPosition() {
		mLayoutParams.x = (int) (xInScreen - xTouchInFloatwindow);
		mLayoutParams.y = (int) (yInScreen - yTouchInFloatwindow);
		mWindowManager.updateViewLayout(this, mLayoutParams);
	}

	/**
	 * 
	 * @comment 获取状态栏高度
	 * @param @return   
	 * @return int  
	 * @throws
	 * @date 2015-12-28 下午1:51:32
	 */
	private int getStatusBarHeight() {
		try {
			Class<?> c = Class.forName("com.android.internal.R$dimen");
			Object o = c.newInstance();
			Field field = c.getField("status_bar_height");
			int x = (Integer) field.get(o);
			return getResources().getDimensionPixelSize(x);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

}
