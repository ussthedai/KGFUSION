package com.usst.kgfusion.util;

import com.usst.kgfusion.pojo.EntityRaw;
import com.usst.kgfusion.pojo.KG;
import edu.uci.ics.jung.graph.SparseGraph;

import java.lang.reflect.Array;
import java.util.*;

/**
 * @program: kgfusion
 * @description: 图相关方法
 * @author: JH_D
 * @create: 2022-01-08 18:07
 **/


class Pair<T> {
    T first;
    T second;

    public Pair(T first, T second) {
        this.first = first;
        this.second = second;
    }

    public T getFirst() {
        return first;
    }

    public T getSecond() {
        return second;
    }

    public void setFirst(T first) {
        this.first = first;
    }

    public void setSecond(T second) {
        this.second = second;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return first.toString() + "," + second.toString();
    }
}
public class GraphUtil {
    // 将图谱转换成为node:[], edges:[[]]
    public static SparseGraph transKGtoGraph(KG kg, int id_or_name){
        SparseGraph graph = new SparseGraph();
        List<EntityRaw> ens = kg.getEntities();
        Map<EntityRaw, List<EntityRaw>> eds = kg.getEdges();
        for(EntityRaw en: ens){
            if(id_or_name == 0){
                graph.addVertex(en.getEntityId());
            }
            if(id_or_name == 1){
                graph.addVertex(en.getName());
            }

        }
        int num_edge = 0;
        for(EntityRaw key: eds.keySet()){
            List<EntityRaw> neis = eds.get(key);
            for(EntityRaw nei: neis){
                if(id_or_name == 0 && num_edge <= (Long.MAX_VALUE - 1)){
                    graph.addEdge(num_edge++, key.getEntityId(), nei.getEntityId());
                }
                if(id_or_name == 1 && num_edge <= (Long.MAX_VALUE - 1)){
                    graph.addEdge(num_edge++, key.getName(), nei.getName());
                }

            }
        }

        return graph;
    }

    public static List<KG> split(KG kg, Set<Set<String>> clusters, Map<String, EntityRaw> idx2Entity){
        List<KG> res = new LinkedList<>();
        for(Set<String> cluster: clusters){
            Map<EntityRaw, List<EntityRaw>> edges = new LinkedHashMap<>();
            List<EntityRaw> entities = new LinkedList<>();
            Map<EntityRaw, List<Integer>> directions = new LinkedHashMap<>();
            for(String enName: cluster){
                entities.add(idx2Entity.get(enName));
                if(kg.getEdges().containsKey(idx2Entity.get(enName))){
                    edges.put(idx2Entity.get(enName), kg.getEdges().get(idx2Entity.get(enName)));
                }
                if(kg.getDirections().containsKey(idx2Entity.get(enName))){
                    directions.put(idx2Entity.get(enName), kg.getDirections().get(idx2Entity.get(enName)));
                }

            }
            res.add(new KG(entities, edges, directions));
        }
        return res;
    }
}
