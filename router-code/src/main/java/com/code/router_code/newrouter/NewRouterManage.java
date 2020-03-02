package com.code.router_code.newrouter;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;

import com.code.route_annotation.model.RouterMeta;
import com.code.router_code.ClassUtils;
import com.code.router_code.PostCard;
import com.code.router_code.template.IGroup;
import com.code.router_code.template.IRoot;
import com.code.router_code.template.IServer;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @Author: czp
 * @Description
 * @CreateDate: 2020/1/4 11:36
 */
public class NewRouterManage {

    private static final String TAG = NewRouterManage.class.getName();

    private Application mContext;
    private static final String CREATE_ROUTER_CLASS_PATH = "com.newrouter";
    private static final String ROUTER = "Router";
    private static final String ROOT = "Root";

    /**
     * 保存路由信息
     */
    public static final HashMap<String, RouterMeta> routers = new HashMap<>();

    /**
     * 保存路由信息
     */
    public static final HashMap<String, Class<? extends IGroup>> rootMap = new HashMap<>();

    /**
     * 保存服务信息
     */
    public static final HashMap<Class, IServer> serverMap = new HashMap<>();

    private final Handler handler;

    private NewRouterManage() {
        handler = new Handler(Looper.getMainLooper());
    }

    public static NewRouterManage getInstance() {
        return Instance.INSTANCE;
    }

    public void init(Application application) {
        mContext = application;
        //加载生成的class文件
        try {
            loadInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取Apk下的所有包并找出com.newrouter包名下的类
     * com.newrouter就是用javapoet生成Class类的包
     *
     * @throws InterruptedException
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws PackageManager.NameNotFoundException
     */
    private void loadInfo() throws InterruptedException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, PackageManager.NameNotFoundException {
        Set<String> aptClass = ClassUtils.getFileNameByPackageName1(mContext, CREATE_ROUTER_CLASS_PATH);
        for (String className : aptClass) {
            //找到com.newrouter包下的Router_Root类，并保存下来
            if (className.startsWith(CREATE_ROUTER_CLASS_PATH + "." + ROUTER + "_" + ROOT)) {
                ((IRoot) Class.forName(className).getConstructor().newInstance()).loadInfo(rootMap);
            }
            Log.d(TAG, "loadInfo: " + className);
        }

        for (Map.Entry<String, Class<? extends IGroup>> entry : rootMap.entrySet()) {
            Log.d(TAG, "Root映射表[: " + entry.getKey() + " : " + entry.getValue());
        }

    }

    public PostCard build(String path) {
        if (TextUtils.isEmpty(path)) {
            throw new NullPointerException("path is null");
        }
        //验证path有效性---之后
        return build(path, extractGroup(path));
    }

    private PostCard build(String path, String group) {
        if (TextUtils.isEmpty(path) || TextUtils.isEmpty(group)) {
            throw new NullPointerException("path and group is null");
        }
        return new PostCard(path, group);
    }

    public Object navigation(Context context, final PostCard card, final int requestCode) {
        try {
            prepare(card);
        } catch (Exception e) {
            e.printStackTrace();
        }
        switch (card.getType()) {
            case Activity:
                final Context currentContext = null == context ? mContext : context;
                final Intent intent = new Intent(mContext, card.getDestination());
                if (card.getFlags() != -1) {
                    intent.setFlags(card.getFlags());
                } else if (!(currentContext instanceof Activity)) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (requestCode != -1) {
                            ActivityCompat.startActivityForResult((Activity) currentContext, intent, requestCode, card.getmBundle());
                        } else {
                            ActivityCompat.startActivity(currentContext, intent, card.getmBundle());
                        }
                    }
                });
                break;
            case SERVICE:
                return card.getServer();
            default:
                break;
        }
        return null;
    }

    /**
     * 准备PostCard
     *
     * @param card
     */
    private void prepare(PostCard card) {
        RouterMeta routerMeta = routers.get(card.getPath());
        if (routerMeta == null) {
            Class<? extends IGroup> iGroupClass = rootMap.get(card.getGroup());
            if (iGroupClass == null) {
                throw new NullPointerException("没有找打分组" + card.getGroup() + "路径");
            }
            //实例化iGroupClass
            IGroup group = null;
            try {
                group = iGroupClass.getConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("路由分组映射表记录失败.", e);
            }
            //将该组的数据存在routers中
            group.loadInfo(routers);
            //清除rootMap缓存
            rootMap.remove(card.getGroup());
            //再次进入 else
            prepare(card);
        } else {
            //类 要跳转的activity 或IService实现类
            card.setDestination(routerMeta.getDestination());
            card.setType(routerMeta.getType());
            switch (routerMeta.getType()) {
                case SERVICE:
                    try {
                        Class<?> aClass = routerMeta.getDestination();
                        IServer iServer = serverMap.get(aClass);
                        if (iServer == null) {
                            iServer = (IServer) aClass.getConstructor().newInstance();
                            serverMap.put(aClass, iServer);
                        }
                        card.setServer(iServer);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }

    }

    /**
     * 获得组别
     *
     * @param path
     * @return
     */
    private String extractGroup(String path) {
        if (TextUtils.isEmpty(path) || !path.startsWith("/")) {
            throw new RuntimeException(path + " : 不能提取group.");
        }
        try {
            String defaultGroup = path.substring(1, path.indexOf("/", 1));
            if (TextUtils.isEmpty(defaultGroup)) {
                throw new RuntimeException(path + " : 不能提取group.");
            } else {
                return defaultGroup;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static class Instance {
        static NewRouterManage INSTANCE = new NewRouterManage();
    }
}
