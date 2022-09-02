package com.cfy.autopunchding;

import static com.cfy.autopunchding.common.Com.EXTRA_PUNCH_TYPE;
import static com.cfy.autopunchding.util.AppUtil.requestRoot;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.NotificationManagerCompat;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.cfy.autopunchding.event.PunchFinishedEvent;
import com.cfy.autopunchding.event.PunchType;
import com.cfy.autopunchding.event.TaskEvent;
import com.cfy.autopunchding.event.TaskType;
import com.cfy.autopunchding.service.DingService;
import com.cfy.autopunchding.service.KeepRunningService;
import com.cfy.autopunchding.service.ManualPunchService;
import com.cfy.autopunchding.service.NotifyService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Calendar;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 9527;

    private Button btnActivity;
    private Switch btnSwitchService;

    private TextView tvClockInTime;

    private TextView tvClockOutTime;

    private TextView tvTip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        startService(new Intent(this, KeepRunningService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initView() {
        tvClockInTime = findViewById(R.id.tv_clock_in_time);
        tvClockOutTime = findViewById(R.id.tv_clock_out_time);
        btnActivity = (Button) findViewById(R.id.button_check_permission);//与Activity的XML中对应的ID绑定
        btnSwitchService = (Switch) findViewById(R.id.switch_auto_playcard);//与Activity的XML中对应的ID绑定
        tvTip = (TextView) findViewById(R.id.mTextTip);//与Activity的XML中对应的ID绑定
        //设置OnClickListener事件（JAVA没事件？）
        btnActivity.setOnClickListener(v -> {
            requestRoot();
            requestPermission();
        });
        btnSwitchService.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Intent intent=new Intent();
            intent.setClass(this,DingService.class);
            if (isChecked){
                startService(intent);
                tvTip.setText("开启");
            }else {
                stopService(intent);
                tvTip.setText("关闭");
            }
        });

    }


    /**
     * 开始打卡点击事件
     */
    public void manualPunch(View view) {
        Calendar now = Calendar.getInstance();
        PunchType punchType = now.get(Calendar.HOUR_OF_DAY) <= 12 ?
                PunchType.CLOCK_IN : PunchType.CLOCK_OUT;
        Intent punchIntent = new Intent(this, ManualPunchService.class);
        punchIntent.putExtra(EXTRA_PUNCH_TYPE, punchType);
        startService(punchIntent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPunchFinished(PunchFinishedEvent event) {
        String text = "";
        if (event.getPunchType() == PunchType.CLOCK_IN) {
            text = getString(R.string.punch_clock_in_time, event.getTime());
            tvClockInTime.setText(text);
        } else if (event.getPunchType() == PunchType.CLOCK_OUT) {
            text = getString(R.string.punch_clock_out_time, event.getTime());
            tvClockOutTime.setText(text);
        }
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTipView(TaskEvent event) {
        if (event.getTaskType() == TaskType.ON) {
            if(!btnSwitchService.isChecked()) {
                btnSwitchService.setChecked(true);
                switchService(true);
            }
        } else if (event.getTaskType() == TaskType.OFF) {
            if(btnSwitchService.isChecked()){
                btnSwitchService.setChecked(false);
                switchService(false);
            }
        }
        tvTip.setText(event.getTip());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 请求权限
     */
    public void requestPermission() {
        if(!isIgnoringBatteryOptimizations()){
            requestIgnoreBatteryOptimizations();
        }else {
            Toast.makeText(this, "电池白名单已开启", Toast.LENGTH_LONG).show();
        }
        if (!isNLServiceEnabled()) {
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            startActivityForResult(intent, REQUEST_CODE);
        } else {
            Toast.makeText(this, "通知服务已开启", Toast.LENGTH_LONG).show();
            toggleNotificationListenerService();
        }
    }

    /**
     * 是否启用通知监听服务
     *
     * @return
     */
    public boolean isNLServiceEnabled() {
        Set<String> packageNames = NotificationManagerCompat.getEnabledListenerPackages(this);
        if (packageNames.contains(getPackageName())) {
            return true;
        }
        return false;
    }

    /**
     * 切换通知监听器服务
     */
    public void toggleNotificationListenerService() {
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(getApplicationContext(), NotifyService.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        pm.setComponentEnabledSetting(new ComponentName(getApplicationContext(), NotifyService.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    private boolean isIgnoringBatteryOptimizations() {
        boolean isIgnoring = false;
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            isIgnoring = powerManager.isIgnoringBatteryOptimizations(getPackageName());
        }
        return isIgnoring;
    }
    public void requestIgnoreBatteryOptimizations() {
        try {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void switchService(boolean isChecked){
        Intent intent=new Intent();
        intent.setClass(this,DingService.class);
        if (isChecked){
            startService(intent);
            tvTip.setText("开启");
        }else {
            stopService(intent);
            tvTip.setText("关闭");
        }
    }
}
