package com.code.router_code.template;

import java.util.HashMap;

/**
 * @Author: czp
 * @Description
 * @CreateDate: 2020/1/4 10:34
 */
public interface IRoot {

    /**
     * @param routes key 路由根路径，value 对应的Class类
     */
    void loadInfo(HashMap<String, Class<? extends IGroup>> routes);
}
