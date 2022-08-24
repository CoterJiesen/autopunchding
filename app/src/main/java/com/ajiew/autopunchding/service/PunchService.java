package com.ajiew.autopunchding.service;

import android.app.IntentService;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.ajiew.autopunchding.common.Com;
import com.ajiew.autopunchding.email.EmaiUtil;
import com.ajiew.autopunchding.event.PunchFinishedEvent;
import com.ajiew.autopunchding.event.PunchType;
import com.ajiew.autopunchding.util.AppUtil;
import com.ajiew.autopunchding.util.OkHttpUtil;
import com.ajiew.autopunchding.util.SharpData;
import com.ajiew.autopunchding.util.ToastUtil;

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

import static com.ajiew.autopunchding.common.Com.DD_PACKAGE_NAME;
import static com.ajiew.autopunchding.util.AppUtil.clickXY;
import static com.ajiew.autopunchding.util.AppUtil.close;
import static com.ajiew.autopunchding.util.AppUtil.inputEvent;
import static com.ajiew.autopunchding.util.AppUtil.screencap;
import static com.ajiew.autopunchding.util.AppUtil.stopApp;
import static com.ajiew.autopunchding.util.AppUtil.swipe;

public class PunchService extends IntentService {

    private PunchType punchType;
    private PowerManager powerManager;
    private KeyguardManager keyguardManager;
    private String punchPositionY;

    //假期配置文件
    private JSONObject configHoliday;
    //假期列表
    private final List<String> holidayList = new ArrayList<>();
    //周末上班列表
    private final List<String> workdayOfWeekdayList = new ArrayList<>();


    Handler handler = new Handler(Looper.getMainLooper());

    public PunchService() {
        super("PunchService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);

        init();
    }

    /**
     * 处理打卡，利用 adb 命令点击屏幕完成打卡
     * 不同屏幕坐标位置不同，可以在开发者选项中开启查看屏幕坐标：Developer options -> Input -> Pointer location
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (!timeForPunch() || !ifDirectCard()) {
            Log.d(this.getClass().getSimpleName(), "今天不打卡或者打卡时间为空或者已打卡："+SharpData.getWorkTime(getApplicationContext(),punchType));
            stopSelf();
            return;
        }
        Log.d(this.getClass().getSimpleName(), "onHandleIntent: start punching...");

        // 唤醒屏幕
        wakeUp();
        // 上滑解锁
        swipe("300", "1000", "300", "500");
        SystemClock.sleep(1000);

//        // 输入 PIN 码解锁
//        inputPinIfNeeded();
//        SystemClock.sleep(3000);

        showToast("打开钉钉");
        startAppLauncher(DD_PACKAGE_NAME);
        SystemClock.sleep(8000);

        showToast("点击中间菜单");
        clickXY("537", "1822");
        SystemClock.sleep(5000);

        showToast("点击考勤打卡");
        clickXY("130", "1113");
        SystemClock.sleep(8000);

        showToast("点击打卡");
        clickXY("528", punchPositionY);
        SystemClock.sleep(800);
        screencap();
        SystemClock.sleep(5000);

//        showToast("点击拍照");
//        clickXY("710", "2280");
//        SystemClock.sleep(8000);
//
//        showToast("点击 OK");
//        clickXY("710", "2281");
//        SystemClock.sleep(5000);

        startAppLauncher(getPackageName());

        // 更新 UI
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(new Date());
        EventBus.getDefault().post(new PunchFinishedEvent(punchType, currentTime));
        Log.d(this.getClass().getSimpleName(), "onHandleIntent: punch finished");
        SharpData.setWorkStatus(getApplicationContext(), punchType == PunchType.CLOCK_IN ? 1 : 2);

        EmaiUtil.sendMsgImage("打卡通知", "438653638@qq.com");
        SystemClock.sleep(10000);
        stopApp(DD_PACKAGE_NAME);
        screenOff();
        close();

        stopSelf();
    }

    /**
     * 检查是否是到上下班打卡时间
     * 1、检查当天是否已经打卡
     * 2、设置当天打卡时间
     *
     * @return true for punching time
     */
    private boolean timeForPunch() {
        if(ifSkipPlayCard()){
            Log.d("不打卡！","节假日或者周末");
            return false;
        }
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.CHINA);
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        if (hourOfDay == 8 && minute > 10) {
            punchType = PunchType.CLOCK_IN;
            punchPositionY = "920";
            if(SharpData.getWorkStatus(getApplicationContext()) == 1){
                Log.d("CLOCK_IN:","已打上班卡");
                return false;
            }
            //设置随机延迟打卡时间
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
            if(SharpData.getWorkStatus(getApplicationContext()) == 2){
                Log.d("CLOCK_OUT:","已打下班卡");
                return false;
            }
            //设置随机延迟打卡时间
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
     * 是否直接打卡
     * 判断打卡时间是否超过当前时间，
     * @return true,打卡；false，不打卡
     */
    private boolean ifDirectCard(){
        String time = SharpData.getWorkTime(getApplicationContext(), punchType);
        if (time.isEmpty()) {
            return false;
        }
        Log.d(this.getClass().getSimpleName(), "判断打卡时间是否超过当前时间-打卡时间："+time);
        try {
            Date cardTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.CHINA).parse(time);
            //当前时间已经过了设定的打卡时间：打卡
            if(new Date().after(cardTime)){
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 唤醒屏幕
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
     * 息屏
     */
    private void screenOff() {
        inputEvent("223");
    }

    /**
     * 如果需要的话输入解锁码
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
     * 启动应用
     */
    private void startAppLauncher(String packageName) {
        Intent intent = this.getPackageManager().getLaunchIntentForPackage(packageName);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /**
     * 显示 Toast 消息
     */
    private void showToast(final String text) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                ToastUtil.showToast(text);
            }
        });
    }

    public void init() {
        new Thread(() -> {
            updateHoliday();
            parseHoliday();
        }).start();
    }
    private String loadJSONConfig() {
        String json = null;
        try (InputStream is = getApplicationContext().openFileInput(Com.holidayFile)) {
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private void writeJSONConfig(String json) {
        try (OutputStream out = getApplicationContext().openFileOutput(Com.holidayFile, Context.MODE_PRIVATE)) {
            out.write(json.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新假日到配置文件
     *
     * @return 配置文件json
     */
    private void updateHoliday() {
        JSONObject config = new JSONObject();
        int yearCurrent = Integer.parseInt(new SimpleDateFormat("yyyy", Locale.CHINA).format(System.currentTimeMillis()));
        String confJson = loadJSONConfig();
        try {
            if (null != confJson && !confJson.isEmpty()) {
                config = new JSONObject(confJson);
                int year = config.getJSONObject("holiday").getInt("year");
                if (year == yearCurrent) {
                    return;
                }
            }
            String netHoliday = OkHttpUtil.getInstence().get(Com.holidayUpdateUrl+yearCurrent);
            JSONObject jsonNetHoliday = new JSONObject(netHoliday);
            if (0 == jsonNetHoliday.getInt("code")) {
                JSONArray days = new JSONArray();
                JSONObject objHolidays = jsonNetHoliday.getJSONObject("holiday");
                Iterator<String> keys = objHolidays.keys();
                while (keys.hasNext()) {
                    JSONObject value = objHolidays.optJSONObject(keys.next());
                    days.put(value);
                }
                config.put("holiday", new JSONObject().put("year", yearCurrent).put("days", days));
                writeJSONConfig(config.toString());
                Log.d("config",config.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            configHoliday = config;
        }
    }

    private void parseHoliday() {
        if (null == configHoliday || configHoliday.length() == 0) {
            Toast.makeText(getApplicationContext(), "节假日获取失败！", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            JSONArray days = configHoliday.getJSONObject("holiday").getJSONArray("days");
            int length = days.length();
            for (int i = 0; i < length; i++) {
                JSONObject day = days.getJSONObject(i);
                String date = day.getString("date");
                if (day.getBoolean("holiday")) {
                    holidayList.add(date);
                } else {
                    workdayOfWeekdayList.add(date);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("节假日：", holidayList.toString());
        Log.d("周末上班：" ,workdayOfWeekdayList.toString());
    }

    //是否跳过打卡
    private boolean ifSkipPlayCard() {
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(System.currentTimeMillis());
        //判断是否节假日
        if (holidayList.contains(date)) {
            return true;
        }
        //判断是否周末上班
        int weekDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        return (weekDay == Calendar.SATURDAY || weekDay == Calendar.SUNDAY) && !workdayOfWeekdayList.contains(date);
    }
}