package com.example.broadcast.analysis;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.view.PreviewView;

import com.example.broadcast.MainActivity;
import com.example.broadcast.utils.ImageProcess;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Base64;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class FullImageAnalyse implements ImageAnalysis.Analyzer{


    public static class Result{

        public Result(long costTime, Bitmap bitmap) {
            this.costTime = costTime;
            this.bitmap = bitmap;

        }
        long costTime;
        Bitmap bitmap;

    }


    PreviewView previewView;
    int rotation;
    ImageProcess imageProcess;
    ImageView imageView ;
    MainActivity.SendReceive sendReceive;
    private TextView result_;
    Context context;

    Handler handler;

    public FullImageAnalyse(Context context,
                            PreviewView previewView,
                            ImageView imageView,
                            int rotation,
                            MainActivity.SendReceive sendReceive,
                            Handler handler
                            ) {
        this.previewView = previewView;
        this.imageView = imageView;
        this.rotation = rotation;
        this.imageProcess = new ImageProcess();
        this.sendReceive = sendReceive;
        this.context = context;
        this.handler = handler;




    }

    @Override
    public void analyze(@NonNull ImageProxy image) {
        int previewHeight = previewView.getHeight();
        int previewWidth = previewView.getWidth();


        Observable.create( (ObservableEmitter<Result> emitter) -> {
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
//
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
//                    cropImageBitmap = Bitmap.createScaledBitmap(cropImageBitmap,previewWidth ,previewHeight, false);



//                    ByteArrayOutputStream byteArrayOutputStream = new
//                            ByteArrayOutputStream();
//
//                    cropImageBitmap.compress(Bitmap.CompressFormat.JPEG, 1, byteArrayOutputStream);//70
//                    byte[] array = byteArrayOutputStream.toByteArray();


                    if(sendReceive !=null) {
//                        SendImageClient sendImageClient = new SendImageClient();
                        Log.i("send", "SENDING FRAME");
//                        sendImageClient.execute(array);
//                        sendReceive.write(array);
                        sendReceive.write(cropImageBitmap);
                    }else{
                        Log.i("send", "NOT SENDING FRAME");
                    }

//                    int size = cropImageBitmap.getRowBytes() * cropImageBitmap.getHeight();
//                    ByteBuffer byteBuffer = ByteBuffer.allocate(size);
//                    cropImageBitmap.copyPixelsToBuffer(byteBuffer);
//                    byte[] array = byteBuffer.array();

//                    Log.i("wid", String.valueOf(width));
//                    Log.i("hei", String.valueOf(height));
//                    Log.i("name", cropImageBitmap.getConfig().name());
//
//
////
//



                    long end = System.currentTimeMillis();

                    long costTime = (end - start);
                    Log.i("time", String.valueOf(costTime));
                    image.close();
                    emitter.onNext(new Result(costTime, cropImageBitmap));
//            emitter.onNext(new Result(costTime, imageBitmap));


                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

                .subscribe((Result result) -> {

//                    im.setImageBitmap(result.bitmap);


                });

    }


//    public class SendImageClient extends AsyncTask<byte[], Void, Void> {
//        @Override
//        protected Void doInBackground(byte[]... voids) {
//
//            try {
//
//                sendReceive.write(voids[0]);
//
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//
////                        Log.i("sent", "Image sent");
//
//                    }
//                });
//
//
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//
//            return null;
//        }
//    }
}


