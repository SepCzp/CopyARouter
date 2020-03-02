package com.code.copyarouter;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.code.module1.LoginServerImpl;
import com.code.route_annotation.NewRouter;
import com.code.router_code.newrouter.NewRouterManage;
import com.newrouter.Router_Group_main;

@NewRouter(path = "/main/main8")
public class MainActivity extends AppCompatActivity {

    private LoginServerImpl loginServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loginServer = (LoginServerImpl) NewRouterManage.getInstance().build("/module/loginInfo").navigation();
        Toast.makeText(this, loginServer.isLogin() ? "登录了" : "没有登录", Toast.LENGTH_SHORT).show();
    }

    public void go(View view) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(getPackageName(), "com.code.module1.MainActivity1"));
        startActivity(intent);
//        NewRouterManage.getInstance().build("/module/main1").withInt("nb", 1).navigation(this, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
