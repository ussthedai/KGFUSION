package com.usst.kgfusion.util;

import com.alibaba.fastjson.JSON;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.protocol.Protocol;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestHelper {

    /**
     * HTTP请求
     * @param surl 接口请求url
     * @param json 接口请求body-json字符串
     *
     * @return 接口返回结果
     */

    public static String sendJsonWithHttp(String surl, String json) throws IOException {
        URL url = new URL(surl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
        conn.setRequestMethod("POST");// 提交模式
        conn.setRequestProperty("Content-Length", json.getBytes().length + "");
        conn.setConnectTimeout(100000);// 连接超时单位毫秒 //
        conn.setReadTimeout(200000);// 读取超时 单位毫秒
        conn.setDoOutput(true);// 是否输入参数
        conn.setDoInput(true);
        conn.setUseCaches(false);
        conn.connect();
        BufferedReader reader = null;
        StringBuilder sb = null;
        try{
            DataOutputStream out = new DataOutputStream(conn.getOutputStream());
            out.write(json.getBytes());
            out.flush();
            out.close();
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
            {
                sb.append(line);
            }
        } catch (IOException e) {
            throw new IOException("IO异常");
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        

        return sb.toString();
    }

    public static void main(String[] args) throws IOException {
        String url = "http://169.254.125.89:12345/EntityGraphRest";
        String url_sim = "http://169.254.125.89:6668/wordSimilarity";
        String url_relcal = "http://169.254.125.89:12345/RelationGraphRest";
        // List<String> re = new ArrayList<>();
        // re.add("安全防护");
        // re.add("安全存储");
        // List<String> re1 = new ArrayList<>();
        // re1.add("大数据开发支撑");
        // re1.add("大数据平台");

        // String json = "{\"ins\":[\"集群负载均衡\", \"服务路由\"]}";
        // String jsonStr = JSON.toJSONString(re);
        // System.out.println(json);
        // String result = sendJsonWithHttp(url,jsonStr);
        // Map<String, Object> req = new HashMap<>();
        // req.put("word1", re);
        // req.put("word2",re1);
        // req.put("threshold",0.3);

        HashMap<String, List<List<String>>> req = new HashMap<>();
        List<List<String>> li = new ArrayList<>();
        List<String> li1 = new ArrayList<>();
        List<String> li2 = new ArrayList<>();
        li1.add("服务管理");
        li1.add("服务路由");
        li2.add("广播");
        li2.add("聚合");
        li.add(li1);
        li.add(li2);
        req.put("ins", li);
        String jsonStr = JSON.toJSONString(req);
        String result = sendJsonWithHttp(url_relcal, jsonStr);

        System.out.println(result);
        // System.out.println(result);

    }
}
