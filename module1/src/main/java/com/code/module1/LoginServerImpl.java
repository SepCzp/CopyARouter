package com.code.module1;

import com.code.route_annotation.NewRouter;
import com.code.router_code.template.LoginServer;

/**
 * @Author: czp
 * @Description
 * @CreateDate: 2020/1/4 15:15
 */
@NewRouter(path = "/module/loginInfo")
public class LoginServerImpl implements LoginServer {
    @Override
    public boolean isLogin() {
        return true;
    }
}
