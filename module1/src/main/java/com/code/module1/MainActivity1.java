package com.code.module1;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.code.route_annotation.NewRouter;


@NewRouter(path = "/module/main1")
public class MainActivity1 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);
        Intent intent = getIntent();
        if (intent != null) {
            int nb = intent.getIntExtra("nb", 1);
            Toast.makeText(this, "" + nb, Toast.LENGTH_SHORT).show();
        }
        intent.putExtra("niua", "niu");
        setResult(Activity.RESULT_OK, intent);

    }

}
