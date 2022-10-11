package com.usst.kgfusion.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonUtil {

    private static final Logger logger = LoggerFactory.getLogger("com.usst.test");

    public static String readJsonStr(InputStream in) throws IOException{
        
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        int ch = 0;
        try{
            br = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));  // 部署的时候要注意编码问题
            while ((ch = br.read()) != -1) sb.append((char) ch);
        }catch(IOException e){
            throw e;
        }finally{
            
            if(br != null) 
            try{
                br.close();
            }catch(IOException e){
                logger.error("failed to close file");
            }
            
        }

        return sb.toString();
    }

//    public static void writeJsonStr(Object obj, String fileName) throws IOException{
//        BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, Charset.forName("UTF-8")));
//        bw.write(JSON.toJSONString(obj, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNonStringKeyAsString));
//        bw.close();
//    }
}
