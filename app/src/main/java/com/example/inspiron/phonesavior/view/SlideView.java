package com.example.inspiron.phonesavior.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;

import com.example.inspiron.phonesavior.utils.LogUtil;
import com.example.inspiron.phonesavior.R;

/**
        *
        * @file SlideView.java
        * @package com.zp.quickaccess.view
        * @comment 自定义的view控件
        * @author zp
        * @date 2015-12-28 下午2:29:51
        *
        * merger标签
        * http://blog.csdn.net/zizidemenghanxiao/article/details/50052025
        */

public class SlideView extends LinearLayout {

    private static final String TAG = "SlideView";

    private Context mContext;
    // 用来放置所有view的容器
    private LinearLayout mViewContent;
    // 用来放置内置view的容器，比如删除 按钮
    private RelativeLayout mHolder;
    // 弹性滑动对象，提供弹性滑动效果
    private Scroller mScroller;
    // 滑动回调接口，用来向上层通知滑动事件
    private OnSlideListener mOnSlideListener;
    // 侧滑距离，需要和布局文件中条目的宽度对应 单位：dp
    // 一个条目宽度是50dp，一共两个条目
    private int mHolderWidth = 100;
    // 记录上次滑动的坐标
    private int mLastX = 0;
    private int mLastY = 0;
    // 用来控制滑动角度，仅当角度a满足如下条件才进行滑动：tan a = deltaX / deltaY > 2
    private static final int TAN = 2;

    public interface OnSlideListener {
        // SlideView的三种状态：开始滑动，打开，关闭
        public static final int SLIDE_STATUS_OFF = 0;
        public static final int SLIDE_STATUS_START_SCROLL = 1;
        public static final int SLIDE_STATUS_ON = 2;

        /**
         * @param view
         *            current SlideView
         * @param status
         *            SLIDE_STATUS_ON or SLIDE_STATUS_OFF
         */
        public void onSlide(View view, int status);
    }

    public SlideView(Context context) {
        super(context);
        initView();
    }

    public SlideView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        mContext = getContext();
        // 初始化弹性滑动对象
        mScroller = new Scroller(mContext);
        // 设置其方向为水平
        setOrientation(LinearLayout.HORIZONTAL);
        // 将slide_view_merge加载进来
        View.inflate(mContext, R.layout.slide_view_merge, this);
        mViewContent = (LinearLayout) findViewById(R.id.view_content);
        mHolderWidth = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                mHolderWidth,
                getResources().getDisplayMetrics()));
    }

    // 设置按钮的内容 ： 图标文本等
    public void setButtonText(CharSequence text) {
        ((TextView) findViewById(R.id.tv_merge_delete)).setText(text);
    }

    // 将view加入到ViewContent中
    public void setContentView(View view) {
        mViewContent.addView(view);
    }

    // 滑动回调，由调用者设置
    public void setOnSlideListener(OnSlideListener onSlideListener) {
        mOnSlideListener = onSlideListener;
    }

    // 将当前状态置为关闭
    public void shrink() {
        if (getScrollX() != 0) {
            this.smoothScrollTo(0, 0);
        }
    }

    /**
     *
     * @comment 根据MotionEvent来进行滑动，这个方法的作用相当于onTouchEvent
     *          如果你不需要处理滑动冲突，可以直接重命名，照样能正常工作
     *
     * @param @param event
     * @return void
     * @throws
     */
    public void onRequireTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        int scrollX = getScrollX();
        LogUtil.i(TAG, "x=" + x + "  y=" + y);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                if (mOnSlideListener != null) {
                    mOnSlideListener.onSlide(this, OnSlideListener.SLIDE_STATUS_START_SCROLL);
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int deltaX = x - mLastX;
                int deltaY = y - mLastY;
                // 滑动不满足条件，不做横向滑动
                if (Math.abs(deltaX) < Math.abs(deltaY) * TAN) {
                    break;
                }
                // 计算滑动终点是否合法，防止滑动越界
                int newScrollX = scrollX - deltaX;
                if (deltaX != 0) {
                    if (newScrollX < 0) {
                        newScrollX = 0;
                    } else if (newScrollX > mHolderWidth) {
                        newScrollX = mHolderWidth;
                    }
                    this.scrollTo(newScrollX, 0);
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                int newScrollX = 0;
                // 当松开手的时候，会自动向两边滑动，具体向哪边滑，要看当前所处的位置
                // 滑动达到下层控件的1/4的时候就可以自动滑动
                if (scrollX - mHolderWidth * 0.25 > 0) {
                    newScrollX = mHolderWidth;
                }
                // 慢慢滑向终点
                this.smoothScrollTo(newScrollX, 0);
                // 通知上层滑动事件
                if (mOnSlideListener != null) {
                    mOnSlideListener.onSlide(this,
                            newScrollX == 0 ? OnSlideListener.SLIDE_STATUS_OFF : OnSlideListener.SLIDE_STATUS_ON);
                }
                break;
            }
            default:
                break;
        }

        mLastX = x;
        mLastY = y;
    }

    private void smoothScrollTo(int destX, int destY) {
        // 缓慢滚动到指定位置
        int scrollX = getScrollX();
        int delta = destX - scrollX;
        // 以三倍时长滑向destX，效果就是慢慢滑动
        mScroller.startScroll(scrollX, 0, delta, 0, Math.abs(delta) * 3);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }

}

