package com.example.inspiron.phonesavior.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.example.inspiron.phonesavior.domain.AppUseStatics;
import com.example.inspiron.phonesavior.utils.IconUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DBManager {
	private static final String TAG = "DBManager";
	private DBHelper helper;
	private SQLiteDatabase db;

	public DBManager(Context context) {
		helper = DBHelper.getInstance(context);
		// 执行了下面这句话，数据库才算创建
		db = helper.getWritableDatabase();
	}

	public void add(AppUseStatics appUseStatics) {
		db.beginTransaction();

		try {
			// TODO: 2016/9/15 应用的图标其实是不必插入的，一方面是转换比较低效，另外一方面是icon本身比较大；
			// 一种可以采用的做法是直接存应用名，使用信息等，到显式的时候，直接使用PackageManager来获取图标

			// 依次插入包名，应用名，是否是系统应用，使用频次，使用时间，权重值
			// 由于这里省略了列的顺序，所以在进行数据插入的时候需要注意和建表顺序一致
			// + "(appName VARCHAR PRIMARY KEY,pkgName VARCHAR,"
			// +
			// "isSysApp INTEGER, useFreq INTEGER, useTime INTEGER, appIcon BLOB,"
			// + "weight INTEGER)";
			db.execSQL(
					"INSERT INTO " + DBHelper.ALL_APP_INFO
							+ " VALUES(?, ?, ?, ?, ?,?,?)",
					new Object[] { appUseStatics.getName(),
							appUseStatics.getPkgName(),
							appUseStatics.isSysApp(),
							appUseStatics.getUseFreq(),
							appUseStatics.getUseTime(),
							IconUtils.getIconData(appUseStatics.getIcon()),
							appUseStatics.getWeight() });

			db.setTransactionSuccessful();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}

	}

	/**
	 * 将列表中所有数据添加到数据库中
	 * 
	 * 实现有一些低效，后续优化
	 * 
	 * @param allAppStatics
	 */
	public void addAll(List<AppUseStatics> allAppStatics) {
		for (AppUseStatics aps : allAppStatics) {
			add(aps);
		}
	}

	/**
	 * 根据应用名将其从数据库中删除
	 * 
	 * @param name
	 * @return
	 */
	public int deleteByAppName(String name) {
		return db.delete(DBHelper.ALL_APP_INFO, "appName=?",
				new String[] { name });
	}
	
	/**
	 * 
	 * @comment 根据应用包名将应用删除
	 * @param @param pkgName
	 * @param @return   
	 * @return int  
	 * @throws
	 * @date 2015-12-30 上午11:12:53
	 */
	public int deleteByPkgName(String pkgName) {
		return db.delete(DBHelper.ALL_APP_INFO, "pkgName=?", new String[] { pkgName });
	}

	/**
	 * 将所有数据从数据库中删除
	 * 
	 * @return
	 */
	public int deleteAll() {
		return db.delete(DBHelper.ALL_APP_INFO, null, null);
	}

	/**
	 * 根据应用名进行查询
	 * 
	 * @param name
	 * @return info
	 */
	public AppUseStatics queryByAppName(String name) {
		String sql = "SELECT * FROM " + DBHelper.ALL_APP_INFO + " where appName= '" + name + "'";

		AppUseStatics info = new AppUseStatics();
		Cursor c = db.rawQuery(sql, null);
		if (c.moveToNext()) {
			info.setPkgName(c.getString(c.getColumnIndex("pkgName")));
			info.setName(c.getString(c.getColumnIndex("appName")));
			info.setUseFreq(c.getInt(c.getColumnIndex("useFreq")));
			info.setUseTime(c.getInt(c.getColumnIndex("useTime")));
			info.setIcon(IconUtils.getDrawableFromBitmap(IconUtils
					.getBitmapFromBytes(c.getBlob(c.getColumnIndex("appIcon")))));
			info.setSysApp(c.getInt(c.getColumnIndex("isSysApp")));
			info.setWeight(c.getInt(c.getColumnIndex("weight")));
		}else{
			info.setName("empty");
			info.setPkgName("empty");
		}
		c.close();
		return info;
	}

	/**
	 * 根据包名进行查询
	 * 
	 * @param pkgName
	 * @return info 返回值非空，可以由调用者判断
	 */
	public AppUseStatics queryByPkgName(String pkgName) {
		String sql = "SELECT * FROM " + DBHelper.ALL_APP_INFO + " where pkgName= '" + pkgName + "'";

		AppUseStatics info = new AppUseStatics();
		Cursor c = db.rawQuery(sql, null);
		if (c.moveToNext()) {
			info.setPkgName(c.getString(c.getColumnIndex("pkgName")));
			info.setName(c.getString(c.getColumnIndex("appName")));
			info.setUseFreq(c.getInt(c.getColumnIndex("useFreq")));
			info.setUseTime(c.getInt(c.getColumnIndex("useTime")));
			info.setIcon(IconUtils.getDrawableFromBitmap(IconUtils.getBitmapFromBytes(c.getBlob(c.getColumnIndex("appIcon")))));
			info.setSysApp(c.getInt(c.getColumnIndex("isSysApp")));
			info.setWeight(c.getInt(c.getColumnIndex("weight")));
		}else{
			info.setName("empty");
			info.setPkgName("empty");
		}
		c.close();
		return info;
	}

	/**
	 * 查询所有应用信息
	 */
	public ArrayList<AppUseStatics> findAll() {
		ArrayList<AppUseStatics> infos = new ArrayList<AppUseStatics>();
		String sql = "SELECT * FROM " + DBHelper.ALL_APP_INFO;
		Cursor c = db.rawQuery(sql, null);
		while (c.moveToNext()) {
			AppUseStatics info = new AppUseStatics();
			info.setName(c.getString(c.getColumnIndex("appName")));
			info.setPkgName(c.getString(c.getColumnIndex("pkgName")));
			info.setSysApp(c.getInt(c.getColumnIndex("isSysApp")));
			info.setUseFreq(c.getInt(c.getColumnIndex("useFreq")));
			info.setUseTime(c.getInt(c.getColumnIndex("useTime")));
			info.setIcon(IconUtils.getDrawableFromBitmap(IconUtils
					.getBitmapFromBytes(c.getBlob(c.getColumnIndex("appIcon")))));
			info.setWeight(c.getInt(c.getColumnIndex("weight")));

			infos.add(info);
		}
		c.close();
		Collections.sort(infos);
		return infos;
	}

	/**
	 * 查找排名前count的应用
	 * 
	 * @param count
	 * @return
	 */
	public ArrayList<AppUseStatics> findTopApp(int count) {
		ArrayList<AppUseStatics> infos = new ArrayList<AppUseStatics>();

		// 查询之后按权重排序
		String sql = "SELECT * FROM " + DBHelper.ALL_APP_INFO
				+ " order by weight desc";
		Cursor c = db.rawQuery(sql, null);

		if (c.getCount() <= 0) {
			return null;
		} else {
			// 取出前count 个应用的信息
			for (int i = 0; i < count; i++) {
				if (c.moveToNext()) {
					AppUseStatics info = new AppUseStatics();
					info.setPkgName(c.getString(c.getColumnIndex("pkgName")));
					info.setName(c.getString(c.getColumnIndex("appName")));
					info.setUseFreq(c.getInt(c.getColumnIndex("useFreq")));
					info.setUseTime(c.getInt(c.getColumnIndex("useTime")));
					info.setIcon(IconUtils.getDrawableFromBitmap(IconUtils
							.getBitmapFromBytes(c.getBlob(c.getColumnIndex("appIcon")))));
					info.setSysApp(c.getInt(c.getColumnIndex("isSysApp")));
					info.setWeight(c.getInt(c.getColumnIndex("weight")));

					infos.add(info);
				}
			}
		}
		c.close();
		Collections.sort(infos);
		return infos;
	}

	/**
	 * 根据应用名更新应用信息，比如修改权重和使用时间信息等
	 * 
	 */
	public int updateAppInfo(AppUseStatics info) {

		ContentValues cv = new ContentValues();
		cv.put("useFreq", info.getUseFreq());
		cv.put("useTime", info.getUseTime());
		cv.put("weight", info.getWeight());
		String[] whereArgs = { String.valueOf(info.getName()) };
		return db.update(DBHelper.ALL_APP_INFO, cv, "appName=?", whereArgs);
	}

	/**
	 * 更新数据库中所有应用信息
	 * 
	 * @return 更新的记录数
	 */
	public int updateAll() {
		return 0;
	}

	/**
	 * @Description: 关闭数据库
	 */
	public void closeDB() {
		// db.close();
		helper.close();
	}
}
