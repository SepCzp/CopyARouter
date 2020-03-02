package com.code.router_code;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.util.Stack;


/**
 * @Author: czp
 * @Description
 * @CreateDate: 2019/12/31 17:07
 */
public class BaseApp extends Application {

    private static BaseApp app;
    public Stack<Activity> store;


    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        store = new Stack<>();
        registerActivityLifecycleCallbacks(new SwitchBackgroundCallbacks());
    }

    private class SwitchBackgroundCallbacks implements Application.ActivityLifecycleCallbacks {

        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {
            store.push(activity);
        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            store.remove(activity);
        }
    }

    public static BaseApp getAppContext() {
        return app;
    }

    public Activity getCurActivity() {
        return store.lastElement();
    }
}
