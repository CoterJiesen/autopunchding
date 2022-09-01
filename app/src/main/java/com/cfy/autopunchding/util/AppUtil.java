package com.cfy.autopunchding.util;

import static com.cfy.autopunchding.common.Com.DD_PACKAGE_NAME;

import android.os.SystemClock;
import android.util.Log;

import com.cfy.autopunchding.email.EmaiUtil;
import com.cfy.autopunchding.event.PunchFinishedEvent;
import com.cfy.autopunchding.event.PunchType;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * author: aaron.chen
 * created on: 2018/9/8 10:00
 * description:
 */
public class AppUtil {

    private static OutputStream os;

    private AppUtil() {
    }
    /**
     * 启动钉钉
     */
    public static void startDingDing() {
        Log.d(AppUtil.class.getSimpleName(), "monkey -p com.alibaba.android.rimet -c android.intent.category.LAUNCHER 1");
        String cmd = "monkey -p com.alibaba.android.rimet -c android.intent.category.LAUNCHER 1 \n";
        exec(cmd);
    }
    /**
     * 强退应用
     *
     * @param packageName
     */
    public static void stopApp(String packageName) {
        String cmd = "am force-stop " + packageName + " \n";
        exec(cmd);
    }

    /**
     * 强退服务
     *
     * @param fullServiceName 完整的服务名，包含包名 e.g. package.name/service.name
     */
    public static void stopService(String fullServiceName) {
        String cmd = "adb shell am stopservice " + fullServiceName + " \n";
        exec(cmd);
    }

    /**
     * 滑动屏幕
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    public static void swipe(String x1, String y1, String x2, String y2) {
        String cmd = String.format("input swipe %s %s %s %s \n", x1, y1, x2, y2);
        exec(cmd);
    }

    /**
     * 点击
     *
     * @param x
     * @param y
     */
    public static void clickXY(String x, String y) {
        Log.d(AppUtil.class.getSimpleName(), "clickXY: " + x + ", " + y);
        String cmd = String.format("input tap %s %s \n", x, y);
        exec(cmd);
    }

    /**
     * 设备截屏保存到sdcard
     */
    public static void screencap() {
        Log.d(AppUtil.class.getSimpleName(), "screencap" );
        String cmd = String.format("screencap -p /sdcard/screen.png\n");
        exec(cmd);
    }
    /**
     * 设备截屏保存到sdcard
     */
    public static void rmScreencap() {
        Log.d(AppUtil.class.getSimpleName(), "rm screencap" );
        String cmd = String.format("rm -r /sdcard/screen.png\n");
        exec(cmd);
    }

    /**
     *  # 熄屏
     *         self.adbscreen_off = 'adb shell input keyevent 223'
     *         # 点亮屏幕
     *         self.adbscreen_on = 'adb shell input keyevent 224'
     * @param x
     */
    public static void inputEvent(String x) {
        Log.d(AppUtil.class.getSimpleName(), "inputEvent: " + x );
        String cmd = String.format("input keyevent %s\n", x);
        exec(cmd);
    }

    /**
     * 执行 ADB 命令
     *
     * @param cmd adb 命令
     */
    public static void exec(String cmd) {
        try {
            if (os == null) {
                os = Runtime.getRuntime().exec("su").getOutputStream();
            }
            os.write(cmd.getBytes());
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭通道，执行完命令记得调用该方法。
     */
    public static void close() {
        try {
            if (os != null) {
                os.close();
            }
            os = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 打卡操作
     * @param punchType 上、下
     */
    public static void playCardOppR9s(PunchType punchType) {
        // 唤醒屏幕
        inputEvent("224");

        // 上滑解锁
        swipe("300", "1000", "300", "500");
        SystemClock.sleep(10000);
        // 返回桌面
        inputEvent("3");
        SystemClock.sleep(2000);

        startDingDing();
        SystemClock.sleep(2000);

        clickXY("537", "1822");
        SystemClock.sleep(20000);

        clickXY("130", "1113");
        SystemClock.sleep(20000);

        clickXY("528", "1154");
        SystemClock.sleep(2000);
        screencap();
        SystemClock.sleep(20000);

        // 更新 UI
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(new Date());
        EventBus.getDefault().post(new PunchFinishedEvent(punchType, currentTime));

        EmaiUtil.sendMsgImage("打卡通知", "438653638@qq.com");
        SystemClock.sleep(20000);
        stopApp(DD_PACKAGE_NAME);
        inputEvent("223");
        close();
    }

}
