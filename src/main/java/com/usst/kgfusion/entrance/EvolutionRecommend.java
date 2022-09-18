package com.usst.kgfusion.entrance;

import java.util.*;

import com.usst.kgfusion.constructer.GraphReader;
import com.usst.kgfusion.databaseQuery.BasicOperation;
import com.usst.kgfusion.pojo.Entity;
import com.usst.kgfusion.pojo.KG;

public class EvolutionRecommend {
    public static Set<String> execute(List<String> reIds) {
        KG kg = GraphReader.readGraph(BasicOperation.queryGraphByReIds(reIds), "zonghe");
        if(kg.getEntities() == null) return new HashSet<>();

        List<Integer> entityIds = new ArrayList<>();
        List<Entity> entityList = kg.getEntities();
        for(Entity entity: entityList){
            entityIds.add(Integer.parseInt(entity.getEntityId()));
        }
        Map<Integer,Map<String,Integer>> updateInfo = new HashMap<>();
        for(Integer jiid : entityIds){
            Map<String,Integer> temp = new HashMap<>();
            temp.put("evolutionTag",1);
            updateInfo.put(jiid,temp);
        }
        BasicOperation.updataByidInteger(updateInfo);

        SchemaExtraction schemaExtraction = new SchemaExtraction(kg);
        Set<String> res = schemaExtraction.evoRecommend();
        return res;
    }
}
