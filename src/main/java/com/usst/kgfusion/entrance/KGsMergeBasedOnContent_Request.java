package com.usst.kgfusion.entrance;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.usst.kgfusion.databaseQuery.ItemQuery;
import com.usst.kgfusion.pojo.Entity;
import com.usst.kgfusion.pojo.KG;
import com.usst.kgfusion.pojo.Triple;
import com.usst.kgfusion.util.GenerateUtil;
import com.usst.kgfusion.util.RequestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class KGsMergeBasedOnContent_Request {

//    @Value("${commonApi.entity_classify_url}")
//    private String url_Type;
//
//    @Value("${commonApi.entity_similarity_url}")
//    private String url_similarity;

    private List<KG> KGs;
    private String source_kgSymbol;
    private String destination_kgSymbol;
    public List<KG> getCleanedKGs() {
        return KGs;
    }
    public String getsource_kgSymbol() {
        return source_kgSymbol;
    }
    public String getdestination_kgSymbol() {
        return destination_kgSymbol;
    }

    public KG runMerge(List<KG> kgs,Map<String,List<String>> Sim_entityids) throws IOException {
        return mergeProcess(kgs,Sim_entityids);
    }

    public KGsMergeBasedOnContent_Request(List<KG> KGs, String source_kgSymbol, String destination_kgSymbol) {
        this.KGs = KGs;
        this.source_kgSymbol = source_kgSymbol;
        this.destination_kgSymbol = destination_kgSymbol;
    }

    static class MergeTool {

        public static Entity findByEntityId(List<Entity> entityList, String id){
            for(Entity entity :entityList){
                if(entity.getEntityId().equals(id)){
                    return entity;
                }
            }
            Entity entity1 = new Entity();
            System.out.println("none");
            return entity1;
        }

    }

    public KG mergeProcess(List<KG> kgs,Map<String,List<String>> Sim_entityids) throws IOException {

        //0.前期数据处理，此阶段为获取entitylist和triplelist;
        List<Entity> tempentitys = new ArrayList<Entity>();
        List<Triple> temptriples = new ArrayList<Triple>();

        List<Entity> entitys = new ArrayList<Entity>();//entitylist集合用于修改
        List<Entity> entitys1 = new ArrayList<Entity>();//entitylist集合用于查询元素（不修改）

        List<Triple> triples = new ArrayList<Triple>();


        for (KG kg : kgs) {
            temptriples = kg.getTriples();
            for (int j = 0; j < temptriples.size(); j++) {
                triples.add(temptriples.get(j));
            }
        }
        for (KG kg : kgs) {
            tempentitys = kg.getEntities();
            for (int i = 0; i < tempentitys.size(); i++) {
                entitys.add(tempentitys.get(i));
                entitys1.add(tempentitys.get(i));
            }
        }



        //0.结束

        //合并过程
        //1.删除被合并结点，修改相关三元组
        List<String> removeid = new ArrayList<String>();//用于存储要删除的实体ID集
        //1.1修改三元组
        for(Map.Entry<String,List<String>> entry:Sim_entityids.entrySet()){
            List<String> ids = entry.getValue();
            for(int i=0;i< ids.size();i++) {
                removeid.add(ids.get(i));
            }

            for (int k = 0; k < ids.size(); k++) {
                for (Triple triple : triples) {
                    Entity entity1 = triple.getHead();
                    Entity entity2 = triple.getTail();
                    if (entity2.getEntityId().equals(ids.get(k))) {//修改三元组
                        triple.setTail(MergeTool.findByEntityId(entitys1, entry.getKey()));
                    }
                    if (entity1.getEntityId().equals(ids.get(k))) {
                        triple.setHead(MergeTool.findByEntityId(entitys1, entry.getKey()));
                    }
                }
            }
        }



        //1.2 通过记录的实体ID删除结点
        for (int iu = 0; iu < removeid.size(); iu++) {
            for (Entity entity : entitys) {
                if (entity.getEntityId().equals(removeid.get(iu))) {
                    entitys.remove(entity);
                    break;
                }
            }
        }
        removeid.clear();//清空List，方便后续操作，以记录头尾实体相同的三元组的实体ID并删除。

        HashMap<String,Integer> triplemap = new HashMap<>();//将新生成的triple放入newtriples
        for (Entity entity : entitys) {
            triplemap.put(entity.getEntityId(), 1);
        }
        List<Integer> removelist=new ArrayList<>();//需删除的三元组ID list
        for(Triple triple:triples){
            String headid=triple.getHead().getEntityId();
            String tailid=triple.getTail().getEntityId();
            if(headid.equals(tailid)){//头尾实体相同，舍去三元组
                if(!removeid.contains(headid)){
                    removeid.add(headid);//同时去掉该实体
                }
                if(!removelist.contains(Integer.parseInt(triple.getTripleId()))){
                    removelist.add(Integer.parseInt(triple.getTripleId()));//三元组删除ID集合
                }
            }
        }

        for (int iu = 0; iu < removelist.size(); iu++) {//通过记录的三元组ID删除三元组
            for (Triple triple : triples) {
                if (Integer.parseInt(triple.getTripleId())==(removelist.get(iu))) {
                    triples.remove(triple);
                    break;
                }
            }
        }
        List<String> removeids=new ArrayList<>();//如果头尾实体相同的三元组中的实体在别的三元组出现过，则不能删除
        for(int iu = 0; iu < removeid.size(); iu++){
            int count=0;
            for(Triple triple:triples){
                if(triple.getHead().getEntityId().equals(removeid.get(iu))||triple.getTail().getEntityId().equals(removeid.get(iu))) {
                    count++;
                }
            }
            if(count>=1){
                String id = removeid.get(iu);
                removeids.add(id);
            }
        }
        for(int iu = 0; iu < removeids.size(); iu++) {//在待删结点idList中去掉在别的三元组出现过的实体ID
            for(String id :removeid){
                if(removeids.get(iu).equals(id)){
                    removeid.remove(id);
                    break;
                }
            }
        }
        for (int iu = 0; iu < removeid.size(); iu++) {//通过记录的结点ID删除结点
            for (Entity entity : entitys) {
                if (entity.getEntityId().equals(removeid.get(iu))) {
                    entitys.remove(entity);
                    break;
                }
            }
        }

        List<Entity> newNodes = new ArrayList<Entity>();//修改完成，将新的ENtity集合转成newnodelist
        for (Entity entity : entitys) {
            String nodeid = entity.getEntityId();
            String nodename = entity.getName();
            String typeid=entity.getEntityType();
            String subclass = entity.getEntitySubClass();
            String graphSym = entity.getGraphSymbol();
            String itemid = entity.getItemId();
            String srcid = entity.getSrcId();
            Entity node = new Entity(nodeid, nodename,typeid,subclass,graphSym, itemid,srcid);
            newNodes.add(node);
        }


        //3.结束


        //4.合并完。相同三元组内容去重
        List<Triple> Quchong_triples = new ArrayList<Triple>();
        List<String> Quchong_List = new ArrayList<>();
        for(Triple triple : triples){
            String temp = triple.getHead().getName() + triple.getRela() + triple.getTail().getName();
            if(!Quchong_List.contains(temp)){
                Quchong_triples.add(triple);
                Quchong_List.add(temp);
            }
        }


        KG kg = new KG(newNodes,Quchong_triples);
        List<Triple> ts = kg.getTriples();
        LinkedHashMap<Entity,List<Entity>> edges = new LinkedHashMap<>();
        LinkedHashMap<Entity,List<Integer>> directions = new LinkedHashMap<>();
        for(Triple triple:ts){
            Entity head = triple.getHead();
            Entity tail = triple.getTail();
            if(edges.get(head)==null)edges.put(head,new ArrayList<>());
            if(edges.get(tail)==null)edges.put(tail,new ArrayList<>());
            if(directions.get(head)==null)directions.put(head,new ArrayList<>());
            if(directions.get(tail)==null)directions.put(tail,new ArrayList<>());
            edges.get(head).add(tail);
            edges.get(tail).add(head);
            directions.get(head).add(0);
            directions.get(tail).add(1);
        }

        kg.setEdges(edges);
        kg.setDirections(directions);

        return kg;

    } //得到相似结点对，进行多个图谱合并


    public Map<String,Object> entrance(String url_sim, String url_type) throws Exception {

        List<KG> cleanedKGs = getCleanedKGs();
        String source = getsource_kgSymbol();
        String des = getdestination_kgSymbol();

        HashMap<String,String> entityidname = new HashMap<>();
        HashMap<String,String> entitynameid = new HashMap<>();
        HashMap<String,List<String>> Query_entityids = new HashMap<>();
        HashMap<String,List<String>> Query_entityName = new HashMap<>();
        HashMap<String,List<String>> Sim_entityids = new HashMap<>();
        List<Entity> sourcekg_entitys = new ArrayList<Entity>();
        List<Entity> deskg_entitys = new ArrayList<Entity>();
        List<Entity> allentitys = new ArrayList<>();


        List<Entity> tempentitys = new ArrayList<Entity>();//读取Entity列表
        for (KG kg : cleanedKGs) {
            tempentitys = kg.getEntities();
            for (int i = 0; i < tempentitys.size(); i++) {
                if(tempentitys.get(i).getGraphSymbol().equals(source)){
                    sourcekg_entitys.add(tempentitys.get(i));
                }else if(tempentitys.get(i).getGraphSymbol().equals(des)){
                    deskg_entitys.add(tempentitys.get(i));
                }
            }
        }
        allentitys.addAll(sourcekg_entitys);
        allentitys.addAll(deskg_entitys);

        //实体分类调用
        String url_Type = url_type;
        Map<Object,Object> SendEntityName = new HashMap<>();
        List<String> entitynamelist = new ArrayList<>();
        for(Entity node1:sourcekg_entitys){
            entitynameid.put(node1.getName(),node1.getEntityId());
            entitynamelist.add(node1.getName());
        }
        for(Entity node2:deskg_entitys){
            entitynameid.put(node2.getName(),node2.getEntityId());
            entitynamelist.add(node2.getName());
        }
        SendEntityName.put("ins",entitynamelist);
        String Typejson = RequestHelper.sendJsonWithHttp(url_Type, JSON.toJSONString(SendEntityName));
        Map maps = (Map)JSON.parse(Typejson);

        JSONArray typelist = (JSONArray) maps.get("classify_name");
        List<String> Source_type_list = new ArrayList<>();
        for(int i = 0; i < typelist.size(); i++) {
            if(typelist.get(i).equals("none")){
                Entity temp = MergeTool.findByEntityId(allentitys,entitynameid.get(entitynamelist.get(i)));
                Source_type_list.add(temp.getEntityType());
            }else{
                JSONObject obj = (JSONObject) typelist.get(i);
                String entity = (String) obj.get("n.entityType");
                Source_type_list.add(entity);
//                JSONArray obj = (JSONArray) typelist.get(i);
//                JSONObject obj1 = obj.getJSONObject(0);
//                String entity = (String) obj1.get("n.entityType");
//                Source_type_list.add(entity);
            }
        }

        for(int i =0 ; i < entitynamelist.size();i++){
            String name = entitynamelist.get(i);
            Entity tempentity = MergeTool.findByEntityId(allentitys,entitynameid.get(name));
            tempentity.setEntityType(Source_type_list.get(i));
        }
        //实体分类调用结束




        for(Entity node1:sourcekg_entitys){
            entityidname.put(node1.getEntityId(),node1.getName());
            for(Entity node2:deskg_entitys){
                entityidname.put(node2.getEntityId(),node2.getName());
                if (node1.getName().equals(node2.getName())){
                    if(Sim_entityids.get(node2.getEntityId())!=null){
                        List<String> temp = Sim_entityids.get(node2.getEntityId());
                        temp.add(node1.getEntityId());
                        Sim_entityids.put(node2.getEntityId(),temp);
                    }else{
                        List<String> temp = new ArrayList<>();
                        temp.add(node1.getEntityId());
                        Sim_entityids.put(node2.getEntityId(),temp);
                    }

                }else if(!node1.getName().equals(node2.getName()) && node1.getEntityType().equals(node2.getEntityType())){
                    if(!Query_entityids.containsKey(node1.getEntityId())){
                        List<String> temp = new ArrayList<>();
                        temp.add(node2.getEntityId());
                        Query_entityids.put(node1.getEntityId(),temp);

                        List<String> tempname = new ArrayList<>();
                        tempname.add(node2.getName());
                        Query_entityName.put(node1.getName(),tempname);

                    }else if (Query_entityids.containsKey(node1.getEntityId())){

                        List<String> front = Query_entityids.get(node1.getEntityId());
                        front.add(node2.getEntityId());
                        Query_entityids.put(node1.getEntityId(),front);

                        List<String> frontname = Query_entityName.get(node1.getName());
                        frontname.add(node2.getName());
                        Query_entityName.put(node1.getName(),frontname);
                    }
                }
            }
        }



        //实体相似度调用
        String url_similarity = url_sim;

        for(Map.Entry<String,List<String>> entry1 : Query_entityids.entrySet()){

            String source_entityname = entityidname.get(entry1.getKey());
            source_entityname = source_entityname.replaceAll(" ", "");
            List<String> source_name = new ArrayList<>();
            source_name.add(source_entityname);

            List<String> deswordids = entry1.getValue();
            List<String> deswordname = new ArrayList<>();
            for(int j=0;j< deswordids.size();j++){
                String des_entityname =  entityidname.get(deswordids.get(j));
                des_entityname = des_entityname.replaceAll(" ", "");
                deswordname.add(des_entityname);
            }


            Map<Object, Object> jsonmap = new HashMap<>();
            jsonmap.put("word1", source_name);
            jsonmap.put("word2", deswordname);
            //jsonmap.put("threshold",0.0);
            String jsonstr = JSON.toJSONString(jsonmap);
            String result = RequestHelper.sendJsonWithHttp(url_similarity,JSON.toJSONString(jsonmap));

            Map maps2 = (Map)JSON.parse(result);

            List<Float> sim_value_list = new ArrayList<>();
            if(maps2!=null){
                JSONArray sim = (JSONArray) maps2.get("value_list");
                for(int i = 0; i < sim.size(); i++) {

                    if(sim.get(i) instanceof Integer){
                        float pp = (float)0.0;
                        sim_value_list.add(pp);
                    }else{
                        BigDecimal obj = (BigDecimal) sim.get(i);
                        float obj1 = obj.floatValue();
                        sim_value_list.add(obj1);
                    }

                }
            }


            for(int i=0;i< sim_value_list.size();i++){
                float sim_score = sim_value_list.get(i);
                if(sim_score>0.6 ){
                    if(Sim_entityids.get(entitynameid.get(deswordname.get(i)))!=null){
                        List<String> temp_bemerge = Sim_entityids.get(entitynameid.get(deswordname.get(i)));
                        temp_bemerge.add(entry1.getKey());
                        Sim_entityids.put(entitynameid.get(deswordname.get(i)),temp_bemerge);
                    }else{
                        List<String> temp_bemerge = new ArrayList<>();
                        temp_bemerge.add(entry1.getKey());
                        Sim_entityids.put(entitynameid.get(deswordname.get(i)),temp_bemerge);
                    }
                }

            }
        }



        Map<String, List<String>> res_sim = new HashMap<>();

        Map<String ,Object> result = new HashMap<>();
        if(Sim_entityids.size()!=0){
            for(Map.Entry<String, List<String>> entry: Sim_entityids.entrySet()){
                List<String> values = entry.getValue();
                List<String> values_set = new ArrayList<>(new HashSet<>(values));
                res_sim.put(entry.getKey(), values_set);
            }

            List<Entity> Allentitylist = new ArrayList<>();
            Allentitylist.addAll(sourcekg_entitys);
            Allentitylist.addAll(deskg_entitys);

            Map<String,Map<String,Object>> ITEMSYNres = KGsMergeBasedOnContent.MergeTool.ItemSYN(Allentitylist,res_sim);

            ItemQuery.InsertToDataBase(ITEMSYNres);

            //KG newkg1 = runMerge(cleanedKGs,res_sim);

            //result.put("kg",newkg1);
        }


        result.put("simMap",res_sim);
        //result.put("queryNodes",Query_entityids);
        return result;




    }

}
