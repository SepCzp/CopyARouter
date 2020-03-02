package com.code.copyarouter;

import android.app.Application;
import android.os.Handler;

import com.code.router_code.newrouter.NewRouterManage;


/**
 * @Author: czp
 * @Description
 * @CreateDate: 2020/1/4 11:38
 */
public class MainApp extends Application {

    private static Handler mHandler = null;

    @Override
    public void onCreate() {
        super.onCreate();
        NewRouterManage.getInstance().init(this);
        mHandler = new android.os.Handler();
    }

    public static Handler getmHandler() {
        return mHandler;
    }
}
