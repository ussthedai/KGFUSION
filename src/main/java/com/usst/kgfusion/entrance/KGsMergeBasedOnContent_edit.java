package com.usst.kgfusion.entrance;
import com.usst.kgfusion.pojo.Entity;
import com.usst.kgfusion.pojo.KG;
import com.usst.kgfusion.pojo.Triple;

import java.util.*;

public class KGsMergeBasedOnContent_edit{

    public KGsMergeBasedOnContent_edit(List<KG> KGs, String source_kgSymbol, String destination_kgSymbol) {
        this.KGs = KGs;
        this.source_kgSymbol = source_kgSymbol;
        this.destination_kgSymbol = destination_kgSymbol;
    }

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

    public KG runMerge(List<KG> kgs,HashMap<String,List<String>> Sim_entityids) {
        return mergeProcess(kgs,Sim_entityids);
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

        public static Triple findByTripleId(List<Triple> TripleList, String id){
            for(Triple triple :TripleList){
                if(triple.getTripleId().equals(id)){
                    return triple;
                }
            }
            Triple triple1 = new Triple();
            System.out.println("none");
            return triple1;
        }

    }

    public KG mergeProcess(List<KG> kgs,HashMap<String,List<String>> Sim_entityids)  {

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

    public void setCleanedKGs(List<KG> KGs) {
        this.KGs = KGs;
    }

    public static float levenshtein(String str1, String str2) {
        // 计算两个字符串的长度。
        int len1 = str1.length();
        int len2 = str2.length();
        // 建立上面说的数组，比字符长度大一个空间
        int[][] dif = new int[len1 + 1][len2 + 1];
        // 赋初值，步骤B。
        for (int a = 0; a <= len1; a++) {
            dif[a][0] = a;
        }
        for (int a = 0; a <= len2; a++) {
            dif[0][a] = a;
        }
        // 计算两个字符是否一样，计算左上的值
        int temp;
        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {

              //  System.out.println("i = " + i + " j = " + j + " str1 = " + str1.charAt(i - 1) + " str2 = " + str2.charAt(j - 1));
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    temp = 0;
                } else {
                    temp = 1;
                }
                // 取三个值中最小的
                dif[i][j] = Math.min(dif[i - 1][j - 1] + temp, dif[i][j - 1] + 1);
                dif[i][j] = Math.min(dif[i][j],dif[i - 1][j] + 1);

               // System.out.println("i = " + i + ", j = " + j + ", dif[i][j] = " + dif[i][j]);
            }
        }
        //System.out.println("字符串\"" + str1 + "\"与\"" + str2 + "\"的比较");
        // 取数组右下角的值，同样不同位置代表不同字符串的比较
        //System.out.println("差异步骤：" + dif[len1][len2]);
        // 计算相似度
        float similarity = 1 - (float) dif[len1][len2]
                / Math.max(str1.length(), str2.length());
        //System.out.println("相似度：" + similarity);
        return similarity;
    }

    public Map<String,Object> entrance() {

        List<KG> cleanedKGs = getCleanedKGs();
        String source = getsource_kgSymbol();
        String des = getdestination_kgSymbol();

        HashMap<String,String> entityidname= new HashMap<>();
        HashMap<String,List<String>> Query_entityids = new HashMap<>();
        HashMap<String,List<String>> Sim_entityids = new HashMap<>();
        List<Entity> sourcekg_entitys = new ArrayList<Entity>();
        List<Entity> deskg_entitys = new ArrayList<Entity>();


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
                    }else if (Query_entityids.containsKey(node1.getEntityId())){
                        List<String> front = Query_entityids.get(node1.getEntityId());
                        front.add(node2.getEntityId());
                        Query_entityids.put(node1.getEntityId(),front);
                    }
                }
            }
        }

        for(Map.Entry<String,List<String>> entry1 : Query_entityids.entrySet()){
            String query_entityname = entityidname.get(entry1.getKey());
            List<String> sourceword = entry1.getValue();
            for(int i=0;i< sourceword.size();i++){
                String source_entityname =  entityidname.get(sourceword.get(i));
                float sim_score = levenshtein(query_entityname,source_entityname);
                if(sim_score>0.5){
                    if(Sim_entityids.get(sourceword.get(i))!=null){
                        List<String> temp_bemerge = Sim_entityids.get(sourceword.get(i));
                        temp_bemerge.add(entry1.getKey());
                        Sim_entityids.put(sourceword.get(i),temp_bemerge);
                    }else{
                        List<String> temp_bemerge = new ArrayList<>();
                        temp_bemerge.add(entry1.getKey());
                        Sim_entityids.put(sourceword.get(i),temp_bemerge);
                    }
                }

            }
        }


        KG newkg1 = runMerge(cleanedKGs,Sim_entityids);

        Map<String ,Object> result = new HashMap<>();
        result.put("kg",newkg1);
        result.put("simMap",Sim_entityids);
        result.put("queryNodes",Query_entityids);
        return result;




    }


} 
