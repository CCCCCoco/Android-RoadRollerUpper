package com.example.roadroller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;


public class RockerView extends SurfaceView implements Callback, Runnable {

	//速度档位
	public int nowSpeedGear;
	public int speedGear = SPEED_GEAR_0;
	public static final int SPEED_GEAR_8 = 8;
	public static final int SPEED_GEAR_7 = 7;
	public static final int SPEED_GEAR_6 = 6;
	public static final int SPEED_GEAR_5 = 5;
	public static final int SPEED_GEAR_4 = 4;
	public static final int SPEED_GEAR_3 = 3;
	public static final int SPEED_GEAR_2 = 2;
	public static final int SPEED_GEAR_1 = 1;
	public static final int SPEED_GEAR_0 = 0;

	//方向档位
	public int nowDirectionGear;
	public int directionGear = DIRECTION_GEAR_0;
	public static final int DIRECTION_GEAR_20 = 20;
	public static final int DIRECTION_GEAR_19 = 19;
	public static final int DIRECTION_GEAR_18 = 18;
	public static final int DIRECTION_GEAR_17 = 17;
	public static final int DIRECTION_GEAR_16 = 16;
	public static final int DIRECTION_GEAR_15 = 15;
	public static final int DIRECTION_GEAR_14 = 14;
	public static final int DIRECTION_GEAR_13 = 13;
	public static final int DIRECTION_GEAR_12 = 12;
	public static final int DIRECTION_GEAR_11 = 11;
	public static final int DIRECTION_GEAR_10 = 10;
	public static final int DIRECTION_GEAR_9 = 9;
	public static final int DIRECTION_GEAR_8 = 8;
	public static final int DIRECTION_GEAR_7 = 7;
	public static final int DIRECTION_GEAR_6 = 6;
	public static final int DIRECTION_GEAR_5 = 5;
	public static final int DIRECTION_GEAR_4 = 4;
	public static final int DIRECTION_GEAR_3 = 3;
	public static final int DIRECTION_GEAR_2 = 2;
	public static final int DIRECTION_GEAR_1 = 1;
	public static final int DIRECTION_GEAR_0 = 0;



	String TAG = "MainActivity";
	/**thread*/
	private Thread th;
	/**SurfaceHolder*/
	private SurfaceHolder sfh;
	/**画布*/
	private Canvas canvas;
	/**画笔*/
	private Paint paint;
	/**标记是否在描画*/
	private boolean isDraw = false;

	/**摇杆1圆心x*/
	private int CIRCLE_X1 = 100;
	/**摇杆1圆心y*/
	private int CIRCLE_Y1 = 100;
	/**摇杆1小圆半径*/
	private float CIRCLE_SMALL_R1 = 20;
	/**摇杆1背景矩形的X坐标*/
	private int RockerRectX1 = CIRCLE_X1;
	/**摇杆1背景矩形的Y坐标*/
	private int RockerRectY1 = CIRCLE_Y1;
	/**摇杆1的X坐标*/
	private float SmallRockerCircleX1 = CIRCLE_X1;
	/**摇杆1的Y坐标*/
	private float SmallRockerCircleY1 = CIRCLE_Y1;
	/**摇杆1的半径*/
	private float SmallRockerCircleR1 = CIRCLE_SMALL_R1;

	/**摇杆2圆心x*/
	private int CIRCLE_X2 = 100+150;
	/**摇杆2圆心y*/
	private int CIRCLE_Y2 = 100;
	/**摇杆2小圆半径*/
	private float CIRCLE_SMALL_R2 = 20;
	/**摇杆2背景圆形的X坐标*/
	private int RockerRectX2 = CIRCLE_X2;
	/**摇杆2背景圆形的Y坐标*/
	private int RockerRectY2 = CIRCLE_Y2;
	/**摇杆2的X坐标*/
	private float SmallRockerCircleX2 = CIRCLE_X2;
	/**摇杆2的Y坐标*/
	private float SmallRockerCircleY2 = CIRCLE_Y2;
	/**摇杆2的半径*/
	private float SmallRockerCircleR2 = CIRCLE_SMALL_R2;


	/**摇杆1按下：true 没有按下 false*/
	private boolean flagIn = false;
	/**摇杆2按下：true 没有按下 false*/
	private boolean flagIn2 = false;
	/**左边先点击:1,右边先点击:2,没有点击在摇杆范围内:0*/
	private int leftOrRight = 0;
	
	public RockerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initNeedClass();
	}
	
	public RockerView(Context context) {
		super(context);
		initNeedClass();
	}
	
	public void surfaceCreated(SurfaceHolder holder) {
		int newWidth =  this.getWidth();
		int newHeight = this.getHeight();
		setPosition1(
				newWidth/4
				, newHeight/2
				, newWidth/4
				, newHeight/2
				, newHeight/10
		);
		setPosition2(
				(newWidth/4)*3
				, newHeight/2
				, (newWidth/4)*3
				, newHeight/2
				, newHeight/10);
		
		isDraw = true;
		th = new Thread(this);
		th.start();
	}
	
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

	public void surfaceDestroyed(SurfaceHolder holder) {
		isDraw = false;
	}
	
	public void run() {
		// TODO Auto-generated method stub
		while (isDraw) {
			draw();
			try {
				Thread.sleep(40);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}


	public void initNeedClass(){
		this.setKeepScreenOn(true);
		sfh = this.getHolder();
		sfh.addCallback(this);
		paint = new Paint();
		paint.setAntiAlias(true);
		setFocusable(true);
		setFocusableInTouchMode(true);
	}
	
	@SuppressLint("NewApi")
	public void draw() {
		try {
			canvas = sfh.lockCanvas();
			canvas.drawColor(0x000000, Mode.CLEAR);//Mode.CLEAR这个比较重要，防止绘制有影子，可以去掉看下效果
//			canvas.drawARGB(0,255,255,255);
			//绘制左摇杆背景
			paint.setColor(0x70ffffff);
			canvas.drawRect(RockerRectX1 - SmallRockerCircleR1,
							RockerRectY1 - 250,
							RockerRectX1 + SmallRockerCircleR1,
							RockerRectY1 + 250,
							paint);
			//绘制左摇杆
			paint.setColor(0xaaffffff);
			canvas.drawCircle(SmallRockerCircleX1, SmallRockerCircleY1, SmallRockerCircleR1, paint);
			//绘制右摇杆背景
            paint.setColor(0x70ffffff);
			canvas.drawRect(RockerRectX2 - 250,
							RockerRectY2 - SmallRockerCircleR2,
							RockerRectX2 + 250,
							RockerRectY2 + SmallRockerCircleR2,
							paint);
			//绘制右摇杆
            paint.setColor(0xaaffffff);
			canvas.drawCircle(SmallRockerCircleX2, SmallRockerCircleY2, SmallRockerCircleR2, paint);

		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			try {
				if (canvas != null)
					sfh.unlockCanvasAndPost(canvas);
			} catch (Exception e2) {

			}
		}
	}


	@SuppressLint("NewApi")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN ) {
			//在这里判断先使用了左边的还是右边的
            if ((int) event.getX() >= RockerRectX1-SmallRockerCircleR1
                        && (int) event.getX() <= RockerRectX1+SmallRockerCircleR1
						&& (int) event.getY() >=  RockerRectY1-250
						&& (int) event.getY() <=  RockerRectY1+250){
				leftOrRight = 1;
			}
            if ((int) event.getX() >= RockerRectX2 - 250
                    && (int) event.getX() <= RockerRectX2 + 250
                    && (int) event.getY() >=  RockerRectY2 - SmallRockerCircleR2
                    && (int) event.getY() <=  RockerRectY2 + SmallRockerCircleR2){
				leftOrRight = 2;
			}
			if(leftOrRight == 1){
				if ((int) event.getY() >=  (RockerRectY1-250) && (int) event.getY() <=  (RockerRectY1+250)){
					SmallRockerCircleY1 = (int) event.getY();
					nowSpeedGear = getSpeedGear((int) event.getY());
					}
				flagIn = true;
				return true;
			}
			if(leftOrRight == 2){
				SmallRockerCircleX2 = (int) event.getX();
				nowDirectionGear = getDirectionGear((int)SmallRockerCircleX2);
				flagIn2 = true;
				return true;
			}
		}
		if (event.getAction() == MotionEvent.ACTION_UP) {
			//当释放最后一个摇杆时全部初始化
//			SmallRockerCircleX1 = CIRCLE_X1;
//			SmallRockerCircleY1 = CIRCLE_Y1;
//			SmallRockerCircleX2 = CIRCLE_X2;
//			SmallRockerCircleY2 = CIRCLE_Y2;
			flagIn = false;
			flagIn2 = false;
			leftOrRight = 0;
		}

		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			int pointerCount = event.getPointerCount();
			//多只手同时按下
	        if (pointerCount > 2) {  
	            pointerCount = 2;  
	        }
	        //一直手按下
			if(pointerCount == 1){
				//左边
				if(flagIn){
					// 当触屏区域在活动范围
                    if ((int) event.getX() >= RockerRectX1-SmallRockerCircleR1
                            && (int) event.getX() <= RockerRectX1+SmallRockerCircleR1
                            && (int) event.getY() >=  RockerRectY1-250
                            && (int) event.getY() <=  RockerRectY1+250){
                        SmallRockerCircleY1 = (int) event.getY();
						nowSpeedGear = getSpeedGear((int) event.getY());
                    }
				}
				//右边
				if(flagIn2){
					// 当触屏区域在活动范围内
                    if ((int) event.getX() >= RockerRectX2 - 250
                            && (int) event.getX() <= RockerRectX2 + 250
                            && (int) event.getY() >=  RockerRectY2 - SmallRockerCircleR2
                            && (int) event.getY() <=  RockerRectY2 + SmallRockerCircleR2){
                        SmallRockerCircleX2 = (int) event.getX();
						nowDirectionGear = getDirectionGear((int)SmallRockerCircleX2);
                    }
				}
			}
			if (pointerCount == 2) {
				//两个摇杆都按下
				if(flagIn && flagIn2){
					if(leftOrRight == 1){
						/**************************处理第一个按下的*****************************/
						// 当触屏区域在活动范围内
                        if ((int) event.getX() >= RockerRectX1-SmallRockerCircleR1
                                && (int) event.getX() <= RockerRectX1+SmallRockerCircleR1
                                && (int) event.getY() >=  RockerRectY1-250
                                && (int) event.getY() <=  RockerRectY1+250){
                            SmallRockerCircleY1 = (int) event.getY();
							nowSpeedGear = getSpeedGear((int) event.getY());
                        }
						/**************************处理第二个按下的*****************************/
						// 当触屏区域在活动范围内
                        if ((int) event.getX(1) >= RockerRectX2 - 250
                                && (int) event.getX(1) <= RockerRectX2 + 250
                                && (int) event.getY(1) >=  RockerRectY2 - SmallRockerCircleR2
                                && (int) event.getY(1) <=  RockerRectY2 + SmallRockerCircleR2){
                            SmallRockerCircleX2 = (int) event.getX(1);
							nowDirectionGear = getDirectionGear((int)SmallRockerCircleX2);
                        }
					}
					if(leftOrRight == 2){
						/**************************处理第一个按下的*****************************/
						// 当触屏区域在活动范围内
                        if ((int) event.getX() >= RockerRectX2 - 250
                                && (int) event.getX() <= RockerRectX2 + 250
                                && (int) event.getY() >=  RockerRectY2 - SmallRockerCircleR2
                                && (int) event.getY() <=  RockerRectY2 + SmallRockerCircleR2){
                            SmallRockerCircleX2 = (int) event.getX();
							nowDirectionGear = getDirectionGear((int)SmallRockerCircleX2);
                        }
						/**************************处理第二个按下的*****************************/
						// 当触屏区域在活动范围内
                        if ((int) event.getX(1) >= RockerRectX1-SmallRockerCircleR1
                                && (int) event.getX(1) <= RockerRectX1+SmallRockerCircleR1
                                && (int) event.getY(1) >=  RockerRectY1-250
                                && (int) event.getY(1) <=  RockerRectY1+250){
                            SmallRockerCircleY1 = (int) event.getY(1);
							nowSpeedGear = getSpeedGear((int) SmallRockerCircleY1);
                        }
					}
				}
				//两个摇杆有一个按下,按下摇杆为左边摇杆
				if(flagIn && (!flagIn2)){
					if(leftOrRight == 1){
						// 当触屏区域在活动范围内
                        if ((int) event.getX() >= RockerRectX1-SmallRockerCircleR1
                                && (int) event.getX() <= RockerRectX1+SmallRockerCircleR1
                                && (int) event.getY() >=  RockerRectY1-250
                                && (int) event.getY() <=  RockerRectY1+250){
                            SmallRockerCircleY1 = (int) event.getY();
							nowSpeedGear = getSpeedGear((int) event.getY());
                        }
                        //期间按下了右摇杆
                        if ((int) event.getX(1) >= RockerRectX2 - 250
                                && (int) event.getX(1) <= RockerRectX2 + 250
                                && (int) event.getY(1) >=  RockerRectY2 - SmallRockerCircleR2
                                && (int) event.getY(1) <=  RockerRectY2 + SmallRockerCircleR2){
                            SmallRockerCircleX2 = (int) event.getX(1);
							nowDirectionGear = getDirectionGear((int)SmallRockerCircleX2);
                            flagIn2 = true;
                        }
					}
					if(leftOrRight == 2){
						// 当触屏区域在活动范围内
                        if ((int) event.getX(1) >= RockerRectX1-SmallRockerCircleR1
                                && (int) event.getX(1) <= RockerRectX1+SmallRockerCircleR1
                                && (int) event.getY(1) >=  RockerRectY1-250
                                && (int) event.getY(1) <=  RockerRectY1+250){
                            SmallRockerCircleY1 = (int) event.getY(1);
							nowSpeedGear = getSpeedGear((int) SmallRockerCircleY1);
                        }
						// 当触屏区域在活动范围内
                        if ((int) event.getX() >= RockerRectX2 - 250
                                && (int) event.getX() <= RockerRectX2 + 250
                                && (int) event.getY() >=  RockerRectY2 - SmallRockerCircleR2
                                && (int) event.getY() <=  RockerRectY2 + SmallRockerCircleR2){
                            SmallRockerCircleX2 = (int) event.getX();
                            flagIn2 = true;
                        }
					}
				}
				//两个摇杆有一个按下,按下摇杆为右边摇杆
				if((!flagIn) && flagIn2){
					if(leftOrRight == 1){
                        if ((int) event.getX(1) >= RockerRectX2 - 250
                                && (int) event.getX(1) <= RockerRectX2 + 250
                                && (int) event.getY(1) >=  RockerRectY2 - SmallRockerCircleR2
                                && (int) event.getY(1) <=  RockerRectY2 + SmallRockerCircleR2){
                            SmallRockerCircleX2 = (int) event.getX(1);
							nowDirectionGear = getDirectionGear((int)SmallRockerCircleX2);
                        }
						// 当触屏区域在活动范围内
                        if ((int) event.getX() >= RockerRectX1-SmallRockerCircleR1
                                && (int) event.getX() <= RockerRectX1+SmallRockerCircleR1
                                && (int) event.getY() >=  RockerRectY1-250
                                && (int) event.getY() <=  RockerRectY1+250){
                            SmallRockerCircleY1 = (int) event.getY();
							nowSpeedGear = getSpeedGear((int) SmallRockerCircleY1);
                            flagIn = true;
                        }
					}
					if(leftOrRight == 2){
						// 当触屏区域在活动范围内
                        if ((int) event.getX() >= RockerRectX1-SmallRockerCircleR1
                                && (int) event.getX() <= RockerRectX1+SmallRockerCircleR1
                                && (int) event.getY() >=  RockerRectY1-250
                                && (int) event.getY() <=  RockerRectY1+250){
                            SmallRockerCircleY1 = (int) event.getY();
							nowSpeedGear = getSpeedGear((int) SmallRockerCircleY1);
                        }
						// 当触屏区域不在活动范围内
                        if ((int) event.getX(1) >= RockerRectX1-SmallRockerCircleR1
                                && (int) event.getX(1) <= RockerRectX1+SmallRockerCircleR1
                                && (int) event.getY(1) >=  RockerRectY1-250
                                && (int) event.getY(1) <=  RockerRectY1+250){
                            SmallRockerCircleY1 = (int) event.getY(1);
							nowSpeedGear = getSpeedGear((int) SmallRockerCircleY1);
                            flagIn = true;
                        }
					}
				}
	        }
		}
		return true;
	}


	/**
	 * 设置摇杆1在view中的位置
	 */
	public void setPosition1(int RockerRectX1,
			int RockerRectY1,
			float SmallRockerCircleX1,
			float SmallRockerCircleY1,
			float SmallRockerRectR1){
		this.RockerRectX1 = RockerRectX1;
		this.RockerRectY1 = RockerRectY1;
		this.SmallRockerCircleX1 = SmallRockerCircleX1;
		this.SmallRockerCircleY1 = SmallRockerCircleY1;
		this.SmallRockerCircleR1 = SmallRockerRectR1;
		CIRCLE_X1 = RockerRectX1;
		CIRCLE_Y1 = RockerRectY1;
		CIRCLE_SMALL_R1 = SmallRockerRectR1;
	}
	
	/**
	 * 设置摇杆2在view中的位置
	 */
	public void setPosition2(int RockerRectX2,
			int RockerRectY2,
			float SmallRockerCircleX2,
			float SmallRockerCircleY2,
			float SmallRockerCircleR2){
		this.RockerRectX2 = RockerRectX2;
		this.RockerRectY2 = RockerRectY2;
		this.SmallRockerCircleX2 = SmallRockerCircleX2;
		this.SmallRockerCircleY2 = SmallRockerCircleY2;
		this.SmallRockerCircleR2 = SmallRockerCircleR2;
		CIRCLE_X2 = RockerRectX2;
		CIRCLE_Y2 = RockerRectY2;
		CIRCLE_SMALL_R2 = SmallRockerCircleR2;
	}

	public int getSpeedGear(int y){
		int gear = 0;
		int dif = Math.abs(y - RockerRectY1);
		if (dif == 0){
			gear = SPEED_GEAR_0;
		}else {
			switch (dif / 30) {
				case 0:
					gear = SPEED_GEAR_1;
					break;
				case 1:
					gear = SPEED_GEAR_2;
					break;
				case 2:
					gear = SPEED_GEAR_3;
					break;
				case 3:
					gear = SPEED_GEAR_4;
					break;
				case 4:
					gear = SPEED_GEAR_5;
					break;
				case 5:
					gear = SPEED_GEAR_6;
					break;
				case 6:
					gear = SPEED_GEAR_7;
					break;
				case 7:
				case 8:
					gear = SPEED_GEAR_8;
					break;
				default:
					break;
			}
		}
		if ((y - RockerRectY1) <= 0){
			return gear;
		}else{
			return -gear;
		}
	}

	/*
	*获取左右方向档位
	* 左右各20个档位
	 */
	public int getDirectionGear(int x){
		int gear = 0;
		int dif = Math.abs(x - RockerRectX2);
		if (dif == 0){
			gear = DIRECTION_GEAR_0;
		}else {
			switch (dif / 12) {
				case 0:
					gear = DIRECTION_GEAR_1;
					break;
				case 1:
					gear = DIRECTION_GEAR_2;
					break;
				case 2:
					gear = DIRECTION_GEAR_3;
					break;
				case 3:
					gear = DIRECTION_GEAR_4;
					break;
				case 4:
					gear = DIRECTION_GEAR_5;
					break;
				case 5:
					gear = DIRECTION_GEAR_6;
					break;
				case 6:
					gear = DIRECTION_GEAR_7;
					break;
				case 7:
					gear = DIRECTION_GEAR_8;
					break;
				case 8:
					gear = DIRECTION_GEAR_9;
					break;
				case 9:
					gear = DIRECTION_GEAR_10;
					break;
				case 10:
					gear = DIRECTION_GEAR_11;
					break;
				case 11:
					gear = DIRECTION_GEAR_12;
					break;
				case 12:
					gear = DIRECTION_GEAR_13;
					break;
				case 13:
					gear = DIRECTION_GEAR_14;
					break;
				case 14:
					gear = DIRECTION_GEAR_15;
					break;
				case 15:
					gear = DIRECTION_GEAR_16;
					break;
				case 16:
					gear = DIRECTION_GEAR_17;
					break;
				case 17:
					gear = DIRECTION_GEAR_18;
					break;
				case 18:
					gear = DIRECTION_GEAR_19;
					break;
				case 19:
				case 20:
					gear = DIRECTION_GEAR_20;
					break;
				default:
					break;
			}
		}
		if ((x - RockerRectX2) <= 0){
			return gear;
		}else{
			return -gear;
		}
	}

}