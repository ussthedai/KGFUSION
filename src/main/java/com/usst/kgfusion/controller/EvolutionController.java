package com.usst.kgfusion.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.usst.kgfusion.databaseQuery.BasicOperation;
import com.usst.kgfusion.databaseQuery.ItemQuery;
import com.usst.kgfusion.entrance.EvolutionAuto;
import com.usst.kgfusion.entrance.EvolutionRecommend;
import com.usst.kgfusion.entrance.MergeAuto;
import com.usst.kgfusion.entrance.MergeConceptInfo;
import com.usst.kgfusion.pojo.*;

import com.usst.kgfusion.util.Algorithm;
import org.neo4j.driver.v1.types.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/graph/evolution")
public class EvolutionController {

    private static final Logger logger = LoggerFactory.getLogger("com.usst.test");

    @Value("${commonApi.relation_classify_url}")
    private String url;

    @Value("${commonApi.useWho}")
    private Integer useWho;

    @Value("${commonApi.DatabaseChose}")
    private Integer DatabaseChose;

    @PostMapping("/auto")
    public String evolution_auto(@RequestBody String jsonStr) throws IOException {
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        String from = jsonObject.getString("graphSymbolSource");
        String destination = jsonObject.getString("graphSymbolAim");

        // 判断参数是否请求成功
        if (!jsonObject.containsKey("graphSymbolSource") || !jsonObject.containsKey("graphSymbolAim")) {
            logger.error("JSON参数错误");
            return JSON.toJSONString(new Res(false, 1002, "JSON参数错误", null));
        } else {
            Node node = BasicOperation.getRandomNode(from);
            if (node == null) {
                logger.error("图谱：" + from + "不存在");
                return JSON.toJSONString(new Res(false, 1002, "图谱不存在", null));
            }
            String entityType = node.get("entityType").asString();
            String ontologySymbol = node.get("ontologySymbol").asString();
            int flag = EvolutionAuto.execute(from, destination, entityType, ontologySymbol, url, useWho);
            if (flag == 1) {
                return JSON.toJSONString(new Res(true, 200, "操作成功", null));
            }
            if (flag == 0) {
                logger.info("图谱不存在");
                return JSON.toJSONString(new Res(false, 1000, "查询图谱不存在", null));
            }

        }

        return JSON.toJSONString(new Res(true, 200, "操作成功", null));
    }

    @PostMapping("/recommend")
    public String evolution_recommend(@RequestBody String jsonStr) throws IOException {

        JSONObject jsonObject = JSONObject.parseObject(jsonStr);

        if (!jsonObject.containsKey("entities") || !jsonObject.containsKey("relations")) {
            logger.error("JSON参数错误,实体或关系不存在");
            return JSON.toJSONString(new Res(false, 1002, "JSON参数错误", null));
        }

        List<EvoRecEnParam> entities = JSON.parseArray(jsonObject.get("entities").toString(), EvoRecEnParam.class);
        List<EvoRecReParam> relations = JSON.parseArray(jsonObject.get("relations").toString(), EvoRecReParam.class);
        if (entities.size() == 0) {
            logger.error("参数错误,实体列表为空");
            return JSON.toJSONString(new Res(false, 1002, "实体或关系列表为空", null));
        }
        if (relations.size() == 0) {
            logger.info("参数接受成功，推荐演化计算开始");
            // Set<String> choices = choice();
            // return choices;
            // return new HashSet<>();

            // use edit distance to return res
            Set<String> res = new HashSet<>(); // select from choices
            // List<Entity> ens = this.kg.getEntities();
            List<String> ensNames = new ArrayList<>();
            for (EvoRecEnParam entity : entities) {
                ensNames.add(entity.getEntityName());
            }

            double maxValue = 0.0;
            String bestChoice = "";

            Map<String, String> typeParentMap = new HashMap<>();
            BufferedReader br = null;
            try {
                InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("static/c_info.csv");
                br = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
                String line = null;
                while ((line = br.readLine()) != null) {
                    String[] strs = line.split(",");
                    String child = strs[0];
                    String parent = strs[2];
                    if (typeParentMap == null) {
                        typeParentMap = new HashMap<>();
                    }
                    typeParentMap.put(child, parent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {

                if (br != null) {
                    br.close();
                }

            }

            Set<String> choices = new HashSet<>();
            choices.addAll(typeParentMap.keySet());

            for (String choiceName : choices) {
                double sum = 0.0;
                for (String enName : ensNames) {
                    double simValue = Algorithm.levenshtein(enName, choiceName);
                    sum += simValue;
                }
                if (sum > maxValue) {
                    maxValue = sum;
                    bestChoice = choiceName;
                }
            }
            res.add(bestChoice);
            // logger.error("参数错误,关系列表为空");
            if (res.size() != 0 && !bestChoice.equals("")) {
                List<String> enIds = new ArrayList<>();
                for (EvoRecEnParam en : entities) {
                    enIds.add(en.getEntityId());
                }
                // List<String> reIds = new ArrayList<>();
                // for (EvoRecReParam re : relations) {
                // reIds.add(re.getRelationId());
                // }
                // Set<String> recommendTypes = EvolutionRecommend.execute(reIds);
                // if(recommendTypes.size() == 0){
                // logger.error("参数错误,关系不存在");
                // return JSON.toJSONString(new Res(false, 1002, "关系不存在", null));
                // }
                // Neo4jUtils.close();

                String entityType = entities.get(0).getEntityType();
                String ontologySymbol = entities.get(0).getOntologySymbol();
                String summary = "";
                List<EvoData> evoRecRes = new ArrayList<>();
                for (String conceptEntityName : res) {
                    if (conceptEntityName != null) {
                        // Set<String> item_ids = BasicOperation.queryItemIdsByEntityIds(enIds,
                        // "zonghe");
                        // if (item_ids.size() == 0) {
                        // evoRecRes.add(new EvoData(conceptEntityName, entityType, ontologySymbol,
                        // summary));
                        // continue;
                        // }
                        // Map<String, String> item_from_zonghe = ItemQuery.queryItem(item_ids,
                        // "misre_km_wdtph_tmnr");
                        // Map<String, String> item_from_raw = ItemQuery.queryItem(item_ids,
                        // "misre_km_bz_bzjznr");
                        // Set<String> texts = new HashSet<>();
                        // if (item_from_zonghe.size() != 0) {
                        // for (String text : item_from_zonghe.values()) {
                        // texts.add(text);
                        // }
                        // } else {
                        // logger.info("综合图谱中不存在条目内容");
                        // }

                        // if (item_from_raw.size() != 0) {
                        // for (String text : item_from_raw.values()) {
                        // texts.add(text);
                        // }
                        // } else {
                        // logger.info("原始文档中不存在条目内容");
                        // }

                        Map<Integer, List<String>> iteminfo = BasicOperation.queryItemIdsByEntityIds_new(enIds);

                        Set<String> texts = new HashSet<>();
                        if (iteminfo.size() == 0) {
                            summary = "";
                        } else {
                            for (int graphtype : iteminfo.keySet()) {
                                Set<String> item_ids = new HashSet<>(iteminfo.get(graphtype));
                                Map<String, String> item_text = ItemQuery.queryItemIds_new(item_ids, graphtype);
                                if (item_text.size() > 0) {
                                    for (String text : item_text.values()) {
                                        texts.add(text);
                                    }
                                }
                            }
                        }

                        String aggText = "";
                        if (texts.size() > 0) {
                            logger.info("概念文档生成成功");
                            aggText = MergeConceptInfo.aggText(new LinkedList<>(texts));
                        } else {

                            aggText = conceptEntityName;
                            logger.info("概念文档生成");
                        }
                        evoRecRes.add(new EvoData(conceptEntityName, entityType, ontologySymbol, aggText));
                    }
                }
                logger.info("推荐演化已完成");
                String jsonRes = JSON.toJSONString(new Res(true, 200, "演化成功", evoRecRes));
                return jsonRes;
                // return JSON.toJSONString(new Res(false, 1002, "演化成功", res));
            } else {
                logger.info("推荐演化已完成");
                return JSON.toJSONString(new Res(false, 1002, "演化结果为空", null));
            }

        }

        logger.info("参数接受成功，推荐演化计算开始");
        List<String> enIds = new ArrayList<>();
        for (EvoRecEnParam en : entities) {
            enIds.add(en.getEntityId());
        }
        List<String> reIds = new ArrayList<>();
        for (EvoRecReParam re : relations) {
            reIds.add(re.getRelationId());
        }
        Set<String> recommendTypes = EvolutionRecommend.execute(reIds);
        if (recommendTypes.size() == 0) {
            logger.error("参数错误,关系不存在");
            return JSON.toJSONString(new Res(false, 1002, "关系不存在", null));
        }
        // Neo4jUtils.close();

        String entityType = entities.get(0).getEntityType();
        String ontologySymbol = entities.get(0).getOntologySymbol();
        String summary = "";
        List<EvoData> evoRecRes = new ArrayList<>();
        for (String conceptEntityName : recommendTypes) {
            if (conceptEntityName != null) {
                // Set<String> item_ids = BasicOperation.queryItemIdsByEntityIds(enIds,
                // "zonghe");
                // if (item_ids.size() == 0) {
                // evoRecRes.add(new EvoData(conceptEntityName, entityType, ontologySymbol,
                // summary));
                // continue;
                // }
                // Map<String, String> item_from_zonghe = ItemQuery.queryItem(item_ids,
                // "misre_km_wdtph_tmnr");
                // Map<String, String> item_from_raw = ItemQuery.queryItem(item_ids,
                // "misre_km_bz_bzjznr");
                // Set<String> texts = new HashSet<>();
                // if (item_from_zonghe.size() != 0) {
                // for (String text : item_from_zonghe.values()) {
                // texts.add(text);
                // }
                // } else {
                // logger.info("综合图谱中不存在条目内容");
                // }

                // if (item_from_raw.size() != 0) {
                // for (String text : item_from_raw.values()) {
                // texts.add(text);
                // }
                // } else {
                // logger.info("原始文档中不存在条目内容");
                // }

                Map<Integer, List<String>> iteminfo = BasicOperation.queryItemIdsByEntityIds_new(enIds);

                Set<String> texts = new HashSet<>();
                if (iteminfo.size() == 0) {
                    summary = "";
                } else {
                    for (int graphtype : iteminfo.keySet()) {
                        Set<String> item_ids = new HashSet<>(iteminfo.get(graphtype));
                        Map<String, String> item_text = ItemQuery.queryItemIds_new(item_ids, graphtype);
                        if (item_text.size() > 0) {
                            for (String text : item_text.values()) {
                                texts.add(text);
                            }
                        }
                    }
                }

                String aggText = "";
                if (texts.size() > 0) {
                    logger.info("概念文档生成成功");
                    aggText = MergeConceptInfo.aggText(new LinkedList<>(texts));
                } else {
                    aggText = conceptEntityName;
                    logger.info("概念文档生成");
                }
                evoRecRes.add(new EvoData(conceptEntityName, entityType, ontologySymbol, aggText));
            }
        }

        // query input entities item_id and text from table zonghetexttable and
        // gainiantexttable
        // Set<String> item_ids = BasicOperation.queryItemIdsByEntityIds(enIds);
        // Map<String, String> item_from_zonghe = ItemQuery.queryItem(item_ids,
        // "misre_km_wdtph_tmnr");
        // Map<String, String> item_from_raw = ItemQuery.queryItem(item_ids,
        // "misre_km_bz_bzjznr");
        // Set<String> texts = new HashSet<>();
        // if(item_from_zonghe != null){
        // for(String text: item_from_zonghe.values()){
        // texts.add(text);
        // }
        // }
        // if(item_from_raw != null){
        // for(String text: item_from_raw.values()){
        // texts.add(text);
        // }
        // }
        // String aggText = MergeConceptInfo.aggText(new LinkedList<>(texts));
        logger.info("推荐演化已完成");
        String jsonRes = JSON.toJSONString(new Res(true, 200, "操作成功", evoRecRes));
        return jsonRes;
    }

}
