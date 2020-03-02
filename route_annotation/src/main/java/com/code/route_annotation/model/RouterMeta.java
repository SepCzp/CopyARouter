package com.code.route_annotation.model;

import com.code.route_annotation.NewRouter;

import javax.lang.model.element.Element;

/**
 * @Author: czp
 * @Description
 * @CreateDate: 2020/1/3 9:42
 */
public class RouterMeta {
    public enum Type {
        Activity, SERVICE
    }

    private Type type;
    /**
     * 节点(Activity)
     */
    private Element element;
    /**
     * 注解使用的对象
     */
    private Class<?> destination;
    /**
     * 路由地址
     */
    private String path;
    /**
     * 路由组
     */
    private String group;

    public RouterMeta() {
    }

    public static RouterMeta build(Type type, Class<?> destination, String path, String
            group) {
        return new RouterMeta(type, null, destination, path, group);
    }

    public RouterMeta(Type type, NewRouter router, Element element) {
        this(type, element, null, router.path(), router.group());
    }

    public RouterMeta(Type type, Element element, Class<?> destination, String path, String group) {
        this.type = type;
        this.element = element;
        this.destination = destination;
        this.path = path;
        this.group = group;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public Class<?> getDestination() {
        return destination;
    }

    public void setDestination(Class<?> destination) {
        this.destination = destination;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
