package com.example.yolov5tfliteandroid;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yolov5tfliteandroid.analysis.FullImageAnalyse;
import com.example.yolov5tfliteandroid.detector.Yolov5TFLiteDetector;
import com.example.yolov5tfliteandroid.utils.ImageProcess;
import com.example.yolov5tfliteandroid.utils.Recognition;
import com.example.yolov5tfliteandroid.utils.WifiDirectBroadcastReceiver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class Wifi_Direct extends AppCompatActivity {


    public Button btnOnOFF, btnDiscover;
    public TextView status, result;

    WifiManager wifiManager;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;

    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;


    ServerClass serverClass;
    ClientClass clientClass;
    public  SendReceive sendReceive;

    private ImageView img_view;

    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    static  final  int MESSAGE_READ=1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_wifi_direct);

        initialWork();
        ex_Listner();

        initModel("yolov5s");
    }



    private void initialWork() {


        btnOnOFF = findViewById(R.id.onOff);
        btnDiscover = findViewById(R.id.discover);

        status = findViewById(R.id.status);
        result = findViewById(R.id.result);

        img_view = findViewById(R.id.imge_vew);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this,getMainLooper(), null);

        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, this);
        mIntentFilter= new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        Predict_Init myThread = new Predict_Init();
        myThread.start();

    }

    private void ex_Listner() {
        if(wifiManager.isWifiEnabled()){
            btnOnOFF.setText("WIFI OFF");

        }
        else{
            btnOnOFF.setText("WIFI ON");
        }

        btnOnOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if(wifiManager.isWifiEnabled()){
                    deletePersistentGroups();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        Intent panelIntent = new Intent(android.provider.Settings.Panel.ACTION_INTERNET_CONNECTIVITY);
                        startActivity(panelIntent);

                    } else {
                        wifiManager.setWifiEnabled(false);
                        btnOnOFF.setText("WIFI ON");
                    }

                }
                else{
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        Intent panelIntent = new Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY);
                        startActivity(panelIntent);

                    } else {
                        wifiManager.setWifiEnabled(true);
                        btnOnOFF.setText("WIFI OFF");
                    }

                }
            }
        });


        btnDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        status.setText("Discovery Started");
                    }
                    @Override
                    public void onFailure(int reason) {
                        status.setText("Discovery Started Failed");


                        String err=new String();
                        if(reason==WifiP2pManager.BUSY) err="BUSY";
                        if(reason==WifiP2pManager.ERROR)err="ERROR";
                        if(reason==WifiP2pManager.P2P_UNSUPPORTED) err="P2P_UNSUPPORTED";

                        Toast.makeText(Wifi_Direct.this, err, Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });


    }

    private void deletePersistentGroups(){
        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(Wifi_Direct.this, "Group removed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int i) {

            }
        });
    }

    public WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupOwnerAdress = wifiP2pInfo.groupOwnerAddress;

            if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner){
                status.setText("Host");
                serverClass= new ServerClass();
                serverClass.start();




            }
            else if(wifiP2pInfo.groupFormed){
                status.setText("Client");
                clientClass = new ClientClass(groupOwnerAdress);
                clientClass.start();

            }

        }
    };
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        registerReceiver(mReceiver, mIntentFilter);
    }





    public  class ServerClass extends  Thread{
        Socket socket;
        ServerSocket serverSocket;

        @Override
        public void run() {

            try{
                serverSocket = new ServerSocket(8888);

                socket = serverSocket.accept();
                sendReceive = new SendReceive(socket);
                sendReceive.start();
                Log.i("sendReceive", "Activated");
            }catch (IOException e){
                e.printStackTrace();
            }

        }
    }



    public class SendReceive extends Thread {


        private DataInputStream dataInputStream;
        int length;
//        byte[] Bbytes;
        public  SendReceive(Socket skt){
            socket = skt;

            try{
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
                dataInputStream=new DataInputStream(inputStream);




            }catch(IOException e){

                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            byte[] Bbytes = new byte[1];

            int bytes = 0;

            int count=0;
            while(socket !=null){

                try {


                    long start = System.currentTimeMillis();

                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );

                    length = dataInputStream.readInt();

                    while (true) {   //
                        bytes = inputStream.read(Bbytes);
                        outputStream.write(Bbytes,0,Bbytes.length);
                        if (bytes == -1 ){
                            break;
                        }
                        count+=bytes;

                        if(count==length){
                            break;
                        }
                    }

                    byte[] realbyte= outputStream.toByteArray();

                    if(count!=0) {
                        handler.obtainMessage(MESSAGE_READ, count, -1, realbyte).sendToTarget();
                    }

                    count=0;

                    long end = System.currentTimeMillis();

                    long costTime = (end - start);
//                    Log.i("time", String.valueOf(costTime));

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
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

    private Yolov5TFLiteDetector yolov5TFLiteDetector;
    ImageProcess imageProcess = new ImageProcess();
    public String label="--";


    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case MESSAGE_READ:
                    byte[] readBuff = (byte[])  msg.obj;

                    Bitmap bitmap = BitmapFactory.decodeByteArray(readBuff, 0, msg.arg1);


                    if(bitmap!=null) {

                        img_view.setImageBitmap(bitmap);
                        main_bitmap=bitmap;
                        result.setText(String.valueOf(label));

                    }

                    break;
            }
            return true;
        }
    });



    public Bitmap main_bitmap=null;

    public  class Predict_Init extends  Thread{
        @Override
        public void run() {
            // Code to run on the new thread

            while(true) {
                if (main_bitmap != null) {
                    long start = System.currentTimeMillis();
                    Matrix previewToModelTransform =
                            imageProcess.getTransformationMatrix(
                                    main_bitmap.getWidth(), main_bitmap.getHeight(),
                                    yolov5TFLiteDetector.getInputSize().getWidth(),
                                    yolov5TFLiteDetector.getInputSize().getHeight(),
                                    0, false);
                    Bitmap modelInputBitmap = Bitmap.createBitmap(main_bitmap, 0, 0,
                            main_bitmap.getWidth(), main_bitmap.getHeight(),
                            previewToModelTransform, false);

                    Matrix modelToPreviewTransform = new Matrix();
                    previewToModelTransform.invert(modelToPreviewTransform);

                    ArrayList<Recognition> recognitions = yolov5TFLiteDetector.detect(modelInputBitmap);

                    for (Recognition res : recognitions) {
                        RectF location = res.getLocation();
                        label = res.getLabelName();
                        break;
                    }


                    long end = System.currentTimeMillis();
                    long costTime = (end - start);


                    Log.i("Predict time", String.valueOf(costTime));
                    Log.i("Predict", String.valueOf(label));

                }
            }


        }

    }

    public class ClientClass extends Thread{
        Socket socket;
        String hostAdd;


        public ClientClass(InetAddress hostAdress){
            hostAdd = hostAdress.getHostAddress();
            socket = new Socket();
        }

        @Override
        public void run() {
            try{
                socket.connect(new InetSocketAddress(hostAdd, 8888), 500);
                sendReceive = new SendReceive(socket);
                sendReceive.start();
                Log.i("sendReceive", "Activated");
            }catch (IOException e){
                e.printStackTrace();
            }

        }

    }
}