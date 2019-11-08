package com.example.roadroller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private String TAG = "MainActivity";

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //全屏，隐藏系统状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        //注册广播接收器
        registerReceiver(mBatInfoReceiver, new IntentFilter(
                Intent.ACTION_BATTERY_CHANGED));

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
        //隐藏标题栏
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.scram:
                if (scram.isActivated()){
                    scram.setActivated(false);
                }else{
                    scram.setActivated(true);
                }
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
}
