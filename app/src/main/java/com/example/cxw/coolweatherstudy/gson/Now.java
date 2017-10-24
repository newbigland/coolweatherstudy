package com.example.cxw.coolweatherstudy.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cxw on 2017/10/23.
 */

//映射天气预报json的Now部分
public class Now {

    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public class More {
        @SerializedName("txt")
        public String info;
    }
}
