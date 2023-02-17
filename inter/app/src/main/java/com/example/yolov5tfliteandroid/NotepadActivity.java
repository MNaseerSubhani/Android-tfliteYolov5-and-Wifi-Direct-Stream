package com.example.yolov5tfliteandroid;



import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yolov5tfliteandroid.databinding.ActivityNotepadBinding;

import java.util.ArrayList;

import java.util.Arrays;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import android.os.Handler;
import android.widget.Toast;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class NotepadActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler(Looper.myLooper());
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

                // Note that some of these constants are new as of API 16 (Jelly Bean)
                // and API 19 (KitKat). It is safe to use them, as they are inlined
                // at compile-time and do nothing on earlier devices.
                mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        }
    };
    private View mControlsView;
    TextToSpeech tts;

    int posiFinal = 0;
    String cartaypos = "";

    String name1;
    String name2;

    String card1 = "";

    String card2 = "";

    String trick = "";

    String now_id = "";
    ArrayList<String> strList = new ArrayList<>();


    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private boolean switch6;
    private String[] stringArray1;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (AUTO_HIDE) {
                        delayedHide(AUTO_HIDE_DELAY_MILLIS);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    view.performClick();
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    public void displayToast(View view) {

        if (trick.toLowerCase().trim().equals(("interceptor").toLowerCase().trim())) {
            Intent intent = new Intent(NotepadActivity.this, InterSettings.class);

            startActivity(intent);
            finish();
        }
        else if (trick.toLowerCase().trim().equals(("daytrick").toLowerCase().trim())) {
            Intent intent = new Intent(NotepadActivity.this, Settings.class);

            startActivity(intent);
            finish();
        }
        else {
            Intent intent = new Intent(NotepadActivity.this, nowSettings.class);

            startActivity(intent);
            finish();
        }



    }
    private ActivityNotepadBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean noti = prefs.getBoolean("switch2", false);
        boolean voice = prefs.getBoolean("switch3", false);
        boolean hideres = prefs.getBoolean("switch4", false);
        boolean topres = prefs.getBoolean("topres", false);
        boolean vibrations = prefs.getBoolean("switch7", false);
        boolean switch5State = prefs.getBoolean("switch5", false); //now notification
        name1 = prefs.getString("name1", "");
        name2 = prefs.getString("name2", "");
        now_id = prefs.getString("nowid", "");
        int cards = prefs.getInt("cards", 0); //created

        String predef =  prefs.getString("predef", ""); //predefined cards

        String strSplit = Arrays.toString(predef.split("-"));
        strList = new ArrayList<>(
                Collections.singletonList(strSplit));

        Intent intent = getIntent();
        card1 = intent.getStringExtra("card1");
        card2 = intent.getStringExtra("card2");
        trick = intent.getStringExtra("trick");


        Vibrator v = (Vibrator) getSystemService(this.VIBRATOR_SERVICE);

        binding = ActivityNotepadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mVisible = true;
        mControlsView = binding.fullscreenContentControls;
        mContentView = binding.fullscreenContent;
        TextView ver3 = findViewById(R.id.ver3);
        if(trick.toLowerCase().trim().equals(("interceptor").toLowerCase().trim())) {

          /*  WebView webView = new WebView(this);
            setContentView(webView);

            webView.loadUrl("https://videonowshare.com/inter/sortbarc.php?email=zapa@gmail.com&nom1=" + name1 + "&nom2=" + name2 + "&data=" + card1 + card2);

*/
            String url = "https://videonowshare.com/inter/sortbarc.php?email=zapa@gmail.com&nom1=" + name1 + "&nom2=" + name2 + "&data=" + card1 + card2;
            Intent intent2 = new Intent(Intent.ACTION_VIEW);
            intent2.setData(Uri.parse(url));
            startActivity(intent2);

        } else if(trick.toLowerCase().trim().equals(("daytrick").toLowerCase().trim())) {
            if(predef =="zero") {
                ArrayList<String> array1 = getIntent().getStringArrayListExtra("recognizedCards");
               stringArray1 = array1.toArray(new String[array1.size()]);
            }

            ArrayList<String> array2 = getIntent().getStringArrayListExtra("recognizedCards2");
            TextView ver = findViewById(R.id.ver);
            TextView ver2 = findViewById(R.id.ver2);
           // ver2.setText(predef);ver3.setText(predef);

            List<String> list = new ArrayList<String>(Arrays.asList(predef.split("-")));
            if (predef != "zero")
            {
               stringArray1 = list.toArray(new String[list.size()]);

            }

            String[] stringArray2 = array2.toArray(new String[array2.size()]);
            int cuantas = 10;//stringArray1.length;

// This algorithm shows what card was changed place and it's new position

            for (int i = 0; i < cuantas; i++) {
                if (!stringArray1[i].equals(stringArray2[i])) {
                    int first_position = Arrays.asList(stringArray1).indexOf(stringArray2[i]) + 1;
                    int second_position = Arrays.asList(stringArray1).indexOf(stringArray1[i]) + 1;
                    int result = first_position - second_position;
                    if (result != 1 && result != -1) {
                        int index = Arrays.asList(stringArray2).indexOf(stringArray2[i]);
                        posiFinal = index + 1;

                        if (topres) {
                            ver3.setText(stringArray2[i] + " " + posiFinal);
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                public void run() {
                                    ver3.setVisibility(View.GONE);
                                }
                            }, 15000);
                        } else {
                            ver.setText(stringArray2[i] + " " + posiFinal);
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                public void run() {
                                    ver.setVisibility(View.GONE);
                                }
                            }, 15000);
                        }
                        cartaypos = (stringArray2[i] + " " + posiFinal);

                    }


                }
            }


            if (voice) {
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    int i = 0;

                    @Override
                    public void run() {
                        if (i < 3) {
                            speak(String.valueOf(posiFinal));
                            i++;
                        } else {
                            timer.cancel();
                        }
                    }
                }, 0, 2000);
            }
////////////////////////////////////////////////////////////////////////////////////////////////


            if (noti) {
                sendNotification(this.getApplicationContext(), cartaypos);
            }

            //Vibrations Option
        /* if selected from settings screen, will vibrate to indicate the closest position to count to find the card (from top, or bottom)
        // if we have 10 cards and the switched one is the 5th, then it will be 5, and five 500 milliseconds vibrations is ok.
        // if the switched card is number 8, then one long vibration will indicate count from bottom, and then 1 vibration more, that is, 2 vibs
        // and count from bottom.
        // So no more than 5 vibrations are need to convey info to find the card.



*/
            if (vibrations) {
                try {
                    Thread.sleep(5000); //wait 5 seconds for giving vibration result
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (posiFinal == 1) {
                    long[] pattern = {0, 300, 500};
                    v.vibrate(pattern, -1);
                }
                if (posiFinal == 2) {
                    long[] pattern = {0, 300, 500, 300, 500};
                    v.vibrate(pattern, -1);
                }
                if (posiFinal == 3) {
                    long[] pattern = {0, 300, 500, 300, 500, 300, 500};
                    v.vibrate(pattern, -1);
                }
                if (posiFinal == 4) {
                    long[] pattern = {0, 300, 500, 300, 500, 300, 500, 300, 500};
                    v.vibrate(pattern, -1);
                }
                if (posiFinal == 5) {
                    long[] pattern = {0, 700, 500};
                    v.vibrate(pattern, -1);
                }
                if (posiFinal == 6) {
                    long[] pattern = {0, 700, 500, 300, 500};
                    v.vibrate(pattern, -1);
                }
                if (posiFinal == 7) {
                    long[] pattern = {0, 700, 500, 300, 500, 300, 500};
                    v.vibrate(pattern, -1);
                }
                if (posiFinal == 8) {
                    long[] pattern = {0, 700, 500, 300, 500, 300, 500, 300, 500};
                    v.vibrate(pattern, -1);
                }
                if (posiFinal == 9) {
                    long[] pattern = {0, 700, 500, 300, 500, 300, 500, 300, 500, 300, 500};
                    v.vibrate(pattern, -1);
                }


            }



        }
//if(trick.toLowerCase().trim().equals(("now").toLowerCase().trim()))
        else  { //now trick
            ArrayList<String> array1 = getIntent().getStringArrayListExtra("recognizedCards");
            String[] stringArray1 = array1.toArray(new String[array1.size()]);

            StringBuilder sb = new StringBuilder();
            for (String str : stringArray1) {
                sb.append(str).append("");
            }
           // String str = sb.deleteCharAt(sb.length() - 1).toString();
            String str = sb.toString();
            sendPostRequest("https://videonowshare.com/inter/sortbarc2now.php", "data="+str+"&u="+now_id+"&email=zapa@gmail.com");
            if(switch5State) {
                sendNotification(this.getApplicationContext(), "SENT");
            }
           // Toast.makeText(NotepadActivity.this, "carta: " + array1, Toast.LENGTH_LONG).show();
        }
    }

    //double click returns to settings

    public void sendNotification(Context context, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel("default", "Default Channel", NotificationManager.IMPORTANCE_DEFAULT);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "default")
                .setContentTitle("")
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_notification_silent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(1, notificationBuilder.build());

    }



    public void speak(final String text) {
        if (tts == null) {
            tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        int result = tts.setLanguage(new Locale("es", "ES"));
                        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            // Language data is missing or the language is not supported.
                        } else {
                            // The TTS engine has been successfully initialized.
                            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                        }
                    } else {
                        // Initialization failed.
                    }
                }
            });
        } else {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void sendPostRequest(String data, String postData) {
        new Thread(() -> {
            try {

                OkHttpClient client = new OkHttpClient();
                MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
                RequestBody body = RequestBody.create(mediaType, postData);
                Request request = new Request.Builder()
                        .url(data)
                        .post(body)
                        .addHeader("Content-Type", "application/x-www-form-urlencoded")
                        .build();

                Response response = client.newCall(request).execute();
                int responseCode = response.code();
                // post the responseCode back to main thread
                //handler.post(() -> System.out.println("Response Code: " + responseCode));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private void show() {
        // Show the system bar

            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}