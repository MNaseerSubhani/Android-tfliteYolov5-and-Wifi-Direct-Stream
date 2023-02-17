package com.example.broadcast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.broadcast.analysis.FullImageAnalyse;
import com.example.broadcast.utils.CameraProcess;
import com.example.broadcast.utils.WifiDirectBroadcastReceiver;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    public Button btnOnOFF, btnDiscover, btnStream;
    ListView listView;
    public TextView read_bbox, connection_status, status;
//    EditText writemsg;

    WifiManager wifiManager;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;

    BroadcastReceiver  mReceiver;
    IntentFilter mIntentFilter;


    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    String[] deviceNameArray;
    WifiP2pDevice[] devicesArray;



    ServerClass serverClass;
    ClientClass clientClass;
    public  SendReceive sendReceive;


    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    static  final  int MESSAGE_READ=1;


    ///Camera

    private PreviewView cameraPreviewMatch;
    private PreviewView cameraPreviewWrap;
    private ImageView img_view;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    private CameraProcess cameraProcess = new CameraProcess();




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main2);
        initialWork();
        ex_Listner();
    }



    private void initialWork(){


        //camera init
        cameraPreviewMatch = findViewById(R.id.camera_preview_match);
        cameraPreviewMatch.setScaleType(PreviewView.ScaleType.FILL_START);

        cameraPreviewWrap = findViewById(R.id.camera_preview_wrap);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        img_view = findViewById(R.id.imge_vew);



        btnOnOFF = findViewById(R.id.onOff);
        btnDiscover = findViewById(R.id.discover);
        btnStream = findViewById(R.id.stream);


        status = findViewById(R.id.readMsg);

        listView = findViewById(R.id.peerListView);
        connection_status = findViewById(R.id.connectionStatus);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this,getMainLooper(), null);


        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, this);
        mIntentFilter= new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
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
                        Intent panelIntent = new Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY);
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

        btnStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(sendReceive!=null) {   //sendReceive!=null
                    camera_run();
                }else{
                    Toast.makeText(MainActivity.this, "Not connected to any device", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        connection_status.setText("Discovery Started");



                    }

                    @Override
                    public void onFailure(int reason) {
                        connection_status.setText("Discovery Started Failed");
                      

                        String err=new String();
                        if(reason==WifiP2pManager.BUSY) err="BUSY";
                        if(reason==WifiP2pManager.ERROR)err="ERROR";
                        if(reason==WifiP2pManager.P2P_UNSUPPORTED) err="P2P_UNSUPPORTED";

                        Toast.makeText(MainActivity.this, err, Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final WifiP2pDevice device= devicesArray[i];
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;

                mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        status.setText("Connected");
                        Toast.makeText(MainActivity.this, "Connected to "+ device.deviceName, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int i) {
                        status.setText("Not Connected");
                        Toast.makeText(MainActivity.this, "Not Connected", Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });


    }

    private void deletePersistentGroups(){
      mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
          @Override
          public void onSuccess() {
              Toast.makeText(MainActivity.this, "Group removed", Toast.LENGTH_SHORT).show();
          }

          @Override
          public void onFailure(int i) {

          }
      });
    }

    FullImageAnalyse fullImageAnalyse;
    private void camera_run() {


        if (!cameraProcess.allPermissionsGranted(this)) {
            cameraProcess.requestPermissions(this);
        }

        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        Log.i("image", "rotation: " + rotation);

        cameraProcess.showCameraSupportSize(this);

        cameraPreviewMatch.removeAllViews();
        fullImageAnalyse = new FullImageAnalyse(
                this,
                cameraPreviewWrap,
                img_view,
                rotation,
                sendReceive,
                handler
        );

        cameraProcess.startCamera(this, fullImageAnalyse, cameraPreviewWrap, false);

    }





    ////////////////////////////////




    public WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
//            Toast.makeText(MainActivity.this, "peers", Toast.LENGTH_SHORT).show();
            if(!peerList.getDeviceList().equals(peers)){
                peers.clear();
                peers.addAll(peerList.getDeviceList());
                deviceNameArray = new String[peerList.getDeviceList().size()];
                devicesArray = new WifiP2pDevice[peerList.getDeviceList().size()];

                int index=0;
                for(WifiP2pDevice device:peerList.getDeviceList()){
                    deviceNameArray[index] = device.deviceName;
                    devicesArray[index] = device;
                    index++;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, deviceNameArray);
                listView.setAdapter(adapter);


            }

            if (peers.size()==0){
                Toast.makeText(MainActivity.this, "No Devices Found", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    };


    public WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupOwnerAdress = wifiP2pInfo.groupOwnerAddress;

            if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner){
                connection_status.setText("Host");
                serverClass= new ServerClass();
                serverClass.start();




            }
            else if(wifiP2pInfo.groupFormed){
                connection_status.setText("Client");
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


    boolean flag=false;
    public class SendReceive extends Thread {

        private InputStream inputStream;
        private OutputStream outputStream;
        private DataOutputStream dos;

        int length;
        byte[] Bbytes;
        public  SendReceive(Socket skt){
            socket = skt;

            try{

                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();




            }catch(IOException e){

                e.printStackTrace();
            }
        }


        public void  write(Bitmap bitmap){
            try{


                dos = new DataOutputStream(outputStream);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.WEBP, 10, stream);
                byte[] bitmapByteArray = stream.toByteArray();

                Log.i("arraysze", String.valueOf(bitmapByteArray.length));
                dos.writeInt(bitmapByteArray.length);
//                outputStream.writeInt(bitmapByteArray.length);
                outputStream.write(bitmapByteArray, 0, bitmapByteArray.length);
                outputStream.flush();

            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case MESSAGE_READ:
                    byte[] readBuff = (byte[])  msg.obj;

//                    Log.i("len", String.valueOf(msg.arg1));

//                    Bitmap.Config configBmp = Bitmap.Config.valueOf("ARGB_8888");
//                    Bitmap bitmap = Bitmap.createBitmap(720, 1386, configBmp);
//                    ByteBuffer buffer = ByteBuffer.wrap(readBuff);
//                    bitmap.copyPixelsFromBuffer(buffer);
//                     Bitmap bitmap = BitmapFactory.decodeByteArray(readBuff, 0, readBuff.length);
//                    Bitmap bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(readBuff));
                    //Create bitmap with width, height, and 4 bytes color (RGBA)
//                    Log.i("width", String.valueOf(bitmap.getWidth()));
//                    Log.i("height", String.valueOf(bitmap.getHeight()));
//                    img_view.setImageBitmap(bitmap);


//                    Image imgPro = new Image(readBuff,0,msg.arg1);
//                    String tempMsg = new String(readBuff, 0, msg.arg1);

                    break;
            }
            return true;
        }
    });

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
                Log.i("sendReceive", " Not Activated");
            }

        }

    }
}