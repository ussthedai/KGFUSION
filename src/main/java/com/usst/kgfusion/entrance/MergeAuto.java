package com.usst.kgfusion.entrance;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.usst.kgfusion.constructer.GraphReader;
import com.usst.kgfusion.controller.SimGuiZe;
import com.usst.kgfusion.databaseQuery.BasicOperation;
import com.usst.kgfusion.databaseQuery.KGASSETOperation;
import com.usst.kgfusion.pojo.*;
import org.neo4j.driver.v1.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MergeAuto {

    /**
     *
     * @param from 源知识图谱
     * @param destination 目标知识图谱
     */

    private static final Logger logger = LoggerFactory.getLogger("com.usst.test");

    //综合分析
    public static Map<String, Object> execute(String from, String destination, String sim_url, String type_url, Integer useWho)   {
        logger.info("综合分析已接收参数,文档图谱symbol:" + from + " 综合图谱symbol:" + destination );
        Map<String, Object> res = new LinkedHashMap<>();
        Long fenxikaishi = System.currentTimeMillis();
        Long zonghekaishi = System.currentTimeMillis();
        int flag = 1;
        Boolean check1 = BasicOperation.checkGraphExist(from);
        Boolean check2 = BasicOperation.checkGraphExist(destination);
        if(!check1 && !check2){

            logger.error("Neo4j里没有graphsymbol为综合图谱:"+destination+"的图谱");
            logger.error("Neo4j里没有graphsymbol为文档图谱:"+from+"的图谱");
            flag = 0;

            res.put("flag", flag);
            res.put("analyse_begin_time", new Timestamp(fenxikaishi));
            res.put("synthesize_begin_time", new Timestamp(zonghekaishi));
            return res;
        }


        if(!check2 && check1){
            logger.info("Neo4j里没有graphsymbol为综合图谱:"+destination+"的图谱");
            logger.info("复制文档图谱作为综合图谱返回");
            flag = 5;
            KG kg = GraphReader.readGraph2(GraphReader.query2(from, false));
            List<Entity> rawentitys = kg.getEntities();
            Map<Integer, List<Integer>> simMap_int = new HashMap<>();
            int tempidd = 1852022;
            List<Integer> templ = new ArrayList<>();
            for(Entity entity: rawentitys){
                templ.add(Integer.parseInt(entity.getEntityId()));
            }
            simMap_int.put(tempidd,templ);
            Map<Integer, Integer> sourceCopyIdMap = BasicOperation.copySubgraphAccSimMap(simMap_int);
            Map<Integer, Map<String, String>> updateInfo = new HashMap<>();
            Map<Integer, Map<String, Integer>> updateInfo1 = new HashMap<>();
            Map<Integer, Map<String, Integer>> updateInfo2 = new HashMap<>();
            for(Integer copyId: sourceCopyIdMap.values()){  // 源图谱中相似点复制出来连通子图的点
                Map<String, String> updateInfo_id = new HashMap<>();
                updateInfo_id.put("graphSymbol", destination);
                Map<String, Integer> updateInfo_id1 = new HashMap<>();
                updateInfo_id1.put("synthesizeTag", 1);

                updateInfo.put(copyId, updateInfo_id);
                updateInfo1.put(copyId,updateInfo_id1);
            }

            for(Integer simNode: templ){
                Map<String, Integer> updateInfo_id = new HashMap<>();
                updateInfo_id.put("canSynthesize", 1);
                //updateInfo_id.put("alreadySynthesize", 1);
                updateInfo2.put(simNode, updateInfo_id);
            }

            BasicOperation.updataByids(updateInfo);
            BasicOperation.updataByidInteger(updateInfo1);
            BasicOperation.updataByidInteger(updateInfo2);
            BasicOperation.setPropertyRelation(destination);
            logger.info("复制文档图谱可综合节点完成");
            res.put("flag", flag);
            res.put("analyse_begin_time", new Timestamp(fenxikaishi));
            res.put("synthesize_begin_time", new Timestamp(zonghekaishi));
            return res;
        }



        //KG kg = GraphReader.readGraph(GraphReader.query(from, destination, false), "raw");
        KG kg = GraphReader.readGraph2(GraphReader.query(from, destination,false));
        if(kg.getEntities() == null) {
            logger.error("图谱中无实体");
            flag = 0;
            res.put("flag", flag);
            res.put("analyse_begin_time", new Timestamp(fenxikaishi));
            res.put("synthesize_begin_time", new Timestamp(zonghekaishi));
            return res;
        }
        List<KG> kgs = new ArrayList<>();
        kgs.add(kg);

//        HashMap<String, List<String>> simMap = new HashMap<>();
//        if((sim_url == null || sim_url.length() == 0) || (type_url == null || type_url.length() == 0)){
//            KGsMergeBasedOnContent_tfidfrw kgm = new KGsMergeBasedOnContent_tfidfrw(kgs,from,destination);
//            Map<String, Object> res_step1 = kgm.entrance();
//            simMap = (HashMap<String, List<String>>)res_step1.get("simMap");
//
//        }
//        if((sim_url != null && sim_url.length() != 0) && (type_url != null && type_url.length() != 0)){
//            KGsMergeBasedOnContent_Request kgm = new KGsMergeBasedOnContent_Request(kgs,from,destination);
//            Map<String, Object> res_step1 = kgm.entrance(sim_url, type_url);
//            simMap = (HashMap<String, List<String>>)res_step1.get("simMap");
//        }

        HashMap<String, List<String>> simMap_from_our = new HashMap<>();
        HashMap<String, List<String>> simMap_from_jinrong = new HashMap<>();
        Map<String, List<String>> simMap_merge = new HashMap<>();


        if(useWho==null){
            useWho = 0;
        }

        if(useWho==0){
            logger.info("综合分析已确认：只使用上理接口");
            logger.info("综合分析开始");
            SimGuiZe sg = new SimGuiZe();
            simMap_merge = sg.All_sim(destination,from);
//            KGsMergeBasedOnContent kgm = new KGsMergeBasedOnContent(kgs,from,destination);
//            Map<String, Object> res_step1 = kgm.entrance();
            logger.info("综合分析已结束");
//            simMap_merge = (HashMap<String, List<String>>)res_step1.get("simMap");

            if(simMap_merge.size()==0){
                logger.info("综合分析未找到可合并节点对");
            }else{
                for(Map.Entry<String,List<String>> simMap_me : simMap_merge.entrySet()){
                    String at = "";
                    for(String t : simMap_me.getValue()){
                        at = at + t +"," ;
                    }
                    logger.info("综合图谱节点id："+simMap_me.getKey()+" 合并到该综合图谱节点的文档图谱节点id有:"+at);
                }
            }
//
//            KGsMergeBasedOnContent kgm = new KGsMergeBasedOnContent(kgs,from,destination);
//            Map<String, Object> res_step1 = kgm.entrance();
//            simMap_merge = (HashMap<String, List<String>>)res_step1.get("simMap");
        }

//        if(useWho==1){
//            logger.info("自动综合分析已确认：只使用金融接口");
//            logger.info("综合分析开始");
//            if((sim_url != null && sim_url.length() != 0) && (type_url != null && type_url.length() != 0)) {
//                KGsMergeBasedOnContent_Request kgmr = new KGsMergeBasedOnContent_Request(kgs, from, destination);
//                Map<String, Object> res_step1 = kgmr.entrance(sim_url, type_url);
//                logger.info("综合分析已结束");
//                simMap_merge = (HashMap<String, List<String>>)res_step1.get("simMap");
//                if(simMap_merge.size()==0){
//                    logger.info("综合分析未找到可合并节点对");
//                }else{
//                    for(Map.Entry<String,List<String>> simMap_me : simMap_merge.entrySet()){
//
//
//                        String at = "";
//                        for(String t : simMap_me.getValue()){
//                            at = at + t +"," ;
//                        }
//                        logger.info("综合图谱节点id："+simMap_me.getKey()+" 合并到该综合图谱节点的文档图谱节点id有:"+at);
//
//                    }
//                }
//            }
//
//
////            if((sim_url != null && sim_url.length() != 0) && (type_url != null && type_url.length() != 0)) {
////                KGsMergeBasedOnContent_Request kgmr = new KGsMergeBasedOnContent_Request(kgs, from, destination);
////                Map<String, Object> res_step1 = kgmr.entrance(sim_url, type_url);
////                simMap_merge = (HashMap<String, List<String>>)res_step1.get("simMap");
////            }
//        }
//
//        if(useWho==2){
//            logger.info("自动综合分析已确认：并用金融与上理接口");
//            logger.info("综合分析开始");
//            KGsMergeBasedOnContent kgm = new KGsMergeBasedOnContent(kgs,from,destination);
//            Map<String, Object> res_step1   = kgm.entrance();
//            logger.info("上理调用结束");
//            simMap_from_our = (HashMap<String, List<String>>)res_step1.get("simMap");
//
//            if((sim_url != null && sim_url.length() != 0) && (type_url != null && type_url.length() != 0)){
//                KGsMergeBasedOnContent_Request kgm2 = new KGsMergeBasedOnContent_Request(kgs,from,destination);
//                Map<String, Object> res_step2 = kgm2.entrance(sim_url, type_url);
//                logger.info("金融调用结束");
//                simMap_from_jinrong = (HashMap<String, List<String>>)res_step2.get("simMap");
//            }
//            logger.info("综合分析已结束");
//            if((simMap_from_our.size()!=0) || (simMap_from_jinrong.size()!=0)){
//                if(simMap_from_our.size() == 0){
//                    for(Map.Entry<String, List<String>> entry: simMap_from_jinrong.entrySet()){
//                        String des = entry.getKey();
//                        List<String> sos = entry.getValue();
//                        if(!simMap_merge.containsKey(des)){
//                            simMap_merge.put(des, new ArrayList<>());
//                        }
//                        simMap_merge.put(des, sos);
//                    }
//                }
//                else if(simMap_from_jinrong.size() == 0){
//                    for(Map.Entry<String, List<String>> entry: simMap_from_our.entrySet()){
//                        String des = entry.getKey();
//                        List<String> sos = entry.getValue();
//                        if(!simMap_merge.containsKey(des)){
//                            simMap_merge.put(des, new ArrayList<>());
//                        }
//                        simMap_merge.put(des, sos);
//                    }
//                }else{
//                    for(Map.Entry<String, List<String>> entry: simMap_from_our.entrySet()){
//                        String des = entry.getKey();
//                        List<String> sos = entry.getValue();
//                        if(!simMap_merge.containsKey(des)){
//                            simMap_merge.put(des, new ArrayList<>());
//                        }
//                        simMap_merge.put(des, sos);
//                    }
//
//                    for(Map.Entry<String, List<String>> entry: simMap_from_jinrong.entrySet()){
//                        String des = entry.getKey();
//                        List<String> sos = entry.getValue();
//                        if(!simMap_merge.containsKey(des)){
//                            simMap_merge.put(des, new ArrayList<>());
//                            simMap_merge.put(des, sos);
//                        }else{
//                            for(String so: sos){
//                                simMap_merge.get(des).add(so);
//                            }
//                        }
//                    }
//
//                    for(Map.Entry<String, List<String>> entry: simMap_merge.entrySet()){
//                        List<String> sos = entry.getValue();
//                        entry.setValue(new ArrayList<>(new HashSet<>(sos))); // 去重
//                    }
//                    for(Map.Entry<String,List<String>> simMap_me : simMap_merge.entrySet()){
//                        String at = "";
//                        for(String t : simMap_me.getValue()){
//                            at = at + t +"," ;
//                        }
//                        logger.info("综合图谱节点id："+simMap_me.getKey()+" 合并到该综合图谱节点的文档图谱节点id有:"+at);
//                    }
//                }
//            }else{
//                logger.info("综合分析未找到可合并节点对");
//            }
//
//
//
//
////            KGsMergeBasedOnContent kgm = new KGsMergeBasedOnContent(kgs,from,destination);
////            Map<String, Object> res_step1   = kgm.entrance();
////            simMap_from_our = (HashMap<String, List<String>>)res_step1.get("simMap");
////
////            if((sim_url != null && sim_url.length() != 0) && (type_url != null && type_url.length() != 0)){
////                KGsMergeBasedOnContent_Request kgm2 = new KGsMergeBasedOnContent_Request(kgs,from,destination);
////                Map<String, Object> res_step2 = kgm2.entrance(sim_url, type_url);
////                simMap_from_jinrong = (HashMap<String, List<String>>)res_step2.get("simMap");
////            }
////
////            if((simMap_from_our.size()!=0) || (simMap_from_jinrong.size()!=0)){
////                if(simMap_from_our.size() == 0){
////                    for(Map.Entry<String, List<String>> entry: simMap_from_jinrong.entrySet()){
////                        String des = entry.getKey();
////                        List<String> sos = entry.getValue();
////                        if(!simMap_merge.containsKey(des)){
////                            simMap_merge.put(des, new ArrayList<>());
////                        }
////                        simMap_merge.put(des, sos);
////                    }
////                }
////                else if(simMap_from_jinrong.size() == 0){
////                    for(Map.Entry<String, List<String>> entry: simMap_from_our.entrySet()){
////                        String des = entry.getKey();
////                        List<String> sos = entry.getValue();
////                        if(!simMap_merge.containsKey(des)){
////                            simMap_merge.put(des, new ArrayList<>());
////                        }
////                        simMap_merge.put(des, sos);
////                    }
////                }else{
////                    for(Map.Entry<String, List<String>> entry: simMap_from_our.entrySet()){
////                        String des = entry.getKey();
////                        List<String> sos = entry.getValue();
////                        if(!simMap_merge.containsKey(des)){
////                            simMap_merge.put(des, new ArrayList<>());
////                        }
////                        simMap_merge.put(des, sos);
////                    }
////
////                    for(Map.Entry<String, List<String>> entry: simMap_from_jinrong.entrySet()){
////                        String des = entry.getKey();
////                        List<String> sos = entry.getValue();
////                        if(!simMap_merge.containsKey(des)){
////                            simMap_merge.put(des, new ArrayList<>());
////                            simMap_merge.put(des, sos);
////                        }else{
////                            for(String so: sos){
////                                simMap_merge.get(des).add(so);
////                            }
////                        }
////                    }
////
////                    for(Map.Entry<String, List<String>> entry: simMap_merge.entrySet()){
////                        List<String> sos = entry.getValue();
////                        entry.setValue(new ArrayList<>(new HashSet<>(sos))); // 去重
////                    }
////                }
////            }
//        }
//


//        Map<String,Object> res_step1 = kgm.entrance(sim_url, type_url);
//        Map<String,Object> res_step2= new kgmergeStructure().runMergeStructure_grm(res_step1);

//        HashMap<String, List<String>> simMap = (HashMap<String, List<String>>)res_step2.get("simMap");

        List<Integer> ids = BasicOperation.queryIdsBySymbol(from);
        if(simMap_merge.size() == 0){
            logger.info("未分析到可综合节点，更新canSynthesize");
            flag = 2;
            res.put("flag", flag);
            Map<Integer, Map<String, Integer>> info = new HashMap<>();
            for(Integer id: ids){
                Map<String, Integer> updateInfo_id = new HashMap<>();
                updateInfo_id.put("canSynthesize", 0);
                info.put(id, updateInfo_id);
            }
            BasicOperation.updataByidInteger(info);
            res.put("analyse_end_time", new Timestamp(System.currentTimeMillis()));
            return res;
        }

        // convert key1 and key2 to Integer from simMap
        Map<Integer, List<Integer>> simMap_int = new HashMap<>();
        for (String key1 : simMap_merge.keySet()) {
            List<String> value = simMap_merge.get(key1);
            List<Integer> value_int = new ArrayList<>();
            for (String key2 : value) {
                value_int.add(Integer.parseInt(key2));
            }
            simMap_int.put(Integer.parseInt(key1), value_int);
        }


        Map<Integer, String> idx2name = new HashMap<>();
        Set<Integer> needTransIdx = new HashSet<>();
        for (Integer key1 : simMap_int.keySet()) {
            needTransIdx.add(key1);
            List<Integer> value = simMap_int.get(key1);
            for (Integer key2 : value) {
                needTransIdx.add(key2);
            }
        }
        idx2name = BasicOperation.queryIdx2name(needTransIdx);
        List<SimResItem> data = new ArrayList<>();
        for(Integer key1: simMap_int.keySet()){
            SimResItem sim_item = new SimResItem();
            sim_item.setAimNodeId(Long.valueOf(key1));
            sim_item.setAimNodeName(idx2name.get(key1));
            List<EnSimpleInfo> sim_list = new ArrayList<>();
            for(Integer key2: simMap_int.get(key1)){
                EnSimpleInfo info = new EnSimpleInfo(Long.valueOf(key2), idx2name.get(key2));
                sim_list.add(info);
            }
            sim_item.setSimRes(sim_list);
            data.add(sim_item);
        }

        res.put("flag", flag);
        res.put("data", data);

        // 去更新canSyn, canSynButNotSim, cannotSyn
        // for source
        Set<Integer> sourceSimNodes = new HashSet<>();  // 源图谱中被合并的点
        for(List<Integer> ints: simMap_int.values()){
            for(Integer item: ints){
                sourceSimNodes.add(item);
            }
        }
        Set<Integer> neighbours_of_different_simNodes = BasicOperation.queryNeighbours(sourceSimNodes);


        // 更新源图谱的点，总共分为可综合点和不可综合点，可综合点分为相似点和非相似点，相似点还要加上相似目标
        Map<Integer, Integer> simParent = new LinkedHashMap<>(); // 被合并节点的寻址信息
        for(Integer key: simMap_int.keySet()){
            List<Integer> vals = simMap_int.get(key);
            for(Integer val: vals){
                simParent.put(val, key);
            }
        }

        Set<Integer> canSynButNotSimNodes = new HashSet<>();
        Set<Integer> canNotSynNodes = new HashSet<>();
        for(Integer id: ids){
            if(!neighbours_of_different_simNodes.contains(id)){
                canNotSynNodes.add(id);
            }
        }
        for(Integer id: neighbours_of_different_simNodes){
            if(!sourceSimNodes.contains(id)){
                canSynButNotSimNodes.add(id);
            }
        }

        Map<Integer, Map<String, Integer>> updateInfo2 = new HashMap<>();
        for(Integer simNode: sourceSimNodes){
            Map<String, Integer> updateInfo_id = new HashMap<>();
            updateInfo_id.put("canSynthesize", 1);
            //updateInfo_id.put("mergedToNode", simParent.get(simNode));
            updateInfo2.put(simNode, updateInfo_id);
        }
        for(Integer synNotSimNode: canSynButNotSimNodes){
            Map<String, Integer> updateInfo_id = new HashMap<>();
            updateInfo_id.put("canSynthesize", 1);
            updateInfo2.put(synNotSimNode, updateInfo_id);
        }
        for(Integer canNotSyn: canNotSynNodes){
            Map<String, Integer> updateInfo_id = new HashMap<>();
            updateInfo_id.put("canSynthesize", 0);
            updateInfo2.put(canNotSyn, updateInfo_id);
        }
        BasicOperation.updataByidInteger(updateInfo2);
        res.put("analyse_begin_time", new Timestamp(fenxikaishi));
        res.put("analyse_end_time", new Timestamp(System.currentTimeMillis()));
        logger.info("neo4j数据库修改文档图谱Tag操作完成");
        logger.info("综合结果已更新");
        // Neo4jUtils.close();

//        System.out.println("Done");
        res.put("flag", flag);
        return res;


    }

    //综合合并
    public static Map<String, Object> execute2(List<SimResItem> params, String source, String destination){

        logger.info("综合操作数据库已接收参数,文档图谱symbol:" + source + " 综合图谱symbol:" + destination );
        Boolean sourceCheck = BasicOperation.checkGraphExist(source);
        Boolean destinationCheck = BasicOperation.checkGraphExist(destination);
        Map<String, Object> res = new HashMap<>();
        if(!sourceCheck || !destinationCheck){
            res.put("success", false);
            return res;
        }

        Set<Integer> allNodes = new HashSet<>();

        Map<Integer, List<Integer>> simMap_int = new HashMap<>();
        for(SimResItem simResItem: params){
            Integer fromId = simResItem.getAimNodeId().intValue();
            allNodes.add(fromId);
            if(!simMap_int.containsKey(fromId)){
                simMap_int.put(fromId, new ArrayList<>());
            }
            for(EnSimpleInfo enSimpleInfo: simResItem.getSimRes()){
                Integer toId = enSimpleInfo.getSourceNodeId().intValue();
                simMap_int.get(fromId).add(toId);
                allNodes.add(toId);
            }
        }


        Boolean checkAllNodes = BasicOperation.checkAllNodeExist(allNodes);
        if(!checkAllNodes){
            res.put("success", false);
            return res;
        }


        Map<Integer, Integer> sourceCopyIdMap = BasicOperation.copySubgraphAccSimMap(simMap_int);
        Map<Integer, List<Integer>> resSimMap = new HashMap<>();
        for(Map.Entry<Integer, List<Integer>> entry: simMap_int.entrySet()){
            Integer des = entry.getKey();
            List<Integer> vals = entry.getValue();
            if(!resSimMap.containsKey(des)){
                resSimMap.put(des, new ArrayList<>());
            }
            for(Integer v: vals){
                resSimMap.get(des).add(sourceCopyIdMap.get(v));
            }
        }


        // construct update info
        // for destination
        Map<Integer, Map<String, String>> updateInfo = new HashMap<>();
        Map<Integer, Map<String, Integer>> updateTag = new HashMap<>();
        for(Integer id: resSimMap.keySet()) {
            //srcId
//            Map<String, String> updateInfo_srcId = new HashMap<>();
//            String srcid = simMap_int.get(id).toString().replaceAll(" ", ""); // 源节点
//            updateInfo_srcId.put("srcId", srcid);
//            updateInfo.put(id, updateInfo_srcId);

            //srcIds
            Map<String, String> updateInfo_srcIds = new HashMap<>();
            List<Integer> srcids_list = simMap_int.get(id);
            Boolean isNull = BasicOperation.judgingSrcIds(id);
            if(isNull == true){
                updateInfo_srcIds.put("srcIds", srcids_list.toString().replaceAll(" ", ""));
            }else{
                List<Integer> scrId_init = BasicOperation.GetSrcIds(id);
                scrId_init.addAll(srcids_list);
                updateInfo_srcIds.put("srcIds", scrId_init.toString().replaceAll(" ", ""));
            }
            updateInfo.put(id, updateInfo_srcIds);

//            Map<String, String> updateInfo_id = new HashMap<>();
//
//            Map<String, String> updateInfo_id2 = new HashMap<>();
//            String srcs = simMap_int.get(id).toString(); // 源节点
//            List<Integer> idss = simMap_int.get(id);
//            String ids2 = null;
//            for(int i=0;i<idss.size();i++){
//                ids2 = ids2 + idss.get(i).toString() + ",";
//            }
//            //updateInfo_id.put("srcId", srcs);
//            updateInfo_id2.put("srcIds", ids2);
//            updateInfo.put(id, updateInfo_id);
//            updateInfo.put(id, updateInfo_id2);
        }
        logger.info("修改ScrId完成");

        // for source
        for(Integer copyId: sourceCopyIdMap.values()){
            Map<String, String> updateInfo_id = new HashMap<>();
            updateInfo_id.put("graphSymbol", destination);
            updateInfo.put(copyId, updateInfo_id);
        }

        logger.info("修改综合图谱graphSymbol完成");

        // for raw source
        for(Integer key1: simMap_int.keySet()){
            for(Integer key2: simMap_int.get(key1)){
                Map<String, Integer> updateInfo_id = new HashMap<>();
                //updateInfo_id.put("canSynthesize", 1);
                updateInfo_id.put("alreadySynthesize", 1);
                updateTag.put(key2, updateInfo_id);
            }
        }


        BasicOperation.mergeNodes(resSimMap);
        BasicOperation.updataByids(updateInfo);
        BasicOperation.updataByidInteger(updateTag);
        logger.info("修改综合图谱alreadySynthesize完成");

        // update synthesizeTag
        Map<Integer, Map<String, String>> updateInfo1 = new HashMap<>();
        Map<Integer, Map<String, Integer>> updateInfoTag = new HashMap<>();
        for(Integer copyId: sourceCopyIdMap.values()){
            Map<String, String> updateInfo_id = new HashMap<>();
            Map<String, Integer> updateInfo_idTag = new HashMap<>();
            updateInfo_idTag.put("synthesizeTag", 1);

            updateInfo1.put(copyId, updateInfo_id);
            updateInfoTag.put(copyId,updateInfo_idTag);
        }
        BasicOperation.updataByids(updateInfo1);
        BasicOperation.updataByidInteger(updateInfoTag);
        BasicOperation.mergeNodesWithSameName(destination);
        BasicOperation.setPropertyRelation(destination);

        logger.info("已更新neo4j数据库");
        res.put("success", true);
        res.put("synthesize_end_time", new Timestamp(System.currentTimeMillis()));

        return res;
    }

    //自动综合合并
    public static Map<String, Object> executeRaw(String from, String destination, String sim_url, String type_url,Integer useWho) {
        logger.info("自动综合已接收参数,文档图谱symbol:" + from + " 综合图谱symbol:" + destination );
        Map<String, Object> res = new LinkedHashMap<>();

        Long zonghekaishi = System.currentTimeMillis();
        Long fenxikaishi = System.currentTimeMillis();

        int flag = 1;
        Boolean sourceCheck = BasicOperation.checkGraphExist(from);
        Boolean destinationCheck = BasicOperation.checkGraphExist(destination);

        if(!sourceCheck && !destinationCheck){
            logger.error("Neo4j里没有graphsymbol为综合图谱:"+destination+"的图谱");
            logger.error("Neo4j里没有graphsymbol为文档图谱:"+from+"的图谱");
            flag = 0;
            res.put("flag", flag);
            res.put("analyse_begin_time", new Timestamp(fenxikaishi));
            res.put("synthesize_begin_time", new Timestamp(zonghekaishi));
            return res;

        }


        if(!destinationCheck && sourceCheck){

            logger.info("Neo4j里没有graphsymbol为综合图谱:"+destination+"的图谱");
            logger.info("复制文档图谱作为综合图谱返回");
            flag = 5;
            KG kg = GraphReader.readGraph2(GraphReader.query2(from, false));
            List<Entity> rawentitys = kg.getEntities();
            Map<Integer, List<Integer>> simMap_int = new HashMap<>();
            int tempidd = 1852022;
            List<Integer> templ = new ArrayList<>();
            for(Entity entity: rawentitys){
                templ.add(Integer.parseInt(entity.getEntityId()));
            }
            simMap_int.put(tempidd,templ);
            Map<Integer, Integer> sourceCopyIdMap = BasicOperation.copySubgraphAccSimMap(simMap_int);
            Map<Integer, Map<String, String>> updateInfo = new HashMap<>();
            Map<Integer, Map<String, Integer>> updateInfo1 = new HashMap<>();
            Map<Integer, Map<String, Integer>> updateInfo2 = new HashMap<>();
            for(Integer copyId: sourceCopyIdMap.values()){  // 源图谱中相似点复制出来连通子图的点
                Map<String, String> updateInfo_id = new HashMap<>();
                updateInfo_id.put("graphSymbol", destination);
                updateInfo_id.put("itemIds", "");
                Map<String, Integer> updateInfo_id1 = new HashMap<>();
                updateInfo_id1.put("synthesizeTag", 1);
                updateInfo.put(copyId, updateInfo_id);
                updateInfo1.put(copyId,updateInfo_id1);
            }

            for(Integer simNode: templ){
                Map<String, Integer> updateInfo_id = new HashMap<>();
                updateInfo_id.put("canSynthesize", 1);
                updateInfo_id.put("alreadySynthesize", 1);
                updateInfo2.put(simNode, updateInfo_id);
            }

            BasicOperation.updataByids(updateInfo);//源图谱复制成综合图谱
            BasicOperation.updataByidInteger(updateInfo1);
            BasicOperation.updataByidInteger(updateInfo2);//源图谱
            BasicOperation.setPropertyRelation(destination);
            logger.info("复制文档图谱可综合节点完成");
            res.put("flag", flag);
            res.put("analyse_begin_time", new Timestamp(fenxikaishi));
            res.put("synthesize_begin_time", new Timestamp(zonghekaishi));
            return res;

        }


        //KG kg = GraphReader.readGraph(GraphReader.query(from, destination, false), "raw");
        KG kg = GraphReader.readGraph2(GraphReader.query(from, destination, false));

        if(kg.getEntities() == null) {
            flag = 0;
            logger.error("该图谱下未找到实体");
            res.put("flag", flag);
            res.put("analyse_begin_time", new Timestamp(fenxikaishi));
            res.put("synthesize_begin_time", new Timestamp(zonghekaishi));
            return res;
        }

        List<KG> kgs = new ArrayList<>();
        kgs.add(kg);

        HashMap<String, List<String>> simMap_from_our = new HashMap<>();
        HashMap<String, List<String>> simMap_from_jinrong = new HashMap<>();
        //Map<String, List<String>> simMap_merge = new HashMap<>();
//        if((sim_url == null || sim_url.length() == 0) || (type_url == null || type_url.length() == 0)){
//            KGsMergeBasedOnContent_tfidfrw kgm = new KGsMergeBasedOnContent_tfidfrw(kgs,from,destination);
//            Map<String, Object> res_step1 = kgm.entrance();
//            simMap_from_our = (HashMap<String, List<String>>)res_step1.get("simMap");
//
//        }

        Map<String,List<String>> simMap_merge = new HashMap<>();
        if(useWho==null){
            useWho = 0;
        }


        if(useWho==0){
            logger.info("自动综合分析已确认：只使用上理接口");
            logger.info("综合分析开始");
            SimGuiZe sg = new SimGuiZe();
            simMap_merge = sg.All_sim(destination,from);

//            KGsMergeBasedOnContent kgm = new KGsMergeBasedOnContent(kgs,from,destination);
//            Map<String, Object> res_step1 = kgm.entrance();
            logger.info("综合分析已结束");
//            simMap_merge = (HashMap<String, List<String>>)res_step1.get("simMap");
            if(simMap_merge.size()==0){
                logger.info("综合分析未找到可合并节点对");
            }else{
                for(Map.Entry<String,List<String>> simMap_me : simMap_merge.entrySet()){

                    String at = "";
                    for(String t : simMap_me.getValue()){
                        at = at + t +"," ;
                    }
                    logger.info("综合图谱节点id："+simMap_me.getKey()+" 合并到该综合图谱节点的文档图谱节点id有:"+at);

                }
            }
        }

//        if(useWho==1){
//            logger.info("自动综合分析已确认：只使用金融接口");
//            logger.info("综合分析开始");
//            if((sim_url != null && sim_url.length() != 0) && (type_url != null && type_url.length() != 0)) {
//                KGsMergeBasedOnContent_Request kgmr = new KGsMergeBasedOnContent_Request(kgs, from, destination);
//                Map<String, Object> res_step1 = kgmr.entrance(sim_url, type_url);
//                logger.info("综合分析已结束");
//                simMap_merge = (HashMap<String, List<String>>)res_step1.get("simMap");
//                if(simMap_merge.size()==0){
//                    logger.info("综合分析未找到可合并节点对");
//                }else{
//                    for(Map.Entry<String,List<String>> simMap_me : simMap_merge.entrySet()){
//
//
//                        String at = "";
//                        for(String t : simMap_me.getValue()){
//                            at = at + t +"," ;
//                        }
//                        logger.info("综合图谱节点id："+simMap_me.getKey()+" 合并到该综合图谱节点的文档图谱节点id有:"+at);
//
//                    }
//                }
//            }
//
//        }
//
//        if(useWho==2){
//            logger.info("自动综合分析已确认：并用金融与上理接口");
//            logger.info("综合分析开始");
//            KGsMergeBasedOnContent kgm = new KGsMergeBasedOnContent(kgs,from,destination);
//            Map<String, Object> res_step1   = kgm.entrance();
//            logger.info("上理调用结束");
//            simMap_from_our = (HashMap<String, List<String>>)res_step1.get("simMap");
//
//            if((sim_url != null && sim_url.length() != 0) && (type_url != null && type_url.length() != 0)){
//                KGsMergeBasedOnContent_Request kgm2 = new KGsMergeBasedOnContent_Request(kgs,from,destination);
//                Map<String, Object> res_step2 = kgm2.entrance(sim_url, type_url);
//                logger.info("金融调用结束");
//                simMap_from_jinrong = (HashMap<String, List<String>>)res_step2.get("simMap");
//            }
//            logger.info("综合分析已结束");
//            if((simMap_from_our.size()!=0) || (simMap_from_jinrong.size()!=0)){
//                if(simMap_from_our.size() == 0){
//                    for(Map.Entry<String, List<String>> entry: simMap_from_jinrong.entrySet()){
//                        String des = entry.getKey();
//                        List<String> sos = entry.getValue();
//                        if(!simMap_merge.containsKey(des)){
//                            simMap_merge.put(des, new ArrayList<>());
//                        }
//                        simMap_merge.put(des, sos);
//                    }
//                }
//                else if(simMap_from_jinrong.size() == 0){
//                    for(Map.Entry<String, List<String>> entry: simMap_from_our.entrySet()){
//                        String des = entry.getKey();
//                        List<String> sos = entry.getValue();
//                        if(!simMap_merge.containsKey(des)){
//                            simMap_merge.put(des, new ArrayList<>());
//                        }
//                        simMap_merge.put(des, sos);
//                    }
//                }else{
//                    for(Map.Entry<String, List<String>> entry: simMap_from_our.entrySet()){
//                        String des = entry.getKey();
//                        List<String> sos = entry.getValue();
//                        if(!simMap_merge.containsKey(des)){
//                            simMap_merge.put(des, new ArrayList<>());
//                        }
//                        simMap_merge.put(des, sos);
//                    }
//
//                    for(Map.Entry<String, List<String>> entry: simMap_from_jinrong.entrySet()){
//                        String des = entry.getKey();
//                        List<String> sos = entry.getValue();
//                        if(!simMap_merge.containsKey(des)){
//                            simMap_merge.put(des, new ArrayList<>());
//                            simMap_merge.put(des, sos);
//                        }else{
//                            for(String so: sos){
//                                simMap_merge.get(des).add(so);
//                            }
//                        }
//                    }
//
//                    for(Map.Entry<String, List<String>> entry: simMap_merge.entrySet()){
//                        List<String> sos = entry.getValue();
//                        entry.setValue(new ArrayList<>(new HashSet<>(sos))); // 去重
//                    }
//                    for(Map.Entry<String,List<String>> simMap_me : simMap_merge.entrySet()){
//                        String at = "";
//                        for(String t : simMap_me.getValue()){
//                            at = at + t +"," ;
//                        }
//                        logger.info("综合图谱节点id："+simMap_me.getKey()+" 合并到该综合图谱节点的文档图谱节点id有:"+at);
//                    }
//                }
//            }else{
//                logger.info("综合分析未找到可合并节点对");
//            }
//        }

//        KGsMergeBasedOnContent_Request kgmr = new KGsMergeBasedOnContent_Request(kgs,from,destination);
//        Map<String, Object> res_step1 = kgmr.entrance(sim_url,type_url);


//        Map<String,Object> res_step1 = kgm.entrance(sim_url, type_url);
//        Map<String,Object> res_step2= new kgmergeStructure().runMergeStructure_grm(res_step1);

//        HashMap<String, List<String>> simMap = (HashMap<String, List<String>>)res_step2.get("simMap");

        List<Integer> ids = BasicOperation.queryIdsBySymbol(from);
        if(simMap_merge.size() == 0){

            logger.info("未分析到可综合节点，更新canSynthesize");
            flag = 2;
            res.put("flag", flag);
            Map<Integer, Map<String, Integer>> info = new HashMap<>();
            for(Integer id: ids){
                Map<String, Integer> updateInfo_id = new HashMap<>();
                updateInfo_id.put("canSynthesize", 0);
                //updateInfo_id.put("alreadySynthesize", 1);
                info.put(id, updateInfo_id);
            }
            BasicOperation.updataByidInteger(info);
            res.put("analyse_begin_time", new Timestamp(fenxikaishi));
            res.put("synthesize_begin_time", new Timestamp(zonghekaishi));
            res.put("analyse_end_time", new Timestamp(System.currentTimeMillis()));
            return res;
        }

        // convert key1 and key2 to Integer from simMap
        Map<Integer, List<Integer>> simMap_int = new HashMap<>();
        for (String key1 : simMap_merge.keySet()) {
            List<String> value = simMap_merge.get(key1);
            List<Integer> value_int = new ArrayList<>();
            for (String key2 : value) {
                value_int.add(Integer.parseInt(key2));
            }
            simMap_int.put(Integer.parseInt(key1), value_int);
        }


        logger.info("开始操作neo4j数据库");

        // copy subGraph 需要正式合并的时候才需要的操作
        Map<Integer, Integer> sourceCopyIdMap = BasicOperation.copySubgraphAccSimMap(simMap_int);
        Map<Integer, List<Integer>> resSimMap = new HashMap<>();
        for(Map.Entry<Integer, List<Integer>> entry: simMap_int.entrySet()){
            Integer des = entry.getKey();
            List<Integer> vals = entry.getValue();
            if(!resSimMap.containsKey(des)){
                resSimMap.put(des, new ArrayList<>());
            }
            for(Integer v: vals){
                resSimMap.get(des).add(sourceCopyIdMap.get(v));
            }
        }


        // construct update info
        // for destination
        Map<Integer, Map<String, String>> updateInfo_src = new HashMap<>();
//        for(Integer id: resSimMap.keySet()) {
//
//            //srcId
////            Map<String, String> updateInfo_srcId = new HashMap<>();
////            String srcid = simMap_int.get(id).toString().replaceAll(" ", ""); // 源节点
////            updateInfo_srcId.put("srcId", srcid);
////            updateInfo.put(id, updateInfo_srcId);
//
//            //srcIds
//            Map<String, String> updateInfo_srcIds = new HashMap<>();
//
//            List<Integer> srcids_list = simMap_int.get(id);
//            Boolean isNull = BasicOperation.judgingSrcIds(id);
//            if(isNull == true){
//                updateInfo_srcIds.put("srcIds", srcids_list.toString().replaceAll(" ", ""));
//            }else{
//                List<Integer> scrId_init = BasicOperation.GetSrcIds(id);
//                scrId_init.addAll(srcids_list);
//                updateInfo_srcIds.put("srcIds", scrId_init.toString().replaceAll(" ", ""));
//            }
//            updateInfo.put(id, updateInfo_srcIds);
//
////            Map<String, String> updateInfo_id = new HashMap<>();
////            Map<String, String> updateInfo_id2 = new HashMap<>();
////            String srcs = simMap_int.get(id).toString(); // 源节点
////            List<Integer> idss = simMap_int.get(id);
////            String ids2 = null;
////            for(int i=0;i<idss.size();i++){
////                ids2 = ids2 + idss.get(i).toString() + ",";
////            }
////            updateInfo_id.put("srcId", srcs);
////            updateInfo_id2.put("srcIds", ids2);
////
////            updateInfo.put(id, updateInfo_id);
////            updateInfo.put(id, updateInfo_id2);
//        }
        for(Integer id: simMap_int.keySet()) {
            //srcIds
            Map<String, String> updateInfo_srcIds = new HashMap<>();
            List<Integer> srcids_list = simMap_int.get(id);
            Boolean isNull = BasicOperation.judgingSrcIds(id);
            if(isNull == true){
                updateInfo_srcIds.put("srcIds", srcids_list.toString().replaceAll(" ", ""));
            }else{
                List<Integer> new_list = new ArrayList<>();
                List<Integer> scrId_init = BasicOperation.GetSrcIds(id);
                for(int src_i:scrId_init){
                    if(!new_list.contains(src_i)){
                        new_list.add(src_i);
                    }
                }
                for(int src_t:srcids_list){
                    if(!new_list.contains(src_t)){
                        new_list.add(src_t);
                    }
                }


                updateInfo_srcIds.put("srcIds", new_list.toString().replaceAll(" ", ""));
            }
            Map<String,String> templ = updateInfo_src.getOrDefault(id,new HashMap<>());
            templ.putAll(updateInfo_srcIds);
            updateInfo_src.put(id, templ);
        }
        BasicOperation.updataByids(updateInfo_src);  // 更新scrids



        // 非·此时更新了综合图谱上的所有点，包括合并点，和相似点摘下来的子图
        logger.info("修改ScrIds完成");



        //更新itemids
        //获取待合并节点的itemids json
        Map<Integer,List<Object>> Bemerged_itemidsjson = new HashMap<>();
        for(Integer id: resSimMap.keySet()) {
            List<Integer> srcids_list = simMap_int.get(id);
            for(int source_id: srcids_list){
                String source_itemids = BasicOperation.GetitemIds(source_id);
                List<Object> source_itemids_list = JSONArray.parseArray(source_itemids);
                if(source_itemids!=null){
                    Bemerged_itemidsjson.put(source_id,source_itemids_list);
                }
            }
        }

        // 更新数据库来源相同的item
        Map<Integer, Map<String, String>> updateInfo_item1 = new HashMap<>();
        Set<Integer> aim_databasesourceid = new HashSet<>();
        Set<JSONObject> all_jsonobject = new HashSet<>();
        for(Integer id: resSimMap.keySet()) {
            Map<String, String> updateInfo_itemIds = new HashMap<>();
            String aim_itemids = BasicOperation.GetitemIds(id);
            if(aim_itemids!=null){
                List<Object> aim_itemids_list = JSONArray.parseArray(aim_itemids);
                List<Object> aim_res = new ArrayList<>();

                for(int i = 0;i<aim_itemids_list.size();i++) {
                    JSONObject temp_j = (JSONObject) aim_itemids_list.get(i);
                    all_jsonobject.add(temp_j);
                    int table = (int) temp_j.get("dataSourceId");
                    aim_databasesourceid.add(table);
                    JSONArray aim_items = (JSONArray) temp_j.get("items");

                    List<Integer> srcids_list = simMap_int.get(id);
                    for(int source_id: srcids_list){
                        if(Bemerged_itemidsjson.get(source_id)!=null){
                            List<Object> source_itemids_list = Bemerged_itemidsjson.get(source_id);
                            for(int a = 0;a<source_itemids_list.size();a++) {
                                JSONObject temp_a = (JSONObject) source_itemids_list.get(a);
                                int iddd = (int) temp_a.get("dataSourceId");
                                if(table==iddd){
                                    JSONArray source_items = (JSONArray) temp_a.get("items");
                                    for(int b = 0; b<source_items.size();b++ ){
                                        if(!aim_items.contains(source_items.get(b))){
                                            aim_items.add(source_items.get(b));
                                        }
                                    }
                                }
                            }
                        }

                    }
                    JSONObject res1 = new JSONObject();
                    res1.put("dataSourceId",table);
                    res1.put("items",aim_items);

                    aim_res.add(res1);
                    updateInfo_itemIds.put("itemIds",JSON.toJSONString(aim_res));
                }

                updateInfo_item1.put(id,updateInfo_itemIds);

            }
        }
        BasicOperation.updataByids(updateInfo_item1);


        // 更新数据库来源不相同的，新增的item
        Map<Integer, Map<String, String>> updateInfo_item2 = new HashMap<>();
        for(Integer id: resSimMap.keySet()) {
            String aim_itemids = BasicOperation.GetitemIds(id);
            Map<String, String> updateInfo_itemIds = new HashMap<>();
            if(aim_itemids!=null){
                List<Object> aim_itemids_list = JSONArray.parseArray(aim_itemids);


                List<Integer> srcids_list = simMap_int.get(id);
                for(int source_id: srcids_list) {
                    if (Bemerged_itemidsjson.get(source_id) != null) {
                        List<Object> source_itemids_list = Bemerged_itemidsjson.get(source_id);
                        for(int a = 0;a<source_itemids_list.size();a++) {
                            JSONObject temp_a = (JSONObject) source_itemids_list.get(a);
                            int iddd = (int) temp_a.get("dataSourceId");
                            if(!aim_databasesourceid.contains(iddd)){
                                if(!all_jsonobject.contains(temp_a)){
                                    aim_itemids_list.add(temp_a);
                                    all_jsonobject.add(temp_a);
                                }
                            }
                        }

                    }
                }
                updateInfo_itemIds.put("itemIds",JSON.toJSONString(aim_itemids_list));
            }
            updateInfo_item2.put(id,updateInfo_itemIds);
        }
        BasicOperation.updataByids(updateInfo_item2);


        // for source
        // 获取源图谱所有节点id

        Set<Integer> sourceSimNodes = new HashSet<>();  // 源图谱中被合并的点
        for(List<Integer> ints: simMap_int.values()){
            for(Integer item: ints){
                sourceSimNodes.add(item);
            }
        }
        Map<Integer, Map<String, String>> updateInfo_gsb = new HashMap<>();
        for(Integer copyId: sourceCopyIdMap.values()){  // 源图谱中相似点复制出来连通子图的点
            Map<String, String> updateInfo_id = new HashMap<>();
            updateInfo_id.put("graphSymbol", destination);
            updateInfo_gsb.put(copyId, updateInfo_id);
        }
        BasicOperation.updataByids(updateInfo_gsb);

        logger.info("复制文档图谱可综合节点-创建为综合图谱节点-完成");

        BasicOperation.mergeNodes(resSimMap);  // 先不合并
        logger.info("综合图谱节点更新完成");


        // 更新源图谱的点，总共分为可综合点和不可综合点，可综合点分为相似点和非相似点，相似点还要加上相似目标
        Map<Integer, Integer> simParent = new LinkedHashMap<>(); // 被合并节点的寻址信息
        for(Integer key: simMap_int.keySet()){
            List<Integer> vals = simMap_int.get(key);
            for(Integer val: vals){
                simParent.put(val, key);
            }
        }
        // update synthesizeTag
        Map<Integer, Map<String, Integer>> updateInfo1 = new HashMap<>();
        for(Integer copyId: sourceCopyIdMap.values()){
            Map<String, Integer> updateInfo_id = new HashMap<>();
            updateInfo_id.put("synthesizeTag", 1);
            updateInfo1.put(copyId, updateInfo_id);
        }
        BasicOperation.updataByidInteger(updateInfo1);
        logger.info("综合图谱新增节点SynthesizeTag属性更新完成");

        Set<Integer> canSynButNotSimNodes = new HashSet<>();
        Set<Integer> canNotSynNodes = new HashSet<>();
        for(Integer id: ids){
            if(!sourceCopyIdMap.containsKey(id)){
                canNotSynNodes.add(id);
            }
        }
        for(Integer id: sourceCopyIdMap.keySet()){
            if(!sourceSimNodes.contains(id)){
                canSynButNotSimNodes.add(id);
            }
        }
        Map<Integer, Map<String, Integer>> updateInfo2 = new HashMap<>();
        for(Integer simNode: sourceSimNodes){
            Map<String, Integer> updateInfo_id = new HashMap<>();
            updateInfo_id.put("canSynthesize", 1);
            updateInfo_id.put("alreadySynthesize", 1);
            updateInfo_id.put("mergedToNode", simParent.get(simNode));
            updateInfo2.put(simNode, updateInfo_id);
        }
        for(Integer synNotSimNode: canSynButNotSimNodes){
            Map<String, Integer> updateInfo_id = new HashMap<>();
            updateInfo_id.put("canSynthesize", 1);
            updateInfo_id.put("alreadySynthesize", 1);
            updateInfo2.put(synNotSimNode, updateInfo_id);
        }
        for(Integer canNotSyn: canNotSynNodes){
            Map<String, Integer> updateInfo_id = new HashMap<>();
            updateInfo_id.put("canSynthesize", 0);
            //updateInfo_id.put("alreadySynthesize", 1);
            updateInfo2.put(canNotSyn, updateInfo_id);
        }
        BasicOperation.updataByidInteger(updateInfo2);

        logger.info("文档图谱节点canSynthesize,alreadySynthesize属性更新完成");
        res.put("analyse_begin_time", new Timestamp(fenxikaishi));
        res.put("synthesize_begin_time", new Timestamp(zonghekaishi));
        res.put("analyse_end_time", new Timestamp(System.currentTimeMillis()));
        //BasicOperation.mergeNodesWithSameName(destination);

        //修改综合图谱的relation.graphSymbol
        BasicOperation.setPropertyRelation(destination);
        res.put("synthesize_end_time", new Timestamp(System.currentTimeMillis()));
        logger.info("neo4j数据库操作完成");
        logger.info("综合结果已更新");
        // Neo4jUtils.close();

//        System.out.println("Done");
        res.put("flag", flag);
        return res;

    }


    //词义消歧分析
    public static Map<String, Object> executeWsd1(String symbol,String task_id){
        logger.info("词义消歧已接收参数,图谱symbol:" + symbol  );
        Map<String, Object> res = new LinkedHashMap<>();

        Long fenxikaishi = System.currentTimeMillis();

        int flag = 1;
        Boolean sourceCheck = BasicOperation.checkGraphExist(symbol);

        if(!sourceCheck){
            logger.error("Neo4j里没有graphsymbol为综合图谱:"+symbol+"的图谱");
            flag = 0;
            res.put("flag", flag);
            res.put("begin_time", new Timestamp(fenxikaishi));
            Long end_tie = System.currentTimeMillis();
            res.put("end_time", new Timestamp(end_tie));
            return res;
        }




        Map<String,String> id2name = new HashMap<>();
        Set<EnSim> all_entitys = BasicOperation.getNodes(symbol);
        for(EnSim entity : all_entitys){
            id2name.put(entity.getId().toString(), entity.getName());
        }
        Map<String,List<String>> simMap_merge = new HashMap<>();


        logger.info("词语歧义分析开始");
        SimGuiZe sg = new SimGuiZe();
        simMap_merge = sg.All_sim(symbol,symbol);

        String create_time = new Timestamp(System.currentTimeMillis()).toString();

        Set<Set<String>> remove_quchong_ids = new HashSet<>();
        logger.info("词语歧义分析已结束");
        if(simMap_merge.size()==0){
            logger.info("分析未找到歧义节点");
            flag = 2;
        }else{
            for(Map.Entry<String,List<String>> entryall : simMap_merge.entrySet()){
                String des_id = entryall.getKey();
                List<String> list_merge_ids = entryall.getValue();
                Set<String> temp_all_ids = new HashSet<>();
                temp_all_ids.add(des_id);
                temp_all_ids.addAll(list_merge_ids);

                if(remove_quchong_ids.contains(temp_all_ids)){
                    continue;
                }else {
                    remove_quchong_ids.add(temp_all_ids);
                    if(list_merge_ids.size()>0){

                        String lr1_str = "";
                        lr1_str = lr1_str + des_id +":"+  id2name.get(des_id) +";";

                        for(String source_id :list_merge_ids) {
                            lr1_str = lr1_str + source_id +":"+ id2name.get(source_id) +";";
                        }

                        Map<String,Object> wsd_res_object = new HashMap<>();
                        wsd_res_object.put("record_id",UUID.randomUUID().toString());
                        wsd_res_object.put("task_id",task_id);
                        wsd_res_object.put("create_time",create_time);
                        wsd_res_object.put("handle_status",1);
                        wsd_res_object.put("handle_time",new Timestamp(System.currentTimeMillis()));
                        wsd_res_object.put("datasouce_type",2);
                        wsd_res_object.put("ambiguity_type",1);
                        wsd_res_object.put("id_numbers",list_merge_ids.size()+1);
                        wsd_res_object.put("ambiguity_ids",lr1_str);
                        KGASSETOperation.WSDTask_Word_jg_insertIntoKgAsset(wsd_res_object);
                    }
                }


            }
            logger.info("歧义分析结果已写入数据库");
        }


        res.put("flag", flag);
        return res;

    }

    //词义消歧处置
    public static Map<String, Object> executeWsd2(Map<String,Object> json_res) {

        String symbol = (String) json_res.get("graphSymbol");

        String ids = (String) json_res.get("ids");

        String aim_id =(String) json_res.get("aim_id");

        String aim_update =(String) json_res.get("aim_update");

        //List<Integer> remove_ids = new ArrayList<>();
        Boolean aimisnull_flag = false;


        logger.info("词语歧义处置已接收参数,symbol:" + symbol );
        Boolean sourceCheck = BasicOperation.checkGraphExist(symbol);

        Map<String, Object> res = new HashMap<>();

        if(!sourceCheck ){
            res.put("success", false);
            logger.info("数据库中不存在该graphSymbol的图谱");
            return res;
        }

        if(ids.length()==0){
            res.put("success", false);
            logger.info("参数ids为空（待合并节点数组）");
            return res;
        }
        if(symbol.length()==0){
            res.put("success", false);
            logger.info("symbol为空");
            return res;
        }

        if(aim_id==null || aim_id.equals("null") || aim_id.equals(null)){
            List<Record> resid = BasicOperation.CreateNewNode(symbol);
            aim_id = resid.get(0).get("id(n)").toString();
            aimisnull_flag = true;
        }

        Set<Integer> allNodes = new HashSet<>();
        allNodes.add(Integer.parseInt(aim_id));

        Boolean checkAimNodes = BasicOperation.checkAllNodeExist(allNodes);
        if(!checkAimNodes){
            res.put("success", false);

            logger.info("数据库中不存在目标节点id的节点");
            return res;
        }


        List<Integer> ids_s = new ArrayList<>();
        String[] temp = ids.split(",");
        for(int i=0;i<temp.length;i++){
            if(!aim_id.equals(temp[i])){
                ids_s.add(Integer.parseInt(temp[i]));
                allNodes.add(Integer.parseInt(temp[i]));
            }
            //remove_ids.add(Integer.parseInt(temp[i]));
        }


        Map<Integer, List<Integer>> simMap_int = new HashMap<>();
        simMap_int.put(Integer.parseInt(aim_id),ids_s);



        Boolean checkAllNodes = BasicOperation.checkAllNodeExist(allNodes);
        if(!checkAllNodes){
            res.put("success", false);
            logger.info("数据库中不存在待合并节点数组中的节点");
            return res;
        }




        // construct update info
        // for destination




        //更新itemids
        //获取待合并节点的itemids json
        Map<Integer,List<Object>> Bemerged_itemidsjson = new HashMap<>();
        for(Integer id: simMap_int.keySet()) {
            List<Integer> srcids_list = simMap_int.get(id);
            for(int source_id: srcids_list){
                String source_itemids = BasicOperation.GetitemIds(source_id);
                List<Object> source_itemids_list = JSONArray.parseArray(source_itemids);
                if(source_itemids!=null){
                    Bemerged_itemidsjson.put(source_id,source_itemids_list);
                }
            }
        }

        // 更新数据库来源相同的item
        Map<Integer, Map<String, String>> updateInfo_item1 = new HashMap<>();
        Set<Integer> aim_databasesourceid = new HashSet<>();
        Set<JSONObject> all_jsonobject = new HashSet<>();
        for(Integer id: simMap_int.keySet()) {
            Map<String, String> updateInfo_itemIds = new HashMap<>();
            String aim_itemids = BasicOperation.GetitemIds(id);
            if(aim_itemids!=null){
                List<Object> aim_itemids_list = JSONArray.parseArray(aim_itemids);
                List<Object> aim_res = new ArrayList<>();

                for(int i = 0;i<aim_itemids_list.size();i++) {
                    JSONObject temp_j = (JSONObject) aim_itemids_list.get(i);
                    all_jsonobject.add(temp_j);
                    int table = (int) temp_j.get("dataSourceId");
                    aim_databasesourceid.add(table);
                    JSONArray aim_items = (JSONArray) temp_j.get("items");

                    List<Integer> srcids_list = simMap_int.get(id);
                    for(int source_id: srcids_list){
                        if(Bemerged_itemidsjson.get(source_id)!=null){
                            List<Object> source_itemids_list = Bemerged_itemidsjson.get(source_id);
                            for(int a = 0;a<source_itemids_list.size();a++) {
                                JSONObject temp1 = (JSONObject) source_itemids_list.get(a);

                                int iddd = (int) temp1.get("dataSourceId");
                                if(table==iddd){
                                    JSONArray source_items = (JSONArray) temp1.get("items");
                                    for(int b = 0; b<source_items.size();b++ ){
                                        if(!aim_items.contains(source_items.get(b))){
                                            aim_items.add(source_items.get(b));
                                        }
                                    }
                                }
                            }
                        }

                    }
                    JSONObject res1 = new JSONObject();
                    res1.put("dataSourceId",table);
                    res1.put("items",aim_items);

                    aim_res.add(res1);
                    updateInfo_itemIds.put("itemIds",JSON.toJSONString(aim_res));
                }

                updateInfo_item1.put(id,updateInfo_itemIds);

            }
        }
        BasicOperation.updataByids(updateInfo_item1);

        // 更新数据库来源不相同的，新增的item
        Map<Integer, Map<String, String>> updateInfo_item2 = new HashMap<>();
        for(Integer id: simMap_int.keySet()) {
            String aim_itemids = BasicOperation.GetitemIds(id);
            Map<String, String> updateInfo_itemIds = new HashMap<>();
            if(aim_itemids!=null){
                List<Object> aim_itemids_list = JSONArray.parseArray(aim_itemids);
                List<Integer> srcids_list = simMap_int.get(id);

                for(int source_id: srcids_list) {
                    if (Bemerged_itemidsjson.get(source_id) != null) {
                        List<Object> source_itemids_list = Bemerged_itemidsjson.get(source_id);
                        for(int a = 0;a<source_itemids_list.size();a++) {
                            JSONObject temp1 = (JSONObject) source_itemids_list.get(a);
                            int iddd = (int) temp1.get("dataSourceId");
                            if(!aim_databasesourceid.contains(iddd)){
                                if(!all_jsonobject.contains(temp1)){
                                    aim_itemids_list.add(temp1);
                                    all_jsonobject.add(temp1);
                                }
                            }
                        }

                    }
                }
                updateInfo_itemIds.put("itemIds",JSON.toJSONString(aim_itemids_list));
            }
            updateInfo_item2.put(id,updateInfo_itemIds);
        }
        BasicOperation.updataByids(updateInfo_item2);




        if(aimisnull_flag){
            BasicOperation.mergeNodes_AimNull(simMap_int);
        }else{
            BasicOperation.mergeNodes(simMap_int);
        }
        logger.info("待处置歧义节点合并置目标节点完成");


        //更新json自带属性
        Map<Integer, Map<String, String>> updateInfo = new HashMap<>();
        if(aim_update.length()>0){
            String[] sum_1 = aim_update.split(";");
            for(int i =0;i< sum_1.length;i++){
                String[] sum_2 = sum_1[i].split(":",2);
                String key1 = sum_2[0];
                String v_all = sum_2[1];
                Map<String,String> tempm = updateInfo.getOrDefault(Integer.parseInt(aim_id),new HashMap<>());
                tempm.put(key1,v_all);
                updateInfo.put(Integer.parseInt(aim_id),tempm);

            }

        }
        BasicOperation.updataByids(updateInfo);//修改属性
        //BasicOperation.updataByidInteger(updateTag);
        logger.info("修改目标节点属性信息完成");

        //更新scrIds
        Map<Integer, Map<String, String>> updateInfo_src = new HashMap<>();
        for(Integer id: simMap_int.keySet()) {
            //srcIds
            Map<String, String> updateInfo_srcIds = new HashMap<>();
            List<Integer> srcids_list = simMap_int.get(id);
            Boolean isNull = BasicOperation.judgingSrcIds(id);
            if(isNull == true){
                updateInfo_srcIds.put("srcIds", srcids_list.toString().replaceAll(" ", ""));
            }else{
                List<Integer> new_list = new ArrayList<>();
                List<Integer> scrId_init = BasicOperation.GetSrcIds(id);
                for(int src_i:scrId_init){
                    if(!new_list.contains(src_i)){
                        new_list.add(src_i);
                    }
                }
                for(int src_t:srcids_list){
                    if(!new_list.contains(src_t)){
                        new_list.add(src_t);
                    }
                }


                updateInfo_srcIds.put("srcIds", new_list.toString().replaceAll(" ", ""));
            }
            Map<String,String> templ = updateInfo_src.getOrDefault(id,new HashMap<>());
            templ.putAll(updateInfo_srcIds);
            updateInfo_src.put(id, templ);
        }
        BasicOperation.updataByids(updateInfo_src);//修改srcids






//        //Map<Integer, Map<String, Integer>> updateTag = new HashMap<>();

//        BasicOperation.updataByids(updateInfo1);
//        BasicOperation.updataByidInteger(updateInfoTag);
//        BasicOperation.mergeNodesWithSameName(destination);
//        BasicOperation.setPropertyRelation(destination);

//        String aim_neighbor_ids = null;
//        String aim_neighbor_relations = null;

//        List<WSDData> wsd_data = new ArrayList<>();
//        wsd_data.add(new WSDData(aim_neighbor_ids,aim_neighbor_relations,remove_ids,Integer.valueOf(aim_id)));


        logger.info("已更新neo4j数据库");
        res.put("aim_id",aim_id);
        res.put("success", true);

        return res;

    }



    public static void main(String[] args) {
        // MergeAuto.execute("SOA服务基础设施", "大数据平台");
        System.out.println("h");
    }
}
