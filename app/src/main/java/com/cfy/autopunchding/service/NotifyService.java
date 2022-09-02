package com.cfy.autopunchding.service;

import static com.cfy.autopunchding.common.Com.EXTRA_PUNCH_TYPE;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.cfy.autopunchding.common.Com;
import com.cfy.autopunchding.email.EmaiUtil;
import com.cfy.autopunchding.event.PunchType;
import com.cfy.autopunchding.event.TaskEvent;
import com.cfy.autopunchding.event.TaskType;
import com.cfy.autopunchding.util.SharpData;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
/**
 * @author llw
 * @description NotifyService
 * @date 2021/8/5 19:14
 */
public class NotifyService extends NotificationListenerService {

    public static final String TAG = "NotifyService";

    public static final String QQ = "com.tencent.mobileqq";//qq信息
    public static final String WX = "com.tencent.mm";//微信信息
    public static final String MMS = "com.android.mms";//短信
    public static final String HONOR_MMS = "com.hihonor.mms";//荣耀短信
    public static final String MESSAGES = "com.google.android.apps.messaging";//信息
    public static final String IN_CALL = "com.android.incallui";//来电
    public static final String DingDing = "com.alibaba.android.rimet";//钉钉

    /**
     * 发布通知
     *
     * @param sbn 状态栏通知
     */
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        switch (sbn.getPackageName()) {
            case MESSAGES:
            case MMS:
            case HONOR_MMS:
                Log.d(TAG, "收到短信");
                break;
            case QQ:
                Log.d(TAG, "收到QQ消息");
                onReceiveMessageDaka(sbn);
                break;
            case WX:
                Log.d(TAG, "收到微信消息");
                onReceiveMessageDaka(sbn);
                break;
            case IN_CALL:
                Log.d(TAG, "收到来电");
                break;
            case DingDing:
                Log.d(TAG, "收到DingDing");
                onReceiveMessage(sbn);
                break;
            default:
                break;
        }
    }

    /**
     * 通知已删除
     *
     * @param sbn 状态栏通知
     */
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        switch (sbn.getPackageName()) {
            case MESSAGES:
            case MMS:
            case HONOR_MMS:
                Log.d(TAG, "移除短信");
                break;
            case QQ:
                Log.d(TAG, "移除QQ消息");
                break;
            case WX:
                Log.d(TAG, "移除微信消息");
                break;
            case IN_CALL:
                Log.d(TAG, "移除来电");
                break;
            default:
                break;
        }
    }

    /**
     * 监听断开
     */
    @Override
    public void onListenerDisconnected() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // 通知侦听器断开连接 - 请求重新绑定
            requestRebind(new ComponentName(this, NotificationListenerService.class));
        }
    }

    public void onReceiveMessage(StatusBarNotification sbn) {
            if (sbn.getNotification() == null ||sbn.getNotification().tickerText == null) return;
            //消息内容
            Notification notification = sbn.getNotification();
            String tikeText = notification.tickerText == null ? "" : notification.tickerText.toString();
            String notTitle = notification.extras.getString("android.title") == null ? "" : notification.extras
                    .getString("android.title");//标题
            String subText = notification.extras.getString("android.subText") == null ? "" : notification.extras
                    .getString("android.subText");//摘要
            String text = notification.extras.getString("android.text") == null ? "" : notification.extras
                    .getString("android.text");  //正文
            String postTime = new SimpleDateFormat("yyyy-MM-dd",Locale.CHINA).format(new Date(notification.when));   //通知时间

            Log.d(TAG,"通知时间-->" + postTime);
            Log.d(TAG,"通知-->tikeText:" + tikeText);
            Log.d(TAG,"通知-->标题:" + notTitle + "--摘要--" + subText + "--正文--" + text);

            SharpData.setNotData(getApplicationContext(), "通知时间-->" + postTime + "-通知-->tikeText:" + tikeText +
                    "通知-->标题:" + notTitle + "--摘要--" + subText + "--正文--" + text);

            //首先判断通知时间是不是当前时间
            String nowTime = new SimpleDateFormat("yyyy-MM-dd",Locale.CHINA).format(System.currentTimeMillis());
            //如果是当天
            if (nowTime.equals(postTime) && text.contains("打卡·成功")) {
                int hour = Integer.parseInt(new SimpleDateFormat("HH", Locale.CHINA).format(new Date(notification.when)));
                if (hour<12) {
//                    SharpData.setIsCompent(getApplicationContext(), 1);
                    EmaiUtil.sendMsg("上班打卡成功通知:"+text, Com.emil);
                    SharpData.setWorkStatus(getApplicationContext(), 1);
                }
                if (hour>18) {
//                    SharpData.setIsCompent(getApplicationContext(), 2);
                    EmaiUtil.sendMsg("下班打卡成功通知:"+text, Com.emil);
                    SharpData.setWorkStatus(getApplicationContext(), 2);
                }
            }
    }

    public void onReceiveMessageDaka(StatusBarNotification sbn) {
        if (sbn.getNotification() == null ||sbn.getNotification().tickerText == null) return;
        //消息内容
        Notification notification = sbn.getNotification();
        String text = notification.extras.getString("android.text") == null ? "" : notification.extras
                .getString("android.text");  //正文
        String postTime = new SimpleDateFormat("yyyy-MM-dd",Locale.CHINA).format(new Date(notification.when));   //通知时间
        //首先判断通知时间是不是当前时间
        String nowTime = new SimpleDateFormat("yyyy-MM-dd",Locale.CHINA).format(System.currentTimeMillis());
        //如果是当天
        if (nowTime.equals(postTime)) {
            if (text.contains("上班打卡")) {
                Intent punchIntent = new Intent(this, ManualPunchService.class);
                PunchType punchType =  PunchType.CLOCK_IN;
                punchIntent.putExtra(EXTRA_PUNCH_TYPE, punchType);
                startService(punchIntent);
            }
            if (text.contains("下班打卡")) {
                Intent punchIntent = new Intent(this, ManualPunchService.class);
                PunchType punchType =  PunchType.CLOCK_OUT;
                punchIntent.putExtra(EXTRA_PUNCH_TYPE, punchType);
                startService(punchIntent);
            }
            if (text.contains("开启任务")) {
                EventBus.getDefault().post(new TaskEvent(TaskType.ON, "开启调度任务"));
            }
            if (text.contains("关闭任务")) {
                EventBus.getDefault().post(new TaskEvent(TaskType.OFF, "关闭调度任务"));
            }
        }
    }
}