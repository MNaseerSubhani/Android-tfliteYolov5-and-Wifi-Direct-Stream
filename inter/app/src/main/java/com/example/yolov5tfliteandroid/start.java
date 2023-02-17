package com.example.yolov5tfliteandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class start extends AppCompatActivity {
    private View mContentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        Button inter = findViewById(R.id.now_perform);
        Button day = findViewById(R.id.daytrick);
        Button now = findViewById(R.id.now);

        Button wifi_direct = findViewById(R.id.wifi_direct);

        wifi_direct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(start.this, Wifi_Direct.class);
                startActivity(intent);
            }
        });



        inter.setOnClickListener(v -> {

            Intent intent = new Intent(start.this, InterSettings.class);
            intent.putExtra("trick", "interceptor");
            startActivity(intent);

        });

        day.setOnClickListener(v -> {

            Intent intent = new Intent(start.this, Settings.class);
            intent.putExtra("trick", "daytrick");
            startActivity(intent);

        });

        now.setOnClickListener(v -> {

            Intent intent = new Intent(start.this, nowSettings.class);
            intent.putExtra("trick", "now");
            startActivity(intent);

        });




    }
}