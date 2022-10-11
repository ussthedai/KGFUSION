package com.usst.kgfusion.module;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import com.usst.kgfusion.pojo.EntityRaw;
import com.usst.kgfusion.pojo.KG;

public class GRM {
    // 构造一个带权重的网络
    // 首先是定义好 节点集合 边集合 和权重
    // 1.初始化 构造一个Egi,Wgi,作为空 Vset是不同类型的集合
    public static Map<String, String> getEdge(KG kg) {
        Map<EntityRaw, List<EntityRaw>> edgelist = kg.getEdges();
        Map<String, String> edges = new HashMap<>();
        Iterator<Map.Entry<EntityRaw, List<EntityRaw>>> edgeiter = edgelist.entrySet().iterator();
        while (edgeiter.hasNext()) {
            Map.Entry<EntityRaw, List<EntityRaw>> edgeEntry = edgeiter.next();
            EntityRaw edgeEnt = edgeEntry.getKey();
            String entityId = edgeEnt.getEntityId();
            List<EntityRaw> entlist = edgeEntry.getValue();
            for (EntityRaw entity2 : entlist) {
                String entityId2 = entity2.getEntityId();
                edges.put(entityId, entityId2);
            }
        }
        return edges;
    }

    public static Map<String, Set<String>> getTypeMap(KG kg) {
        List<EntityRaw> entList = kg.getEntities();
        Map<String, Set<String>> typeMap = new HashMap<>();
        Set<String> typeSet = new HashSet<>();
        for (EntityRaw entity : entList) {
            String typeId = entity.getEntityType();
            String entityId = entity.getEntityId();

            if (typeMap.containsKey(typeId)) {
                typeSet = typeMap.get(typeId);
                typeSet.add(entityId);
                typeMap.put(typeId, typeSet);

            } else {
                typeSet = new HashSet<>();
                typeSet.add(entityId);
                typeMap.put(typeId, typeSet);
            }
        }
        return typeMap;
    }
    

    public boolean isInOrOut(Map<String, Set<String>> inOrOutMap, String entityId1, String entityId2) {
        Set<String> inSet = inOrOutMap.get(entityId1);
        boolean flag = false;
        for (String id : inSet) {
            if (id.equals(entityId2)) {
                flag = true;
            }
        }
        return flag;
    }

    public static Map<String, Set<String>> getTypeEns(KG kg) {
        Map<String, Set<String>> res = new HashMap<>();
        List<EntityRaw> ens = kg.getEntities();
        for (EntityRaw entity : ens) {
            String id = entity.getEntityId();
            String typeId = entity.getEntityType();
            if (!res.containsKey(typeId)) {
                res.put(typeId, new HashSet<>());
            }
            res.get(typeId).add(id);
        }
        return res;
    }

    public static Map<String, Map<String, Double>> calRW(KG kg, Map<String, List<String>> transEdges) {
        Map<String, Set<String>> typeEns = getTypeEns(kg);
        List<String> types = new ArrayList<>(typeEns.keySet());
        Map<String, Map<String, Double>> res = new LinkedHashMap<>();

        for (int i = 0; i < types.size(); i++) {
            Map<String, Double> temp = new HashMap<>();
            for (int j = 0; j < types.size(); j++) {
                Set<String> left_set = typeEns.get(types.get(i));
                Set<String> right_set = typeEns.get(types.get(j));
                int count = 0;
                for (String le : left_set) {
                    for (String re : right_set) {
                        if (transEdges.containsKey(le) && transEdges.get(le).contains(re)) {
                            count++;
                        }
                    }
                }
                temp.put(types.get(j), (double) count);
            }
            res.put(types.get(i), temp);
        }

        for (String key : res.keySet()) {
            int sum = 0;
            for (Double num : res.get(key).values()) {
                sum += num;
            }
            for (String key2 : res.get(key).keySet()) {
                res.get(key).put(key2, res.get(key).get(key2) / sum);
            }

        }

        return res;
    }

    public static Map<String, String> getEntityTypeMap(KG kg) {
        Map<String, String> entityTypeMap = new HashMap<>();
        List<EntityRaw> ens = kg.getEntities();
        for (EntityRaw entity : ens) {
            if (entity.getEntityType() != null) {
                String type = entity.getEntityType(); // 对于实体类型只有一个的情况
                entityTypeMap.put(entity.getEntityId(), type);
            }

        }
        return entityTypeMap;
    }

    public static Map<String, List<String>> translate(KG kg) {  // outmap
        Map<String, List<String>> res = new LinkedHashMap<>();
        Map<EntityRaw, List<EntityRaw>> edges = kg.getEdges();
        Map<EntityRaw, List<Integer>> directions = kg.getDirections();
        for (EntityRaw en : edges.keySet()) {
            List<EntityRaw> list = edges.get(en);
            List<Integer> dir = directions.get(en);
            for (int i = 0; i < list.size(); i++) {
                EntityRaw en2 = list.get(i);
                int direction = dir.get(i);
                if (direction == 0) {
                    if (!res.containsKey(en.getEntityId())) {
                        res.put(en.getEntityId(), new ArrayList<>());
                    }
                    res.get(en.getEntityId()).add(en2.getEntityId());
                }
            }
        }
        return res;
    }

    // 统计实体分别和不同类型的实体连接了几次
    public static Map<String, Map<String, Integer>> typeCountMap(Map<String, List<String>> transEdges,
            Map<String, String> entityTypeMap) {
        Map<String, Map<String, Integer>> res = new LinkedHashMap<>();
        for (String en : transEdges.keySet()) {
            Map<String, Integer> temp = new LinkedHashMap<>();
            for (String nei : transEdges.get(en)) {
                String type = entityTypeMap.get(nei);
                if (!temp.containsKey(type)) {
                    temp.put(type, 1);
                } else {
                    temp.put(type, temp.get(type) + 1);
                }

            }
            res.put(en, temp);
        }

        return res;
    }

    public static Map<String, Map<String, Double>> geRw(KG kg){
        Map<String, List<String>> transEdges = translate(kg);
        Map<String, Map<String, Double>> rw = calRW(kg, transEdges);
        return rw;
    }

    public static Map<String, Map<String, Double>> geNodesWeightMap(KG kg) {
        Map<String, Map<String, Double>> res = new LinkedHashMap<>();
        Map<String, List<String>> transEdges = translate(kg);
        Map<String, Map<String, Double>> rw = calRW(kg, transEdges);

        Map<String, String> entityTypeMap = getEntityTypeMap(kg);
        Map<String, Map<String, Integer>> typeCountMap = typeCountMap(transEdges, entityTypeMap);
        for (Entry<String, List<String>> entry : transEdges.entrySet()) {
            String from = entry.getKey();
            String from_type = entityTypeMap.get(from);
            List<String> neighbours = entry.getValue();
            Map<String, Double> temp = new LinkedHashMap<>();
            for (String neighbour : neighbours) {
                String to_type = entityTypeMap.get(neighbour);
                Double value = 0.0;
                int connectEdge = 0;
                Double rwValue = 0.0;
                if (rw.containsKey(from_type) && rw.get(from_type).containsKey(to_type)) {
                    rwValue = rw.get(from_type).get(to_type);
                }

                if (typeCountMap.get(from) != null && typeCountMap.get(from).get(to_type) != null) {
                    connectEdge = typeCountMap.get(from).get(to_type);
                }
                value += rwValue * (1.0 / connectEdge);
                temp.put(neighbour, value);
            }
            res.put(from, temp);
        }

        // normalize
        // for (String key : res.keySet()) {
        //     double sum = 0;
        //     for (Double num : res.get(key).values()) {
        //         sum += num;
        //     }
        //     for (String key2 : res.get(key).keySet()) {
        //         res.get(key).put(key2, res.get(key).get(key2) / sum);
        //     }
        // }
        return res;
    }


    public static void main(String[] args) throws IOException {
        // GRM grm=new GRM();
        // GraphConstructer constructGraph = new GraphConstructer("all", "triple");
        // KG kg = constructGraph.construct();
        // Map<Integer, Map<Integer, Double>> weightMap = grm.geNodesWeightMap(kg);
        // Map < String, Map < String, Double >> rwScoreMap=grm.calRW(kg);

        // Iterator<Map.Entry<String, Map<String, Double>>> entries =
        // rwScoreMap.entrySet().iterator();

        // while (entries.hasNext()) {
        // Map.Entry<String, Map<String, Double>> entry = entries.next();
        // String key=entry.getKey();
        // Map<String,Double> value=entry.getValue();
        // Iterator<Map.Entry<String, Double>> entries2 = value.entrySet().iterator();
        // while (entries2.hasNext()){
        // Map.Entry<String, Double> entry2 = entries2.next();
        // String key2=entry2.getKey();
        // Double weight=entry2.getValue();
        // System.out.println(key+" "+key2+" "+weight);
        // }
    }

    // }

}