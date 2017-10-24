package com.example.cxw.coolweatherstudy.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cxw on 2017/10/23.
 */

// 映射天气预报返回Json数据中的suggestion部分
public class Suggestion {

    @SerializedName("comf")
    public Comfort comfor;

    @SerializedName("cw")
    public CarWash carWash;

    @SerializedName("sport")
    public Sport sport;


    public class Comfort {
        @SerializedName("txt")
        public String info;
    }

    public class CarWash {
        @SerializedName("txt")
        public String info;
    }

    public class Sport {
        @SerializedName("txt")
        public String info;
    }
}
