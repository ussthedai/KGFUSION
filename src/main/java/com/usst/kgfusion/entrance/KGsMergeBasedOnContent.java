package com.usst.kgfusion.entrance;


import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huaban.analysis.jieba.JiebaSegmenter;
import com.usst.kgfusion.databaseQuery.ItemQuery;
import com.usst.kgfusion.pojo.EntityRaw;
import com.usst.kgfusion.pojo.Item;
import com.usst.kgfusion.pojo.KG;
import com.usst.kgfusion.pojo.Triple;
import com.usst.kgfusion.pojo.itemIds;
import com.usst.kgfusion.util.GenerateUtil;
import com.usst.kgfusion.util.MatrixUtil;

public class KGsMergeBasedOnContent {

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

    private static final Logger logger = LoggerFactory.getLogger("com.usst.test");

    public KGsMergeBasedOnContent(List<KG> cleanedKGs, String source_kgSymbol, String destination_kgSymbol) {
        this.cleanedKGs = cleanedKGs;
        this.source_kgSymbol = source_kgSymbol;
        this.destination_kgSymbol = destination_kgSymbol;
    }

    public KG runMerge(List<KG> kgs,HashMap<String,List<String>> Sim_entityids) throws IOException {
        return mergeProcess(kgs,Sim_entityids);
    }

    public KG mergeProcess(List<KG> kgs,HashMap<String,List<String>> Sim_entityids) throws IOException {

        //0.前期数据处理，此阶段为获取entitylist和triplelist;
        List<EntityRaw> tempentitys = new ArrayList<EntityRaw>();
        List<Triple> temptriples = new ArrayList<Triple>();

        List<EntityRaw> entitys = new ArrayList<EntityRaw>();//entitylist集合用于修改
        List<EntityRaw> entitys1 = new ArrayList<EntityRaw>();//entitylist集合用于查询元素（不修改）

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
                    EntityRaw entity1 = triple.getHead();
                    EntityRaw entity2 = triple.getTail();
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
            for (EntityRaw entity : entitys) {
                if (entity.getEntityId().equals(removeid.get(iu))) {
                    entitys.remove(entity);
                    break;
                }
            }
        }
        removeid.clear();//清空List，方便后续操作，以记录头尾实体相同的三元组的实体ID并删除。

        HashMap<String,Integer> triplemap = new HashMap<>();//将新生成的triple放入newtriples
        for (EntityRaw entity : entitys) {
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
                    if(count <= Integer.MAX_VALUE-1){
                        count++;
                    }else{
                        break;
                    }
                    // count++;
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
            for (EntityRaw entity : entitys) {
                if (entity.getEntityId().equals(removeid.get(iu))) {
                    entitys.remove(entity);
                    break;
                }
            }
        }

        List<EntityRaw> newNodes = new ArrayList<EntityRaw>();//修改完成，将新的ENtity集合转成newnodelist
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

    } //得到相似结点对，进行多个图谱合并

    static class MergeTool {

        static Map<String,String> entityidname= new HashMap<>();

        public static Map<String,Map<String,Double>> ComputeTF(List<KG> kgs, List<Item> items) {

            List<EntityRaw> tempentitys = new ArrayList<EntityRaw>();
            List<EntityRaw> entitys = new ArrayList<EntityRaw>();

            for (KG kg : kgs) {
                tempentitys = kg.getEntities();
                for (int i = 0; i < tempentitys.size(); i++) {
                    entitys.add(tempentitys.get(i));
                }
            }

            for(EntityRaw node:entitys){
                entityidname.put(node.getEntityId(),node.getName());
            }
            Map<String,Map<String,Double>> tfidf1 = new HashMap<String,Map<String,Double>>();
            //条目ID，词ID，值
            Map<String, Double> idf = new HashMap<String, Double>();

            for (EntityRaw node : entitys) {//IDF
                int D = items.size(); //总条目数目
                int Dt = 0;// Dt为出现该实体的条目数目
                for(Item item : items){
                    if(item.getItemText().contains(node.getName())){
                        if(Dt <= Integer.MAX_VALUE-1){
                            Dt++;
                        }else{
                            break;
                        }
                        // Dt++;
                    }
                }
                double idfvalue = (double) Math.log(Float.valueOf(D) / (1 + Dt));
                idf.put(node.getEntityId(), idfvalue);
            }

            for (EntityRaw node :entitys){//TF * IDF
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
                        if(cutwords.get(i).contains(nodename) && count <= (Integer.MAX_VALUE - 1)){
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

        public static ArrayList getArray(Map<String,Map<String,Double>> tfidf_trans,List<String> itemid,String Entityid){
            ArrayList vector  = new ArrayList();
            if(tfidf_trans.get(Entityid)!=null){
                Map<String,Double> temp = tfidf_trans.get(Entityid);
                for(String id : itemid){
                    if(temp.get(id)==null){
                        int a = 0;
                        vector.add(a);
                    }else{
                        vector.add(temp.get(id));
                    }
                }
            }
            return vector;
        }
        public static ArrayList getArray_rule(Map<String,Map<String,Double>> tfidf_trans,Set<String> itemid,String Entityid){
            ArrayList vector  = new ArrayList();
            if(tfidf_trans.get(Entityid)!=null){
                Map<String,Double> temp = tfidf_trans.get(Entityid);
                for(String id : itemid){
                    if(temp.get(id)==null){
                        int a = 0;
                        vector.add(a);
                    }else{
                        vector.add(temp.get(id));
                    }
                }
            }
            return vector;
        }

        //更新Item
        public static Map<String,Map<String,Object>> ItemSYN(List<EntityRaw> entityList, Map<String,List<String>> Sim_entityids){
            Map<String,Map<String,Object>> res = new HashMap<>();
            for(Entry<String,List<String>> entry : Sim_entityids.entrySet()){
                String Aimid = entry.getKey();
                EntityRaw AimEntity = KGsMergeBasedOnContent.MergeTool.findByEntityId(entityList,Aimid);
                String newD = "";
                String symbol = AimEntity.getGraphSymbol();
                String itemid = "";
                boolean type ; // true为创建 //false为更新
                if(AimEntity.getItemId()==null || AimEntity.getItemId().equals("null")){
                    type = true;

                    //itemid = AimEntity.getGraphSymbol()+"_id="+Aimid;

                    itemid = GenerateUtil.generateUniqueId().toString();
                    List<String> sourceids = entry.getValue();
                    Set<String> itemids = new HashSet<>();
                    for(int i = 0; i < sourceids.size();i++){
                        EntityRaw SourceEntity = KGsMergeBasedOnContent.MergeTool.findByEntityId(entityList,sourceids.get(i));
                        if(SourceEntity.getItemId()==null || SourceEntity.getItemId().equals("null")){
                            newD +=SourceEntity.getName()+",";
                            continue;
                        }else {
                            itemids.add(SourceEntity.getItemId());
                        }
                    }
                    Map<String,String> query = ItemQuery.queryItem(itemids);

                    for(String i : query.values()){
                        newD += i;
                    }


                }else{
                    type = false;
                    itemid = AimEntity.getItemId();
                    List<String> sourceids = entry.getValue();
                    Set<String> itemids = new HashSet<>();
                    for(int i = 0; i < sourceids.size();i++){
                        EntityRaw SourceEntity = KGsMergeBasedOnContent.MergeTool.findByEntityId(entityList,sourceids.get(i));
                        if(SourceEntity.getItemId()==null || SourceEntity.getItemId().equals("null")){
                            newD += SourceEntity.getName()+",";
                            continue;
                        }else {
                            itemids.add(SourceEntity.getItemId());
                        }
                    }
                    Map<String,String> query = ItemQuery.queryItem(itemids);

                    for(String i : query.values()){
                        newD += i;
                    }
                }
                Map<String,Object> ress = new HashMap<>();
                ress.put("itemid",itemid);
                ress.put("symbol",symbol);
                ress.put("itemcontent",newD);
                ress.put("type",type);
                res.put(entry.getKey(),ress);

            }
            return res;
        }


    } //计算TF-IDF,SVD,SIM

    public Map<String,Object> entrance() throws SQLException{

        List<KG> cleanedKGs = getCleanedKGs();
        String source = getsource_kgSymbol();
        String des = getdestination_kgSymbol();

        HashMap<String,String> entityidname= new HashMap<>();
        HashMap<String,List<String>> Query_entityids = new HashMap<>();
        HashMap<String,List<String>> Sim_entityids = new HashMap<>();
        List<EntityRaw> sourcekg_entitys = new ArrayList<EntityRaw>();
        List<EntityRaw> deskg_entitys = new ArrayList<EntityRaw>();
        List<Item> rawDocuments = new ArrayList<>();
        List<Integer> queryids = new ArrayList<>();
        List<String> Item_id = new ArrayList<>();

        List<EntityRaw> tempentitys = new ArrayList<EntityRaw>();//读取Entity列表
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
        for(EntityRaw node1:sourcekg_entitys){
            entityidname.put(node1.getEntityId(),node1.getName());
            if(node1.getItemId().equals("null")){
                itemforname.add(node1.getName());
            }else{
                itemids.add(node1.getItemId());
                Item_id.add(node1.getItemId());
            }

            for(EntityRaw node2:deskg_entitys){
                entityidname.put(node2.getEntityId(),node2.getName());
                if(node2.getItemId().equals("null")){
                    itemforname.add(node2.getName());
                }else{
                    itemids.add(node2.getItemId());
                    Item_id.add(node2.getItemId());
                }
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


        Map<String,String> rawItems = ItemQuery.queryItem(itemids);

        for(Entry<String,String> entry : rawItems.entrySet()){
            Item newitem = new Item(entry.getKey(), entry.getValue());
            rawDocuments.add(newitem);
        }

        Iterator<String> it1 = itemforname.iterator();
        while(it1.hasNext()){
            Item newitem = new Item(String.valueOf(itemidauto),it1.next());
            rawDocuments.add(newitem);
            Item_id.add(String.valueOf(itemidauto));
            if(itemidauto <= Integer.MAX_VALUE -1){
                itemidauto++;
            }else{
                break;
            }
            // itemidauto++;
        }





        Map<String,Map<String,Double>> tfidfMap = MergeTool.ComputeTF(cleanedKGs,rawDocuments);


//        HashMap<Integer, HashMap<Integer, Double>> tfidf = new HashMap<>();
//
//        for(Entry<String,Map<String,Double>> entry: tfidfMap.entrySet()){
//            HashMap<Integer,Double> temp = new HashMap<>();
//            for(Entry<String,Double> entry1 : entry.getValue().entrySet()){
//                temp.put(Integer.parseInt(entry1.getKey()),entry1.getValue());
//            }
//            tfidf.put(Integer.parseInt(entry.getKey()),temp);
//        }

        Map<String,Map<String,Double>> tfidf_trans = MatrixUtil.transpose(tfidfMap);




        for(Entry<String,List<String>> entry1 : Query_entityids.entrySet()){
            String S_id = entry1.getKey();
            ArrayList va = new ArrayList();
            if(tfidf_trans.get(S_id)!=null){
                va = MergeTool.getArray(tfidf_trans,Item_id, S_id);
            }else{
                continue;
            }
            List<String> temp = entry1.getValue();
            for(int i=0;i< temp.size();i++){
                String entityid = temp.get(i);
                if(tfidf_trans.get(entityid)!=null) {
                    ArrayList vb = MergeTool.getArray(tfidf_trans,Item_id, entityid);
                    if(MergeTool.ComputeSim(va,vb)>0.7){
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


        //新增条目，更新入库
        if(Sim_entityids.size()!=0){
            List<EntityRaw> Allentitylist = new ArrayList<>();
            Allentitylist.addAll(sourcekg_entitys);
            Allentitylist.addAll(deskg_entitys);
            Map<String,Map<String,Object>> ITEMSYNres = MergeTool.ItemSYN(Allentitylist,Sim_entityids);
            ItemQuery.InsertToDataBase(ITEMSYNres);
        }


        //KG newkg1 = runMerge(cleanedKGs,Sim_entityids);


        //result.put("kg",newkg1);
        //result.put("queryNodes",Query_entityids);

        Map<String ,Object> result = new HashMap<>();

        result.put("simMap",Sim_entityids);
        return result;
    }

    public Map<String,Map<String,Double>> New_rule_entrance(KG read_kg,String des, String source) {


        HashMap<String,String> entityidname= new HashMap<>();


        HashMap<String,List<String>> Sim_entityids = new HashMap<>();

        Map<String,Map<String,Double>> Sim_map = new HashMap<>();


        List<EntityRaw> sourcekg_entitys = new ArrayList<EntityRaw>();
        List<EntityRaw> deskg_entitys = new ArrayList<EntityRaw>();
        List<Item> rawDocuments = new ArrayList<>();


        List<EntityRaw> ALLentitys = new ArrayList<EntityRaw>();//读取Entity列表
        ALLentitys = read_kg.getEntities();
        for (int i = 0; i < ALLentitys.size(); i++) {
            if(!des.equals(source)){
                if(ALLentitys.get(i).getGraphSymbol().equals(source)){
                    sourcekg_entitys.add(ALLentitys.get(i));
                }else if(ALLentitys.get(i).getGraphSymbol().equals(des)){
                    deskg_entitys.add(ALLentitys.get(i));
                }
            }else{
                sourcekg_entitys.add(ALLentitys.get(i));
                deskg_entitys.add(ALLentitys.get(i));
            }

        }


        //文档构建
        int itemidauto = 12344000;
        Set<String> itemids_forquery = new HashSet<>();
        Set<String> itemforname = new HashSet<>();
        Set<String> Item_id_forArray = new HashSet<>();

        Set<String> itemids_for60 = new HashSet<>();
        Set<String> itemids_for61 = new HashSet<>();
        Set<String> itemids_for10 = new HashSet<>();

        for(EntityRaw node1:sourcekg_entitys) {
            entityidname.put(node1.getEntityId(), node1.getName());
            if (node1.getItemId().equals("null") || node1.getItemId() == null || node1.getItemId().length() == 0 ||!node1.getItemId().contains(",")) {
                itemforname.add(node1.getName());
            } else {
                String tex = node1.getItemId();
                List<Object>  itemids_list = JSONArray.parseArray(tex);
                for(int i = 0;i<itemids_list.size();i++){
                    JSONObject temp = (JSONObject)itemids_list.get(i);
                    int table = (int)temp.get("dataSourceId");
                    JSONArray items = (JSONArray)temp.get("items");
                    for(int j =0 ;j<items.size();j++){
                        if(table==60){
                            itemids_for60.add((String) items.get(j));
                            Item_id_forArray.add((String) items.get(j));
                        }
                        if(table==61){
                            itemids_for61.add((String) items.get(j));
                            Item_id_forArray.add((String) items.get(j));
                        }
                        if(table==10){
                            itemids_for10.add((String) items.get(j));
                            Item_id_forArray.add((String) items.get(j));
                        }
                    }

                }

            }
        }
        if(!des.equals(source)){
            for(EntityRaw node2:deskg_entitys){
                entityidname.put(node2.getEntityId(),node2.getName());
                if(node2.getItemId().equals("null") || node2.getItemId() == null || node2.getItemId().length() == 0 || !node2.getItemId().contains(",")){
                    itemforname.add(node2.getName());
                }else{

                    String tex = node2.getItemId();
                    List<Object>  itemids_list = JSONArray.parseArray(tex);
                    for(int i = 0;i<itemids_list.size();i++){
                        JSONObject temp = (JSONObject)itemids_list.get(i);
                        int table = (int)temp.get("dataSourceId");
                        JSONArray items = (JSONArray)temp.get("items");
                        for(int j =0 ;j<items.size();j++){
                            if(table==60){
                                itemids_for60.add((String) items.get(i));
                                Item_id_forArray.add((String) items.get(i));
                            }
                            if(table==61){
                                itemids_for61.add((String) items.get(i));
                                Item_id_forArray.add((String) items.get(i));
                            }
                            if(table==10){
                                itemids_for10.add((String) items.get(i));
                                Item_id_forArray.add((String) items.get(i));
                            }
                        }

                    }

                }
            }
        }

        Map<String,String> rawItems = new HashMap<>();

        //Map<String,String> rawItems = ItemQuery.queryItem(itemids_forquery);
        if(itemids_for10!=null){

            Map<String,String> rawItems1 = ItemQuery.queryItemIds_new(itemids_for10,10);
            rawItems.putAll(rawItems1);
        }
        if(itemids_for60!=null){

            Map<String,String> rawItems1 = ItemQuery.queryItemIds_new(itemids_for60,60);
            rawItems.putAll(rawItems1);
        }
        if(itemids_for61!=null){

            Map<String,String> rawItems1 = ItemQuery.queryItemIds_new(itemids_for61,61);
            rawItems.putAll(rawItems1);
        }

        for(Entry<String,String> entry : rawItems.entrySet()){
            Item newitem = new Item(entry.getKey(), entry.getValue());
            rawDocuments.add(newitem);
        }//数据库查询文档

        Iterator<String> it1 = itemforname.iterator();
        while(it1.hasNext()){
            Item newitem = new Item(String.valueOf(itemidauto),it1.next());
            rawDocuments.add(newitem);

            Item_id_forArray.add(String.valueOf(itemidauto));

            if(itemidauto <= Integer.MAX_VALUE - 1){
                itemidauto++;
            }else{
                break;
            }
            // itemidauto++;
        }//名称构建文档


        //文档构建结束





        Map<String,Map<String,Double>> tfidfMap = MergeTool.ComputeTF(cleanedKGs,rawDocuments);

        Map<String,Map<String,Double>> tfidf_trans = MatrixUtil.transpose(tfidfMap);




        for(EntityRaw des_entity : deskg_entitys){
            String des_id = des_entity.getEntityId();
            ArrayList va = new ArrayList();
            if(tfidf_trans.get(des_id)!=null){
                va = MergeTool.getArray_rule(tfidf_trans,Item_id_forArray, des_id);
            }else{
                continue;
            }
            for(EntityRaw source_entity : sourcekg_entitys){
                String source_id = source_entity.getEntityId();
                if(!des_id.equals(source_id)){
                    if(tfidf_trans.get(source_id)!=null) {
                        ArrayList vb = MergeTool.getArray_rule(tfidf_trans,Item_id_forArray, source_id);
                        double score = MergeTool.ComputeSim(va,vb);
                        if(source_entity.getName().equals(des_entity.getName()) && source_entity.getEntityType().equals(des_entity.getEntityType())){
                            score = 0.9;
                        }
                        if(source_entity.getName().equals(des_entity.getName()) && !source_entity.getEntityType().equals(des_entity.getEntityType())){
                            score = 0.7;
                        }
                        if(score>0.0){
                            if(Sim_map.get(des_id)!=null){
                                Map<String,Double> temp_bemerge = Sim_map.get(des_id);
                                temp_bemerge.put(source_id,score);
                                Sim_map.put(des_id,temp_bemerge);
                            }else{
                                Map<String,Double> temp_bemerge = new HashMap<>();
                                temp_bemerge.put(source_id,score);
                                Sim_map.put(des_id,temp_bemerge);
                            }
                        }

                    }else {
                        continue;
                    }
                }


            }
        }

//
//        if(Sim_entityids.size()!=0){
//            List<Entity> Allentitylist = new ArrayList<>();
//            Allentitylist.addAll(sourcekg_entitys);
//            Allentitylist.addAll(deskg_entitys);
//            Map<String,Map<String,Object>> ITEMSYNres = MergeTool.ItemSYN(Allentitylist,Sim_entityids);
//            ItemQuery.InsertToDataBase(ITEMSYNres);
//        }


        Map<String ,Object> result = new HashMap<>();

        result.put("simMap",Sim_map);
        return Sim_map;
    }

    public static void main(String[] args) {
        String tex = "[{\"dataSourceId\":60,\"items\":[\"487581847707131904\",\"770713190448758184\"]},{\"dataSourceId\":60,\"items\":[\"184770713190448758\"]}]";
        List<itemIds> users = JSON.parseArray(tex, itemIds.class);

        tex = tex.substring(1, tex.length()-1);
        String[] texs = tex.replaceAll("\\{","").replaceAll(",","").split("\\}");

        tex = tex.replaceAll("\\\\","");
        tex = tex.replaceAll("\\[","");
        tex = tex.replaceAll("\\]","");
        Map maps = (Map) JSON.parse(tex);
        String itemSearch_id = (String)maps.get("items");
        String[] Search_id = itemSearch_id.split(",");
        boolean a = tex.contains(",");
        System.out.println(tex);
    }
}
