package com.usst.kgfusion.entrance;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.usst.kgfusion.databaseQuery.ItemQuery;
import com.usst.kgfusion.pojo.EntityRaw;
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

        public static EntityRaw findByEntityId(List<EntityRaw> entityList, String id){
            for(EntityRaw entity :entityList){
                if(entity.getEntityId().equals(id)){
                    return entity;
                }
            }
            EntityRaw entity1 = new EntityRaw();
            System.out.println("none");
            return entity1;
        }

    }

    public KG mergeProcess(List<KG> kgs,Map<String,List<String>> Sim_entityids) throws IOException {

        //0.???????????????????????????????????????entitylist???triplelist;
        List<EntityRaw> tempentitys = new ArrayList<EntityRaw>();
        List<Triple> temptriples = new ArrayList<Triple>();

        List<EntityRaw> entitys = new ArrayList<EntityRaw>();//entitylist??????????????????
        List<EntityRaw> entitys1 = new ArrayList<EntityRaw>();//entitylist???????????????????????????????????????

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



        //0.??????

        //????????????
        //1.?????????????????????????????????????????????
        List<String> removeid = new ArrayList<String>();//??????????????????????????????ID???
        //1.1???????????????
        for(Map.Entry<String,List<String>> entry:Sim_entityids.entrySet()){
            List<String> ids = entry.getValue();
            for(int i=0;i< ids.size();i++) {
                removeid.add(ids.get(i));
            }

            for (int k = 0; k < ids.size(); k++) {
                for (Triple triple : triples) {
                    EntityRaw entity1 = triple.getHead();
                    EntityRaw entity2 = triple.getTail();
                    if (entity2.getEntityId().equals(ids.get(k))) {//???????????????
                        triple.setTail(MergeTool.findByEntityId(entitys1, entry.getKey()));
                    }
                    if (entity1.getEntityId().equals(ids.get(k))) {
                        triple.setHead(MergeTool.findByEntityId(entitys1, entry.getKey()));
                    }
                }
            }
        }



        //1.2 ?????????????????????ID????????????
        for (int iu = 0; iu < removeid.size(); iu++) {
            for (EntityRaw entity : entitys) {
                if (entity.getEntityId().equals(removeid.get(iu))) {
                    entitys.remove(entity);
                    break;
                }
            }
        }
        removeid.clear();//??????List????????????????????????????????????????????????????????????????????????ID????????????

        HashMap<String,Integer> triplemap = new HashMap<>();//???????????????triple??????newtriples
        for (EntityRaw entity : entitys) {
            triplemap.put(entity.getEntityId(), 1);
        }
        List<Integer> removelist=new ArrayList<>();//?????????????????????ID list
        for(Triple triple:triples){
            String headid=triple.getHead().getEntityId();
            String tailid=triple.getTail().getEntityId();
            if(headid.equals(tailid)){//????????????????????????????????????
                if(!removeid.contains(headid)){
                    removeid.add(headid);//?????????????????????
                }
                if(!removelist.contains(Integer.parseInt(triple.getTripleId()))){
                    removelist.add(Integer.parseInt(triple.getTripleId()));//???????????????ID??????
                }
            }
        }

        for (int iu = 0; iu < removelist.size(); iu++) {//????????????????????????ID???????????????
            for (Triple triple : triples) {
                if (Integer.parseInt(triple.getTripleId())==(removelist.get(iu))) {
                    triples.remove(triple);
                    break;
                }
            }
        }
        List<String> removeids=new ArrayList<>();//?????????????????????????????????????????????????????????????????????????????????????????????
        for(int iu = 0; iu < removeid.size(); iu++){
            int count=0;
            for(Triple triple:triples){
                if(triple.getHead().getEntityId().equals(removeid.get(iu))||triple.getTail().getEntityId().equals(removeid.get(iu))) {
                    if(count <= Integer.MAX_VALUE -1){
                        count++;
                    }else{
                        break;
                    }
                    
                }
            }
            if(count>=1){
                String id = removeid.get(iu);
                removeids.add(id);
            }
        }
        for(int iu = 0; iu < removeids.size(); iu++) {//???????????????idList?????????????????????????????????????????????ID
            for(String id :removeid){
                if(removeids.get(iu).equals(id)){
                    removeid.remove(id);
                    break;
                }
            }
        }
        for (int iu = 0; iu < removeid.size(); iu++) {//?????????????????????ID????????????
            for (EntityRaw entity : entitys) {
                if (entity.getEntityId().equals(removeid.get(iu))) {
                    entitys.remove(entity);
                    break;
                }
            }
        }

        List<EntityRaw> newNodes = new ArrayList<EntityRaw>();//????????????????????????ENtity????????????newnodelist
        for (EntityRaw entity : entitys) {
            String nodeid = entity.getEntityId();
            String nodename = entity.getName();
            String typeid=entity.getEntityType();
            String subclass = entity.getEntitySubClass();
            String graphSym = entity.getGraphSymbol();
            String itemid = entity.getItemId();
            String srcid = entity.getSrcId();
            EntityRaw node = new EntityRaw(nodeid, nodename,typeid,subclass,graphSym, itemid,srcid);
            newNodes.add(node);
        }


        //3.??????


        //4.???????????????????????????????????????
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
        LinkedHashMap<EntityRaw,List<EntityRaw>> edges = new LinkedHashMap<>();
        LinkedHashMap<EntityRaw,List<Integer>> directions = new LinkedHashMap<>();
        for(Triple triple:ts){
            EntityRaw head = triple.getHead();
            EntityRaw tail = triple.getTail();
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

    } //????????????????????????????????????????????????


    public Map<String,Object> entrance(String url_sim, String url_type) throws IOException {

        List<KG> cleanedKGs = getCleanedKGs();
        String source = getsource_kgSymbol();
        String des = getdestination_kgSymbol();

        HashMap<String,String> entityidname = new HashMap<>();
        HashMap<String,String> entitynameid = new HashMap<>();
        HashMap<String,List<String>> Query_entityids = new HashMap<>();
        HashMap<String,List<String>> Query_entityName = new HashMap<>();
        HashMap<String,List<String>> Sim_entityids = new HashMap<>();
        List<EntityRaw> sourcekg_entitys = new ArrayList<EntityRaw>();
        List<EntityRaw> deskg_entitys = new ArrayList<EntityRaw>();
        List<EntityRaw> allentitys = new ArrayList<>();


        List<EntityRaw> tempentitys = new ArrayList<EntityRaw>();//??????Entity??????
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

        //??????????????????
        String url_Type = url_type;
        Map<Object,Object> SendEntityName = new HashMap<>();
        List<String> entitynamelist = new ArrayList<>();
        for(EntityRaw node1:sourcekg_entitys){
            entitynameid.put(node1.getName(),node1.getEntityId());
            entitynamelist.add(node1.getName());
        }
        for(EntityRaw node2:deskg_entitys){
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
                EntityRaw temp = MergeTool.findByEntityId(allentitys,entitynameid.get(entitynamelist.get(i)));
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
            EntityRaw tempentity = MergeTool.findByEntityId(allentitys,entitynameid.get(name));
            tempentity.setEntityType(Source_type_list.get(i));
        }
        //????????????????????????




        for(EntityRaw node1:sourcekg_entitys){
            entityidname.put(node1.getEntityId(),node1.getName());
            for(EntityRaw node2:deskg_entitys){
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



        //?????????????????????
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

            List<EntityRaw> Allentitylist = new ArrayList<>();
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
