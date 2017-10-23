package com.example.cxw.coolweatherstudy.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by cxw on 2017/10/23.
 */

public class HttpUtil {

    public static void sendOkHttpRequest(String address,okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback); //enqueue()是异步请求方法，开启子线程执行
    }
}
