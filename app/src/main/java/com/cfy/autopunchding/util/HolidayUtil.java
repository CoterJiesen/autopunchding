package com.cfy.autopunchding.util;

import android.content.Context;
import android.util.Log;

import com.cfy.autopunchding.common.Com;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class HolidayUtil {
    //假期配置文件
    private JSONObject configHoliday;
    //假期列表
    private final List<String> holidayList = new ArrayList<>();
    //周末上班列表
    private final List<String> workdayOfWeekdayList = new ArrayList<>();

    private static HolidayUtil holidayUtil;
    public static HolidayUtil getInstance(Context context)
    {
        if (holidayUtil == null)
        {
            synchronized (HolidayUtil.class)
            {
                if (holidayUtil == null)
                    holidayUtil = new HolidayUtil(context);
            }
        }
        return holidayUtil;
    }

    private HolidayUtil(Context context)
    {
        updateHoliday(context);
        parseHoliday();
    }

    private String loadJSONConfig(Context context) {
        String json = null;
        try (InputStream is = context.openFileInput(Com.holidayFile)) {
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

    private void writeJSONConfig(Context context, String json) {
        try (OutputStream out = context.openFileOutput(Com.holidayFile, Context.MODE_PRIVATE)) {
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
    private void updateHoliday(Context context) {
        JSONObject config = new JSONObject();
        int yearCurrent = Integer.parseInt(new SimpleDateFormat("yyyy", Locale.CHINA).format(System.currentTimeMillis()));
        String confJson = loadJSONConfig(context);
        try {
            if (null != confJson && !confJson.isEmpty()) {
                config = new JSONObject(confJson);
                int year = config.getJSONObject("holiday").getInt("year");
                if (year == yearCurrent) {
                    return;
                }
            }
            String netHoliday = OkHttpUtil.getInstance().get(Com.holidayUpdateUrl+yearCurrent);
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
                writeJSONConfig(context,config.toString());
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
            Log.d("parseHoliday",  "节假日获取失败！");
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
    public boolean ifSkipPlayCard() {
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
