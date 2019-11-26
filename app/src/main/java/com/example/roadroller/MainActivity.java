package com.example.roadroller;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,View.OnTouchListener {

    public byte[] receivePocket = new byte[20];
    //接受数据包 20字节
    public byte[] receiveNum = new byte[4];
    public byte[] receiveData = new byte[4];
    public byte flankData = (byte)0x00;
    public byte verticalSpeed = (byte) 0x00;
    public byte horizontalSpeed = 0x00;
    public byte purlingState= 0x00;
    public byte receiveFlag= 0x00;
    public byte[] coordinate = new byte[8];

    //发送数据包 14字节
    public final byte sendStart = (byte)0xAA;
    public byte[] sendNum = new byte[4];
    public byte[] sendData = new byte[4];
    public byte sendFlag;
    public final byte sendEnd =  (byte) 0xBB;

    private String TAG = "MainActivity";

    public TextView run_mode_text;
    public ImageView bleSignal_icon;
    public Button scram;
    public Button flash;
    public Button doubleFlash;
    public Button purling;
    public Button holdCar;
    public Button stop;
    public Button trumpet;
    public Button speedCut;
    public Button speedUp;
    public Button turtleOrRabbit;
    public Button snake;
    public Button smallSnake;
    public Button bigSnake;
    public Button frontSnake;
    public Button backSnake;
    public Button engineOpen;
    public Button engineClose;
    public Button backMid;
    public Button connect;
    public Button lock;
    public Button mode;
    private RockerView rockerView;

    private  Timer mTimer ;


    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private String mBluetoothDeviceAddress;
    private boolean isConnected = false;

    private AlertDialog alertDialog;
    private ListView bleListview;
    private List<String> bleName = new ArrayList<>();
    private List<String> bleAddress = new ArrayList<>();
    private ArrayAdapter<String> listViewAdapter;

    private int connectionState = STATE_DISCONNECTED;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    private int connectionSignal ;
    private static final int SIGNAL_0 = 0;
    private static final int SIGNAL_1 = 1;
    private static final int SIGNAL_2 = 2;
    private static final int SIGNAL_3 = 3;
    private static final int SIGNAL_4 = 4;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    //Nordic_UART_UUID
    private static final String UUID_HEART_RATE_MEASUREMENT = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    private static final String UUID_CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    private static final String UUID_WRITE_CHARACTERISTIC = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
    private static final String UUID_READ_CHARACTERISTIC = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
    //message
    private static final int MESSAGE_CONNECTED = 1;
    private static final int MESSAGE_DISCONNECTED = 2;
    private static final int MESSAGE_CLOSE_ENGINE = 3;
    //button data
    private static byte[] myData = {0x00,0x00,0x00,0x00};
    private static final byte[] SPEED_GEAR_UP_DATA = {0x01,0x00,0x00,0x00};//速度档加速1
    private static final byte[] SPEED_GEAR_CUT_DATA = {0x02,0x00,0x00,0x00};//速度档减速2
    private static final byte[] SNAKE_DATA = {0x04,0x00,0x00,0x00};//振动3
    private static final byte[] TRUMPET_DATA = {0x08,0x00,0x00,0x00};//喇叭4
    private static final byte[] PURLING_DATA = {0x10,0x00,0x00,0x00};//洒水5
    private static final byte[] TURTLE_OR_RABBIT_DATA = {0x20,0x00,0x00,0x00};//龟兔档6
    private static final byte[] SUB_DATA = {0x00,0x01,0x00,0x00}; //- 7
    private static final byte[] STOP_DATA = {0x00,0x02,0x00,0x00};//停车8
    private static final byte[] SNAKE_FRONT_DATA = {0x00,0x04,0x00,0x00};//前振9
    private static final byte[] SNAKE_BACK_DATA = {0x00,0x08,0x00,0x00};//后振10
    private static final byte[] FLASH_DATA = {0x00,0x10,0x00,0x00};//远关灯11
    private static final byte[] SPEED_UP_DATA = {0x00,0x20,0x00,0x00};//转速+ 12
    private static final byte[] ADD_DATA = {0x00,0x00,0x01,0x00};//+ 13
    private static final byte[] DIRECTION_GEAR_LEFT_DATA = {0x00,0x00,0x02,0x00};//方向档向左14
    private static final byte[] SNAKE_BIG_DATA = {0x00,0x00,0x04,0x00};//大振15
    private static final byte[] SNAKE_SMALL_DATA = {0x00,0x00,0x08,0x00};//小振16
    private static final byte[] HOLD_CAR_DATA = {0x00,0x00,0x10,0x00};//驻车17
    private static final byte[] SPEED_CUT_DATA = {0x00,0x00,0x20,0x00};//转速- 18
    private static final byte[] BACK_MID_DATA = {0x00,0x00,0x00,0x01};//回中19
    private static final byte[] DIRECTION_GEAR_RIGHT_DATA = {0x00,0x00,0x00,0x02};//方向档向右20
    private static final byte[] DOUBLE_FLASH_DATA = {0x00,0x00,0x00,0x04};//双闪21
    private static final byte[] ENGINE_OPEN_DATA = {0x00,0x00,0x00,0x08};//发动机开22
    private static final byte[] ENGINE_CLOSE_DATA = {0x00,0x00,0x00,0x10};//发动机关23
    private static final byte[] LOCK_DATA = {0x00,0x00,0x00,0x20};//锁定24
    private static final byte SCRAM_DATA = (byte)0x80;//急停 侧1
    private static final byte MODE_DATA = (byte)0x40;//切换模式 侧2

    //CRC校验
    static final short[] crc16tab = {
            (short)0x0000, (short)0x1021, (short)0x2042, (short)0x3063, (short)0x4084, (short)0x50a5,
            (short)0x60c6, (short)0x70e7, (short)0x8108, (short)0x9129, (short)0xa14a, (short)0xb16b,
            (short)0xc18c, (short)0xd1ad, (short)0xe1ce, (short)0xf1ef, (short)0x1231, (short)0x0210,
            (short)0x3273, (short)0x2252, (short)0x52b5, (short)0x4294, (short)0x72f7, (short)0x62d6,
            (short)0x9339, (short)0x8318, (short)0xb37b, (short)0xa35a, (short)0xd3bd, (short)0xc39c,
            (short)0xf3ff, (short)0xe3de, (short)0x2462, (short)0x3443, (short)0x0420, (short)0x1401,
            (short)0x64e6, (short)0x74c7, (short)0x44a4, (short)0x5485, (short)0xa56a, (short)0xb54b,
            (short)0x8528, (short)0x9509, (short)0xe5ee, (short)0xf5cf, (short)0xc5ac, (short)0xd58d,
            (short)0x3653, (short)0x2672, (short)0x1611, (short)0x0630, (short)0x76d7, (short)0x66f6,
            (short)0x5695, (short)0x46b4, (short)0xb75b, (short)0xa77a, (short)0x9719, (short)0x8738,
            (short)0xf7df, (short)0xe7fe, (short)0xd79d, (short)0xc7bc, (short)0x48c4, (short)0x58e5,
            (short)0x6886, (short)0x78a7, (short)0x0840, (short)0x1861, (short)0x2802, (short)0x3823,
            (short)0xc9cc, (short)0xd9ed, (short)0xe98e, (short)0xf9af, (short)0x8948, (short)0x9969,
            (short)0xa90a, (short)0xb92b, (short)0x5af5, (short)0x4ad4, (short)0x7ab7, (short)0x6a96,
            (short)0x1a71, (short)0x0a50, (short)0x3a33, (short)0x2a12, (short)0xdbfd, (short)0xcbdc,
            (short)0xfbbf, (short)0xeb9e, (short)0x9b79, (short)0x8b58, (short)0xbb3b, (short)0xab1a,
            (short)0x6ca6, (short)0x7c87, (short)0x4ce4, (short)0x5cc5, (short)0x2c22, (short)0x3c03,
            (short)0x0c60, (short)0x1c41, (short)0xedae, (short)0xfd8f, (short)0xcdec, (short)0xddcd,
            (short)0xad2a, (short)0xbd0b, (short)0x8d68, (short)0x9d49, (short)0x7e97, (short)0x6eb6,
            (short)0x5ed5, (short)0x4ef4, (short)0x3e13, (short)0x2e32, (short)0x1e51, (short)0x0e70,
            (short)0xff9f, (short)0xefbe, (short)0xdfdd, (short)0xcffc, (short)0xbf1b, (short)0xaf3a,
            (short)0x9f59, (short)0x8f78, (short)0x9188, (short)0x81a9, (short)0xb1ca, (short)0xa1eb,
            (short)0xd10c, (short)0xc12d, (short)0xf14e, (short)0xe16f, (short)0x1080, (short)0x00a1,
            (short)0x30c2, (short)0x20e3, (short)0x5004, (short)0x4025, (short)0x7046, (short)0x6067,
            (short)0x83b9, (short)0x9398, (short)0xa3fb, (short)0xb3da, (short)0xc33d, (short)0xd31c,
            (short)0xe37f, (short)0xf35e, (short)0x02b1, (short)0x1290, (short)0x22f3, (short)0x32d2,
            (short)0x4235, (short)0x5214, (short)0x6277, (short)0x7256, (short)0xb5ea, (short)0xa5cb,
            (short)0x95a8, (short)0x8589, (short)0xf56e, (short)0xe54f, (short)0xd52c, (short)0xc50d,
            (short)0x34e2, (short)0x24c3, (short)0x14a0, (short)0x0481, (short)0x7466, (short)0x6447,
            (short)0x5424, (short)0x4405, (short)0xa7db, (short)0xb7fa, (short)0x8799, (short)0x97b8,
            (short)0xe75f, (short)0xf77e, (short)0xc71d, (short)0xd73c, (short)0x26d3, (short)0x36f2,
            (short)0x0691, (short)0x16b0, (short)0x6657, (short)0x7676, (short)0x4615, (short)0x5634,
            (short)0xd94c, (short)0xc96d, (short)0xf90e, (short)0xe92f, (short)0x99c8, (short)0x89e9,
            (short)0xb98a, (short)0xa9ab, (short)0x5844, (short)0x4865, (short)0x7806, (short)0x6827,
            (short)0x18c0, (short)0x08e1, (short)0x3882, (short)0x28a3, (short)0xcb7d, (short)0xdb5c,
            (short)0xeb3f, (short)0xfb1e, (short)0x8bf9, (short)0x9bd8, (short)0xabbb, (short)0xbb9a,
            (short)0x4a75, (short)0x5a54, (short)0x6a37, (short)0x7a16, (short)0x0af1, (short)0x1ad0,
            (short)0x2ab3, (short)0x3a92, (short)0xfd2e, (short)0xed0f, (short)0xdd6c, (short)0xcd4d,
            (short)0xbdaa, (short)0xad8b, (short)0x9de8, (short)0x8dc9, (short)0x7c26, (short)0x6c07,
            (short)0x5c64, (short)0x4c45, (short)0x3ca2, (short)0x2c83, (short)0x1ce0, (short)0x0cc1,
            (short)0xef1f, (short)0xff3e, (short)0xcf5d, (short)0xdf7c, (short)0xaf9b, (short)0xbfba,
            (short)0x8fd9, (short)0x9ff8, (short)0x6e17, (short)0x7e36, (short)0x4e55, (short)0x5e74,
            (short)0x2e93, (short)0x3eb2, (short)0x0ed1, (short)0x1ef0
    };

    public int i = 0;
    private long time;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //全屏，隐藏系统状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        //注册广播接收器
        registerReceiver(mBatInfoReceiver, new IntentFilter(
                Intent.ACTION_BATTERY_CHANGED));
        registerReceiver(mGattUpdateReceiver,makeGattUpdateIntentFilter());
        registerPermissions();
        initBleListview();
        initBluetooth();


        run_mode_text = (TextView)findViewById(R.id.run_mode_text);
        bleSignal_icon = (ImageView)findViewById(R.id.bleSignal_icon);
        scram = (Button) findViewById(R.id.scram);
        flash = (Button) findViewById(R.id.flash);
        doubleFlash = (Button) findViewById(R.id.doubleFlash);
        purling = (Button) findViewById(R.id.purling);
        holdCar = (Button) findViewById(R.id.holdCar);
        stop = (Button) findViewById(R.id.stop);
        speedCut = (Button) findViewById(R.id.speedCut);
        speedUp = (Button) findViewById(R.id.speedUp);
        trumpet = (Button) findViewById(R.id.trumpet);
        turtleOrRabbit = (Button) findViewById(R.id.turtleOrRabbit);
        snake = (Button) findViewById(R.id.snake);
        smallSnake = findViewById(R.id.smallSnake);
        bigSnake = findViewById(R.id.bigSnake);
        frontSnake = findViewById(R.id.frontSnake);
        backSnake = findViewById(R.id.backSnake);
        engineOpen = (Button) findViewById(R.id.engineOpen);
        engineClose = findViewById(R.id.engineClose);
        backMid = (Button) findViewById(R.id.backMid);
        connect = (Button) findViewById(R.id.connect);
        rockerView = findViewById(R.id.rockerView);
        mode = findViewById(R.id.mode);
        lock = findViewById(R.id.lock);


        connect.setOnClickListener(this);

        scram.setOnTouchListener(this);
        flash.setOnTouchListener(this);
        doubleFlash.setOnTouchListener(this);
        purling.setOnTouchListener(this);
        holdCar.setOnTouchListener(this);
        stop.setOnTouchListener(this);
        speedCut.setOnTouchListener(this);
        speedUp.setOnTouchListener(this);
        trumpet.setOnTouchListener(this);
        turtleOrRabbit.setOnTouchListener(this);
        snake.setOnTouchListener(this);
        smallSnake.setOnTouchListener(this);
        bigSnake.setOnTouchListener(this);
        frontSnake.setOnTouchListener(this);
        backSnake.setOnTouchListener(this);
        backMid.setOnTouchListener(this);
        engineOpen.setOnTouchListener(this);
        engineClose.setOnTouchListener(this);
        mode.setOnTouchListener(this);
        lock.setOnTouchListener(this);
        //隐藏标题栏
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        final Handler rssiHandler=new Handler();

        rssiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                i++;
                if (isConnected){
                    if (i >= 10) {
                        i = 0;
                        getRssiVal();
                    }
                    onSpeedGear(isConnected);
                    onDirectionGear(isConnected);
                }
                writeCharacteristic(sender(myData),isConnected);
                rssiHandler.postDelayed(this,50);
            }
        },50);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.connect:
                if (connect.getText().toString().equals("连接")) {
                    //打开蓝牙
                    if (!mBluetoothAdapter.isEnabled()) {
                        Intent openBLE = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(openBLE, 1);
                    }
                    //开始进行扫描设备，传递进去一个监听
                    mBluetoothAdapter.startLeScan(mLeScanCallback);
                    alertDialog.show();
                }else if (connect.getText().toString().equals("断开")){
                    mBluetoothGatt.disconnect();
                }
                break;
            default:
                break;
        }
    }



    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN||
//                motionEvent.getAction() == MotionEvent.ACTION_MOVE||
                motionEvent.getAction() == MotionEvent.ACTION_POINTER_DOWN){

            switch (view.getId()){
                case R.id.mode:
                    mode.setPressed(true);
                    if (run_mode_text.getText().toString().equals("本地模式")){
                        run_mode_text.setText(R.string.telecontrol_mode);
                    }else{
                        run_mode_text.setText(R.string.local_mode);
                    }
                    flankData |= MODE_DATA;
                    break;
                case R.id.scram:
                    if (!scram.isActivated()){
                        scram.setActivated(true);
                        scram.setPressed(true);
                        flankData |= SCRAM_DATA;
                    }else{
                        flankData &= ~SCRAM_DATA;
                        scram.setPressed(false);
                        scram.setActivated(false);
                    }
                    break;
                case R.id.flash:
                    flash.setPressed(true);
                    myData[1] |= FLASH_DATA[1];
                    break;
                case R.id.doubleFlash:
                    doubleFlash.setPressed(true);
                    myData[3] |= DOUBLE_FLASH_DATA[3];
                    break;
                case R.id.holdCar:
                    holdCar.setPressed(true);
                    myData[2] |= HOLD_CAR_DATA[2];
                    break;
                case R.id.purling:
                    purling.setPressed(true);
                    myData[0] |= PURLING_DATA[0];
                    break;
                case R.id.stop:
                    stop.setPressed(true);
                    myData[1] |= STOP_DATA[1];
                    break;
                case R.id.speedCut:
                    speedCut.setPressed(true);
                    myData[1] |= SUB_DATA[1];
                    break;
                case R.id.speedUp:
                    speedUp.setPressed(true);
                    myData[2] |= ADD_DATA[2];
                    break;
                case R.id.backMid:
                    backMid.setPressed(true);
                    myData[3] |= BACK_MID_DATA[3];
                    break;
                case R.id.trumpet:
                    trumpet.setPressed(true);
                    myData[0] |= TRUMPET_DATA[0];
                    break;
                case R.id.turtleOrRabbit:
                    turtleOrRabbit.setPressed(true);
                    myData[0] |= TURTLE_OR_RABBIT_DATA[0];
                    break;
                case R.id.snake:
                    snake.setPressed(true);
                    myData[0] |= SNAKE_DATA[0];
                    break;
                case R.id.bigSnake:
                    bigSnake.setPressed(true);
                    myData[2] |= SNAKE_BIG_DATA[2];
                    break;
                case R.id.smallSnake:
                    smallSnake.setPressed(true);
                    myData[2] |= SNAKE_SMALL_DATA[2];
                    break;
                case R.id.frontSnake:
                    frontSnake.setPressed(true);
                    myData[1] |= SNAKE_FRONT_DATA[1];
                    break;
                case R.id.backSnake:
                    backSnake.setPressed(true);
                    myData[1] |= SNAKE_BACK_DATA[1];
                    break;
                case R.id.engineOpen:
                    engineOpen.setPressed(true);
                    myData[3] |= ENGINE_OPEN_DATA[3];
                    break;
                case R.id.engineClose:
                    engineClose.setPressed(true);
                    time = System.currentTimeMillis();
                    Log.d(TAG, "onTouch: ");
                    mTimer = new Timer();
                    mTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (System.currentTimeMillis() - time >= 2000){
                                sendMessage(MESSAGE_CLOSE_ENGINE);
                                myData[3] |= ENGINE_CLOSE_DATA[3];
                                Log.d(TAG, "run: close");
                                mTimer.cancel();
                            }
                        }
                    },2000);
                    break;
                case R.id.lock:
                    lock.setPressed(true);
                    myData[3] |= LOCK_DATA[3];
                    break;
                default:
                    break;
            }
        }else if(motionEvent.getAction() == MotionEvent.ACTION_UP ||
                motionEvent.getAction() == MotionEvent.ACTION_CANCEL||
                motionEvent.getAction() == MotionEvent.ACTION_POINTER_UP){

            switch (view.getId()){
                case R.id.mode:
                    mode.setPressed(false);
                    flankData &= ~MODE_DATA;
                    break;
                case R.id.scram:
                    break;
                case R.id.flash:
                    flash.setPressed (false);
                    myData[1] &= ~FLASH_DATA[1];
                    break;
                case R.id.doubleFlash:
                    doubleFlash.setPressed (false);
                    myData[3] &= ~DOUBLE_FLASH_DATA[3];
                    break;
                case R.id.purling:
                    purling.setPressed (false);
                    myData[0] &= ~PURLING_DATA[0];
                    break;
                case R.id.holdCar:
                    holdCar.setPressed (false);
                    myData[2] &= ~HOLD_CAR_DATA[2];
                    break;
                case R.id.stop:
                    stop.setPressed (false);
                    myData[1] &= ~STOP_DATA[1];
                    break;
                case R.id.speedCut:
                    speedCut.setPressed(false);
                    myData[1] &= ~SUB_DATA[1];
                    break;
                case R.id.speedUp:
                    speedUp.setPressed(false);
                    myData[2] &= ~ADD_DATA[2];
                    break;
                case R.id.backMid:
                    backMid.setPressed(false);
                    myData[3] &= ~BACK_MID_DATA[3];
                    break;
                case R.id.trumpet:
                    trumpet.setPressed(false);
                    myData[0] &= ~TRUMPET_DATA[0];
                    break;
                case R.id.turtleOrRabbit:
                    turtleOrRabbit.setPressed (false);
                    myData[0] &= ~TURTLE_OR_RABBIT_DATA[0];
                    break;
                case R.id.snake:
                    snake.setPressed (false);
                    myData[0] &= ~SNAKE_DATA[0];
                    break;
                case R.id.bigSnake:
                    bigSnake.setPressed (false);
                    myData[2] &= ~SNAKE_BIG_DATA[2];
                    break;
                case R.id.smallSnake:
                    smallSnake.setPressed (false);
                    myData[2] &= ~SNAKE_SMALL_DATA[2];
                    break;
                case R.id.frontSnake:
                    frontSnake.setPressed (false);
                    myData[1] &= ~ SNAKE_FRONT_DATA[1];
                    break;
                case R.id.backSnake:
                    backSnake.setPressed (false);
                    myData[1] &= ~SNAKE_BACK_DATA[1];
                    break;
                case R.id.engineOpen:
                    engineOpen.setPressed(false);
                    myData[3] &= ~ENGINE_OPEN_DATA[3];
                    break;
                case R.id.engineClose:
                    engineClose.setPressed(false);
                    mTimer.cancel();
                    myData[3] &= ~ENGINE_CLOSE_DATA[3];
                    break;
                case R.id.lock:
                    lock.setPressed(false);
                    myData[3] &= ~LOCK_DATA[3];
                    break;
                default:
                    break;
            }

        }


        return true;
    }

    /**
     *获取电池状态
     *BATTERY_PLUGGED_AC = 1
     *BATTERY_PLUGGED_USB = 2
     *BATTERY_PLUGGED_WIRELESS = 4
     */
    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int  chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED,-1);
            boolean isCharging = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;//usb充电
            //如果捕捉到的Action是ACTION_BATTERY_CHANGED则运行onBatteryInforECEIVER()
            if(intent.ACTION_BATTERY_CHANGED.equals(action))
            {
                //获得当前电量
                int intLevel = intent.getIntExtra("level",0);
                //获得手机总电量
                int intScale = intent.getIntExtra("scale",100);
                // 在下面会定义这个函数，显示手机当前电量
                onBatteryInfoReceiver(intLevel, intScale, isCharging);
            }
        }
    };

    private void onBatteryInfoReceiver(int intLevel, int intScale, boolean isCharging) {
        int percent = intLevel*100/ intScale;
        //不乘100得到的percent为0
        TextView battery_percent = (TextView) findViewById(R.id.battery_percent);
        ImageView battery_icon = (ImageView) findViewById(R.id.battery_icon);
        battery_percent.setText(percent + "%");
        if (isCharging){
            if (percent != 100) {
                battery_icon.setImageResource(R.drawable.battery_charging);
            }else{
                battery_icon.setImageResource(R.drawable.battery_charged);
            }
        }else {
            if (percent > 0 && percent <= 10) {
                battery_icon.setImageResource(R.drawable.battery_empty);
            } else if (percent > 10 && percent <= 35) {
                battery_icon.setImageResource(R.drawable.battery_quarter);
            } else if (percent > 35 && percent <= 60) {
                battery_icon.setImageResource(R.drawable.battery_half);
            } else if (percent > 60 && percent <= 85) {
                battery_icon.setImageResource(R.drawable.battery_three_quarter);
            } else if (percent > 85 && percent <= 100) {
                battery_icon.setImageResource(R.drawable.battery_full);
            } else {
                battery_icon.setImageResource(R.drawable.battery_empty);
            }
        }
    }

    /*
    *初始化蓝牙
     */
    private void initBluetooth(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }


    /**
     *蓝牙信号检测
     */
    private int checkBleSignal(int rssi){
        if (rssi <= -100){
            return SIGNAL_0;
        }else if ( rssi <= -80){
            return SIGNAL_1;
        }else if ( rssi <= -70){
            return SIGNAL_2;
        }else if ( rssi <= -60){
            return SIGNAL_3;
        }else{
            return SIGNAL_4;
        }
    }

    /**
     * 获取蓝牙信号
     * @return
     */
    public boolean getRssiVal() {
        if (mBluetoothGatt == null)
            return false;
        return mBluetoothGatt.readRemoteRssi();

    }

    /**
     *蓝牙扫描回调
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String name = device.getName();
                            if ( name != null ) {
                                if (!bleName.contains(name)) {
                                    bleName.add(device.getName());
                                    bleAddress.add(device.getAddress());
                                    Log.d(TAG, "name :" + device.getName() + "/n"
                                            + "address :" + device.getAddress());
                                    listViewAdapter.notifyDataSetChanged();
                                }

                            }
                        }
                    });
                }
            };


    /**
     *去连接蓝牙
     */
    public boolean toConnectBle(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            return false;
        }
        //如果已连接上该设备
        if ( address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            if (mBluetoothGatt.connect()) {
                connectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }
        //获取远程设备
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            return false;
        }
        //GATT连接
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        mBluetoothDeviceAddress = address;
        connectionState = STATE_CONNECTING;
        return true;
    }

    /**
     *蓝牙连接回调
     */
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
//            Log.d(TAG, "onReceive: same" + rssi);
            if (checkBleSignal(rssi) != connectionSignal){
                connectionSignal = checkBleSignal(rssi);
                changeRssiIcon(new MyInterface() {
                    @Override
                    public void changeRssiIcon() {
                        switch (connectionSignal){
                            case SIGNAL_0:
                                bleSignal_icon.setImageResource(R.drawable.signal_0);
                                break;
                            case SIGNAL_1:
                                bleSignal_icon.setImageResource(R.drawable.signal_1);
                                break;
                            case SIGNAL_2:
                                bleSignal_icon.setImageResource(R.drawable.signal_2);
                                break;
                            case SIGNAL_3:
                                bleSignal_icon.setImageResource(R.drawable.signal_3);
                                break;
                            case SIGNAL_4:
                                bleSignal_icon.setImageResource(R.drawable.signal_4);
                                break;
                            default:
                                break;
                        }
                    }
                });
            }
        }

        /*
         *蓝牙状态改变回调
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            //已连接蓝牙设备
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                connectionState = STATE_CONNECTED;
                sendMessage(MESSAGE_CONNECTED);
                broadcastUpdate(intentAction);
                gatt.discoverServices();
                isConnected = true;
                if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.O) {
                    requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
                }
                Log.d(TAG, "onConnectionStateChange: connected");
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                connectionState = STATE_DISCONNECTED;
                broadcastUpdate(intentAction);
                sendMessage(MESSAGE_DISCONNECTED);
                isConnected = false;
                Log.d(TAG, "onConnectionStateChange: disconnect");
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public void requestConnectionPriority(int priority){
            if (mBluetoothGatt != null) {
                boolean requestConnectionPriority = mBluetoothGatt.requestConnectionPriority(priority);
                Log.w("wsh"," requestConnectionPriority : "+ requestConnectionPriority);
            }
        }

        /**
        *发现设备服务回调
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                //匹配UUID
                displayGattServices(gatt.getServices());
            } else {
                Log.d(TAG, "onServicesDiscovered received: " + status);
            }
        }

        /*
         *蓝牙接收数据回调
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            Log.d(TAG, "onCharacteristicRead: 1");
            if (status == BluetoothGatt.GATT_SUCCESS) {
//                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        /*
         *蓝牙写入数据回调
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
//            Log.d(TAG, "onCharacteristicWrite: ");
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.d(TAG, "onDescriptorRead: ");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            receivePocket = characteristic.getValue();
            receiver(receivePocket);
            Log.d(TAG, "onCharacteristicChanged: ");
//            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };


    /**
     *连接蓝牙设备后
     * 匹配UUID
     */
    private void displayGattServices(List<BluetoothGattService> gattServices){
        if (gattServices == null){
            return;
        }
        Log.d(TAG, "displayGattServices: 开始匹配");
        String uuid;
        for (BluetoothGattService gattService : gattServices) {
            //获取每个服务的uuid
            uuid = gattService.getUuid().toString();
            //匹配我们的uuid，只要不匹配就跳过继续匹配
            if (!uuid.equals(UUID_HEART_RATE_MEASUREMENT)) {
                Log.d(TAG, "displayGattServices: 匹配失败");
                continue;
            }
            Log.d(TAG, "displayGattServices: 服务UUID匹配");
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            // 上面服务匹配成功后，再匹配特征，其实和上面一样，拿到特定的uuid，匹配上后就可以发送数据了
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                uuid = gattCharacteristic.getUuid().toString();
                //匹配我们的uuid，只要不匹配的姐跳过继续匹配
                if (!uuid.equals(UUID_READ_CHARACTERISTIC)) {
                    continue;
                }
                Log.d(TAG, "displayGattServices: 特征UUID匹配");
                //发送特征通知，是否启用还特征设备通知。
//                setCharacteristicNotification(gattCharacteristic, true);
            }
        }
    }

    /**
    *接收蓝牙通知
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.d(TAG, "BluetoothAdapter not initialized ");
            return;
        }

        boolean isEnableNotification =  mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        Log.d(TAG, "setCharacteristicNotification: " + isEnableNotification);
        //配置使能接收通知
        if(isEnableNotification) {
            List<BluetoothGattDescriptor> descriptorList = characteristic.getDescriptors();
            Log.d(TAG, "setCharacteristicNotification: " + descriptorList.size());
            if(descriptorList != null && descriptorList.size() > 0) {
                for(BluetoothGattDescriptor descriptor : descriptorList) {
                    if (enabled) {
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    } else {
                        descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                    }
                    mBluetoothGatt.writeDescriptor(descriptor);
                }
            }
        }
    }
    /**
    *蓝牙-写入数据
    * 20字节包
     */
    private boolean writeCharacteristic(byte[] data, boolean enable){
        if (enable) {
            if (mBluetoothAdapter == null || mBluetoothGatt == null) {
                return false;
            }
            BluetoothGattService mBluetoothGattService = mBluetoothGatt
                    .getService(UUID.fromString(UUID_HEART_RATE_MEASUREMENT));
            if (mBluetoothGattService != null) {
                BluetoothGattCharacteristic characteristic = mBluetoothGattService
                        .getCharacteristic(UUID.fromString(UUID_WRITE_CHARACTERISTIC));
                characteristic.setValue(data);
                mBluetoothGatt.writeCharacteristic(characteristic);
            }
//            mBluetoothGatt.readCharacteristic(characteristic);
            return true;
        }else{
            return false;
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_GATT_CONNECTED);
        intentFilter.addAction(ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    /**
     *广播接收器
     */
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (ACTION_GATT_CONNECTED.equals(action)) {
//                mConnected = true;
            } else if (ACTION_GATT_DISCONNECTED.equals(action)) {
//                mConnected = false;
            } else if (ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
            } else if (ACTION_DATA_AVAILABLE.equals(action)) {
//                receiver(receivePocket);
//                displayData(intent.getStringExtra(EXTRA_DATA));
                //收到数据通知
                Log.d(TAG, "onReceive: get " + intent.getStringExtra(EXTRA_DATA));
                Log.d(TAG, "onReceive: get");
            }
        }
    };

    /**
     *更新广播
     */
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }
    /**
     *更新广播
     */
    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
        } else {
            receivePocket = characteristic.getValue();
//            if (receivePocket != null && receivePocket.length > 0) {
//                final StringBuilder stringBuilder = new StringBuilder(receivePocket.length);
//                for(byte byteChar : receivePocket)
//                    stringBuilder.append(String.format("%02X ", byteChar));
//                intent.putExtra(EXTRA_DATA, new String(receivePocket) + "\n" + stringBuilder.toString());
//            }
        }
        sendBroadcast(intent);
    }
    /**
     *handler实现UI更新
     * MESSAGE_CONNECTED = 1
     * MESSAGE_DISCONNECTED = 2
     */
    Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case MESSAGE_CONNECTED:
                    alertDialog.cancel();
                    bleSignal_icon.setVisibility(View.VISIBLE);
                    connect.setText(R.string.disconnect);
//                    run_mode_text.setText(R.string.telecontrol_mode);
                    unlockButtons();
                    Toast.makeText(MainActivity.this, "已成功连接", Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_DISCONNECTED:
                    bleSignal_icon.setVisibility(View.INVISIBLE);
                    connect.setText(R.string.connect);
                    run_mode_text.setText(R.string.local_mode);
                    initButtons();
                    lockButtons();
                    Toast.makeText(MainActivity.this, "已断开连接", Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_CLOSE_ENGINE:
                    engineClose.setPressed(false);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 自定义接口获取蓝牙信号RSSI
     */
    private interface MyInterface{
        void changeRssiIcon();
    }

    private void changeRssiIcon(MyInterface myInterface){
        myInterface.changeRssiIcon();
    }
    /*
     *发送信息
     */
    private void sendMessage(int message){
        Message msg = new Message();
        msg.what = message;
        handler.sendMessage(msg);
    }

    /**
     *申请权限
     * BLE扫描需申请危险权限-定位权限
     */
    private void registerPermissions(){
        List<String> listPermission = new ArrayList<>();
        //权限-获取当前定位
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            listPermission.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (!listPermission.isEmpty()){
            String[] permissions = listPermission.toArray(new String[0]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        }
    }

    /**
     * 初始化蓝牙设备列表listview
     */
    private void initBleListview(){
        View bleView = getLayoutInflater().inflate(R.layout.ble_listview, null);
        alertDialog = new AlertDialog.Builder(this)
                .setTitle("蓝牙")
                .setIcon(R.mipmap.ic_launcher)
                .setView(bleView)
                .setPositiveButton("return", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface paramAnonymousDialogInterface,
                                        int paramAnonymousInt) {
                        alertDialog.cancel();
                    }
                }).create();
        bleListview=bleView.findViewById(R.id.listView);
        listViewAdapter=new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1,bleName);
        bleListview.setAdapter(listViewAdapter);
        bleListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //连接蓝牙设备
                String address = bleAddress.get(position);
                toConnectBle(address);
            }
        });
    }

    /**
     *接收存储数据
     * 20字节
     */
    public void receiver(byte[] data){
        if (data.length == 20) {
            receiveNum[0] = data[0];
            receiveNum[1] = data[1];
            receiveNum[2] = data[2];
            receiveNum[3] = data[3];
            receiveData[0] = data[4];
            receiveData[1] = data[5];
            receiveData[2] = data[6];
            receiveData[3] = data[7];
            verticalSpeed = data[8];
            horizontalSpeed = data[9];
            purlingState = data[10];
            receiveFlag = data[11];
            coordinate[0] = data[12];
            coordinate[1] = data[13];
            coordinate[2] = data[14];
            coordinate[3] = data[15];
            coordinate[4] = data[16];
            coordinate[5] = data[17];
            coordinate[6] = data[18];
            coordinate[7] = data[19];
        }
    }
    /*
     *发送数据包
     */
    public byte[] sender(byte[] data){
        byte[] sendData = new byte[16];
        senderNum();
        sendData[0] = sendStart;    //帧头0xAA
        sendData[1] = sendNum[0];   //帧计数
        sendData[2] = sendNum[1];
        sendData[3] = sendNum[2];
        sendData[4] = sendNum[3];
        sendData[5] = data[0];      //功能位
        sendData[6] = data[1];
        sendData[7] = data[2];
        sendData[8] = data[3];
        sendData[9] = (byte)0x00;   //暂时空着
        sendData[10] = (byte)0x00;
        sendData[11] = (byte)0x00;
        sendData[12] = (byte)0x00;
        sendData[13] = flankData;//高两位侧键位 0x80急停 0x40模式切换
        sendData[14] = (byte)checkSum(sendData,14);
        sendData[15] = sendEnd;     //帧尾0xBB
//        sendData[14] = (byte)crc16_ccitt(sendData,14);  //校验位

        return sendData;
    }
    /**
     * CRC校验
     * @param data  数据包
     * @param len   长度
     * @return  short校验位
     */
    public short crc16_ccitt( byte[] data, int len)
    {
        int counter;
        short crc = 0;
        for (counter = 0; counter < len; counter++)
            crc = (short)((crc << 8) ^  crc16tab[((crc >> 8) ^ data[counter++]) & 0x00FF]);
        return crc;
    }

    /**
     * 和校验
     * 校验位前n位和
     */
    public byte checkSum(byte[] data, int len){
        byte sum = (byte)0x00;
        for (int i = 0;i < len; i++){
            sum += (byte)data[i];
        }
        return sum;
    }

    /**
     *发送包计数
     * 发送一次包，累计+1
     *
     */
    private void senderNum(){
        //计算0xFF 0xFF 0xFF 0xFF清零
        if (sendNum[3] != (byte) 0xFF) {
            sendNum[3] += 1;
        }else{
            sendNum[2] += 1;
            if (sendNum[2] == (byte)0xFF){
                sendNum[2] = (byte)0x00;
                sendNum[1] += 1;
                if (sendNum[1] == (byte)0xFF){
                    sendNum[1] = (byte)0x00;
                    sendNum[0] += 1;
                    if (sendNum[0] == (byte)0xFF)
                        sendNum[0] = (byte)0x00;
                }
            }
            sendNum[3] =(byte)0x01;
        }
    }


    /**
     * 速度档位发生变化
     * 发送数据包
     */
    private void onSpeedGear(boolean enable) {
        if (enable) {
            if (rockerView.nowSpeedGear < rockerView.speedGear) {
                //减速档
                myData[0] |= SPEED_GEAR_CUT_DATA[0];
                myData[0] &= ~SPEED_GEAR_UP_DATA[0];
            } else if (rockerView.nowSpeedGear > rockerView.speedGear) {
                //加速档
                myData[0] |= SPEED_GEAR_UP_DATA[0];
                myData[0] &= ~SPEED_GEAR_CUT_DATA[0];
            } else if (rockerView.nowSpeedGear == 0 && rockerView.speedGear ==0){
                myData[0] &= ~SPEED_GEAR_CUT_DATA[0];
                myData[0] &= ~SPEED_GEAR_UP_DATA[0];
            }
            rockerView.speedGear = rockerView.nowSpeedGear;
        }
    }

    /**
     * 方向档位发生变化
     * 发送数据包
     */
    private void onDirectionGear(boolean enable) {
        if (enable) {
            if (rockerView.nowDirectionGear < rockerView.directionGear) {
                //左档
                myData[2] |= DIRECTION_GEAR_LEFT_DATA[2];
                myData[3] &= ~DIRECTION_GEAR_RIGHT_DATA[3];
            } else if (rockerView.nowDirectionGear > rockerView.directionGear) {
                //右档
                myData[3] |= DIRECTION_GEAR_RIGHT_DATA[3];
                myData[2] &= ~DIRECTION_GEAR_LEFT_DATA[2];
            } else if (rockerView.nowDirectionGear == 0 && rockerView.directionGear == 0){
                myData[2] &= ~DIRECTION_GEAR_LEFT_DATA[2];
                myData[3] &= ~DIRECTION_GEAR_RIGHT_DATA[3];
            }
            rockerView.directionGear = rockerView.nowDirectionGear;
        }
    }

    /**
    *锁定按钮
     */
    private void lockButtons(){

        scram.setEnabled(false);
        flash.setEnabled(false);
        doubleFlash.setEnabled(false);
        purling.setEnabled(false);
        holdCar.setEnabled(false);
        stop.setEnabled(false);
        speedCut.setEnabled(false);
        speedUp.setEnabled(false);
        trumpet.setEnabled(false);
        turtleOrRabbit.setEnabled(false);
        snake.setEnabled(false);
        smallSnake.setEnabled(false);
        bigSnake.setEnabled(false);
        frontSnake.setEnabled(false);
        backSnake.setEnabled(false);
        backMid.setEnabled(false);
        engineOpen.setEnabled(false);
        engineClose.setEnabled(false);
        lock.setEnabled(false);
    }

    /**
    *解锁按钮
     */
    private void unlockButtons(){
        scram.setEnabled(true);
        flash.setEnabled(true);
        doubleFlash.setEnabled(true);
        purling.setEnabled(true);
        holdCar.setEnabled(true);
        stop.setEnabled(true);
        speedCut.setEnabled(true);
        speedUp.setEnabled(true);
        trumpet.setEnabled(true);
        turtleOrRabbit.setEnabled(true);
        snake.setEnabled(true);
        smallSnake.setEnabled(true);
        bigSnake.setEnabled(true);
        frontSnake.setEnabled(true);
        backSnake.setEnabled(true);
        backMid.setEnabled(true);
        engineOpen.setEnabled(true);
        engineClose.setEnabled(true);
        lock.setEnabled(true);
//       scram.setTextColor(R.drawable.button_color_selector);
    }



    /**
     * 断开连接 按钮复位
     */
    private void initButtons(){

        scram.setActivated(false);
        snake.setActivated(false);
        purling.setActivated(false);
        turtleOrRabbit.setText("龟兔");

        stop.setActivated(false);
        frontSnake.setActivated(false);
        backSnake.setActivated(false);
        flash.setActivated(false);
        speedUp.setPressed(false);

        bigSnake.setActivated(false);
        holdCar.setActivated(false);
        speedCut.setActivated(false);

        backMid.setActivated(false);
        doubleFlash.setActivated(false);

    }
}



