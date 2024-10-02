package com.testbubble; // replace your-apps-package-name with your app’s package name
import static android.content.Context.LAYOUT_INFLATER_SERVICE;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.Map;
import java.util.HashMap;
import java.util.Random;

public class ActionModule extends ReactContextBaseJavaModule {
    private boolean isListen = false;
    protected WindowManager wm;
    private FrameLayout Layout;
    private LayoutInflater li;
    private View myview;
    protected ImageView view;
    protected   WindowManager.LayoutParams params;
    public int width, height;
    private NotificationManager nm;
    private MediaPlayer mp,sp,bp;
    private CountDownTimer touched;
    private int Touch=0;
    private long timu=5000;
    private int timer;
    private boolean sticky= false, over=false, under=false;
    private CountDownTimer running, timing, moving;
    private int sens=1;
    private boolean isPaused = true;
    private boolean isMuted = false;
    Handler handler = new Handler(Looper.getMainLooper());

//    NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
    boolean isFlipped = false;
    AnimationDrawable walk_an, blink_an;
    Context context;

    @NonNull
    @Override
    public String getName() {
        return "ActionModule";
    }

    private void updateBooleanVariable(boolean newValue) {
        isListen = newValue;
    }

    @SuppressLint("ObsoleteSdkInt")
    private boolean hasOverlayPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        return Settings.canDrawOverlays(getReactApplicationContext());
    }

    private static final int OVERLAY_PERMISSION_REQUEST_CODE = 100;
    private boolean isOverlayPermissionRequested = false;

    @SuppressLint("ObsoleteSdkInt")
    private boolean checkDrawOverlayPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        if (!Settings.canDrawOverlays(getReactApplicationContext())) {
            if (!isOverlayPermissionRequested) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + getReactApplicationContext().getPackageName()));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                Activity currentActivity = getCurrentActivity();
                if (currentActivity != null) {
                    currentActivity.startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
                    isOverlayPermissionRequested = true;
                }
            }

            return false;
        }

        return true;
    }

//Module
    ActionModule(ReactApplicationContext context) {
        super(context);
    }

    @ReactMethod
    public void onCreate() {
//        super.onCreate();
        if (!checkDrawOverlayPermission()) {
            return;
        }
        ShimejiView();
        randomsens();
        handler.post(draw());
    }

    @ReactMethod
    public void hideNotifyHead() {
//        super.onDestroy();
            if (view != null && view.getParent() != null) {
                // updateBooleanVariable(false);
//                count = 0;
                wm.removeView(view);
                wm = null;
            }
    }
    @ReactMethod
    private class action implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:

                    if (isFlipped) {
                        // Nếu đang lật, chuyển về animation bình thường
                        view.setBackgroundResource(R.drawable.walk);
                        walk_an = (AnimationDrawable)view.getBackground();
                        walk_an.start();
                    } else {
                        // Nếu bình thường, chuyển sang animation lật

                        // Lật sang trạng thái ngược
                        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "scaleX", 1f, -1f);
                        animator.setDuration(200);
                        animator.start();
                    }

                    // Đổi trạng thái
                    isFlipped = !isFlipped; // Chuyển đổi trạng thái
                    return true;
                case MotionEvent.ACTION_MOVE:
                    int x_cord = (int) event.getRawX();
                    int y_cord = (int) event.getRawY();
                    params.x= (x_cord-(width/2));
                    params.y =(y_cord-(height/2)-150);
                    wm.updateViewLayout(view, params);
                    break;
                case MotionEvent.ACTION_UP:
//                touchcheck();
                    view.setBackgroundResource(R.drawable.walk);
                    walk_an = (AnimationDrawable)view.getBackground();
                    walk_an.start();
                    break;
                case MotionEvent.ACTION_OUTSIDE:
            }
            return false;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @ReactMethod
    private void ShimejiView()
    {
        Context context = getReactApplicationContext();
        view = new ImageView(getReactApplicationContext());
        view.setBackgroundResource(R.drawable.walk);
        walk_an = (AnimationDrawable)view.getBackground();
        walk_an.start();
        view.setOnTouchListener(new action());
        li = (LayoutInflater) getReactApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        wm = (WindowManager) getReactApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                        WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.NO_GRAVITY;
        myview = li.inflate(R.layout.playground, null);

        wm.addView(view, params);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;
    }

    @ReactMethod
    private void randomsens()
    {
        Random s = new Random();
        timing = new CountDownTimer(8000,(s.nextInt(16000-1000)+1000))
        {
            @Override
            public void onTick(long millisUntilFinished)
            {
                Random r = new Random();
                sens = r.nextInt(6) + 1;
            }
            @Override
            public void onFinish() {
                timing.start();
            }
        }.start();
    }

    private int minLimitX;
    private int maxLimitX;
    private int minLimitY;
    private int maxLimitY;

    @ReactMethod
    private Runnable draw() {
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;
        int screenHeight = size.y;

        // Cập nhật giới hạn
        minLimitX = -300;
        maxLimitX = 280; // Giới hạn bên phải
        minLimitY = 0;
        maxLimitY = screenHeight - view.getHeight();
        running = new CountDownTimer(10000, 16) {
            @Override
            public void onTick(long millisUntilFinished) {
                switch (sens) {
                    case 1: // Di chuyển sang phải
                        if (params.x < maxLimitX) {
                            params.x++;
                            right();
                        } else {
                            sens = 2; // Đổi hướng sang trái
                        }
                        break;

                    case 2: // Di chuyển sang trái
                        if (params.x > minLimitX) {
                            params.x--;
                            left();
                        } else {
                            sens = 1; // Đổi hướng sang phải
                        }
                        break;

                    case 3: // Di chuyển chéo lên phải
                        if (params.x < maxLimitX && params.y > minLimitY) {
                            params.x++;
                            params.y--;
                            right();
                            // up();
                        } else {
                            sens = 4; // Đổi hướng chéo lên trái
                        }
                        break;

                    case 4: // Di chuyển chéo lên trái
                        if (params.x > minLimitX && params.y > minLimitY) {
                            params.x--;
                            params.y--;
                            left();
                            // up();
                        } else {
                            sens = 3; // Đổi hướng chéo xuống phải
                        }
                        break;

                    case 5: // Di chuyển chéo xuống phải
                        if (params.x < maxLimitX && params.y < maxLimitY) {
                            params.x++;
                            params.y++;
                            right();
                            // down();
                        } else {
                            sens = 6; // Đổi hướng chéo xuống trái
                        }
                        break;

                    case 6: // Di chuyển chéo xuống trái
                        if (params.x > minLimitX && params.y < maxLimitY) {
                            params.x--;
                            params.y++;
                            left();
                            // down();
                        } else {
                            sens = 5; // Đổi hướng chéo lên phải
                        }
                        break;
                }

                // Cập nhật lại view sau khi thay đổi params
                wm.updateViewLayout(view, params);
            }
            @Override
            public void onFinish() {
                running.start(); // Khởi động lại khi kết thúc
            }
        }.start();
        return null;
    }

    @ReactMethod
    private void left()
    {
        view.setBackgroundResource(R.drawable.walk);
        walk_an = (AnimationDrawable)view.getBackground();
        walk_an.start();
    }
    @ReactMethod
    private void right()
    {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "scaleX", -1f, 1f);
        animator.setDuration(400);
        animator.start();
    }
    @ReactMethod
    public void getIsListen() {
        WritableMap eventData = Arguments.createMap();
        eventData.putString("isListen", Boolean.toString(isListen));
        eventData.putString("typeMess", "data transfer");
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("LISTEN", eventData);
    }
}