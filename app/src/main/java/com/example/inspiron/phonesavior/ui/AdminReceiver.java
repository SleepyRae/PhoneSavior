package com.example.inspiron.phonesavior.ui;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class AdminReceiver extends DeviceAdminReceiver {

    private final String Tag = "Tag：";

    public DevicePolicyManager getManager(Context context) {
        Log.e(Tag, "調用getManger()");
        return super.getManager(context);
    }

    @Override
    public IBinder peekService(Context myContext, Intent service) {
        Log.e(Tag, "調用peekService()");
        return super.peekService(myContext, service);
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        Log.e(Tag, "調用onDisableRequested()");
        return super.onDisableRequested(context, intent);
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        Log.e(Tag, "管理權限取消");
        super.onDisabled(context, intent);
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        Log.e(Tag, "管理權限開啟");
        super.onEnabled(context, intent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(Tag, "調用onReceive()");
        super.onReceive(context, intent);
    }

}