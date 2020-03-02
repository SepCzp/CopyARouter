package com.code.router_code.template;

import com.code.route_annotation.model.RouterMeta;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: czp
 * @Description 根据对应根目录下的路径
 * @CreateDate: 2020/1/4 10:31
 */
public interface IGroup extends IServer {

    /**
     * @param routers key 路径，Activity的信息
     */
    void loadInfo(HashMap<String, RouterMeta> routers);
}
