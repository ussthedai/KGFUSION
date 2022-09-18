package com.usst.kgfusion.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.usst.kgfusion.databaseQuery.KGASSETOperation;
import com.usst.kgfusion.entrance.MergeAuto;
import com.usst.kgfusion.pojo.MergeResData;
import com.usst.kgfusion.pojo.Res;

import com.usst.kgfusion.pojo.SimResItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.usst.kgfusion.databaseQuery.BasicOperation;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@EnableAsync
@RequestMapping("api/graph/synthesize")
public class MergeController {

    private static final Logger logger = LoggerFactory.getLogger("com.usst.test");

    @Value("${commonApi.entity_classify_url}")
    private String url_Type;

    @Value("${commonApi.entity_similarity_url}")
    private String url_similarity;

    @Value("${commonApi.useWho}")
    private Integer useWho;





    @PostMapping("/auto")//自动综合
    public String merge_auto(@RequestBody String jsonStr)  {
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        String from = jsonObject.getString("graphSymbolSource");
        String destination = jsonObject.getString("graphSymbolAim");
//        String mode = jsonObject.getString("mode");

        // 判断参数是否请求成功
        if(!jsonObject.containsKey("graphSymbolSource") || !jsonObject.containsKey("graphSymbolAim")) {
            Map<String, String> record = new HashMap<String, String>();
            String idf = KGASSETOperation.queryLinkId(destination,from);
            if(idf.length()==0 || idf==null){
                record.put("synthesize_link_id", UUID.randomUUID().toString());
            }else{
                record.put("synthesize_link_id", idf);
            }
            if(jsonObject.containsKey("graphSymbolAim")){
                record.put("kg_symbol_synthesize", destination);
            }
            if(jsonObject.containsKey("graphSymbolSource")){
                record.put("kg_symbol_doc", from);
            }

//            record.put("generate_status", "4");
//            record.put("generate_begin_time", new Timestamp(System.currentTimeMillis())+"");
//            record.put("generate_end_time", new Timestamp(System.currentTimeMillis())+"");
//            record.put("generate_msg", "7");
            record.put("synthesize_status", Integer.toString(3));
//            record.put("synthesize_begin_time", new Timestamp(System.currentTimeMillis())+"");
//            record.put("synthesize_end_time",  new Timestamp(System.currentTimeMillis())+"");
            record.put("synthesize_msg", "未查找到源图谱或目标图谱");
            record.put("analyse_status", Integer.toString(3));
            record.put("analyse_begin_time", new Timestamp(System.currentTimeMillis())+"");
            record.put("analyse_end_time", new Timestamp(System.currentTimeMillis())+"");
            record.put("analyse_msg", "分析失败");
            KGASSETOperation.insertIntoKgAsset(record);

            return JSON.toJSONString(new Res(false, 1002, "参数错误", null));
        }else{
            MergeAuto mg = new MergeAuto();
            Map<String, Object> res = mg.executeRaw(from, destination, url_similarity, url_Type , useWho);
            int flag = (Integer)res.get("flag");
            if(flag == 1){
                Map<String, String> record = new HashMap<String, String>();
                String idf = KGASSETOperation.queryLinkId(destination,from);
                if(idf.length()==0 || idf==null){
                    record.put("synthesize_link_id", UUID.randomUUID().toString());
                }else{
                    record.put("synthesize_link_id", idf);
                }
                record.put("kg_symbol_synthesize", destination);
                record.put("kg_symbol_doc", from);
                record.put("analyse_status", Integer.toString(2));
                if(res.containsKey("analyse_begin_time")){
                    record.put("analyse_begin_time", res.get("analyse_begin_time") + "");
                }
                if(res.containsKey("analyse_end_time")){
                    record.put("analyse_end_time", res.get("analyse_end_time") + "");
                }

                record.put("analyse_msg", "分析成功");

                record.put("synthesize_status", Integer.toString(2));
                if(res.containsKey("synthesize_begin_time")){
                    record.put("synthesize_begin_time", res.get("synthesize_begin_time")+"");
                }
                if(res.containsKey("synthesize_end_time")){
                    record.put("synthesize_end_time",  res.get("synthesize_end_time") + "");
                }

                record.put("synthesize_msg", "综合成功");
                KGASSETOperation.insertIntoKgAsset(record);

                return JSON.toJSONString(new Res(true, 200, "综合成功", null));
            }
            if(flag == 2){
                Map<String, String> record = new HashMap<String, String>();
                String idf = KGASSETOperation.queryLinkId(destination,from);
                if(idf.length()==0 || idf==null){
                    record.put("synthesize_link_id", UUID.randomUUID().toString());
                }else{
                    record.put("synthesize_link_id", idf);
                }
                record.put("kg_symbol_synthesize", destination);
                record.put("kg_symbol_doc", from);
                record.put("analyse_status", Integer.toString(2));
                if(res.containsKey("analyse_begin_time")){
                    record.put("analyse_begin_time", res.get("analyse_begin_time") + "");
                }
                if(res.containsKey("analyse_end_time")){
                    record.put("analyse_end_time", res.get("analyse_end_time") + "");
                }

                record.put("analyse_msg", "分析完成，未分析到可综合节点对");

                record.put("synthesize_status", Integer.toString(2));
                if(res.containsKey("synthesize_begin_time")){
                    record.put("synthesize_begin_time", res.get("synthesize_begin_time")+"");
                }
                if(res.containsKey("synthesize_end_time")){
                    record.put("synthesize_end_time",  res.get("synthesize_end_time") + "");
                }

                record.put("synthesize_msg", "综合分析完成，未分析到可综合节点对");
                KGASSETOperation.insertIntoKgAsset(record);
                logger.error("未分析到相似实体");
                return JSON.toJSONString(new Res(true, 1001, "未找到相似实体", null));
            }
            if(flag == 0){
                    Map<String, String> record = new HashMap<String, String>();
                    String idf = KGASSETOperation.queryLinkId(destination,from);
                    if(idf.length()==0 || idf==null){
                        record.put("synthesize_link_id", UUID.randomUUID().toString());
                    }else{
                        record.put("synthesize_link_id", idf);
                    }
                    record.put("kg_symbol_synthesize", destination);
                    record.put("kg_symbol_doc", from);
                    record.put("analyse_status", Integer.toString(3));
                    if(res.containsKey("analyse_begin_time")){
                        record.put("analyse_begin_time", res.get("analyse_begin_time") + "");
                    }
                    if(res.containsKey("analyse_end_time")){
                        record.put("analyse_end_time", res.get("analyse_end_time") + "");
                    }

                    record.put("analyse_msg", "分析失败");

                    record.put("synthesize_status", Integer.toString(3));
                    if(res.containsKey("synthesize_begin_time")){
                        record.put("synthesize_begin_time", res.get("synthesize_begin_time")+"");
                    }
                    if(res.containsKey("synthesize_end_time")){
                        record.put("synthesize_end_time",  res.get("synthesize_end_time") + "");
                    }

                    record.put("synthesize_msg", "综合失败");
                    KGASSETOperation.insertIntoKgAsset(record);
                    logger.error("数据错误");
                    return JSON.toJSONString(new Res(true, 200, "数据错误", null));
            }
            if(flag == 5){
                Map<String, String> record = new HashMap<String, String>();
                String idf = KGASSETOperation.queryLinkId(destination,from);
                if(idf.length()==0 || idf==null){

                    record.put("synthesize_link_id", UUID.randomUUID().toString());
                }else{

                    record.put("synthesize_link_id", idf);
                }
                record.put("kg_symbol_synthesize", destination);
                record.put("kg_symbol_doc", from);
                record.put("analyse_status", Integer.toString(2));
                if(res.containsKey("analyse_begin_time")){
                    record.put("analyse_begin_time", res.get("analyse_begin_time") + "");
                }
                if(res.containsKey("analyse_end_time")){
                    record.put("analyse_end_time", res.get("analyse_end_time") + "");
                }

                record.put("analyse_msg", "分析成功");

                record.put("synthesize_status", Integer.toString(2));
                if(res.containsKey("synthesize_begin_time")){
                    record.put("synthesize_begin_time", res.get("synthesize_begin_time")+"");
                }
                if(res.containsKey("synthesize_end_time")){
                    record.put("synthesize_end_time",  res.get("synthesize_end_time") + "");
                }

                record.put("synthesize_msg", "综合进行中");
                KGASSETOperation.insertIntoKgAsset(record);
                return JSON.toJSONString(new MergeResData(true, 200, "分析成功,复制文档图谱作为综合图谱", null));

            }
        }

        return JSON.toJSONString(new Res(true, 200, "合并成功", null));
    }



    @PostMapping("/auto/simres")//综合分析
    public String merge_auto_return_simres(@RequestBody String jsonStr)   {
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        String from = jsonObject.getString("graphSymbolSource");
        String destination = jsonObject.getString("graphSymbolAim");
//        String mode = jsonObject.getString("mode");

        // 判断参数是否请求成功
        if(!jsonObject.containsKey("graphSymbolSource") || !jsonObject.containsKey("graphSymbolAim")) {
            Map<String, String> record = new HashMap<String, String>();
            String idf = KGASSETOperation.queryLinkId(destination,from);
            if(idf.length()==0 || idf==null){
                record.put("synthesize_link_id", UUID.randomUUID().toString());
            }else{
                record.put("synthesize_link_id", idf);
            }
            if(jsonObject.containsKey("graphSymbolAim")){
                record.put("kg_symbol_synthesize", destination);
            }
            if(jsonObject.containsKey("graphSymbolSource")){
                record.put("kg_symbol_doc", from);
            }

//            record.put("generate_status", "4");
//            record.put("generate_begin_time", new Timestamp(System.currentTimeMillis())+"");
//            record.put("generate_end_time", new Timestamp(System.currentTimeMillis())+"");
//            record.put("generate_msg", "7");
            record.put("synthesize_status", Integer.toString(3));
//            record.put("synthesize_begin_time", new Timestamp(System.currentTimeMillis())+"");
//            record.put("synthesize_end_time",  new Timestamp(System.currentTimeMillis())+"");
            record.put("synthesize_msg", "未查找到源图谱或目标图谱");
            record.put("analyse_status", Integer.toString(3));
            record.put("analyse_begin_time", new Timestamp(System.currentTimeMillis())+"");
            record.put("analyse_end_time", new Timestamp(System.currentTimeMillis())+"");
            record.put("analyse_msg", "分析失败");
            KGASSETOperation.insertIntoKgAsset(record);
            return JSON.toJSONString(new Res(false, 1002, "参数错误", null));
        }else{

            // fenxi
            Map<String, Object> ans = MergeAuto.execute(from, destination, url_similarity, url_Type, useWho);
            logger.info("综合分析结果已入库");
            int flag = (Integer)ans.get("flag");
            if(flag == 1 && ans.containsKey("data")){
                Map<String, String> record = new HashMap<String, String>();
                String idf = KGASSETOperation.queryLinkId(destination,from);
                if(idf.length()==0 || idf==null){
                    record.put("synthesize_link_id", UUID.randomUUID().toString());
                }else{
                    record.put("synthesize_link_id", idf);
                }
                record.put("kg_symbol_synthesize", destination);
                record.put("kg_symbol_doc", from);
                record.put("analyse_status", Integer.toString(2));
                if(ans.containsKey("analyse_begin_time")){
                    record.put("analyse_begin_time", ans.get("analyse_begin_time") + "");
                }
                if(ans.containsKey("analyse_end_time")){
                    record.put("analyse_end_time", ans.get("analyse_end_time") + "");
                }

                record.put("analyse_msg", "分析成功");

                record.put("synthesize_status", Integer.toString(2));
                if(ans.containsKey("synthesize_begin_time")){
                    record.put("synthesize_begin_time", ans.get("synthesize_begin_time")+"");
                }
                if(ans.containsKey("synthesize_end_time")){
                    record.put("synthesize_end_time",  ans.get("synthesize_end_time") + "");
                }

                record.put("synthesize_msg", "综合成功");
                KGASSETOperation.insertIntoKgAsset(record);
                return JSON.toJSONString(new MergeResData(true, 200, "分析成功", (List<SimResItem>)ans.get("data")));


            }
            if(flag == 2){
                Map<String, String> record = new HashMap<String, String>();
                String idf = KGASSETOperation.queryLinkId(destination,from);
                if(idf.length()==0 || idf==null){
                    record.put("synthesize_link_id", UUID.randomUUID().toString());
                }else{
                    record.put("synthesize_link_id", idf);
                }
                record.put("kg_symbol_synthesize", destination);
                record.put("kg_symbol_doc", from);
                record.put("analyse_status", Integer.toString(3));
                if(ans.containsKey("analyse_begin_time")){
                    record.put("analyse_begin_time", ans.get("analyse_begin_time") + "");
                }
                if(ans.containsKey("analyse_end_time")){
                    record.put("analyse_end_time", ans.get("analyse_end_time") + "");
                }

                record.put("analyse_msg", "分析失败，未分析到可综合节点对");

                record.put("synthesize_status", Integer.toString(3));
                if(ans.containsKey("synthesize_begin_time")){
                    record.put("synthesize_begin_time", ans.get("synthesize_begin_time")+"");
                }
                if(ans.containsKey("synthesize_end_time")){
                    record.put("synthesize_end_time",  ans.get("synthesize_end_time") + "");
                }

                record.put("synthesize_msg", "综合失败，未分析到可综合节点对");
                KGASSETOperation.insertIntoKgAsset(record);

                return JSON.toJSONString(new MergeResData(false, 1001, "未找到相似实体", null));

            }
            if(flag == 0){
                Map<String, String> record = new HashMap<String, String>();
                String idf = KGASSETOperation.queryLinkId(destination,from);
                if(idf.length()==0 || idf==null){
                    record.put("synthesize_link_id", UUID.randomUUID().toString());
                }else{
                    record.put("synthesize_link_id", idf);
                }
                if(jsonObject.containsKey("graphSymbolAim")){
                    record.put("kg_symbol_synthesize", destination);
                }
                if(jsonObject.containsKey("graphSymbolSource")){
                    record.put("kg_symbol_doc", from);
                }

//            record.put("generate_status", "4");
//            record.put("generate_begin_time", new Timestamp(System.currentTimeMillis())+"");
//            record.put("generate_end_time", new Timestamp(System.currentTimeMillis())+"");
//            record.put("generate_msg", "7");
                record.put("synthesize_status", Integer.toString(3));
//                record.put("synthesize_begin_time", new Timestamp(System.currentTimeMillis())+"");
//                record.put("synthesize_end_time",  new Timestamp(System.currentTimeMillis())+"");
                record.put("synthesize_msg", "未查找到源图谱或目标图谱");
                record.put("analyse_status", Integer.toString(3));
                record.put("analyse_begin_time", new Timestamp(System.currentTimeMillis())+"");
                record.put("analyse_end_time", new Timestamp(System.currentTimeMillis())+"");
                record.put("analyse_msg", "分析失败");
                KGASSETOperation.insertIntoKgAsset(record);

                return JSON.toJSONString(new MergeResData(false, 1000, "未找到图谱数据", null));

            }
            if(flag == 5){
                Map<String, String> record = new HashMap<String, String>();
                String idf = KGASSETOperation.queryLinkId(destination,from);
                if(idf.length()==0 || idf==null){
                    record.put("synthesize_link_id", UUID.randomUUID().toString());
                }else{
                    record.put("synthesize_link_id", idf);
                }
                record.put("kg_symbol_synthesize", destination);
                record.put("kg_symbol_doc", from);
                record.put("analyse_status", Integer.toString(2));
                if(ans.containsKey("analyse_begin_time")){
                    record.put("analyse_begin_time", ans.get("analyse_begin_time") + "");
                }
                if(ans.containsKey("analyse_end_time")){
                    record.put("analyse_end_time", ans.get("analyse_end_time") + "");
                }

                record.put("analyse_msg", "分析成功");

                record.put("synthesize_status", Integer.toString(2));
                if(ans.containsKey("synthesize_begin_time")){
                    record.put("synthesize_begin_time", ans.get("synthesize_begin_time")+"");
                }
                if(ans.containsKey("synthesize_end_time")){
                    record.put("synthesize_end_time",  ans.get("synthesize_end_time") + "");
                }

                record.put("synthesize_msg", "综合进行中");
                KGASSETOperation.insertIntoKgAsset(record);
                return JSON.toJSONString(new MergeResData(true, 200, "分析成功,复制文档图谱", null));

            }
            // return JSON.toJSONString(new Res(true, 200, "分析成功", null));

        }
        return JSON.toJSONString(new MergeResData(false, 1003, "未知错误", null));

    }



    @PostMapping("/auto/merge")//综合合并处置neo4j
    public String merge_auto_return_merge(@RequestBody String jsonStr)  {
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);

        // 判断参数是否请求成功
        if(!jsonObject.containsKey("selectedSimRes") || !jsonObject.containsKey("graphSymbolSource") || !jsonObject.containsKey("graphSymbolAim")) {
            Map<String, String> record = new HashMap<String, String>();

            if(jsonObject.containsKey("graphSymbolAim")){
                record.put("kg_symbol_synthesize", jsonObject.getString("graphSymbolAim"));
            }
            if(jsonObject.containsKey("graphSymbolSource")){
                record.put("kg_symbol_doc", jsonObject.getString("graphSymbolSource"));
            }
            String idf = KGASSETOperation.queryLinkId(jsonObject.getString("graphSymbolAim"), jsonObject.getString("graphSymbolSource"));
            if(idf.length()==0 || idf==null){
                record.put("synthesize_link_id", UUID.randomUUID().toString());
            }else{
                record.put("synthesize_link_id", idf);
            }

//            record.put("generate_status", "4");
//            record.put("generate_begin_time", new Timestamp(System.currentTimeMillis())+"");
//            record.put("generate_end_time", new Timestamp(System.currentTimeMillis())+"");
//            record.put("generate_msg", "7");
            record.put("synthesize_status", Integer.toString(3));
//                record.put("synthesize_begin_time", new Timestamp(System.currentTimeMillis())+"");
//                record.put("synthesize_end_time",  new Timestamp(System.currentTimeMillis())+"");
            record.put("synthesize_msg", "输入错误");
//            record.put("analyse_status", Integer.toString(3));
//            record.put("analyse_begin_time", new Timestamp(System.currentTimeMillis())+"");
//            record.put("analyse_end_time", new Timestamp(System.currentTimeMillis())+"");
//            record.put("analyse_msg", "分析失败");
            KGASSETOperation.insertIntoKgAsset(record);
            return JSON.toJSONString(new Res(false, 1002, "参数错误", null));
        }else{
            String array_str = jsonObject.getString("selectedSimRes");
            if(array_str.isEmpty()){
                Map<String, String> record = new HashMap<String, String>();
                String idf = KGASSETOperation.queryLinkId(jsonObject.getString("graphSymbolAim"), jsonObject.getString("graphSymbolSource"));
                if(idf.length()==0 || idf==null){
                    record.put("synthesize_link_id", UUID.randomUUID().toString());
                }else{
                    record.put("synthesize_link_id", idf);
                }
                if(jsonObject.containsKey("graphSymbolAim")){
                    record.put("kg_symbol_synthesize", jsonObject.getString("graphSymbolAim"));
                }
                if(jsonObject.containsKey("graphSymbolSource")){
                    record.put("kg_symbol_doc", jsonObject.getString("graphSymbolSource"));
                }
                record.put("synthesize_status", Integer.toString(3));
                record.put("synthesize_msg", "无输入");
                KGASSETOperation.insertIntoKgAsset(record);
                return JSON.toJSONString(new Res(true, 200, "操作成功", null));
            }
            List<SimResItem> sim_items = JSON.parseArray(array_str, SimResItem.class);
            if(sim_items.size() == 0){
                Map<String, String> record = new HashMap<String, String>();
                String idf = KGASSETOperation.queryLinkId(jsonObject.getString("graphSymbolAim"), jsonObject.getString("graphSymbolSource"));
                if(idf.length()==0 || idf==null){
                    record.put("synthesize_link_id", UUID.randomUUID().toString());
                }else{
                    record.put("synthesize_link_id", idf);
                }
                if(jsonObject.containsKey("graphSymbolAim")){
                    record.put("kg_symbol_synthesize", jsonObject.getString("graphSymbolAim"));
                }
                if(jsonObject.containsKey("graphSymbolSource")){
                    record.put("kg_symbol_doc", jsonObject.getString("graphSymbolSource"));
                }
                record.put("synthesize_status", Integer.toString(3));
                record.put("synthesize_msg", "无输入");
                KGASSETOperation.insertIntoKgAsset(record);
                return JSON.toJSONString(new Res(true, 200, "操作成功", null));
            }
            Map<String, Object> ans = MergeAuto.execute2(sim_items, jsonObject.get("graphSymbolSource").toString(), jsonObject.get("graphSymbolAim").toString());
            Boolean success = (Boolean)ans.get("success");
            if(success){
                Map<String, String> record = new HashMap<String, String>();
                String idf = KGASSETOperation.queryLinkId(jsonObject.getString("graphSymbolAim"), jsonObject.getString("graphSymbolSource"));
                if(idf.length()==0 || idf==null){
                    record.put("synthesize_link_id", UUID.randomUUID().toString());
                }else{
                    record.put("synthesize_link_id", idf);
                }
                record.put("kg_symbol_synthesize", jsonObject.get("graphSymbolAim").toString());
                record.put("kg_symbol_doc", jsonObject.get("graphSymbolSource").toString());

                record.put("synthesize_status", Integer.toString(2));
                if(ans.containsKey("synthesize_begin_time")){
                    record.put("synthesize_begin_time", ans.get("synthesize_begin_time")+"");
                }
                if(ans.containsKey("synthesize_end_time")){
                    record.put("synthesize_end_time",  ans.get("synthesize_end_time") + "");
                }

                record.put("synthesize_msg", "综合成功");
                KGASSETOperation.insertIntoKgAsset(record);
                return JSON.toJSONString(new Res(true, 200, "操作成功", null));
            }else{
                Map<String, String> record = new HashMap<String, String>();
                String idf = KGASSETOperation.queryLinkId(jsonObject.getString("graphSymbolAim"), jsonObject.getString("graphSymbolSource"));
                if(idf.length()==0 || idf==null){
                    record.put("synthesize_link_id", UUID.randomUUID().toString());
                }else{
                    record.put("synthesize_link_id", idf);
                }
                if(jsonObject.containsKey("graphSymbolAim")){
                    record.put("kg_symbol_synthesize", jsonObject.getString("graphSymbolAim"));
                }
                if(jsonObject.containsKey("graphSymbolSource")){
                    record.put("kg_symbol_doc", jsonObject.getString("graphSymbolSource"));
                }
                record.put("synthesize_status", Integer.toString(3));
                record.put("synthesize_msg", "输入错误");
                KGASSETOperation.insertIntoKgAsset(record);
                return JSON.toJSONString(new Res(false, 1002, "操作失败，请检查图是否存在或节点是否存在", null));
            }
        }

    }



}

class Zonghefenxi{
    private static final Logger logger = LoggerFactory.getLogger("com.usst.test");


    @Async
    public void executezhfx(String from,String destination,String url_similarity,String url_Type, int useWho,JSONObject jsonObject ,double sim_value){

    }
}
