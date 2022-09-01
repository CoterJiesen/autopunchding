package com.cfy.autopunchding.service;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.Nullable;

import com.cfy.autopunchding.email.EmaiUtil;
import com.cfy.autopunchding.event.PunchFinishedEvent;
import com.cfy.autopunchding.event.PunchType;
import com.cfy.autopunchding.util.AppUtil;
import com.cfy.autopunchding.util.SharpData;
import com.cfy.autopunchding.util.ToastUtil;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.cfy.autopunchding.common.Com.DD_PACKAGE_NAME;
import static com.cfy.autopunchding.common.Com.EXTRA_PUNCH_TYPE;
import static com.cfy.autopunchding.util.AppUtil.clickXY;
import static com.cfy.autopunchding.util.AppUtil.inputEvent;
import static com.cfy.autopunchding.util.AppUtil.playCardOppR9s;
import static com.cfy.autopunchding.util.AppUtil.screencap;
import static com.cfy.autopunchding.util.AppUtil.startDingDing;
import static com.cfy.autopunchding.util.AppUtil.stopApp;
import static com.cfy.autopunchding.util.AppUtil.swipe;

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
            punchType = (PunchType) intent.getSerializableExtra(EXTRA_PUNCH_TYPE);
        }

        playCardOppR9s(punchType);

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
