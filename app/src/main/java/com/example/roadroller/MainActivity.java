package com.example.roadroller;

import androidx.annotation.NonNull;
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
import android.media.Image;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

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
    public Button engine;
    public Button backMid;
    public Button connect;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private String mBluetoothDeviceAddress;
    private int bluetoothDeviceRssi;

    private AlertDialog alertDialog;
    private ListView bleListview;
    private List<String> bleName = new ArrayList<>();
    private List<String> bleAddress = new ArrayList<>();
    private ArrayAdapter<String> listViewAdapter;

    private int connectionState = STATE_DISCONNECTED;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    private int connectionSignal = SIGNAL_0;
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
    //UUID
    private static final String UUID_HEART_RATE_MEASUREMENT = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    private static final String UUID_CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    private static final String UUID_WRITE_CHARACTERISTIC = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
    private static final String UUID_READ_CHARACTERISTIC = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
    //message
    private static final int MESSAGE_CONNECTED = 1;
    private static final int MESSAGE_DISCONNECTED = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //全屏，隐藏系统状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        //申请权限
        registerPermissions();
        //注册广播接收器
        registerReceiver(mBatInfoReceiver, new IntentFilter(
                Intent.ACTION_BATTERY_CHANGED));
        initBleListview();
        initBluetooth();

        run_mode_text = findViewById(R.id.run_mode_text);
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
        turtleOrRabbit = (Button) findViewById(R.id.turtleOrRubbin);
        snake = (Button) findViewById(R.id.snake);
        engine = (Button) findViewById(R.id.engine);
        backMid = (Button) findViewById(R.id.backMid);
        connect = (Button) findViewById(R.id.connect);

        scram.setOnClickListener(this);
        flash.setOnClickListener(this);
        doubleFlash.setOnClickListener(this);
        purling.setOnClickListener(this);
        holdCar.setOnClickListener(this);
        stop.setOnClickListener(this);
        speedCut.setOnClickListener(this);
        speedUp.setOnClickListener(this);
        trumpet.setOnClickListener(this);
        turtleOrRabbit.setOnClickListener(this);
        snake.setOnClickListener(this);
        engine.setOnClickListener(this);
        backMid.setOnClickListener(this);
        connect.setOnClickListener(this);
        //隐藏标题栏
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.connect:
                //打开蓝牙
                if(!mBluetoothAdapter.isEnabled()){
                    Intent  openBLE = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(openBLE,1);
                }
                //开始进行扫描设备，传递进去一个监听
                mBluetoothAdapter.startLeScan(mLeScanCallback);
                alertDialog.show();
                break;
            case R.id.scram:
                if (scram.isActivated()){
                    scram.setActivated(false);
                }else{
                    scram.setActivated(true);
                }
                byte[] data = {0x00,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09};
                boolean flag = writeCharacteristic(data);
                Log.d(TAG, "onClick: " + flag);
                break;
            case R.id.flash:
                if (flash.isActivated()){
                    flash.setActivated(false);
                }else{
                    flash.setActivated(true);
                }
                break;
            case R.id.doubleFlash:
                if (doubleFlash.isActivated()){
                    doubleFlash.setActivated(false);
                }else{
                    doubleFlash.setActivated(true);
                }
                break;
            case R.id.holdCar:
                if (holdCar.isActivated()){
                    holdCar.setActivated(false);
                }else{
                    holdCar.setActivated(true);
                }
                break;
            case R.id.purling:
                if (purling.isActivated()){
                    purling.setActivated(false);
                }else{
                    purling.setActivated(true);
                }
                break;
            case R.id.stop:

                break;
            case R.id.speedCut:

                break;
            case R.id.speedUp:

                break;
            case R.id.trumpet:

                break;
            case R.id.turtleOrRubbin:
                if (turtleOrRabbit.getText().toString() != "龟档"){
                    turtleOrRabbit.setText("龟档");
                }else{
                    turtleOrRabbit.setText("兔档");
                }

                break;
            case R.id.engine:

                break;
            case R.id.backMid:

                break;
            case R.id.snake:
                if (snake.isActivated()){
                    snake.setActivated(false);
                }else{
                    snake.setActivated(true);
                }
                break;
        }
    }
/*
 获取电池状态
    BATTERY_PLUGGED_AC = 1
    BATTERY_PLUGGED_USB = 2
    BATTERY_PLUGGED_WIRELESS = 4
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

    /*
    *蓝牙信号检测
     */
    private int checkBleSignal(int rssi){
        if (rssi <= -30){
            return SIGNAL_4;
        }else if ( rssi <= 0){
            return SIGNAL_3;
        }else if ( rssi <= 20){
            return SIGNAL_2;
        }else if ( rssi < 40){
            return SIGNAL_1;
        }else{
            return SIGNAL_0;
        }
    }
    /*
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
                                    bluetoothDeviceRssi = rssi;
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
                                                }
                                            }
                                        });
                                    }
//                                    sendMessage(MESSAGE_RSSI_CHANGE);

                                }

                            }
                        }
                    });
                }
            };


    /*
     *连接蓝牙
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

    /*
     *蓝牙连接回调
     */
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            //已连接蓝牙设备
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                connectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                gatt.discoverServices();
                sendMessage(MESSAGE_CONNECTED);
                Log.d(TAG, "onConnectionStateChange: connected");
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                connectionState = STATE_DISCONNECTED;
                broadcastUpdate(intentAction);
                sendMessage(MESSAGE_DISCONNECTED);
                Log.d(TAG, "onConnectionStateChange: disconnect");
            }
        }
        /*
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
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        /*
         *蓝牙写入数据回调
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.d(TAG, "onCharacteristicWrite: ");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };


    /*
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
                setCharacteristicNotification(gattCharacteristic, true);
            }
        }
    }

    /*
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
    /*
    *蓝牙-写入数据
    * 20字节包
     */
    private boolean writeCharacteristic(byte[] data){
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return false;
        }
        BluetoothGattService mBluetoothGattService = mBluetoothGatt
                .getService(UUID.fromString(UUID_HEART_RATE_MEASUREMENT));
        BluetoothGattCharacteristic characteristic = mBluetoothGattService
                .getCharacteristic(UUID.fromString(UUID_WRITE_CHARACTERISTIC));
        characteristic.setValue(data);
        mBluetoothGatt.writeCharacteristic(characteristic);
        return true;
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_GATT_CONNECTED);
        intentFilter.addAction(ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    /*
     *广播接收器
     */
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (ACTION_GATT_CONNECTED.equals(action)) {
//                mConnected = true;
//                updateConnectionState(R.string.connected);
//                invalidateOptionsMenu();
            } else if (ACTION_GATT_DISCONNECTED.equals(action)) {
//                mConnected = false;
//                updateConnectionState(R.string.disconnected);
//                invalidateOptionsMenu();
//                clearUI();
            } else if (ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // 在用户接口上展示所有的services and characteristics
//                displayGattServices(getSupportedGattServices());
//                displayGattServices(mBluetoothGatt.getServices());

            } else if (ACTION_DATA_AVAILABLE.equals(action)) {
//                displayData(intent.getStringExtra(EXTRA_DATA));
                //收到数据通知
                Log.d(TAG, "onReceive: get " + intent.getStringExtra(EXTRA_DATA));
            }
        }
    };

    /*
     *更新广播
     */
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }
    /*
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
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                Log.d(TAG, "broadcastUpdate: lenth :" + data.length);
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
                Log.d(TAG, "broadcastUpdate: get :" + stringBuilder.toString());
            }
        }
        sendBroadcast(intent);
    }
    /*
    *子线程实现UI更新
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
                    Toast.makeText(MainActivity.this, "已成功连接", Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_DISCONNECTED:
                    bleSignal_icon.setVisibility(View.INVISIBLE);
                    connect.setText(R.string.connect);
                    Toast.makeText(MainActivity.this, "已断开连接", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    /*
    * 回调
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
    /*
     *申请权限
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

    /*
     *蓝牙设备列表listview
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
}



