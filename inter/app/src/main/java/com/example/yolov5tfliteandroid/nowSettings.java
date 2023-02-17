package com.example.yolov5tfliteandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Objects;

public class nowSettings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now);

        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);


        View now_perform = findViewById(R.id.now_perform);
        NumberPicker np = findViewById(R.id.numberPicker);
        TextView nowid = findViewById(R.id.nowid);
        Switch switch3 = findViewById(R.id.switch3);
        Switch switch5 = findViewById(R.id.noti1);

        nowid.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    nowid.setText("");
                }
            }
        });

        String[] nums = new String[20];
        for(int i=1; i<=nums.length; i++)
            nums[i-1] = Integer.toString(i);

        np.setMinValue(1);
        np.setMaxValue(7);
        np.setWrapSelectorWheel(false);
        np.setDisplayedValues(nums);
        //np.setValue(1);
        np.setValue(prefs.getInt("cards",1));
        switch3.setChecked(prefs.getBoolean("switch3", false));
        switch5.setChecked(prefs.getBoolean("switch5", false));
        String nowid1 = prefs.getString("nowid", "");
        nowid.setText(nowid1);


        switch3.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences prefs1 = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs1.edit();
            editor.putBoolean("switch3", isChecked);
            editor.apply();
        });

        switch5.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences prefs1 = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs1.edit();
            editor.putBoolean("switch5", isChecked);
            editor.apply();
        });

        now_perform.setOnClickListener(v -> {

            String nowids = Objects.requireNonNull(nowid.getText()).toString();
            int nc = np.getValue();

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("nowid", nowids);
            editor.putInt("cards", nc);
            editor.apply();


            Intent intent = new Intent(nowSettings.this, MainActivity.class);
            intent.putExtra("trick", "now");


            startActivity(intent);

        });




    }
}