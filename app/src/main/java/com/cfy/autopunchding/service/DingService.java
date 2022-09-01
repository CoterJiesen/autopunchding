package com.cfy.autopunchding.service;

import static com.cfy.autopunchding.util.AppUtil.playCardOppR9s;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import com.cfy.autopunchding.event.PunchType;
import com.cfy.autopunchding.util.HolidayUtil;

import java.util.Calendar;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import it.sauronsoftware.cron4j.Scheduler;

public class DingService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.e(getPackageName(), "Service Start!");

        ClockThread ct = new ClockThread();
        ct.start();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(getPackageName(), "scheduler.onDestroy");
    }

    public class ClockThread extends Thread {

        public void run() {

            Scheduler scheduler = new Scheduler();
            // 秒 分 小时 天 月份 星期 年
            //设置打开时间为工作日的 8:20,18:20
            scheduler.schedule("20 8,18 * * *", new Thread() {
                @Override
                public void run() {
                    if(HolidayUtil.getInstance(getApplicationContext()).ifSkipPlayCard()){
                        return;
                    }
                    Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.CHINA);
                    int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
                    int minus = hourOfDay == 8 ? new Random().nextInt(28):new Random().nextInt(160);
                    Log.e(getPackageName(), "等待?分钟开始打卡：" + minus);
                    SystemClock.sleep(minus * 1000 * 60);
                    playCardOppR9s(hourOfDay == 8 ? PunchType.CLOCK_IN : PunchType.CLOCK_OUT);
                }
            });
            scheduler.start();
            Log.e(getPackageName(), "开始调度任务");
        }
    }

}
