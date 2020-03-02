package com.code.router_code;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.code.route_annotation.model.RouterMeta;
import com.code.router_code.newrouter.NewRouterManage;
import com.code.router_code.template.IServer;

/**
 * @Author: czp
 * @Description
 * @CreateDate: 2019/12/31 16:55
 */
public class PostCard extends RouterMeta {

    private String activityName;
    private Bundle mBundle;
    private int flags = -1;
    private IServer server;

    public PostCard(String activityName) {
        this.activityName = activityName;
        Log.d("PostCard", "PostCard: " + activityName);
    }

    public PostCard(String path, String group) {
        setPath(path);
        setGroup(group);
        mBundle = new Bundle();
    }

    public PostCard withString(String k, String v) {
        mBundle.putString(k, v);
        return this;
    }

    public PostCard withInt(String key, int value) {
        mBundle.putInt(key, value);
        return this;
    }

    public PostCard with(Bundle bundle) {
        if (null != bundle) {
            mBundle = bundle;
        }
        return this;
    }

    public int getFlags() {
        return flags;
    }

    public Bundle getmBundle() {
        return mBundle;
    }

    public IServer getServer() {
        return server;
    }

    public void setServer(IServer server) {
        this.server = server;
    }

    public Object navigation() {
       return NewRouterManage.getInstance().navigation(null, this, -1);
    }

    public Object navigation(Context context, int requestCode) {
        return NewRouterManage.getInstance().navigation(context, this, requestCode);
    }

//    public void navigation(Activity context, int requestCode) {
//        Intent intent = new Intent();
//        intent.setComponent(new ComponentName(context.getPackageName(), activityName));
//        intent.putExtras(mBundle);
//        context.startActivityForResult(intent, requestCode);
//    }


}
