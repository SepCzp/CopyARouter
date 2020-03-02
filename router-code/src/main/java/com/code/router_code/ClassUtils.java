package com.code.router_code;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import com.code.router_code.thread.DefaultPoolExecutor;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;

import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexFile;

/**
 * @Author: czp
 * @Description
 * @CreateDate: 2019/12/31 16:37
 */
public class ClassUtils {
    //通过BaseDexClassLoader反射获取app所有的DexFile
    private static List<DexFile> getDexFiles(Context context) throws IOException {
        List<DexFile> dexFiles = new ArrayList<>();
        BaseDexClassLoader loader = (BaseDexClassLoader) context.getClassLoader();
        try {
            Field pathListField = field("dalvik.system.BaseDexClassLoader","pathList");
            Object list = pathListField.get(loader);
            Field dexElementsField = field("dalvik.system.DexPathList","dexElements");
            Object[] dexElements = (Object[]) dexElementsField.get(list);
            Field dexFilefield = field("dalvik.system.DexPathList$Element","dexFile");
            for(Object dex:dexElements){
                DexFile dexFile = (DexFile) dexFilefield.get(dex);
                dexFiles.add(dexFile);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return dexFiles;
    }

    private static Field field(String clazz,String fieldName) throws ClassNotFoundException, NoSuchFieldException {
        Class cls = Class.forName(clazz);
        Field field = cls.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }
    /**
     * 通过指定包名，扫描包下面包含的所有的ClassName
     *
     * @param context     U know
     * @param packageName 包名
     * @return 所有class的集合
     */
    public static Set<String> getFileNameByPackageName(Context context, final String packageName) throws IOException {
        final Set<String> classNames = new HashSet<>();

        List<DexFile> dexFiles = getDexFiles(context);
        for (final DexFile dexfile : dexFiles) {
            Enumeration<String> dexEntries = dexfile.entries();
            while (dexEntries.hasMoreElements()) {
                String className = dexEntries.nextElement();
                if (className.startsWith(packageName)) {
                    classNames.add(className);
                }
            }
        }
        return classNames;
    }

    /**
     * 获得程序所有的apk(instant run会产生很多split apk)
     * @param context
     * @return apk名字
     * @throws PackageManager.NameNotFoundException
     */
    private static List<String> getSourcePaths(Context context) throws PackageManager.NameNotFoundException {
        ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
        List<String> sourcePaths = new ArrayList<>();
        sourcePaths.add(applicationInfo.sourceDir);
        //instant run
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (null != applicationInfo.splitSourceDirs) {
                sourcePaths.addAll(Arrays.asList(applicationInfo.splitSourceDirs));
            }
        }
        return sourcePaths;
    }

    /**
     * 得到路由表的类名
     * @param context
     * @param packageName
     * @return
     * @throws PackageManager.NameNotFoundException
     * @throws InterruptedException
     */
    public static Set<String> getFileNameByPackageName1(Application context, final String packageName)
            throws PackageManager.NameNotFoundException, InterruptedException {
        final Set<String> classNames = new HashSet<>();
        List<String> paths = getSourcePaths(context);
        //使用同步计数器判断均处理完成
        final CountDownLatch countDownLatch = new CountDownLatch(paths.size());
        ThreadPoolExecutor threadPoolExecutor = DefaultPoolExecutor.newDefaultPoolExecutor(paths.size());
        for (final String path : paths) {
            threadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    DexFile dexFile = null;
                    try {
                        //加载 apk中的dex 并遍历 获得所有包名为 {packageName} 的类
                        dexFile = new DexFile(path);
                        Enumeration<String> dexEntries = dexFile.entries();
                        while (dexEntries.hasMoreElements()) {
                            String className = dexEntries.nextElement();
                            if (!TextUtils.isEmpty(className) && className.startsWith(packageName)) {
                                classNames.add(className);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (null != dexFile) {
                            try {
                                dexFile.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        //释放一个
                        countDownLatch.countDown();
                    }
                }
            });
        }
        //等待执行完成
        countDownLatch.await();
        return classNames;
    }

}
