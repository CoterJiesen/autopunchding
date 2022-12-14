package com.cfy.autopunchding.service;

import android.app.IntentService;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.cfy.autopunchding.common.Com;
import com.cfy.autopunchding.email.EmaiUtil;
import com.cfy.autopunchding.event.PunchFinishedEvent;
import com.cfy.autopunchding.event.PunchType;
import com.cfy.autopunchding.util.HolidayUtil;
import com.cfy.autopunchding.util.OkHttpUtil;
import com.cfy.autopunchding.util.SharpData;
import com.cfy.autopunchding.util.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static com.cfy.autopunchding.common.Com.DD_PACKAGE_NAME;
import static com.cfy.autopunchding.util.AppUtil.clickXY;
import static com.cfy.autopunchding.util.AppUtil.close;
import static com.cfy.autopunchding.util.AppUtil.inputEvent;
import static com.cfy.autopunchding.util.AppUtil.playCardOppR9s;
import static com.cfy.autopunchding.util.AppUtil.screencap;
import static com.cfy.autopunchding.util.AppUtil.startDingDing;
import static com.cfy.autopunchding.util.AppUtil.stopApp;
import static com.cfy.autopunchding.util.AppUtil.swipe;

public class PunchService extends IntentService {

    private PunchType punchType;
    private PowerManager powerManager;
    private KeyguardManager keyguardManager;
    private String punchPositionY;

    Handler handler = new Handler(Looper.getMainLooper());

    public PunchService() {
        super("PunchService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
    }

    /**
     * ????????????????????? adb ??????????????????????????????
     * ???????????????????????????????????????????????????????????????????????????????????????Developer options -> Input -> Pointer location
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (!timeForPunch() || !ifDirectCard()) {
            Log.d(this.getClass().getSimpleName(), "?????????????????????????????????????????????????????????"+SharpData.getWorkTime(getApplicationContext(),punchType));
            stopSelf();
            return;
        }
        Log.d(this.getClass().getSimpleName(), "onHandleIntent: start punching...");

        playCardOppR9s(punchType);

        SharpData.setWorkStatus(getApplicationContext(), SharpData.getWorkStatus(getApplicationContext()) + (punchType == PunchType.CLOCK_IN ? 1 : 2));

        stopSelf();
    }

    /**
     * ???????????????????????????????????????
     * 1?????????????????????????????????
     * 2???????????????????????????
     *
     * @return true for punching time
     */
    private boolean timeForPunch() {
        if(HolidayUtil.getInstance(getApplicationContext()).ifSkipPlayCard()){
            Log.d("????????????","?????????????????????");
            return false;
        }
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.CHINA);
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        if (hourOfDay == 8 && minute > 10) {
            punchType = PunchType.CLOCK_IN;
            punchPositionY = "920";
            if(SharpData.getWorkStatus(getApplicationContext()) >= 1){
                Log.d("CLOCK_IN:","???????????????");
                return false;
            }
            //??????????????????????????????
            if(SharpData.getWorkTime(getApplicationContext(),punchType).isEmpty()){
                int min = Math.min((int) (minute + Math.random() * 40), 50);
                calendar.set(Calendar.MINUTE,min);
                SharpData.setWorkTime(getApplicationContext(), punchType, calendar);
            }
            return true;
        }
        if (hourOfDay >= 18) {
            punchType = PunchType.CLOCK_OUT;
            punchPositionY = "1550";
            if(SharpData.getWorkStatus(getApplicationContext()) >= 2){
                Log.d("CLOCK_OUT:","???????????????");
                return false;
            }
            //??????????????????????????????
            if(SharpData.getWorkTime(getApplicationContext(),punchType).isEmpty()){
                int min = (int) (minute + Math.random() * 80) % 60;
                int hour = hourOfDay;
                if(min>30){
                    hour++;
                    min = Math.min(min, 50);
                }
                calendar.set(Calendar.MINUTE,min);
                calendar.set(Calendar.HOUR_OF_DAY,hour);
                SharpData.setWorkTime(getApplicationContext(), punchType, calendar);
            }
            return true;
        }
        return false;
    }

    /**
     * ??????????????????
     * ?????????????????????????????????????????????
     * @return true,?????????false????????????
     */
    private boolean ifDirectCard(){
        String time = SharpData.getWorkTime(getApplicationContext(), punchType);
        if (time.isEmpty()) {
            return false;
        }
        Log.d(this.getClass().getSimpleName(), "??????????????????????????????????????????-???????????????"+time);
        try {
            Date cardTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.CHINA).parse(time);
            //??????????????????????????????????????????????????????
            if(new Date().after(cardTime)){
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * ????????????
     */
    private void wakeUp() {
        boolean screenOn = powerManager.isScreenOn();
        if (!screenOn) {
            PowerManager.WakeLock wl = powerManager.newWakeLock(
                    PowerManager.ACQUIRE_CAUSES_WAKEUP |
                            PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");
            wl.acquire(10000);
            wl.release();
        }

        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("unLock");
        keyguardLock.reenableKeyguard();
        keyguardLock.disableKeyguard();
    }

    /**
     * ?????????????????????????????????
     */
    private void inputPinIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (keyguardManager.isDeviceLocked()) {
                clickXY("315", "1305");
                clickXY("715", "2115");
                clickXY("715", "1305");
                clickXY("315", "1600");
                clickXY("1105", "2115");
            }
        }
    }

    /**
     * ????????????
     */
    private void startAppLauncher(String packageName) {
        Intent intent = this.getPackageManager().getLaunchIntentForPackage(packageName);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /**
     * ?????? Toast ??????
     */
    private void showToast(final String text) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                ToastUtil.showToast(text);
            }
        });
    }
}