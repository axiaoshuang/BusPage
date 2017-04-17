package com.win.buspage.base;

import android.app.Application;

import com.google.gson.Gson;
import com.win.buspage.network.OkHttpClientManager;

import okhttp3.OkHttpClient;

/**
 * Author: wangshuang
 * Time: 2017/4/17
 * Email:xiaoshuang990@sina.com
 */

public class BaseApplication extends Application {

    private static BaseApplication instance;
    private static Gson gson;
    private static OkHttpClient okHttpClient;

    @Override
    public void onCreate() {
        super.onCreate();
        initUtil();
    }

    public static BaseApplication getInstance() {
        if (instance == null) {
            instance = new BaseApplication();
        }
        return instance;
    }


    public static Gson getGson() {
        if (gson==null) {
            gson=new Gson();
        }
        return gson;
    }

    private static void initUtil() {
        okHttpClient = OkHttpClientManager.getInstance().getOkHttpClient();
        getGson();
    }


    public static  OkHttpClient getOkHttpClient(){
        return okHttpClient;
    }
}
