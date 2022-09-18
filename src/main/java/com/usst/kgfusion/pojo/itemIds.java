package com.usst.kgfusion.pojo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;


import java.util.List;

public class itemIds {
    int dataSourceId;
    JSONArray items;

    public static void main(String[] args) {
//        String content = "[\"487581847707131904\",\"770713190448758184\"]";
//        JSONArray contentArray = JSONArray.parseArray(content);
        String tex1 = "[{\"dataSourceId\":60,\"items\":[\"487581847707131904\",\"770713190448758184\"]},{\"dataSourceId\":60,\"items\":[\"184770713190448758\"]}]";
        String tex = "[{\"dataSourceId\":60,\"items\":[\"487581847707131904\"]}]";
        System.out.println(tex);
        List<Object>  users = JSONArray.parseArray(tex);
        JSONObject JSJ = JSONObject.parseObject(tex);
        System.out.println("sd");
    }

}


