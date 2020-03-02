package com.code.router_code;

import android.app.Application;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @Author: czp
 * @Description
 * @CreateDate: 2019/12/31 16:32
 */
public class ARouter {

    private Map<String, String> routes = new HashMap<>();
    private static final ARouter aRouter = new ARouter();

    private ARouter() {

    }

    public static ARouter getInstance() {
        return aRouter;
    }

    public void init(Application context) {
        try {
            Set<String> fileNameByPackageName = ClassUtils.getFileNameByPackageName1(context, "com.code.copyarouter.routes");
            initRoutes(fileNameByPackageName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initRoutes(Set<String> names) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        for (String name : names) {
//            Class<?> aClass = Class.forName(name);
//            Object newInstance = aClass.newInstance();
//            if (newInstance instanceof IRouter) {
//                IRouter router = (IRouter) newInstance;
//                router.loadInto(routes);
//            }
            Log.d("initRoutes", "initRoutes: "+name);
        }
    }

    public PostCard build(String path) {
        String component = routes.get(path);
        if (component == null) throw new RuntimeException("could not find route with " + path);
        return new PostCard(component);
    }


}
