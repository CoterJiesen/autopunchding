package com.ajiew.autopunchding.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
//import android.support.v4.app.NotificationCompat;

import com.ajiew.autopunchding.R;
import com.ajiew.autopunchding.broadcast.AutoStartReceiver;
import com.ajiew.autopunchding.broadcast.PunchReceiver;
import com.ajiew.autopunchding.common.Com;

/**
 * author: aaron.chen
 * created on: 2018/9/9 15:21
 * description: Make sure this will keep running
 */
public class KeepRunningService extends Service {

    private PunchReceiver receiver;

    @Override
    public void onCreate() {
        super.onCreate();

        receiver = new PunchReceiver();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Notification.Builder builder = new Notification.Builder(this.getApplicationContext())
                .setContentTitle("AutoPunchDing")
                .setContentText("Day Day Up!")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setWhen(System.currentTimeMillis());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String NOTIFICATION_CHANNEL_ID = this.getClass().getCanonicalName();
            String channelName = this.getClass().getSimpleName();
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            channel.setLightColor(Color.BLUE);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager managerNo = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert managerNo != null;
            managerNo.createNotificationChannel(channel);

            builder = new Notification.Builder(this.getApplicationContext(), NOTIFICATION_CHANNEL_ID)
                    .setContentTitle("AutoPunchDing")
                    .setContentText("Day Day Up!")
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setWhen(System.currentTimeMillis())
                    .setCategory(Notification.CATEGORY_SERVICE);
        }
        startForeground(101, builder.build());

        // 闹钟也会发送这个广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(receiver, filter);

        // 每 5 分钟跑一次
        int time = 5 * 60 * 1000;
        long triggerAtTime = System.currentTimeMillis() + time;
        addAlarm(0, triggerAtTime);

        return START_STICKY;
    }

    private void addAlarm(int alarmId,  long triggerAtTime) {
        Intent myIntent = new Intent(this, AutoStartReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, alarmId, myIntent, 0);
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (manager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtTime, pi);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                manager.setExact(AlarmManager.RTC_WAKEUP, triggerAtTime, pi);
            } else {
                manager.set(AlarmManager.RTC_WAKEUP, triggerAtTime, pi);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopForeground(true);
        unregisterReceiver(receiver);
    }
}
