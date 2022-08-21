package com.ajiew.autopunchding.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.Nullable;

import com.ajiew.autopunchding.MainActivity;
import com.ajiew.autopunchding.event.PunchFinishedEvent;
import com.ajiew.autopunchding.event.PunchType;
import com.ajiew.autopunchding.util.AppUtil;
import com.ajiew.autopunchding.util.ToastUtil;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.ajiew.autopunchding.service.PunchService.DD_PACKAGE_NAME;
import static com.ajiew.autopunchding.util.AppUtil.clickXY;
import static com.ajiew.autopunchding.util.AppUtil.stopApp;

/**
 * author: aaron.chen
 * created on: 2018/9/8 10:36
 * description: 手动打卡
 */
public class ManualPunchService extends IntentService {

    private PunchType punchType = PunchType.CLOCK_IN;

    private Handler handler = new Handler(Looper.getMainLooper());

    public ManualPunchService() {
        super(ManualPunchService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            punchType = (PunchType) intent.getSerializableExtra(MainActivity.EXTRA_PUNCH_TYPE);
        }

        String punchPositionY = punchType == PunchType.CLOCK_IN ? "1153" : "1154";

        showToast("打开钉钉");
        startAppLauncher(DD_PACKAGE_NAME);
        SystemClock.sleep(8000);

//        showToast("点击中间菜单");
//        clickXY("537", "1822");
//        SystemClock.sleep(5000);
//
//        showToast("点击考勤打卡");
//        clickXY("130", "1113");
//        SystemClock.sleep(8000);
//
//        showToast("点击打卡");
//        clickXY("528", punchPositionY);
//        SystemClock.sleep(5000);

//        showToast("点击拍照");
//        clickXY("710", "2280");
//        SystemClock.sleep(8000);
//
//        showToast("点击 OK");
//        clickXY("710", "2281");
//        SystemClock.sleep(5000);

        startAppLauncher(getPackageName());
        stopApp(DD_PACKAGE_NAME);

        // 更新 UI
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(new Date());
        EventBus.getDefault().post(new PunchFinishedEvent(punchType, currentTime));

        AppUtil.close();
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
}
