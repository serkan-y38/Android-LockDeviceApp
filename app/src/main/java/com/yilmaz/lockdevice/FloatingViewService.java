package com.yilmaz.lockdevice;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.util.Objects;

public class FloatingViewService extends Service {

    private NotificationManager notificationManager = null;
    private WindowManager windowManager;
    private View view;
    private WindowManager.LayoutParams params;

    static String ACTION_START = "ACTION_START";
    static String ACTION_STOP = "ACTION_STOP";
    static String SERVICE_ACTION = "SERVICE_ACTION";
    static String CHANNEL_ID = "CHANNEL_ID";
    static String NOTIFICATION_NAME = "NOTIFICATION_NAME ";

    public FloatingViewService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        getNotificationManager();
        switch (Objects.requireNonNull(intent.getStringExtra(SERVICE_ACTION))) {
            case "ACTION_START":
                start();
                break;
            case "ACTION_STOP":
                stop();
                break;
        }
        return START_STICKY;
    }

    private void stop() {
        windowManager.removeView(view);
        notificationManager.deleteNotificationChannel(CHANNEL_ID);
        stopForeground(true);
        stopSelf();
    }

    private void start() {
        startForeground(1, buildNotification());
        setUpFloatingView();
    }

    @SuppressLint({"RtlHardcoded", "InflateParams"})
    private void setUpFloatingView() {
        view = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null);
        int LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.RIGHT;
        params.x = 0;
        params.y = 100;

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManager.addView(view, params);

        view.findViewById(R.id.close_btn).setOnClickListener(view -> {
            DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            if (devicePolicyManager.isAdminActive(new ComponentName(FloatingViewService.this, Controller.class)))
                devicePolicyManager.lockNow();
        });

        view.findViewById(R.id.mainFl).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(view, params);
                        break;
                }
                return false;
            }
        });
    }

    private void createNotificationChannel() {
        NotificationChannel notificationChannel = new NotificationChannel(
                CHANNEL_ID,
                NOTIFICATION_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
        );
        notificationChannel.setSound(null, null);
        notificationChannel.setShowBadge(true);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    private void getNotificationManager() {
        notificationManager = ContextCompat.getSystemService(
                this,
                NotificationManager.class
        );
    }

    private Notification buildNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setOngoing(true)
                .setContentText(getString(R.string.app_name) + " app running on foreground")
                .setColorized(true)
                .setSmallIcon(R.drawable.baseline_android_24)
                .setOnlyAlertOnce(true)
                .setContentIntent(pIntent)
                .build();
    }

}