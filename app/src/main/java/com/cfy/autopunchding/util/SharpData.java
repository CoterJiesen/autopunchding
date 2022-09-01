package com.cfy.autopunchding.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.cfy.autopunchding.event.PunchType;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by zhangxiaoming on 2018/7/26.
 */

public class SharpData {
    private static final String spName = "spName";


    private static final String ORDER_TYPE = "order_type";

    private static final String Compent_TYPE = "compent_type_";

    private static final String open_TYPE = "open_type";
    private static final String email_adress = "email_adress";

    private static final String open_job = "oepn_job";
    private static final String reset_sys = "reset_sys";
    private static final String reset_sys_day = "reset_sys_day";

    private static final String dd_up_time = "dd_up_time";
    private static final String dd_down_time = "dd_down_time";

    private static final String Heightmetrics = "Heightmetrics";
    private static final String widthmetrics = "widthmetrics";


    private static final String on_work_time_pre = "on_work_time_";
    private static final String off_work_time_pre = "off_work_time_";


    public static String getWorkTime(Context context, PunchType type) {
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(System.currentTimeMillis());
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        String order = sp.getString(on_work_time_pre, "");
        Log.d("打卡时间:", order);
        String[] arr = order.split(";");
        if (type != null && arr.length == 2 && type.toString().equals(arr[0]) && arr[1].length() > 10 && date.equals(arr[1].substring(0, 10))) {
            return arr[1];
        }
        return "";
    }

    public static boolean setWorkTime(Context context, PunchType type, Calendar calendar) {
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(calendar.getTime());
        return sp.edit().putString(on_work_time_pre, type.toString() + ";" + time).commit();
    }


    //1,代表已打上班卡,2,代表已打下班卡,0代表还未打卡
    //获取指令类型
    public static int getWorkStatus(Context context) {
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(System.currentTimeMillis());
        String order = sp.getString(Compent_TYPE, "");
        String[] arr = order.split(":");
        if (arr.length == 2 && arr[0].equals(date)) {
            return Integer.parseInt(arr[1]);
        }
        return 0;
    }


    //存储
    public static boolean setWorkStatus(Context context, int order) {
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(System.currentTimeMillis());
        return sp.edit().putString(Compent_TYPE, date + ":" + order).commit();
    }

    // 0默认什么不做,1为打上班卡,2为打下班卡
    //获取指令类型
    public static int getOrderType(Context context) {
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        int order = sp.getInt(ORDER_TYPE, 0);
        return order;
    }


    //存储
    public static void setOrderType(Context context, int order) {
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        sp.edit().putInt(ORDER_TYPE, order).commit();
    }


    //1,代表已打上班卡,2,代表已打下班卡,0代表还未打卡
    //获取指令类型
    public static int getIsCompent(Context context) {
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        int order = sp.getInt(Compent_TYPE, 0);
        return order;
    }


    //存储
    public static void setIsCompent(Context context, int order) {
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        sp.edit().putInt(Compent_TYPE, order).commit();
    }


    //0,开启工作,1代表关闭工作
    //获取指令类型
    public static int getOpenApp(Context context) {
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        int order = sp.getInt(open_TYPE, 0);
        return order;
    }


    //存储邮箱
    public static void setOpenApp(Context context, int order) {
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        sp.edit().putInt(open_TYPE, order).commit();
    }

    public static String getEmailData(Context context) {
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        String order = sp.getString(email_adress, "");
        return order;
    }


    //存储邮箱
    public static boolean setEmailData(Context context, String order) {
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        return sp.edit().putString(email_adress, order).commit();
    }


    //存储邮箱
    public static boolean setNotData(Context context, String order) {
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        return sp.edit().putString("notdata", order).commit();
    }


    //上班打卡开启状态,0,都关闭,1是上班,2是下班,3是上下班,
    public static int getOpenJob(Context context) {
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        int order = sp.getInt(open_job, 0);
        return order;
    }


    //存储邮箱
    public static boolean setOpenJob(Context context, int order) {
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        return sp.edit().putInt(open_job, order).commit();
    }


    //0表示当日未重启,1表示当日已重启
    public static boolean getResetSys(Context context) {
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        int order = sp.getInt(reset_sys, 0);
        String sys_dat = sp.getString(reset_sys_day, "");
        String s = new SimpleDateFormat("dd").format(new Date(System.currentTimeMillis()));

        if (s.equals(sys_dat)) {
            //判断为当天
            if (order == 0) {
                return false;
            } else {
                return true;
            }


        }
        return false;

    }


    //
    public static boolean setResetSys(Context context) {
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        String s = new SimpleDateFormat("dd").format(new Date(System.currentTimeMillis()));
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(reset_sys_day, s);
        editor.putInt(reset_sys, 1);

        return editor.commit();
    }


//    //存储上班时间
//    public static String getDDupTime(Context context) {
//        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
//        String order = sp.getString(dd_up_time, Comm.upJobTime);
//        return order;
//    }
//
//
//    //存储上班时间
//    public static boolean setDDupTime(Context context, String order) {
//        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
//        return sp.edit().putString(dd_up_time, order).commit();
//    }
//
//
//    //获取下班时间
//    public static String getDDdownTime(Context context) {
//        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
//        String order = sp.getString(dd_down_time, Comm.downJobTime);
//        return order;
//    }
//
//
//    //存储下班时间
//    public static boolean setDDdownTime(Context context, String order) {
//        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
//        return sp.edit().putString(dd_down_time, order).commit();
//    }


//    //获取屏幕高度
//    public static int getHeightmetrics(Context context) {
//        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
//        int order = sp.getInt(Heightmetrics, Comm.heightmetrics_defult);
//        return order;
//    }
//
//
//    public static boolean setHeightmetrics(Context context, int order) {
//        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
//        return sp.edit().putInt(Heightmetrics, order).commit();
//    }
//
//    //获取屏幕宽度
//    public static int getWidthmetrics(Context context) {
//        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
//        int order = sp.getInt(widthmetrics, Comm.widthmetrics_defult);
//        return order;
//    }
//
//
//    public static boolean setWidthmetrics(Context context, int order) {
//        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
//        return sp.edit().putInt(widthmetrics, order).commit();
//    }
}
