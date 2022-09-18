package com.usst.kgfusion.entrance;

import com.usst.kgfusion.pojo.Entity;
import com.usst.kgfusion.pojo.KG;
import com.usst.kgfusion.pojo.Triple;
import org.checkerframework.framework.qual.LiteralKind;
import org.neo4j.driver.internal.async.HandshakeHandler;

import java.util.*;

public class JaccardMethod {

    private KG kg;
    private String graphsymbol;

    public KG getKg() {
        return kg;
    }

    public void setKg(KG kg) {
        this.kg = kg;
    }

    public String getGraphsymbol() {
        return graphsymbol;
    }

    public void setGraphsymbol(String graphsymbol) {
        this.graphsymbol = graphsymbol;
    }

    public JaccardMethod(String symbol, KG kg){
        this.graphsymbol = symbol;
        this.kg = kg;
    }

    public Map<Entity,List<Entity>> getSameEntitys(Entity en1,List<Entity> entityList) {
        Map<Entity,List<Entity>> res = new HashMap<>();
        List<Entity> res2 = new ArrayList<>();
        res2.add(en1);
        for(int a =0 ;a <entityList.size();a++){
            if(en1.getName().equals(entityList.get(a).getName())){
                if(!en1.getEntityId().equals(entityList.get(a).getEntityId())){
                    res2.add(entityList.get(a));
                }
            }
        }
        if(res2.size()>1){
            res.put(en1,res2);
        }
        return res;
    }

    public Set<String> getEntityNeighbor(Entity en , List<Triple> synTriples) {
        Set<String> res = new HashSet<>();
        Set<String> neighbor = new HashSet<>();
        neighbor.add(en.getEntityId());
        for(int a =0 ;a <synTriples.size();a++){
            Entity head = synTriples.get(a).getHead();
            Entity tail = synTriples.get(a).getTail();
            if(head.equals(en) && !tail.equals(en)){
                if(!neighbor.contains(tail.getEntityId())){
                    res.add(tail.getEntityId());
                    neighbor.add(tail.getEntityId());
                }
            }else if(!head.equals(en) && tail.equals(en)){
                if(!neighbor.contains(head.getEntityId())){
                    res.add(head.getEntityId());
                    neighbor.add(head.getEntityId());
                }
            }
        }
        return res;
    }

    public float JaccardScore (Set<String> s1,Set<String> s2){
        float mergeNum = 0;//并集元素个数
        float commonNum = 0;//相同元素个数（交集）

        for(String ch1:s1) {
            for(String ch2:s2) {
                if(ch1.equals(ch2)) {
                    commonNum++;
                }
            }
        }

        mergeNum = s1.size()+s2.size()-commonNum;

        float res = commonNum/mergeNum;
        return res;
    }

    public Map<String,Object> entrance(float EX_score,String task_id) {
        KG synKG = getKg();
        List<Triple> synTriples = synKG.getTriples();
        List<Entity> synEntitys = synKG.getEntities();
        Map<String,Object> res = new HashMap<>();
        Set<String> nameList = new HashSet<>();

        Map<String,List<Map<String,String>>> ids_res = new LinkedHashMap<>();

        int count = 0;
        for(int i=0;i<synEntitys.size();i++){
            if(!nameList.contains(synEntitys.get(i).getName())){
                nameList.add(synEntitys.get(i).getName());
                Map<Entity,List<Entity>> SameNameList = getSameEntitys(synEntitys.get(i),synEntitys);
                List<Entity> te1 = SameNameList.get(synEntitys.get(i));
                if(SameNameList.size()>0){
                    Set<String> en1_set = getEntityNeighbor(te1.get(0),synTriples);
                    String en1_type = te1.get(0).getEntityType();

                    Map<String,String> ambiguity_id2name_1 = new LinkedHashMap<>();
                    Map<String,String> ambiguity_id2name_2 = new LinkedHashMap<>();

                    Map<String,String> ambiguity_id2name_3 = new LinkedHashMap<>();
                    ambiguity_id2name_1.put(te1.get(0).getEntityId(),te1.get(0).getName());
                    ambiguity_id2name_3.put(te1.get(0).getEntityId(),te1.get(0).getName());

                    for(int b=1;b<te1.size();b++){
                        Set<String> en2_set = getEntityNeighbor(te1.get(b),synTriples);
                        String en2_type = te1.get(b).getEntityType();
                        if(!en1_type.equals(en2_type)){
                            float score = JaccardScore(en1_set,en2_set);
                            if(score > EX_score){
                                ambiguity_id2name_1.put(te1.get(b).getEntityId(),te1.get(b).getName());
                            }else{
                                ambiguity_id2name_2.put(te1.get(b).getEntityId(),te1.get(b).getName());
                            }
                        }
                        ambiguity_id2name_3.put(te1.get(b).getEntityId(),te1.get(b).getName());

                    }
                    List<Map<String,String>> ids_res1 = new ArrayList<>();
                    if(ambiguity_id2name_1.size()>1){

                        ids_res1.add(ambiguity_id2name_1);
                    }
                    if(ambiguity_id2name_2.size()>0){

                        ids_res1.add(ambiguity_id2name_2);
                    }
                    if(ambiguity_id2name_1.size()==1 && ambiguity_id2name_2.size()==0){
                        ids_res1.add(ambiguity_id2name_3);
                    }

                    count++;
                    if(ids_res1.size()>0){

                        ids_res.put(te1.get(0).getEntityId(),ids_res1);
                    }

                }


            }

        }
        res.put("ids",ids_res);

        res.put("count",count);
        return res;

    }



}
