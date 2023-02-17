package com.example.yolov5tfliteandroid;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;

public class Settings extends AppCompatActivity {
    private Activity activity;
    private String m_Text = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings2);


        Switch switch1 = findViewById(R.id.switch1);
        switch1.bringToFront();
        Switch switch2 = findViewById(R.id.switch2);
        switch2.bringToFront();
        Switch switch3 = findViewById(R.id.switch3);
        Switch switch4 = findViewById(R.id.switch4);
        Switch switch6 = findViewById(R.id.topres);
        Switch switch7= findViewById(R.id.vibrate);



        RadioGroup radioGroup = findViewById(R.id.RadioGroup);
        int selectedId;
        RadioButton camfront = findViewById(R.id.camfront);

        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        switch1.setChecked(prefs.getBoolean("switch1", false));
        switch2.setChecked(prefs.getBoolean("switch2", false));
        switch3.setChecked(prefs.getBoolean("switch3", false));
        switch4.setChecked(prefs.getBoolean("switch4", false));
        switch6.setChecked(prefs.getBoolean("switch6", false));
        switch7.setChecked(prefs.getBoolean("switch7", false));

        String predef1 =  prefs.getString("predef1", "");

        String selectedValue = prefs.getString(getString(R.string.saved_radio_button_value), "");
        if(!selectedValue.isEmpty()) {
            // loop through all the radio buttons in the radio group
            for (int i = 0; i < radioGroup.getChildCount(); i++) {
                RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i);
                // compare the value of the radio button with the value stored in SharedPreferences
                if (radioButton.getText().toString().equals(selectedValue)) {
                    // set the radio button as selected
                    radioGroup.check(radioButton.getId());
                    break;
                }
            }
        }


        Button perform = findViewById(R.id.now_perform);

        switch1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences prefs1 = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs1.edit();
            editor.putBoolean("switch1", isChecked);
            editor.apply();
        });
        switch2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences prefs1 = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs1.edit();
            editor.putBoolean("switch2", isChecked);
            editor.apply();
        });
        switch3.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences prefs1 = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs1.edit();
            editor.putBoolean("switch3", isChecked);
            editor.apply();
        });
        switch4.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences prefs1 = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs1.edit();
            editor.putBoolean("switch4", isChecked);
            editor.apply();
        });
        switch6.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences prefs1 = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs1.edit();
            editor.putBoolean("switch6", isChecked);
            editor.apply();
        });
        switch7.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences prefs1 = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs1.edit();
            editor.putBoolean("switch7", isChecked);
            editor.apply();
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                int selectedId = radioGroup.getCheckedRadioButtonId();
                RadioButton radioButton = findViewById(selectedId);
                String selectedValue = radioButton.getText().toString();
                prefs.edit().putString(getString(R.string.saved_radio_button_value), selectedValue).apply();

            }
        });


        Button predef = findViewById(R.id.button2);
        predef.setOnClickListener(v-> {

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Enter 10 cards");

// Set up the input
                    final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    builder.setView(input);
                    //input.setText(predef1);

// Set up the buttons
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            m_Text = input.getText().toString();
                            SharedPreferences prefs1 = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs1.edit();
                            editor.putString("predef1", m_Text);
                            editor.putString("predef", m_Text);
                            editor.apply();

                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();
                    input.setText(predef1);


                }
    );


        perform.setOnClickListener(v -> {


            Intent intent = new Intent(Settings.this, MainActivity.class);
            intent.putExtra("trick", "daytrick");

            if(m_Text != null && !m_Text.trim().isEmpty()) {

                SharedPreferences prefs1 = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs1.edit();
                editor.putString("predef", m_Text);
                editor.apply();

            }else {
                SharedPreferences prefs1 = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs1.edit();
                editor.putString("predef", "zero");
                editor.apply();
               }


            startActivity(intent);

        });


    }
}