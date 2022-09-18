package com.usst.kgfusion.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.usst.kgfusion.databaseQuery.BasicOperation;
import com.usst.kgfusion.databaseQuery.KGASSETOperation;
import com.usst.kgfusion.entrance.MergeAuto;
import com.usst.kgfusion.pojo.Res;
import com.usst.kgfusion.pojo.WSDRes;
import org.neo4j.driver.v1.types.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@EnableAsync
@RequestMapping("api/graph/synthesize")
public class
WsdController {

    private static final Logger logger = LoggerFactory.getLogger("com.usst.test");

    @Autowired
    private Ciyixiaoqi ciyixiaoqi;



    @PostMapping("/wsd/res")//歧义分析
    public String word_sense_disambiguation_res(@RequestBody String jsonStr)  {
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        String symbol = jsonObject.getString("graphSymbol");

        if(!jsonObject.containsKey("graphSymbol") ){
            return JSON.toJSONString(new Res(false, 1002, "参数错误", null));
        }
        String graphSymbol = jsonObject.getString("graphSymbol");
        if(graphSymbol == null || graphSymbol.equals("")){
            return JSON.toJSONString(new Res(false, 1002, "graphSymbol不能为空", null));
        }
        Node node = BasicOperation.getRandomNode(graphSymbol);
        if (node == null) {
            return JSON.toJSONString(new Res(false, 1007, "该图无数据，请检查graphSymbol对应的图是否真实存在", null));
        }




        // fenxi
        ciyixiaoqi.excutewsd(graphSymbol);

        // return JSON.toJSONString(new Res(true, 200, "分析成功", null));
        return JSON.toJSONString(new Res(true, 200, "词义消歧已接受请求,分析中...", null));
    }

    @PostMapping("/wsd/merge")//歧义处置neo4j
    public String word_sense_disambiguation_merge(@RequestBody String jsonStr)  {

        JSONObject jsonObject = JSONObject.parseObject(jsonStr);

        String symbol = jsonObject.getString("graphSymbol");
        String ids = jsonObject.getString("ids");
        String aim_id = jsonObject.getString("aim_id");
        String aim_update = jsonObject.getString("aim_update");
        Map<String,Object> json_res = new HashMap<>();
        json_res.put("graphSymbol",symbol);
        json_res.put("ids",ids);
        json_res.put("aim_id",aim_id);
        json_res.put("aim_update",aim_update);


        Map<String, Object> ans = MergeAuto.executeWsd2(json_res);
        Boolean success = (Boolean)ans.get("success");

        if(success){
            aim_id = (String) ans.get("aim_id");

            return JSON.toJSONString(new WSDRes(true, 200, "操作成功", aim_id));
        }else{
            return JSON.toJSONString(new Res(false, 1002, "操作失败，请检查图是否存在或节点是否存在", null));

        }


    }


}
@Service
class Ciyixiaoqi{

    private static final Logger logger = LoggerFactory.getLogger("com.usst.test");
    @Async
    public void excutewsd(String symbol) {
        String task_id_random = UUID.randomUUID().toString();
        Boolean sa = KGASSETOperation.WSDFX_queryIsNull(symbol);
        if(!sa){
            String idf = KGASSETOperation.queryTaskId_wsd(symbol);
            if(idf.length()!=0 || idf!=null){
                task_id_random = idf;
            }
        }

        Long begint = System.currentTimeMillis();


        Map<String, Object> ans = MergeAuto.executeWsd1(symbol,task_id_random);
        logger.info("词义分析任务信息已入库");
        Long zonghekaishi = System.currentTimeMillis();
        int flag = (Integer)ans.get("flag");
        if(flag == 1){
            Map<String, String> record = new HashMap<String, String>();

            Long endt = System.currentTimeMillis();

            record.put("task_id", task_id_random);
            record.put("graph_symbol",symbol);
            record.put("task_type", "0");
            record.put("datasouce_type", "2");
            record.put("task_status", "1");
            record.put("task_status_desc", "词义歧义分析成功");
            record.put("begin_time",new Timestamp(begint).toString());
            record.put("end_time",new Timestamp(endt).toString());

            KGASSETOperation.WSDTask_gl_insertIntoKgAsset(record);
        } else if(flag==0){

            Map<String, String> record = new HashMap<String, String>();
            Long endt = System.currentTimeMillis();
            record.put("task_id", task_id_random);

            record.put("graph_symbol",symbol);
            record.put("task_type", "0");
            record.put("datasouce_type", "2");
            record.put("task_status", "0");
            record.put("task_status_desc", "词义歧义分析失败，无图谱");
            record.put("begin_time",new Timestamp(begint).toString());
            record.put("end_time",new Timestamp(endt).toString());

            KGASSETOperation.WSDTask_gl_insertIntoKgAsset(record);
        }  else if(flag==2){

            Map<String, String> record = new HashMap<String, String>();
            Long endt = System.currentTimeMillis();
            record.put("task_id", task_id_random);

            record.put("graph_symbol",symbol);
            record.put("task_type", "0");
            record.put("datasouce_type", "2");
            record.put("task_status", "1");
            record.put("task_status_desc", "分析完成，未找到歧义信息");
            record.put("begin_time",new Timestamp(begint).toString());
            record.put("end_time",new Timestamp(endt).toString());

            KGASSETOperation.WSDTask_gl_insertIntoKgAsset(record);
        } else if(flag==3){

            Map<String, String> record = new HashMap<String, String>();
            Long endt = System.currentTimeMillis();
            record.put("task_id", task_id_random);

            record.put("graph_symbol",symbol);
            record.put("task_type", "0");
            record.put("datasouce_type", "2");
            record.put("task_status", "1");
            record.put("task_status_desc", "分析完成，图谱内不存在同名不同类型的词语集合");
            record.put("begin_time",new Timestamp(begint).toString());
            record.put("end_time",new Timestamp(endt).toString());

            KGASSETOperation.WSDTask_gl_insertIntoKgAsset(record);
        }

    }
}