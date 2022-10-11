package com.usst.kgfusion.entrance;

import java.io.IOException;
import java.util.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

import com.usst.kgfusion.constructer.GraphReader;
import com.usst.kgfusion.databaseQuery.BasicOperation;
import com.usst.kgfusion.databaseQuery.ItemQuery;
import com.usst.kgfusion.pojo.EntityRaw;
import com.usst.kgfusion.pojo.KG;
import com.usst.kgfusion.util.GenerateUtil;
import com.usst.kgfusion.util.GraphUtil;
import com.usst.kgfusion.util.RequestHelper;
import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.graph.Graph;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EvolutionAuto {

    private static final Logger logger = LoggerFactory.getLogger("com.usst.test");

    public static int executeGraphCluster(String from, String destination, String entityType, String ontologySymbol, String url_relation_classify) throws IOException {
        logger.info("自动演化已接受参数：综合图谱symbol:" + from + ",概念图谱symbol:" + destination);
        int flag = 1;
        KG kg = GraphReader.readGraph(GraphReader.query2(from, true), "zonghe");

        if(kg.getEntities() == null){
            logger.error("图谱"+from+"中无实体");
            flag = 0;
            return flag;
        }

        Graph g = GraphUtil.transKGtoGraph(kg, 0);
        WeakComponentClusterer weakComponentClusterer = new WeakComponentClusterer();
        Set<Set<String>> clusterRes = weakComponentClusterer.apply(g);


        SchemaExtraction schemaExtraction = new SchemaExtraction(kg);

        if((url_relation_classify == null) || (url_relation_classify.length() == 0)){
            Map<String, String> recommendTypeTree = schemaExtraction.getConceptSetWithParent(clusterRes); // parent child
            if(recommendTypeTree != null){
                BasicOperation.insertAndMergeWithSameName(recommendTypeTree, entityType, ontologySymbol, destination);
                // update evolutionTag
                List<EntityRaw> ens = kg.getEntities();
                List<Integer> ids = new ArrayList<>();
                for(EntityRaw en: ens){
                    ids.add(Integer.parseInt(en.getEntityId()));
                }

                Map<Integer, Map<String, Integer>> updateInfo = new HashMap<>();
                for(Integer id: ids) {
                    Map<String, Integer> updateInfo_id = new HashMap<>();
                    updateInfo_id.put("evolutionTag", 1);
                    updateInfo.put(id, updateInfo_id);
                }
                BasicOperation.updataByidInteger(updateInfo);

            }
        }

        if((url_relation_classify != null) && (url_relation_classify.length() != 0)){
            // here with relation calculation
            Set<String> recommendTypes = schemaExtraction.getConceptSet(clusterRes);
            List<String> recommendTypesList = new ArrayList<>();
            for(String v: recommendTypes){
                recommendTypesList.add(v);
            }
            List<String> nullList = new ArrayList<>();
            nullList.add(null);
            recommendTypesList.removeAll(nullList);
            Map<String, List<List<String>>> req = new HashMap<>();
            List<List<String>> param = new ArrayList<>();
            for (int i = 0; i < recommendTypesList.size(); i++) {
                for (int j = 0; j < recommendTypesList.size(); j++) {
                    if(i == j) continue;
                    List<String> temp = new ArrayList<>();
                    String leftE = recommendTypesList.get(i);
                    String rightE = recommendTypesList.get(j);
                    temp.add(leftE);
                    temp.add(rightE);
                    param.add(temp);
                }
            }
            req.put("ins", param);
            String url = url_relation_classify;
            String jsonRes = RequestHelper.sendJsonWithHttp(url, JSON.toJSONString(req));
            //结果是map形式{classify_name: List<String>}
            Map parseRes = (Map)JSON.parse(jsonRes);
            List<String> values = Arrays.asList(((JSONArray)parseRes.get("classify_name")).toArray(new String[]{}));

            // 组织成triple的形式
            List<Triple<String,String,String>> triples = new ArrayList<>();
            for (int i = 0; i < values.size(); i++) {
                String head = param.get(i).get(0);
                String tail = param.get(i).get(1);
                String relation = values.get(i);
//            if(relation.equals("None")){
//                continue;
//            }
                Triple<String, String, String> tri = new ImmutableTriple<>(head, relation, tail);
                triples.add(tri);
            }


            if(recommendTypesList.size() != 0 && triples.size() != 0){
                BasicOperation.insertAndMergeWithSameName(triples, entityType, ontologySymbol, destination);

                // update evolutionTag
                List<EntityRaw> ens = kg.getEntities();
                List<Integer> ids = new ArrayList<>();
                for(EntityRaw en: ens){
                    ids.add(Integer.parseInt(en.getEntityId()));
                }

                Map<Integer, Map<String, Integer>> updateInfo = new HashMap<>();
                for(Integer id: ids) {
                    Map<String, Integer> updateInfo_id = new HashMap<>();
                    updateInfo_id.put("evolutionTag", 1);
                    updateInfo.put(id, updateInfo_id);
                    BasicOperation.updataByidInteger(updateInfo);
                }

            }
        }

        return flag;

    }

    public static int execute(String from, String destination, String entityType, String ontologySymbol, String url_relation_classify, Integer useWho) throws IOException {
        int flag = 1;
//        KG kg = GraphReader.readGraph(GraphReader.queryEdge(from, true));
        logger.info("自动演化已接受参数：综合图谱symbol:" + from + ",概念图谱symbol:" + destination);
        KG kg = GraphReader.readGraph(GraphReader.query2(from, true), "zonghe");

        if(kg.getEntities() == null){
            logger.error("图谱"+from+"中无实体");
            flag = 0;
            return flag;
        }

        // 对KG进行聚类
        Graph g = GraphUtil.transKGtoGraph(kg, 0);
        WeakComponentClusterer weakComponentClusterer = new WeakComponentClusterer();
        Set<Set<String>> clusterRes = weakComponentClusterer.apply(g);  // 以id划分

        Map<String, EntityRaw> id2Entity = new HashMap<>();
        for(EntityRaw entity: kg.getEntities()){
            id2Entity.put(entity.getEntityId(), entity);
        }

        // 切分kg
        List<KG> kgs = GraphUtil.split(kg, clusterRes, id2Entity);
        List<SchemaExtraction> schemaExtractions = new LinkedList<>();
        for(KG small_kg: kgs){
            schemaExtractions.add(new SchemaExtraction(small_kg));
        }
//        SchemaExtraction schemaExtraction1 = new SchemaExtraction(kg);

        if((useWho!=null && useWho == 0) || ((url_relation_classify == null) || (url_relation_classify.length() == 0))){
            for(SchemaExtraction schemaExtraction: schemaExtractions){
                Map<String, String> recommendTypeTree = schemaExtraction.evoAuto(); // parent child
                if(recommendTypeTree != null && recommendTypeTree.size() != 0){
                    BasicOperation.insertAndMergeWithSameName(recommendTypeTree, entityType, ontologySymbol, destination);
                    //更新关系的graphSymbol
                    BasicOperation.setPropertyRelation(destination);
                    // update evolutionTag
                    List<EntityRaw> ens = schemaExtraction.getKg().getEntities();
                    List<Integer> ids = new ArrayList<>();
                    for(EntityRaw en: ens){
                        ids.add(Integer.parseInt(en.getEntityId()));
                    }

                    Map<Integer, Map<String, Integer>> updateInfo = new HashMap<>();
                    for(Integer id: ids) {
                        Map<String, Integer> updateInfo_id = new HashMap<>();
                        updateInfo_id.put("evolutionTag", 1);
                        updateInfo.put(id, updateInfo_id);
                    }
                    BasicOperation.updataByidInteger(updateInfo);
                    Set<String> types = new HashSet<>(recommendTypeTree.values());
                    for(String type: types){
                        List<String> enIds = new ArrayList<>();
                        for(EntityRaw entity: schemaExtraction.getKg().getEntities()){
                            enIds.add(entity.getEntityId());
                        }
                        Set<String> item_ids = BasicOperation.queryItemIdsByEntityIds(enIds, "zonghe");
                        if(item_ids.size() == 0){
                            continue;
                        }
                        Map<String, String> item_from_zonghe = ItemQuery.queryItem(item_ids, "misre_km_wdtph_tmnr");
                        Map<String, String> item_from_raw = ItemQuery.queryItem(item_ids, "misre_km_bz_bzjznr");
                        Set<String> texts = new HashSet<>();
                        if(item_from_zonghe.size() != 0){
                            for(String text: item_from_zonghe.values()){
                                texts.add(text);
                            }
                        }
                        if(item_from_raw.size() != 0){
                            for(String text: item_from_raw.values()){
                                texts.add(text);
                            }
                        }
                        String aggText = "";
                        if(texts.size() > 0){
                            aggText = MergeConceptInfo.aggText(new LinkedList<>(texts));
                        }

                        if(aggText.length() == 0){
                            continue;
                        }

                        List<Long> checkNodeProperty = BasicOperation.checkNodeProp(type, "item_id", destination);
                        if(checkNodeProperty.size() == 0){
                            // 生成新的item_id并将其文本信息插入数据库
                            Long item_id = GenerateUtil.generateUniqueId();
                            BasicOperation.setProperty(type, destination, "item_id", item_id);
                            ItemQuery.insertConceptItemInfo(item_id, aggText);
                        }else{
                            // 将摘要信息插入到原始文档末尾，分为item_id找得到和找不到,这里直接再原内容上追加
                            Long item_id = checkNodeProperty.get(0);
                            ItemQuery.updateConceptInfo(item_id, aggText);
                        }

                    }

                }
            }

            logger.info("已更新evolutionTag");
        }

        if((useWho != null && useWho == 2) && (url_relation_classify != null) && (url_relation_classify.length() != 0)){
            // here with relation calculation
            for(SchemaExtraction schemaExtraction: schemaExtractions){
                Set<String> recommendTypes = schemaExtraction.evoRecommend();
                List<String> recommendTypesList = new ArrayList<>();
                for(String v: recommendTypes){
                    recommendTypesList.add(v);
                }
                if(recommendTypes.size() == 0){
                    continue;
                }
                if(recommendTypes.size() == 1){  // size 等于1的时候就不需要发请求了, 后面默认item_id是long类型的
                    // 插入节点并去重
                    Boolean checkConceptExist = BasicOperation.checkNode(recommendTypesList.get(0), destination);
                    if(!checkConceptExist){
                        BasicOperation.insertAndMergeWithSameName(recommendTypesList.get(0), entityType, ontologySymbol, destination);
                    }

                    List<String> enIds = new ArrayList<>();
                    for(EntityRaw entity: schemaExtraction.getKg().getEntities()){
                        enIds.add(entity.getEntityId());
                    }
                    Set<String> item_ids = BasicOperation.queryItemIdsByEntityIds(enIds, "zonghe");
                    if(item_ids.size() == 0){
                        continue;
                    }
                    Map<String, String> item_from_zonghe = ItemQuery.queryItem(item_ids, "misre_km_wdtph_tmnr");
                    Map<String, String> item_from_raw = ItemQuery.queryItem(item_ids, "misre_km_bz_bzjznr");
                    Set<String> texts = new HashSet<>();
                    if(item_from_zonghe.size() != 0){
                        for(String text: item_from_zonghe.values()){
                            texts.add(text);
                        }
                    }
                    if(item_from_raw.size() != 0){
                        for(String text: item_from_raw.values()){
                            texts.add(text);
                        }
                    }
                    String aggText = "";
                    if(texts.size() > 0){
                        aggText = MergeConceptInfo.aggText(new LinkedList<>(texts));
                    }

                    if(aggText.length() == 0){
                        continue;
                    }

                    List<Long> checkNodeProperty = BasicOperation.checkNodeProp(recommendTypesList.get(0), "item_id", destination);
                    if(checkNodeProperty.size() == 0){
                        // 生成新的item_id并将其文本信息插入数据库
                        Long item_id = GenerateUtil.generateUniqueId();
                        BasicOperation.setProperty(recommendTypesList.get(0), destination, "item_id", item_id);
                        ItemQuery.insertConceptItemInfo(item_id, aggText);
                    }else{
                        // 将摘要信息插入到原始文档末尾，分为item_id找得到和找不到,这里直接再原内容上追加
                        Long item_id = checkNodeProperty.get(0);
                        ItemQuery.updateConceptInfo(item_id, aggText);
                    }
                    continue;
                }

                List<String> nullList = new ArrayList<>();
                nullList.add(null);
                recommendTypesList.removeAll(nullList);
                Map<String, List<List<String>>> req = new HashMap<>();
                List<List<String>> param = new ArrayList<>();
                for (int i = 0; i < recommendTypesList.size(); i++) {
                    for (int j = 0; j < recommendTypesList.size(); j++) {
                        if(i == j) continue;
                        List<String> temp = new ArrayList<>();
                        String leftE = recommendTypesList.get(i);
                        String rightE = recommendTypesList.get(j);
                        temp.add(leftE);
                        temp.add(rightE);
                        param.add(temp);
                    }
                }
                req.put("ins", param);
                String url = url_relation_classify;
                String jsonRes = RequestHelper.sendJsonWithHttp(url, JSON.toJSONString(req));
                // 这里没看到样子，假设结果是map形式{classify_name: List<String>}
                Map parseRes = (Map)JSON.parse(jsonRes);
                List<String> values = Arrays.asList(((JSONArray)parseRes.get("classify_name")).toArray(new String[]{}));
                Set<String> filter = new HashSet<>(values);
                if(filter.size() == 1) {
                    logger.info("金融返回的关系全为None");
                }
                // 组织成triple的形式
                List<Triple<String,String,String>> triples = new ArrayList<>();
                for (int i = 0; i < values.size(); i++) {
                    String head = param.get(i).get(0);
                    String tail = param.get(i).get(1);
                    String relation = values.get(i);
//            if(relation.equals("None")){
//                continue;
//            }
                    Triple<String, String, String> tri = new ImmutableTriple<>(head, relation, tail);
                    triples.add(tri);
                }

                if(recommendTypesList.size() != 0 && triples.size() != 0){
                    BasicOperation.insertAndMergeWithSameName(triples, entityType, ontologySymbol, destination);

                    // update evolutionTag
                    List<EntityRaw> ens = kg.getEntities();
                    List<Integer> ids = new ArrayList<>();
                    for(EntityRaw en: ens){
                        ids.add(Integer.parseInt(en.getEntityId()));
                    }

                    Map<Integer, Map<String, Integer>> updateInfo = new HashMap<>();
                    for(Integer id: ids) {
                        Map<String, Integer> updateInfo_id = new HashMap<>();
                        updateInfo_id.put("evolutionTag", 1);
                        updateInfo.put(id, updateInfo_id);
                        BasicOperation.updataByidInteger(updateInfo);
                    }

                }

                for(String recommendType: recommendTypesList){
                    List<String> enIds = new ArrayList<>();
                    for(EntityRaw entity: schemaExtraction.getKg().getEntities()){
                        enIds.add(entity.getEntityId());
                    }
                    Set<String> item_ids = BasicOperation.queryItemIdsByEntityIds(enIds, "zonghe");
                    if(item_ids.size() == 0){
                        continue;
                    }
                    Map<String, String> item_from_zonghe = ItemQuery.queryItem(item_ids, "misre_km_wdtph_tmnr");
                    Map<String, String> item_from_raw = ItemQuery.queryItem(item_ids, "misre_km_bz_bzjznr");
                    Set<String> texts = new HashSet<>();
                    if(item_from_zonghe.size() != 0){
                        for(String text: item_from_zonghe.values()){
                            texts.add(text);
                        }
                    }
                    if(item_from_raw.size() != 0){
                        for(String text: item_from_raw.values()){
                            texts.add(text);
                        }
                    }
                    String aggText = "";
                    if(texts.size() > 0){
                        aggText = MergeConceptInfo.aggText(new LinkedList<>(texts));
                    }

                    if(aggText.length() == 0){
                        continue;
                    }

                    List<Long> checkNodeProperty = BasicOperation.checkNodeProp(recommendType, "item_id", destination);
                    if(checkNodeProperty.size() == 0){
                        // 生成新的item_id并将其文本信息插入数据库
                        Long item_id = GenerateUtil.generateUniqueId();
                        BasicOperation.setProperty(recommendType, destination, "item_id", item_id);
                        ItemQuery.insertConceptItemInfo(item_id, aggText);
                    }else{
                        // 将摘要信息插入到原始文档末尾，分为item_id找得到和找不到,这里直接再原内容上追加
                        Long item_id = checkNodeProperty.get(0);
                        ItemQuery.updateConceptInfo(item_id, aggText);
                    }
                }
            }
            logger.info("已更新evolutionTag");
        }
        logger.info("自动演化已完成");
        return flag;
        // here with raw method
//        Map<String, String> recommendTypeTree = schemaExtraction.evoAuto(); // parent child

    }



}
