package com.usst.kgfusion.module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.usst.kgfusion.pojo.Entity;
import com.usst.kgfusion.pojo.KG;
import com.usst.kgfusion.pojo.Triple;

public class simFusion {
    Map<String,Object> simres;
    Map<Integer, HashMap<Integer, Double>> simMatrix;


    public simFusion(Map<Integer, HashMap<Integer, Double>> simMatrix, Map<String,Object> simres) {
        this.simMatrix = simMatrix;
        this.simres = simres;
    }


    //    融合的三元组
    public List<Triple> Fusion(int k, int k1, List<Triple> tripleList) {
        int tripleSize = tripleList.size();
//        System.out.println(tripleSize);
        Entity tempEntity = null;

//get k
        for (int i = 0; i < tripleSize; i++) {
            String entityHId = null;
            String entityTId = null;
            if (tripleList.get(i).getHead() != null) {
                entityHId = tripleList.get(i).getHead().getEntityId();
            }
            if (tripleList.get(i).getTail() != null) {
                entityTId = tripleList.get(i).getTail().getEntityId();
            }
//            find entity of k then use this entity instead of other sim node
            if (Integer.toString(k).equals(entityHId)) {
                tempEntity = tripleList.get(i).getHead();
//                tempEntity.setSrcId("2");
                break;
            }
            if (Integer.toString(k).equals(entityTId)) {
                tempEntity = tripleList.get(i).getTail();
//                tempEntity.setSrcId("2");
                break;
            }

        }

//find k1 and instead of it
        if (tempEntity != null) {

            for (int j = 0; j < tripleSize; j++) {
                Entity entityHId2 = null;
                Triple tp         = tripleList.get(j);
                if (tripleList.get(j).getHead() != null) {
                    entityHId2 = tripleList.get(j).getHead();
                }
                Entity entityTId2 = null;
                if (tripleList.get(j).getTail() != null) {
                    entityTId2 = tripleList.get(j).getTail();
                }

                if (Integer.toString(k1).equals(entityHId2.getEntityId())) {
                    tp.setHead(tempEntity);
                    List<Triple>     tpList   = new ArrayList<>();
                    Iterator<Triple> iterator = tripleList.iterator();
                    while (iterator.hasNext()) {
                        Triple triple = iterator.next();
                        if (triple.getTail() == tripleList.get(j).getTail() && triple.getHead() == tripleList.get(j).getHead()) {
                            tpList.add(triple);
                        }
                    }
                    tripleList.removeAll(tpList);
                    tripleList.add(tp);
                    break;
                }


                if (Integer.toString(k1).equals(entityTId2)) {
                    tp.setTail(tempEntity);

                    List<Triple>     tpList   = new ArrayList<>();
                    Iterator<Triple> iterator = tripleList.iterator();
                    while (iterator.hasNext()) {
                        Triple triple = iterator.next();
                        if (triple.getTail() == tripleList.get(j).getTail() && triple.getHead() == tripleList.get(j).getHead()) {
                            tpList.add(triple);
                        }
                    }
                    tripleList.removeAll(tpList);
                    tripleList.add(tp);
                    break;
                }
            }
        }
        return tripleList;
    }//finish once kn turn into k



    public Map<String,Object> entityFusion(Map<Integer, HashMap<Integer, Double>> simMatrix, Map<String,Object> simres) {
        KG tempKg= (KG) simres.get("kg");
        HashMap<String,List<String>> querynodes= (HashMap<String, List<String>>) simres.get("queryNodes");
        List<Triple> tripleList = new ArrayList<>();
//存放相似节点的map
        Map<Integer, HashMap<Integer, Double>> oldnodeSimMap = (Map<Integer, HashMap<Integer, Double>>) simres.get("simMap");
        Map<Integer, HashMap<Integer, Double>> nodeSimMap = new HashMap<>();
        Map<Integer, Double>                   simMap     = new HashMap<>();

        Map<String,Object> res=new HashMap<>();
//迭代
        Iterator<Map.Entry<Integer, HashMap<Integer, Double>>> it_3  = simMatrix.entrySet().iterator();
        HashMap<Integer, Double>                               temp1 = new HashMap();
//    temp1是相连的节点 并且与相连节点的相似度得分
        while (it_3.hasNext()) {
//		    遍历所有查询的节点
            Map.Entry entry2 = it_3.next();
            int       k      = (int) entry2.getKey();
//            System.out.print(k + "\t");
//        遍历所有相连节点的节点id和分数
            temp1 = (HashMap<Integer, Double>) entry2.getValue();
            Iterator<Map.Entry<Integer, Double>> it_4 = temp1.entrySet().iterator();
            tripleList = tempKg.getTriples();

            while (it_4.hasNext()) {
//            遍历相连的节点
                Map.Entry entry3 = it_4.next();
                int       k1     = (int) entry3.getKey();
                double    v      = (double) entry3.getValue();
                if (v != 0) {
                    if (v >= 0.5 && k1 != k) {
//                        改srcID
                        if (nodeSimMap.containsKey(k)) {
                            simMap = nodeSimMap.get(k);
                            simMap.put(k1, v);
                            nodeSimMap.put(k, (HashMap<Integer, Double>) simMap);
                        } else {
                            simMap = new HashMap<>();
                            simMap.put(k1, v);
                            nodeSimMap.put(k, (HashMap<Integer, Double>) simMap);
                        }

                        tripleList = Fusion(k, k1, tempKg.getTriples());


                    }
                }
                tempKg.setTriples(tripleList);
            }
//            System.out.println("\n");
        }



        List<Triple>                         ts         = tempKg.getTriples();
        Map<Entity, List<Entity>>  edges      = new HashMap<>();
        Map<Entity, List<Integer>> directions = new HashMap<>();
        for (Triple triple : ts) {
            Entity head = triple.getHead();
            Entity tail = triple.getTail();
            if (edges.get(head) == null) edges.put(head, new ArrayList<>());
            if (edges.get(tail) == null) edges.put(tail, new ArrayList<>());
            if (directions.get(head) == null) directions.put(head, new ArrayList<>());
            if (directions.get(tail) == null) directions.put(tail, new ArrayList<>());
            edges.get(head).add(tail);
            edges.get(tail).add(head);
            directions.get(head).add(0);
            directions.get(tail).add(1);
        }
        tempKg.setEdges(edges);
        tempKg.setDirections(directions);

        oldnodeSimMap.putAll(nodeSimMap);
        res.put("kg",tempKg);
        res.put("simMap",oldnodeSimMap);
        res.put("queryNodes",querynodes);

        return res;
    }


}