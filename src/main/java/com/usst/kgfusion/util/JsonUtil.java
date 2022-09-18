package com.usst.kgfusion.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class JsonUtil {

    public static String readJsonStr(InputStream in) throws IOException{
        
        StringBuffer sb = new StringBuffer();
        BufferedReader br = null;
        int ch = 0;
        try{
            br = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));  // 部署的时候要注意编码问题
            while ((ch = br.read()) != -1) sb.append((char) ch);
        }catch(IOException e){
            throw e;
        }finally{
            if(br != null) br.close();
        }

        return sb.toString();
    }

//    public static void writeJsonStr(Object obj, String fileName) throws IOException{
//        BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, Charset.forName("UTF-8")));
//        bw.write(JSON.toJSONString(obj, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNonStringKeyAsString));
//        bw.close();
//    }
}
