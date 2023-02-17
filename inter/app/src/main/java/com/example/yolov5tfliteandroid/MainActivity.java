package com.example.yolov5tfliteandroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.yolov5tfliteandroid.analysis.FullImageAnalyse;
import com.example.yolov5tfliteandroid.detector.Yolov5TFLiteDetector;
import com.example.yolov5tfliteandroid.utils.CameraProcess;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {




    private PreviewView cameraPreviewMatch;
    private PreviewView cameraPreviewWrap;
    private ImageView boxLabelCanvas;
    private Switch camera_switch;
    private TextView result;
    private TextView res2;



    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private Yolov5TFLiteDetector yolov5TFLiteDetector;

    private CameraProcess cameraProcess = new CameraProcess();




    protected int getScreenOrientation() {
        switch (getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_270:
                return 270;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_90:
                return 90;
            default:
                return 0;
        }
    }


    private void initModel(String modelName) {
        try {
            this.yolov5TFLiteDetector = new Yolov5TFLiteDetector();
            this.yolov5TFLiteDetector.setModelFile(modelName);
//            this.yolov5TFLiteDetector.addNNApiDelegate();
            this.yolov5TFLiteDetector.addGPUDelegate();
            this.yolov5TFLiteDetector.initialModel(this);
            Log.i("model", "Success loading model" + this.yolov5TFLiteDetector.getModelFile());
        } catch (Exception e) {
            Log.e("image", "load model error: " + e.getMessage() + e.toString());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean switch1State = prefs.getBoolean("switch1", false);
        boolean switch2State = prefs.getBoolean("switch2", false);
        boolean switch3State = prefs.getBoolean("switch3", false);
        boolean switch4State = prefs.getBoolean("switch4", false);
        boolean switch5State = prefs.getBoolean("switch5", false);
        boolean switch7State = prefs.getBoolean("switch7", false);
        int cards = prefs.getInt("cards", 0); //how many cards to recognize for NOW sending



        String selectedValue = prefs.getString(getString(R.string.saved_radio_button_value), "");
        boolean is_interceptor;
        boolean is_now;

        String trick = getIntent().getStringExtra("trick");
        String predef =  prefs.getString("predef", "");
        is_interceptor = Objects.equals(trick, "interceptor");
        is_now =  Objects.equals(trick, "now");



        if(is_interceptor) {

            switch1State = false;
            switch2State = true;
            switch7State = false;
            selectedValue = "Back Camera";
            Toast.makeText(MainActivity.this, "Tap two times to go back", Toast.LENGTH_LONG).show();

        }

        if(is_now) {

            switch1State = false;
            switch2State = true;
            switch7State = false;
            selectedValue = "Back Camera";
            Toast.makeText(MainActivity.this, "Tap two times to go back", Toast.LENGTH_LONG).show();

        }


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        Vibrator p = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            p = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        }

        cameraPreviewMatch = findViewById(R.id.camera_preview_match);
        cameraPreviewMatch.setScaleType(PreviewView.ScaleType.FILL_START);

        cameraPreviewWrap = findViewById(R.id.camera_preview_wrap);


        boxLabelCanvas = findViewById(R.id.box_label_canvas);
        View fullscreenImage1 = findViewById(R.id.fullscreen_image);
        fullscreenImage1.setVisibility(View.GONE);

        camera_switch = findViewById(R.id.camera_switch);
        camera_switch.setVisibility(View.GONE);


        result = findViewById(R.id.result);
        res2 = findViewById(R.id.res2);

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);


        if (!cameraProcess.allPermissionsGranted(this)) {
            cameraProcess.requestPermissions(this);
        }

        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        Log.i("image", "rotation: " + rotation);

        cameraProcess.showCameraSupportSize(MainActivity.this);


        initModel("yolov5s");


//        Toast.makeText(MainActivity.this, "loading model: " + "yolov5", Toast.LENGTH_LONG).show();

        cameraPreviewMatch.removeAllViews();
        FullImageAnalyse fullImageAnalyse = new FullImageAnalyse(
                MainActivity.this,

                cameraPreviewWrap,
                boxLabelCanvas,
                rotation,
                result,
                res2,
                switch2State,
                switch3State,
                switch7State,
                is_interceptor,
                is_now,
                cards,
                switch5State,
                predef,
                p,
                yolov5TFLiteDetector);

        ImageView myImageView = findViewById(R.id.fullscreen_image); //double click returns to settings
        final long[] lastClickTime = {0};
        myImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - lastClickTime[0] < 1000) {
                    if(!is_interceptor) {

                        if (is_now) {
                            Intent intent = new Intent(MainActivity.this, nowSettings.class);

                            startActivity(intent);
                            finish();
                        } else {


                            Intent intent = new Intent(MainActivity.this, Settings.class);

                            startActivity(intent);
                            finish();
                        }
                    }

                    else {


                        Intent intent = new Intent(MainActivity.this, InterSettings.class);

                        startActivity(intent);
                        finish();

                    }

                }
                lastClickTime[0] = SystemClock.elapsedRealtime();
            }
        });

        cameraPreviewWrap.setOnClickListener(new View.OnClickListener() { //on double click returns to settings
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - lastClickTime[0] < 1000) {
                    if(!is_interceptor) {

                        if (is_now) {
                            Intent intent = new Intent(MainActivity.this, nowSettings.class);

                            startActivity(intent);
                            finish();
                        } else {


                            Intent intent = new Intent(MainActivity.this, Settings.class);

                            startActivity(intent);
                            finish();
                        }
                    }

                    else {


                            Intent intent = new Intent(MainActivity.this, InterSettings.class);

                            startActivity(intent);
                            finish();

                    }

                }
                lastClickTime[0] = SystemClock.elapsedRealtime();
            }
        });


        if(switch1State){

        }
        else {
            // switch practice is off
            if(switch4State){

                res2.setVisibility(View.GONE);
            }
            fullscreenImage1.setVisibility(View.VISIBLE);
            camera_switch.setVisibility(View.GONE);
        }


            if(selectedValue.toLowerCase().trim().equals(("Front Camera").toLowerCase().trim())) {

//                Toast.makeText(this, selectedValue, Toast.LENGTH_SHORT).show();
                result.setText(selectedValue); //says front camera
                cameraProcess.startCamera(MainActivity.this, fullImageAnalyse, cameraPreviewWrap, true);}
            else{
                result.setText(selectedValue); //says back camera
                cameraProcess.startCamera(MainActivity.this, fullImageAnalyse, cameraPreviewWrap, false);

        }


    }


}

