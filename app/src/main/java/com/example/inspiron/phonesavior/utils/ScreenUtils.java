package com.example.inspiron.phonesavior.utils;

import android.content.Context;
import android.view.WindowManager;

public class ScreenUtils {
    private Context context;
    private static WindowManager mWindowManager;

    public ScreenUtils(Context c){
        this.context = c;
        mWindowManager = (WindowManager) this.context.getSystemService(Context.WINDOW_SERVICE);
    }
    /**
     * 获取屏幕宽度
     * @param
     * @return
     */
    @SuppressWarnings("deprecation")
    public static int getScreenWidth() {
        return mWindowManager.getDefaultDisplay().getWidth();
    }

    /**
     * 获取屏幕高度
     * @param
     * @return
     */
    @SuppressWarnings("deprecation")
    public static int getScreenHeight() {
        return mWindowManager.getDefaultDisplay().getHeight();
    }

}

