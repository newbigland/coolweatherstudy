package com.example.cxw.coolweatherstudy.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.example.cxw.coolweatherstudy.gson.Weather;
import com.example.cxw.coolweatherstudy.util.HttpUtil;
import com.example.cxw.coolweatherstudy.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {

    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // 服务间隔时间为8小时，8小时后onStartCommand()方法重新执行,实现了后台定时更新数据功能
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        updateBingPic();
        //下面是定时任务代码
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int anHour = 8 * 60 * 60 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        alarmManager.cancel(pi);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    //更新天气信息
    private void updateWeather() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        if (weatherString != null) { //有缓存数据时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            String weatherId = weather.basic.weatherId;
            String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=93520a39fe684633a5f10625ed92d733";
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {  //调用天气API接口发送网络请求
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body().toString();
                    Weather weather = Utility.handleWeatherResponse(responseText);  //解析响应的天气数据为weather对象
                    if (weather != null && "ok".equals(weather.status)) {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("weather", responseText);  //将数据缓存
                        editor.apply();
                    }
                }
            });
        }
    }


    //更新必应每日一图
    private void updateBingPic() {

        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
            }
        });
    }
}
