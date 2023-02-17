package com.example.yolov5tfliteandroid.analysis;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import androidx.core.app.NotificationCompat;


import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.view.PreviewView;

import com.example.yolov5tfliteandroid.NotepadActivity;
import com.example.yolov5tfliteandroid.R;
import com.example.yolov5tfliteandroid.detector.Yolov5TFLiteDetector;
import com.example.yolov5tfliteandroid.utils.ImageProcess;
import com.example.yolov5tfliteandroid.utils.Recognition;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class FullImageAnalyse implements ImageAnalysis.Analyzer {


    private final boolean voice;
    private final boolean vibrate;
    private final Boolean tricksta;
    private final boolean tricksta2;
    private final int cards;
    private final boolean noti1;
    private final String predef;


    public static class Result {

        public Result(long costTime, Bitmap bitmap, String label) {
            this.costTime = costTime;
            this.bitmap = bitmap;
            this.label = label;
        }

        long costTime;
        Bitmap bitmap;
        String label;
    }
    TextToSpeech tts;
    ImageView boxLabelCanvas;
    PreviewView previewView;
    int rotation;
    ImageProcess imageProcess;
    private TextView result_;
    private TextView res2;

    private boolean noti;

    private Vibrator p;
    String card1 = "";
    String card2  = "";


    private Yolov5TFLiteDetector yolov5TFLiteDetector;

    ArrayList<String> recognizedCards = new ArrayList<>();
    ArrayList<String> recognizedCards2 = new ArrayList<>();


    ArrayList<String> strList;



     int firstTimeCleanLabel = 0;

    private Activity activity;

    public FullImageAnalyse(Activity activity,
                            PreviewView previewView,
                            ImageView boxLabelCanvas,
                            int rotation,
                            TextView result_,
                            TextView res2, boolean switch2State, boolean switch3State, boolean switch7State, Boolean is_interceptor, boolean is_now, int cards, boolean switch5State, String predef, Vibrator p, Yolov5TFLiteDetector yolov5TFLiteDetector) {
        this.previewView = previewView;
        this.boxLabelCanvas = boxLabelCanvas;
        this.rotation = rotation;
        this.result_ = result_;
        this.res2 = res2;
        this.noti = switch2State;
        this.voice = switch3State;
        this.vibrate = switch7State;
        this.tricksta = is_interceptor;
        this.tricksta2 = is_now;
        this.cards = cards;
        this.noti1 = switch5State;
        this.predef = predef;
        this.imageProcess = new ImageProcess();
        this.yolov5TFLiteDetector = yolov5TFLiteDetector;
        this.activity = activity;
        this.p = p;
        this.activity.getApplicationContext();


    }

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

    String lastRecognizedCard = "";

    String lastRecognizedCard2 = "";
    int counter = 0;
    int counter2 = 0;

    int counter3 = 0;






   /* public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();


        AudioManager audioManager = (AudioManager) activity.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        switch (keyCode) {

            case KeyEvent.KEYCODE_VOLUME_UP:
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_RAISE,
                        AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                if (event.getAction() == KeyEvent.ACTION_UP) {
                   speak("nuevo comienzo");
                    Intent intent = new Intent(activity, NotepadActivity.class);

                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    activity.startActivity(intent);
                    break;

                    }
                }

        } */

    public void speak(final String text) {
        if (tts == null) {
            tts = new TextToSpeech(activity, new TextToSpeech.OnInitListener() {
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

    @SuppressLint({"CheckResult", "SuspiciousIndentation", "SetTextI18n"})
    @Override
    public void analyze(@NonNull ImageProxy image) {
        int previewHeight = previewView.getHeight();
        int previewWidth = previewView.getWidth();



        Observable.create((ObservableEmitter<Result> emitter) -> {
                    long start = System.currentTimeMillis();

                    byte[][] yuvBytes = new byte[3][];
                    ImageProxy.PlaneProxy[] planes = image.getPlanes();
                    int imageHeight = image.getHeight();
                    int imagewWidth = image.getWidth();

                    imageProcess.fillBytes(planes, yuvBytes);
                    int yRowStride = planes[0].getRowStride();
                    final int uvRowStride = planes[1].getRowStride();
                    final int uvPixelStride = planes[1].getPixelStride();

                    int[] rgbBytes = new int[imageHeight * imagewWidth];
                    imageProcess.YUV420ToARGB8888(
                            yuvBytes[0],
                            yuvBytes[1],
                            yuvBytes[2],
                            imagewWidth,
                            imageHeight,
                            yRowStride,
                            uvRowStride,
                            uvPixelStride,
                            rgbBytes);


                    Bitmap imageBitmap = Bitmap.createBitmap(imagewWidth, imageHeight, Bitmap.Config.ARGB_8888);
                    imageBitmap.setPixels(rgbBytes, 0, imagewWidth, 0, 0, imagewWidth, imageHeight);


                    double scale = Math.max(
                            previewHeight / (double) (rotation % 180 == 0 ? imagewWidth : imageHeight),
                            previewWidth / (double) (rotation % 180 == 0 ? imageHeight : imagewWidth)
                    );
                    Matrix fullScreenTransform = imageProcess.getTransformationMatrix(
                            imagewWidth, imageHeight,
                            (int) (scale * imageHeight), (int) (scale * imagewWidth),
                            rotation % 180 == 0 ? 90 : 0, false
                    );


                    Bitmap fullImageBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, imagewWidth, imageHeight, fullScreenTransform, false);

                    Bitmap cropImageBitmap = Bitmap.createBitmap(fullImageBitmap, 0, 0, previewWidth, previewHeight);


                    Matrix previewToModelTransform =
                            imageProcess.getTransformationMatrix(
                                    cropImageBitmap.getWidth(), cropImageBitmap.getHeight(),
                                    yolov5TFLiteDetector.getInputSize().getWidth(),
                                    yolov5TFLiteDetector.getInputSize().getHeight(),
                                    0, false);
                    Bitmap modelInputBitmap = Bitmap.createBitmap(cropImageBitmap, 0, 0,
                            cropImageBitmap.getWidth(), cropImageBitmap.getHeight(),
                            previewToModelTransform, false);

                    Matrix modelToPreviewTransform = new Matrix();
                    previewToModelTransform.invert(modelToPreviewTransform);

                    ArrayList<Recognition> recognitions = yolov5TFLiteDetector.detect(modelInputBitmap);
//            ArrayList<Recognition> recognitions = yolov5TFLiteDetector.detect(imageBitmap);

                    Bitmap emptyCropSizeBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
                    Canvas cropCanvas = new Canvas(emptyCropSizeBitmap);
//            Paint white = new Paint();
//            white.setColor(Color.WHITE);
//            white.setStyle(Paint.Style.FILL);
//            cropCanvas.drawRect(new RectF(0,0,previewWidth, previewHeight), white);

                    Paint boxPaint = new Paint();
                    boxPaint.setStrokeWidth(5);
                    boxPaint.setStyle(Paint.Style.STROKE);
                    boxPaint.setColor(Color.RED);

                    Paint textPain = new Paint();
                    textPain.setTextSize(50);
                    textPain.setColor(Color.RED);
                    textPain.setStyle(Paint.Style.FILL);
                    String label = null;
                    result_.setText("");
                    if (!Objects.equals(predef, "zero")) {


                        String strSplit = Arrays.toString(predef.split("-"));
                        strList = new ArrayList<>(
                                Collections.singletonList(strSplit));

                    }

                    for (Recognition res : recognitions) {
                        RectF location = res.getLocation();
                        label = res.getLabelName();
                        float confidence = res.getConfidence();
                        modelToPreviewTransform.mapRect(location);
                        if (!tricksta2) {
                                if (!tricksta) {
                                    if (Objects.equals(predef, "zero")) {
                                        //start of filling the two arrays ///////////////////////////////////////////////////////////////////////////////
                                        if (counter != 10) {
                                            if (!label.equals("") && !label.equals("10C") && !recognizedCards.contains(label) && !label.equals(lastRecognizedCard)) {
                                                recognizedCards.add(label);
                                                lastRecognizedCard = label;
                                                if (voice) {
                                                    speak("carta");
                                                }
                                                if (vibrate) {
                                                    long[] pattern = {0, 300, 500};
                                                    p.vibrate(pattern, -1);
                                                }
                                                counter++;
                                                res2.setText(label);
                                                if (noti) {
                                                    sendNotification(activity.getApplicationContext(), label);
                                                }
                                                if (counter == 10) {
                                                    label = "";
                                                    try {
                                                        Thread.sleep(1200); //wait so no error card is get
                                                    } catch (InterruptedException e) {
                                                        e.printStackTrace();
                                                    }
                                                    res2.setText("waiting for set 2");
                                                    if (voice) {
                                                        speak("segunda");
                                                    }
                                                }


                                            }
                                        } else {
                                            if (firstTimeCleanLabel == 0) {
                                                label = "";
                                                lastRecognizedCard = "";
                                                firstTimeCleanLabel = 1;

                                                try {
                                                    Thread.sleep(1000); //wait 5 seconds for entering the 2 set
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            if (!label.equals("") && !label.equals("10C") && !recognizedCards2.contains(label) && !label.equals(lastRecognizedCard2)) {
                                                recognizedCards2.add(label);
                                                lastRecognizedCard2 = label;
                                                if (voice) {
                                                    speak("carta");
                                                }
                                                if (vibrate) {
                                                    long[] pattern = {0, 300, 500};
                                                    p.vibrate(pattern, -1);
                                                }
                                                counter2++;
                                                res2.setText(label);
                                                if (noti) {
                                                    sendNotification(activity.getApplicationContext(), "SET 2: " + label);
                                                }

                                                if (counter2 == 10) {
                                                    Intent intent = new Intent(activity, NotepadActivity.class);
                                                    intent.putExtra("recognizedCards", recognizedCards);
                                                    intent.putExtra("recognizedCards2", recognizedCards2);
                                                    intent.putExtra("trick", "daytrick"); //so it does not open webview for interceptor

                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    activity.startActivity(intent);
                                                    break;
                                                }
                                            }


                                        }
                                    }

                                    else { //predefined list of cards

                                        if (!label.equals("") && !label.equals("10C") && !recognizedCards2.contains(label) && !label.equals(lastRecognizedCard2)) {
                                            recognizedCards2.add(label);
                                            lastRecognizedCard2 = label;
                                            if (voice) {
                                                speak("carta");
                                            }
                                            if (vibrate) {
                                                long[] pattern = {0, 300, 500};
                                                p.vibrate(pattern, -1);
                                            }
                                            counter2++;
                                            res2.setText(label);

                                            if (noti) {
                                                sendNotification(activity.getApplicationContext(), "SET 2: " + label);
                                            }

                                            if (counter2 == 10) {
                                                Intent intent = new Intent(activity, NotepadActivity.class);
                                               // intent.putExtra("recognizedCards", strList);
                                                intent.putExtra("recognizedCards2", recognizedCards2);
                                                intent.putExtra("trick", "daytrick"); //so it does not open webview for interceptor

                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                activity.startActivity(intent);
                                                break;
                                            }
                                        }


                                    }
                                }


                            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////


                            if (tricksta) {
                                if (!label.equals("") && card1 == "") {

                                    card1 = label;
                                    if (voice) {
                                        speak("carta uno");
                                    }
                                    sendNotification(activity.getApplicationContext(), label);


                                } else if (card1 != "" && !label.equals(card1) && card2 == "") {

                                    card2 = label;
                                    if (voice) {
                                        speak("carta dos");
                                    }
                                    sendNotification(activity.getApplicationContext(), label);


                                    if (card1.equals("10H")) {
                                        card1 = "1H";
                                    }
                                    if (card1.equals("10C")) {
                                        card1 = "1C";
                                    }
                                    if (card1.equals("10S")) {
                                        card1 = "1S";
                                    }
                                    if (card1.equals("10D")) {
                                        card1 = "1D";
                                    }
                                    if (card2.equals("10H")) {
                                        card2 = "1H";
                                    }
                                    if (card2.equals("10C")) {
                                        card2 = "1C";
                                    }
                                    if (card2.equals("10S")) {
                                        card2 = "1S";
                                    }
                                    if (card2.equals("10D")) {
                                        card2 = "1D";
                                    }

                                    //res2.setText(card1 + " " + card2);

                                    Intent intent = new Intent(activity, NotepadActivity.class);
                                    intent.putExtra("card1", card1);
                                    intent.putExtra("card2", card2);
                                    intent.putExtra("trick", "interceptor");
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    activity.startActivity(intent);
                                    break;

                                }
                            }

                            break;
                        }
                        else { //now! trick

                            if (counter3 != cards) {
                                if (!label.equals("") && !label.equals("10C") && !recognizedCards.contains(label) && !label.equals(lastRecognizedCard)) {
                                    recognizedCards.add(label);
                                    lastRecognizedCard = label;
                                    if (voice) {
                                        speak("carta");
                                    }
                                    if (vibrate) {
                                        long[] pattern = {0, 300, 500};
                                        p.vibrate(pattern, -1);
                                    }
                                    counter3++;
                                    res2.setText(label);
                                    if (noti1) {
                                       sendNotification(activity.getApplicationContext(), label);
                                    }
                                    if (counter3 == cards) {
                                        Intent intent = new Intent(activity, NotepadActivity.class);
                                        intent.putExtra("recognizedCards", recognizedCards);
                                        intent.putExtra("trick", "now"); //so it does not open webview for other effects
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        activity.startActivity(intent);
                                        break;
                                    }
                                }
                            }

                        }
                    }

                    long end = System.currentTimeMillis();
                    long costTime = (end - start);
                    image.close();
                    emitter.onNext(new Result(costTime, emptyCropSizeBitmap, label));
//            emitter.onNext(new Result(costTime, imageBitmap));

                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

                .subscribe((Result result) -> {
                    boxLabelCanvas.setImageBitmap(result.bitmap);


                  //  result_.setText(result.label);


                });

    }
}
