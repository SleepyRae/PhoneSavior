package com.example.inspiron.phonesavior.domain;

public class DWAppInfo {

	private String appName; 
	private String pkgName; 
	private int useTime; 
	private int useFreq;
	
	public DWAppInfo() {
		super();
	}
	public DWAppInfo(String appName, String pkgName, int useTime, int useFreq) {
		super();
		this.appName = appName;
		this.pkgName = pkgName;
		this.useTime = useTime;
		this.useFreq = useFreq;
	}
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public String getPkgName() {
		return pkgName;
	}
	public void setPkgName(String pkgName) {
		this.pkgName = pkgName;
	}
	public int getUseTime() {
		return useTime;
	}
	public void setUseTime(int useTime) {
		this.useTime = useTime;
	}
	public int getUseFreq() {
		return useFreq;
	}
	public void setUseFreq(int useFreq) {
		this.useFreq = useFreq;
	} 
}
