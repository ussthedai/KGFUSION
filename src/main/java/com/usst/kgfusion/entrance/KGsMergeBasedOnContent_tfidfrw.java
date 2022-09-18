package com.usst.kgfusion.entrance;


import com.usst.kgfusion.pojo.KG;

import com.usst.kgfusion.pojo.*;
import com.usst.kgfusion.util.MatrixUtil;
import com.huaban.analysis.jieba.JiebaSegmenter;

import java.io.*;
import java.util.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class KGsMergeBasedOnContent_tfidfrw {

    private List<KG> cleanedKGs;
    private String source_kgSymbol;
    private String destination_kgSymbol;
    public List<KG> getCleanedKGs() {
        return cleanedKGs;
    }
    public String getsource_kgSymbol() {
        return source_kgSymbol;
    }
    public String getdestination_kgSymbol() {
        return destination_kgSymbol;
    }

    public KGsMergeBasedOnContent_tfidfrw(List<KG> cleanedKGs, String source_kgSymbol, String destination_kgSymbol) {
        this.cleanedKGs = cleanedKGs;
        this.source_kgSymbol = source_kgSymbol;
        this.destination_kgSymbol = destination_kgSymbol;
    }

    public KG runMerge(List<KG> kgs,HashMap<String,List<String>> Sim_entityids) throws IOException {
        return mergeProcess(kgs,Sim_entityids);
    }

    public KG mergeProcess(List<KG> kgs,HashMap<String,List<String>> Sim_entityids) throws IOException {

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
        for(Entry<String,List<String>> entry:Sim_entityids.entrySet()){
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

    static class MergeTool {

        static Map<String,String> entityidname= new HashMap<>();

        public static Map<String,Map<String,Double>> ComputeTF(List<KG> kgs, List<Item> items) {

            List<Entity> tempentitys = new ArrayList<Entity>();

            List<Entity> entitys = new ArrayList<Entity>();

            for (KG kg : kgs) {
                tempentitys = kg.getEntities();
                for (int i = 0; i < tempentitys.size(); i++) {
                    entitys.add(tempentitys.get(i));
                }
            }

            for(Entity node:entitys){
                entityidname.put(node.getEntityId(),node.getName());
            }

            double[][] tfidfMatrix = new double[items.size()+1][entitys.size()+1];//矩阵存放TFIDF

            Map<String,Map<String,Double>> tfidf1 = new HashMap<String,Map<String,Double>>();
            //条目ID，词ID，值
            Map<String, Double> idf = new HashMap<String, Double>();

            for (Entity node : entitys) {//IDF
                int D = items.size(); //总条目数目
                int Dt = 0;// Dt为出现该实体的条目数目
                for(Item item : items){
                    if(item.getItemText().contains(node.getName())){
                        Dt++;
                    }
                }
                double idfvalue = (double) Math.log(Float.valueOf(D) / (1 + Dt));
                idf.put(node.getEntityId(), idfvalue);
            }

            for (Entity node :entitys){//TF * IDF
                String nodename=node.getName();
                for(Item item :items){
                    int count=0;
                    String itemtext=item.getItemText();
                    JiebaSegmenter segmenter = new JiebaSegmenter();
                    List<String> cutwords=segmenter.sentenceProcess(itemtext);
//                    if(!cutwords.contains(nodename)){
//                        cutwords.add(nodename);
//                    }
                    for(int i=0;i<cutwords.size();i++){
                        //cutwords.removeAll(stopwords);
                        if(cutwords.get(i).contains(nodename)){
                            count++;
                        }
                    }

                    double TF=(double) count/cutwords.size();
                    double IDF=idf.get(node.getEntityId());
                    if(TF*IDF >0.0){
                        Map<String, Double> value = tfidf1.getOrDefault(item.getItemId(),new HashMap<String, Double>());
                        value.put(node.getEntityId(), TF * IDF );
                        tfidf1.put(item.getItemId(),value);
                    }
                }
            }

            return tfidf1;

        }// 方法：计算TF-IDF矩阵

        public static double ComputeSim(ArrayList va,ArrayList vb)  {
            int size = va.size();
            double simVal = 0;
            double num = 0;
            double den = 1;
            double powa_sum = 0;
            double powb_sum = 0;
            for (int k = 0; k < size; k++) {
                String ssa = String.valueOf(va.get(k));
                String ssb = String.valueOf(vb.get(k));
                double sa = Double.parseDouble(ssa);
                double sb = Double.parseDouble(ssb);
                num = num + sa * sb;
                powa_sum = powa_sum + (double) Math.pow(sa, 2);
                powb_sum = powb_sum + (double) Math.pow(sb, 2);
            }
            double sqrta = (double) Math.sqrt(powa_sum);
            double sqrtb = (double) Math.sqrt(powb_sum);
            den = sqrta * sqrtb;
            simVal = num / den;

            return simVal;
        }

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


        public static HashMap<Integer, HashMap<Integer, Double>> trans_matrix(
                HashMap<Integer, HashMap<Integer, Double>> hm) { // 转置TFIDF矩阵得到Trans_TFIDF矩阵； // 转置TFIDF矩阵得到Trans_TFIDF矩阵；
            HashMap<Integer, HashMap<Integer, Double>> result = new HashMap<Integer, HashMap<Integer, Double>>();
            ArrayList<String> al = new ArrayList<String>();
            Iterator<Integer> it = hm.keySet().iterator();
            HashMap<Integer, Double> m3 = new HashMap<Integer, Double>();
            HashMap<Integer, Double> hm1 = new HashMap<Integer, Double>();

            while (it.hasNext()) {
                int k1 = it.next();
                m3 = hm.get(k1);
                Iterator<Integer> it_1 = m3.keySet().iterator();
                while (it_1.hasNext()) {

                    int k2 = it_1.next();
                    double v = m3.get(k2);
                    al.add(k2 + " " + k1 + " " + v);
//				it_2.remove();//2020.1.1
                }
            }
            Collections.sort(al);
            Iterator<String> it_2 = al.iterator();
            String s1 = it_2.next().toString();//
            String s2[] = s1.split("\\s+");
            String s3 = s2[0];

            hm1.put(Integer.parseInt(s2[1]), Double.parseDouble(s2[2]));
            while (it_2.hasNext()) {
                s1 = it_2.next().toString();
                s2 = s1.split("\\s+");
                String s4 = s2[0];
                if (s4.equals(s3)) {
                    hm1.put(Integer.parseInt(s2[1]), Double.parseDouble(s2[2]));
                    it_2.remove();
                } else {
                    result.put(Integer.parseInt(s3), hm1);
                    HashMap<Integer, Double> hm2 = new HashMap<Integer, Double>();
                    hm1 = hm2;
                    s3 = s4;
                    hm1.put(Integer.parseInt(s2[1]), Double.parseDouble(s2[2]));
                }
                // System.out.println(Integer.parseInt(s3)+" "+Integer.parseInt(s2[1])+" "+
                // Double.parseDouble(s2[2]));
            }
            result.put(Integer.parseInt(s3), hm1);
//		System.out.println("==方法里面类中的转置==="+Trans_TFIDF);
            return result;

        }

        public static ArrayList getArray(HashMap<Integer, HashMap<Integer, Double>> tfidf_trans,int Entityid,int itemsize){
            ArrayList vector  = new ArrayList();
            int item = 12344000;
            if(tfidf_trans.get(Entityid)!=null){
                HashMap<Integer,Double> temp = tfidf_trans.get(Entityid);
                for(int i = 0; i<itemsize;i++){
                    if(temp.get(item)==null){
                        int a = 0;
                        vector.add(a);
                    }else{
                        vector.add(temp.get(item));
                    }
                    item++;
                }
            }
            return vector;
        }
    } //计算TF-IDF,SVD,SIM

    public Map<String,Object> entrance() throws Exception{

        List<KG> cleanedKGs = getCleanedKGs();
        String source = getsource_kgSymbol();
        String des = getdestination_kgSymbol();

        HashMap<String,String> entityidname= new HashMap<>();
        HashMap<String,List<String>> Query_entityids = new HashMap<>();
        HashMap<String,List<String>> Sim_entityids = new HashMap<>();
        List<Entity> sourcekg_entitys = new ArrayList<Entity>();
        List<Entity> deskg_entitys = new ArrayList<Entity>();
        List<Item> rawDocuments = new ArrayList<>();
        List<Integer> queryids = new ArrayList<>();

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


        int itemidauto = 12344000;
        Set<String> itemids = new HashSet<>();
        Set<String> itemforname = new HashSet<>();
        for(Entity node1:sourcekg_entitys){
            entityidname.put(node1.getEntityId(),node1.getName());
            //if(node1.getItemId().equals("null")){
                itemforname.add(node1.getName());
            //}else{
            //    itemids.add(node1.getItemId());
            //}

            for(Entity node2:deskg_entitys){
                entityidname.put(node2.getEntityId(),node2.getName());
                //if(node2.getItemId().equals("null")){
                    itemforname.add(node2.getName());
                //}else{
                //    itemids.add(node2.getItemId());
                //}
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


//        Map<String,String> rawItems = ItemQuery.queryItem(itemids);
//        List<String> test = new ArrayList<>();
//        for(String a : itemids){
//            if(!rawItems.containsKey(a)){
//                test.add(a);
//            }
//        }
//
//
//        for(Entry<String,String> entry : rawItems.entrySet()){
//            Item newitem = new Item(entry.getKey(), entry.getValue());
//            rawDocuments.add(newitem);
//        }

        Iterator<String> it1 = itemforname.iterator();
        while(it1.hasNext()){
            Item newitem = new Item(String.valueOf(itemidauto),it1.next());
            rawDocuments.add(newitem);
            itemidauto++;
        }



        Map<String,Map<String,Double>> tfidfMap = MergeTool.ComputeTF(cleanedKGs,rawDocuments);
        HashMap<Integer, HashMap<Integer, Double>> tfidf = new HashMap<>();

        for(Entry<String,Map<String,Double>> entry: tfidfMap.entrySet()){
            HashMap<Integer,Double> temp = new HashMap<>();
            for(Entry<String,Double> entry1 : entry.getValue().entrySet()){
                temp.put(Integer.parseInt(entry1.getKey()),entry1.getValue());
            }
            tfidf.put(Integer.parseInt(entry.getKey()),temp);
        }

        HashMap<Integer, HashMap<Integer, Double>> tfidf_trans = MergeTool.trans_matrix(tfidf);




        for(Entry<String,List<String>> entry1 : Query_entityids.entrySet()){
            int S_id = Integer.parseInt(entry1.getKey());
            ArrayList va = new ArrayList();
            if(tfidf_trans.get(S_id)!=null){
                va = MergeTool.getArray(tfidf_trans,S_id, rawDocuments.size());
            }else{
                continue;
            }
            List<String> temp = entry1.getValue();
            for(int i=0;i< temp.size();i++){
                String entityid = temp.get(i);
                int D_id = Integer.parseInt(entityid);
                if(tfidf_trans.get(D_id)!=null) {
                    ArrayList vb = MergeTool.getArray(tfidf_trans, D_id, rawDocuments.size());
                    if(MergeTool.ComputeSim(va,vb)>0.9){
                        if(Sim_entityids.get(temp.get(i))!=null){
                            List<String> temp_bemerge = Sim_entityids.get(temp.get(i));
                            temp_bemerge.add(entry1.getKey());
                            Sim_entityids.put(temp.get(i),temp_bemerge);
                        }else{
                            List<String> temp_bemerge = new ArrayList<>();
                            temp_bemerge.add(entry1.getKey());
                            Sim_entityids.put(temp.get(i),temp_bemerge);
                        }
                    }
                }else {
                    continue;
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
