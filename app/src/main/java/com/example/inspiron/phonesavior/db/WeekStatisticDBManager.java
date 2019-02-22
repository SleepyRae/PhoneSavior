package com.example.inspiron.phonesavior.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.example.inspiron.phonesavior.domain.DWAppInfo;

import java.util.ArrayList;


public class WeekStatisticDBManager {
	private DBHelper helper;
	private SQLiteDatabase db;

	public WeekStatisticDBManager(Context context) {
		helper = DBHelper.getInstance(context);
		db = helper.getWritableDatabase();
	}

	public void add(DWAppInfo appInfo) {
		db.beginTransaction();
		// appName pkgName useFreq useTime
		try {
			db.execSQL(
					"INSERT INTO " + DBHelper.WEEK_APPINFO + " VALUES(?, ?, ?, ?)",
					new Object[] { appInfo.getAppName(),
							appInfo.getPkgName(),
							appInfo.getUseFreq(),
							appInfo.getUseTime() });

			db.setTransactionSuccessful();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}

	}


	/**
	 * 将所有数据从数据库中删除
	 * 
	 * @return
	 */
	public int deleteAll() {
		return db.delete(DBHelper.WEEK_APPINFO, null, null);
	}


	/**
	 * 查询所有应用信息：此处仅仅需要知道应用个数，使用次数，使用时间
	 * 
	 * 其他无关信息就不提供了
	 */
	public ArrayList<DWAppInfo> findAll() {
		ArrayList<DWAppInfo> infos = new ArrayList<DWAppInfo>();
		String sql = "SELECT * FROM " + DBHelper.WEEK_APPINFO;
		Cursor c = db.rawQuery(sql, null);
		while (c.moveToNext()) {
			DWAppInfo info = new DWAppInfo();
			
			info.setAppName(c.getString(c.getColumnIndex("appName")));
			info.setUseFreq(c.getInt(c.getColumnIndex("useFreq")));
			info.setUseTime(c.getInt(c.getColumnIndex("useTime")));

			infos.add(info);
		}
		c.close();
		return infos;
	}


	/**
	 * 根据应用名更新应用信息，
	 * 仅仅更改使用次数和使用时间
	 * 
	 */
	public void updateAppInfo(DWAppInfo info) {

		ContentValues cv = new ContentValues();
		cv.put("useFreq", info.getUseFreq());
		cv.put("useTime", info.getUseTime());
		String[] whereArgs = { String.valueOf(info.getAppName()) };
		db.update(DBHelper.WEEK_APPINFO, cv, "appName=?", whereArgs);
	}

	/**
	 * 
	 * @comment 获取所有的应用名称
	 * @param @return   
	 * @return ArrayList<String>  
	 * @throws
	 */
	public ArrayList<String> findAllPkgNames() {
		ArrayList<String> infos = new ArrayList<String>();
		String sql = "SELECT pkgName FROM " + DBHelper.WEEK_APPINFO;
		Cursor c = db.rawQuery(sql, null);
		while (c.moveToNext()) {
			String name = c.getString(0);
			infos.add(name);
		}
		c.close();
		return infos;
	}
	
	/**
	 * 
	 * @comment 根据名称添加应用到数据库
	 * 
	 * @param @param pkgName   
	 * @return void  
	 * @throws
	 */
	public void addByName(String pkgName) {
		db.beginTransaction();
		// appName pkgName useFreq useTime
		DWAppInfo appInfo = new DWAppInfo();
		appInfo.setAppName(pkgName);
		appInfo.setPkgName(pkgName);
		appInfo.setUseFreq(0);
		appInfo.setUseTime(0);
		try {
			db.execSQL(
					"INSERT INTO " + DBHelper.WEEK_APPINFO + " VALUES(?, ?, ?, ?)",
					new Object[] { appInfo.getAppName(),
							appInfo.getPkgName(),
							appInfo.getUseFreq(),
							appInfo.getUseTime() });
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
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
