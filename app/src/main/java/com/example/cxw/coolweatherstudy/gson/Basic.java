package com.example.cxw.coolweatherstudy.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cxw on 2017/10/23.
 */

//映射天气预报数据json中的basic部分
public class Basic {

    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update {
        @SerializedName("loc")
        public String updateTime;
    }
}
