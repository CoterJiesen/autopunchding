package com.cfy.autopunchding.util;

import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by zhangxiaoming on 2018/9/17.
 */

public class OkHttpUtil
{


    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final int MAX_TIMEOUT = 5; //最大超时

    private OkHttpClient okHttpClient;
    private static OkHttpUtil okHttpUtil;


    public static OkHttpUtil getInstance()
    {
        if (okHttpUtil == null)
        {

            synchronized (OkHttpUtil.class)
            {
                if (okHttpUtil == null)
                    okHttpUtil = new OkHttpUtil();

            }
        }
        return okHttpUtil;
    }

    public OkHttpUtil()
    {

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.readTimeout(MAX_TIMEOUT, TimeUnit.SECONDS);
        okHttpClient = builder.build();
    }

    /**
     * 同步请求
     *
     * @param url
     * @param parm
     * @return
     */
    public String post(String url, String parm)
    {
        Log.d("请求地址-请求数据->" ,url + parm);
        String ret = null;
        try
        {
            RequestBody body = RequestBody.create(JSON, parm);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            Response response = okHttpClient.newCall(request).execute();
            int code = response.code();
            if (code == 200)
            {
                String res = response.body().string();
                if (!((null == res || res.trim().length() == 0)))
                {
                    ret = res;
                }
            }
        } catch (IOException e)
        {
            return ret;
        }
        Log.d("响应数据-->" , ret);

        return ret;
    }

    public String get(String url)
    {
        String ret = null;
        try
        {
            RequestBody body = RequestBody.create(JSON, "");
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();
            Response response = okHttpClient.newCall(request).execute();
            int code = response.code();
            if (code == 200)
            {
                String res = response.body().string();
                if (!((null == res || res.trim().length() == 0)))
                {
                    ret = res;
                }
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return ret;

    }
}
