package com.code.copyarouter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.code.route_annotation.NewRouter;

@NewRouter(path = "/main/got")
public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
    }
}
