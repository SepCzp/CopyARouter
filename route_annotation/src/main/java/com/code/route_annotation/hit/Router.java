package com.code.route_annotation.hit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: czp
 * @Description
 * @CreateDate: 2019/12/31 14:22
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Router {

    /**
     * 路由的路径
     * @return
     */
    String path();

    /**
     * 将路由节点进行分组，可以实现动态加载
     * @return
     */
    String group() default "";
}
