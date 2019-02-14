package com.example.inspiron.phonesavior.utils;

import android.util.Log;

/**
 *
 * @file LogUtil.java
 * @package com.zp.quickaccess.utils
 * @comment 日志管理类，统一对不同级别的日志输出进行管理。实质上仅仅是对log.x的一层封装而已
 * 			但是添加了日志级别，通过修改LOGLEVEL的值使输出更加自由可控
 *
 */

public class LogUtil {
    private static int LOGLEVEL = 6; // 控制输出级别

    private static int VERBOSE = 1;
    private static int DEBUG = 2;
    private static int INFO = 3;
    private static int WARN = 4;
    private static int ERROE = 5;

    public static void v(String tag, String msg){
        if(LOGLEVEL > VERBOSE){
            Log.v(tag, msg);
        }
    }

    public static void d(String tag, String msg){
        if(LOGLEVEL > DEBUG){
            Log.d(tag, msg);
        }
    }

    public static void i(String tag, String msg){
        if(LOGLEVEL > INFO){
            Log.i(tag, msg);
        }
    }

    public static void w(String tag, String msg){
        if(LOGLEVEL > WARN){
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg){
        if(LOGLEVEL > ERROE){
            Log.e(tag, msg);
        }
    }
}
