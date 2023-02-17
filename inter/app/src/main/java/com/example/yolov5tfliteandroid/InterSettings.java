package com.example.yolov5tfliteandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import java.util.Objects;

public class InterSettings extends AppCompatActivity {
    private Activity activity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inter_settings);


        EditText nameone = findViewById(R.id.nowid);
        EditText nametwo = findViewById(R.id.name2);

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch svoice = findViewById(R.id.switch3);


        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        svoice.setChecked(prefs.getBoolean("switch3", false));

      nameone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    nameone.setText("");
                }
            }
        });

        nametwo.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    nametwo.setText("");
                }
            }
        });


        Button perform = findViewById(R.id.now_perform);

        svoice.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences prefs1 = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs1.edit();
            editor.putBoolean("switch3", isChecked);
            editor.apply();
        });




        perform.setOnClickListener(v -> {

            String n1 = Objects.requireNonNull(nameone.getText()).toString();
            String n2 = Objects.requireNonNull(nametwo.getText()).toString();

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("name1", n1);
            editor.putString("name2", n2);
            editor.apply();


            Intent intent = new Intent(InterSettings.this, MainActivity.class);
            intent.putExtra("trick", "interceptor");


            startActivity(intent);

        });


    }
}