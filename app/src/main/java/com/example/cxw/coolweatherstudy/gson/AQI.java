package com.example.cxw.coolweatherstudy.gson;

/**
 * Created by cxw on 2017/10/23.
 */

//映射天气预报json数据的aqi部分
public class AQI {

    public AQICity city;

    public class AQICity {
        public String aqi;
        public String pm25;
    }

}
