package com.example.inspiron.phonesavior.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 
 * @author zhangpeng
 * 
 */
public class DBHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "shortshot.db"; // 数据库名字
	private static final int DATABASE_VERSION = 1; // 数据库版本号
	public static String ALL_APP_INFO = "all_appinfo"; // 所有应用信息表
	public static String DAY_APPINFO = "day_appinfo"; // 每日应用实用信息统计表
	public static String WEEK_APPINFO = "week_appinfo"; // 每周应用信息统计表

	// 包含包名，应用名，使用频次，使用时长，以及计算得到的优先级
	// 其中对于是否是系统应用采用的是布尔类型进行判断，而布尔类型是不存在的，所以使用INTEGER，0代表false，1代表true
	private final static String ALL_APP_INFO_SQL = "CREATE TABLE IF NOT EXISTS "
			+ ALL_APP_INFO
			+ "(appName VARCHAR PRIMARY KEY,pkgName VARCHAR,"
			+ "isSysApp INTEGER, useFreq INTEGER, useTime INTEGER, appIcon BLOB,"
			+ "weight INTEGER)";
	// 每日信息只需要知道时间长度和使用次数这些基本信息就可以了
	private final static String DAY_APPINFO_SQL = "CREATE TABLE IF NOT EXISTS "
			+ DAY_APPINFO + "(appName VARCHAR PRIMARY KEY,pkgName VARCHAR,"
			+ "useFreq INTEGER, useTime INTEGER)";

	private final static String WEEK_APPINFO_SQL = "CREATE TABLE IF NOT EXISTS "
			+ WEEK_APPINFO
			+ "(appName VARCHAR PRIMARY KEY,pkgName VARCHAR,"
			+ "useFreq INTEGER, useTime INTEGER)";

	private static DBHelper mInstance = null;

	private DBHelper(Context context) {
		// CursorFactory设置为null,使用默认值
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/**
	 * 单例模式 获取数据库helper实例
	 */
	public static synchronized DBHelper getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new DBHelper(context);
		}
		return mInstance;
	}

	/**
	 * 数据库第一次被创建时onCreate会被调用 创建系统所需要使用的3张表
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(ALL_APP_INFO_SQL);
		db.execSQL(DAY_APPINFO_SQL);
		db.execSQL(WEEK_APPINFO_SQL);
	}

	/**
	 * 数据库表结构发生变化时，onUpgrade会被调用
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
	}
}
