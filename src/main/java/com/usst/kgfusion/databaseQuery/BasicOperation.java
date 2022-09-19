package com.usst.kgfusion.databaseQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.tuple.Triple;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Path;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mysql.cj.xdevapi.JsonArray;
import com.usst.kgfusion.pojo.EnSim;
import com.usst.kgfusion.pojo.EntityEasy;
import com.usst.kgfusion.pojo.EntityEasyForNodesCluster;
import com.usst.kgfusion.util.Neo4jUtils;

public class BasicOperation{
    
    public static List<Record> queryGraph(String graphSymbol, Boolean bidirectional){
        String param = "'" + graphSymbol + "'";
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        StatementResult result = null;
        if (bidirectional == true){
           result = transaction.run(String.format("match (n1 {graphSymbol:%s})-[r]-(n2 {graphSymbol:%s}) return n1,r.name as rel,n2", param, param));
           //result = transaction.run(String.format("match (n1 {graphSymbol:%s})-[r {graphSymbol:%s}]-(n2 {graphSymbol:%s}) return n1,r.name as rel,n2", param,param, param));
        }else{
           result = transaction.run(String.format("match (n1 {graphSymbol:%s})-[r]->(n2 {graphSymbol:%s}) return n1,r.name as rel,n2", param, param));
           //result = transaction.run(String.format("match (n1 {graphSymbol:%s})-[r {graphSymbol:%s}]->(n2 {graphSymbol:%s}) return n1,r.name as rel,n2", param,param,param));
        }
        transaction.success();
        session.close();
        List<Record> records = new ArrayList<>();
        while (result.hasNext()) {
            Record record = (Record) result.next();
            records.add(record);
        }
        // driver.close();
        return records;
    }

    public static Map<Integer, String> queryIdx2name(Set<Integer> queryIdxs){
        Map<Integer, String> res = new HashMap<>();
        String param = "";
        for (Integer reId : queryIdxs){
            param += "" + reId + ",";
        }
        param = param.substring(0, param.length() - 1);
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        StatementResult result = transaction.run(String.format("match (n) where id(n) in [%s] return id(n) as id, n.name as name", param));
        transaction.success();
        session.close();
        List<Record> records = new ArrayList<>();
        while (result.hasNext()) {
            Record record = (Record) result.next();
            records.add(record);
        }
        // driver.close();
        for(Record record: records){
            Integer id = record.get("id").asInt();
            String name = record.get("name").toString();
            res.put(id, name);
        }
        return res;
    }


    public static Set<String> queryItemIdsByEntityIds(List<String> ids, String graphType){
        Set<String> res = new HashSet<>();
        Set<Integer> ids_int = new HashSet<>();
        for(String id: ids){
            ids_int.add(Integer.parseInt(id));
        }
        String param = "";
        for (Integer enId : ids_int){
            param += "" + enId + ",";
        }
        param = param.substring(0, param.length() - 1);
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        StatementResult result = null;
        if(graphType.equals("raw")){
            result = transaction.run(String.format("match (n) where id(n) in [%s] return n.sentence_original_id as itemIds", param));
        }
        if(graphType.equals("zonghe")){
            result = transaction.run(String.format("match (n) where id(n) in [%s] return n.ItemId as itemIds", param));
        }
        if(graphType.equals("concept")){
            result = transaction.run(String.format("match (n) where id(n) in [%s] return n.item_id as itemIds", param));
        }
        transaction.success();
        session.close();
        List<Record> records = new ArrayList<>();
        while (result.hasNext()) {
            Record record = (Record) result.next();
            records.add(record);
        }
        // driver.close();
        for(Record record: records){
            String itemId = record.get("itemIds").asString();
            res.add(itemId);
        }
        return res;
    }

    // todo 根据实体ids查询 不同类型的文档ids  graphtype-items
    public static Map<Integer, List<String>> queryItemIdsByEntityIds_new(List<String> ids){
        Map<Integer, List<String>> res = new HashMap<>();
        Set<Integer> ids_int = new HashSet<>();
        for(String id: ids){
            ids_int.add(Integer.parseInt(id));
        }
        String param = "";
        for (Integer enId : ids_int){
            param += "" + enId + ",";
        }
        param = param.substring(0, param.length() - 1);
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        StatementResult result = null;
        
        result = transaction.run(String.format("match (n) where id(n) in [%s] return n.itemIds as iteminfo", param));

        transaction.success();
        session.close();
        
        // 查询结果是json字符串，需要使用json解析器解析，并按照dataSourceId进行分组
        List<Record> records = new ArrayList<>();
        while (result.hasNext()) {
            Record record = (Record) result.next();
            // remove null
            if(record.get("iteminfo").isNull()){
                continue;
            }
            records.add(record);
        }

        if(records.size() > 0){
            for(Record record: records){
                String itemInfo = record.get("iteminfo").asString();  // 是一个jsonarray字符串
                JSONArray jsonArray = JSONArray.parseArray(itemInfo);
                if(jsonArray == null){
                    continue;
                }
                // 获取 dataSourceId
                for(int i=0; i<jsonArray.size(); i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Integer dataSourceId = jsonObject.getInteger("dataSourceId");
                    String items_str = jsonObject.getString("items");
                    // items_str是个jsonarray字符串
                    JSONArray items = JSONArray.parseArray(items_str);
                    List<String> items_list = new ArrayList<>();
                    for(int j=0; j<items.size(); j++){
                        items_list.add(items.getString(j));
                    }
                    if(res.containsKey(dataSourceId)){
                        res.get(dataSourceId).addAll(items_list);
                    }else{
                        res.put(dataSourceId, items_list);
                    }
    
                }
                
            }
        }

        return res;
    }


    public static Set<Integer> queryNeighbours(Set<Integer> simNodes){
        Set<Integer> res = new HashSet<>();
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        StatementResult result = null;
        String sql = "match (n) where id(n) in " + simNodes.toString() + " with collect(n) as ps call apoc.path.subgraphAll(ps, {maxLevel:2}) yield nodes return nodes as nodes;";
        result = transaction.run(sql);
        transaction.success();
        session.close();
        List<Record> records = new ArrayList<>();
        while (result.hasNext()) {
            Record record = (Record) result.next();
            records.add(record);
        }

        for(Record record: records){
            int size = record.get("nodes").size();
            for (int i = 0; i < size; i++) {
                Integer needId = Math.toIntExact(record.get("nodes").get(i).asNode().id());
                res.add(needId);
            }
        }
        return res;

    }


    public static List<Integer> queryIdsBySymbol(String graphSymbol){
        List<Integer> res = new ArrayList<>();
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        StatementResult result = transaction.run(String.format("match (n) where n.graphSymbol = '%s' return id(n) as id", graphSymbol));
        transaction.success();
        session.close();
        List<Record> records = new ArrayList<>();
        while (result.hasNext()) {
            Record record = (Record) result.next();
            records.add(record);
        }
        // driver.close();
        for(Record record: records){
            Integer id = record.get("id").asInt();
            res.add(id);
        }
        return res;
    }


    public static Boolean checkGraphExist(String graphName){
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        StatementResult result = transaction.run(String.format("match (n) where n.graphSymbol = '%s' return count(n) as num", graphName));
        transaction.success();
        session.close();
        List<Record> records = new ArrayList<>();
        while (result.hasNext()) {
            Record record = (Record) result.next();
            records.add(record);
        }
        // driver.close();
        Integer num = 0;
        for(Record record: records){
            num = record.get("num").asInt();
        }
        if(num > 0){
            return true;
        }else{
            return false;
        }
    }

    public static Boolean checkAllNodeExist(Set<Integer> nodes){
        String param = "";
        for (Integer reId : nodes){
            param += "" + reId + ",";
        }
        param = param.substring(0, param.length() - 1);
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        StatementResult result = transaction.run(String.format("match (n) where id(n) in [%s] return count(n) as num", param));
        transaction.success();
        session.close();
        List<Record> records = new ArrayList<>();
        while (result.hasNext()) {
            Record record = (Record) result.next();
            records.add(record);
        }
        // driver.close();
        int num = 0;
        for(Record record: records){
            num = record.get("num").asInt();
        }
        if(nodes.size() - num > 0){
            return false;
        }else{
            return true;
        }
    }


    public static Boolean checkNode(String nodeName, String graphSymbol){
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        StatementResult result = transaction.run(String.format("match (n) where n.name = '%s' and n.graphSymbol = '%s' return count(n) as num", nodeName, graphSymbol));
        transaction.success();
        session.close();
        List<Record> records = new ArrayList<>();
        while (result.hasNext()) {
            Record record = (Record) result.next();
            records.add(record);
        }
        // driver.close();
        int num = 0;
        for(Record record: records){
            num = record.get("num").asInt();
        }
        if(num >= 1){
            return true;
        }
        return false;
    }


    public static List<Long> checkNodeProp(String nodeName, String prop, String graphSymbol){
        List<Long> props = new ArrayList<>();
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        StatementResult result = transaction.run(String.format("match (n) where n.name = '%s' and n.graphSymbol = '%s' and exists(n."+prop+") return n." +prop +" as "+prop , nodeName, graphSymbol, prop, prop, prop));
        transaction.success();
        session.close();
        List<Record> records = new ArrayList<>();
        while (result.hasNext()) {
            Record record = (Record) result.next();
            records.add(record);
        }
        // driver.close();

        for(Record record: records){
            props.add(record.get(prop).asLong());
        }

        return props;
    }

    public static List<String> checkNodeProp1(String id1, String symbol, String prop){
        int id = Integer.valueOf(id1);
        List<String> props = new ArrayList<>();
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        StatementResult result = transaction.run(String.format("match (n) where id(n)= %d and n.graphSymbol = '%s' and exists(n."+prop+") return n." +prop +" as "+prop , id, symbol, prop, prop, prop));
        transaction.success();
        session.close();
        List<Record> records = new ArrayList<>();
        while (result.hasNext()) {
            Record record = (Record) result.next();
            records.add(record);
        }
        // driver.close();

        for(Record record: records){
            props.add(record.get(prop).toString());
        }

        return props;
    }


    public static List<Record> queryGraphByReIds(List<String> reIds){
        String param = "";
        for (String reId : reIds){
            param += "" + reId + ",";
        }
        param = param.substring(0, param.length() - 1);
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        StatementResult result = transaction.run(String.format("match (n1)-[r]-(n2) where id(r) in [%s] return n1,r.name as rel,n2", param));
        transaction.success();
        session.close();
        List<Record> records = new ArrayList<>();
        while (result.hasNext()) {
            Record record = (Record) result.next();
            records.add(record);
        }
        // driver.close();
        return records;
    }

    public static Boolean judingNullAccordingGraphSymbol(String gSymbol){
        String param = "'" + gSymbol + "'";
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        StatementResult result = null;
        result = transaction.run(String.format("match (n) where n.graphSymbol = %s return count(n) as flag", param));
        transaction.success();
        session.close();
        List<Record> records = new ArrayList<>();
        while (result.hasNext()) {
            Record record = (Record) result.next();
            records.add(record);
        }
        String changefor_flag = records.get(0).asMap().get("flag").toString();
        if(changefor_flag.equals("0")){
            return true;
        }
        return false;
    }

    public static Boolean judingNullAccordingGraphSymbolAndSynTag(String graphSymbol, Integer synthesizeTag) {
        String param = "'" + graphSymbol + "'";
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        StatementResult result = null;
        String sql = String.format("match (n) where n.graphSymbol = %s and n.synthesizeTag = %d return count(n) as flag", param, synthesizeTag);
        result = transaction.run(sql);
        transaction.success();
        session.close();
        List<Record> records = new ArrayList<>();
        while (result.hasNext()) {
            Record record = (Record) result.next();
            records.add(record);
        }
        String changefor_flag = records.get(0).asMap().get("flag").toString();
        if(changefor_flag.equals("0")){
            return true;
        }
        return false;
    }

    public static Boolean judgingSrcId(Integer entityId){
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        StatementResult result = null;
        String sql = String.format("match (n) where id(n) = %d return n.srcId is null as flag", entityId);
        result = transaction.run(sql);
        transaction.success();
        session.close();
        List<Record> records = new ArrayList<>();
        while (result.hasNext()) {
            Record record = (Record) result.next();
            records.add(record);
        }
        if(records.size()==0){
            return false;
        }else{
            String changefor_flag = records.get(0).asMap().get("flag").toString();
            if(changefor_flag.equals("0")){
                return true;
            }
            return false;
        }

    }

    public static List<Integer> GetSrcIds(Integer entityId){
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        StatementResult result = null;
        String sql = String.format("match (n) where id(n) = %d return n.srcIds as ids", entityId);
        result = transaction.run(sql);
        transaction.success();
        session.close();

        List<Record> records = new ArrayList<>();
        while (result.hasNext()) {
            Record record = (Record) result.next();
            records.add(record);
        }

        List<Integer> res = new ArrayList<>();
        Map<String, Object> a1 = records.get(0).asMap();
        if(records.size()==0 ||records.get(0).asMap().get("ids")==null){
            return res;
        }else {
            String init_ids = records.get(0).asMap().get("ids").toString();
            int count_kuohao = 0;
            for(int len=0;len<init_ids.length();len++){
                char a = init_ids.charAt(len);
                if(a=='['){
                    count_kuohao++;
                }
            }
            if(count_kuohao>1){
                return res;
            }
            init_ids = init_ids.replaceAll("\\[", "");
            init_ids = init_ids.replaceAll("\\]", "");
            init_ids = init_ids.replaceAll("null", "");
            init_ids = init_ids.replaceAll(" ", "");
            List<String> init_ids_string = Arrays.asList(init_ids.split(","));

            for (String ids : init_ids_string) {
                res.add(Integer.parseInt(ids));
            }
            return res;
        }



    }

    public static String GetitemIds(Integer entityId){
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        StatementResult result = null;
        String sql = String.format("match (n) where id(n) = %d return n.itemIds as ids", entityId);
        result = transaction.run(sql);
        transaction.success();
        session.close();

        List<Record> records = new ArrayList<>();
        while (result.hasNext()) {
            Record record = (Record) result.next();
            records.add(record);
        }

        if(records.size()==0){
            return null;
        }else {
            if(records.get(0).asMap().get("ids")==null){
                return null;
            }
            String init_ids = records.get(0).asMap().get("ids").toString();
            if(init_ids.length()==0){
                return null;
            }

            return init_ids;
        }



    }

    public static Boolean judgingSrcIds(Integer entityId){
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        StatementResult result = null;
        String sql = String.format("match (n) where id(n) = %d return n.srcIds is null as flag", entityId);
        result = transaction.run(sql);

        transaction.success();
        session.close();
        List<Record> records = new ArrayList<>();
        while (result.hasNext()) {
            Record record = (Record) result.next();
            records.add(record);
        }
        if(records.size()==0){
            return false;
        }else{
            if(records.get(0).asMap().get("flag").toString().equals("true")){
                return true;
            }
            return false;
        }

    }

//    public static Boolean judingNullAccordingSrcId(Integer id){
//        String param = "'" + graphSymbol + "'";
//        Driver driver = Neo4jUtils.getDriver();
//        Session session = driver.session();
//        Transaction transaction = session.beginTransaction();
//        StatementResult result = null;
//        String sql = String.format("match (n) where id(n) = %d and n. = %d return count(n) as flag", param, synthesizeTag);
//        result = transaction.run(sql);
//        transaction.success();
//        session.close();
//        List<Record> records = new ArrayList<>();
//        while (result.hasNext()) {
//            Record record = (Record) result.next();
//            records.add(record);
//        }
//        if(records.get(0).asMap().get("flag").toString().equals("0")){
//            return true;
//        }
//        return false;
//    }


    public static Node getRandomNode(String graphSymbol){
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        StatementResult result = null;
        result = transaction.run(String.format("match (n) where n.graphSymbol = '%s' return n as randomNode limit 1", graphSymbol));
        transaction.success();
        session.close();
        List<Record> records = new ArrayList<>();
        while (result.hasNext()) {
            Record record = (Record) result.next();
            records.add(record);
        }
        if(records.size() != 0){
            return records.get(0).get("randomNode").asNode();
        }else{
            return null;
        }
    }


//    public static void insertGraph(Map<String, String> graphMap, String entityType, String ontologySymbol, String graphSymbol){
//
//        Set<String> ens = new HashSet<>();
//        ens.addAll(graphMap.keySet());
//        ens.addAll(graphMap.values());
//
//        Driver driver = Neo4jUtils.getDriver();
//        Session session = driver.session();
//        Transaction transaction = session.beginTransaction();
//        String sql = "";
//        // create node
//        for(String en: ens){
//            sql = String.format("create (n:concept {name:'%s', entityType:'%s', graphSymbol:'%s', ontologySymbol:'%s'}) return n,", en, entityType, graphSymbol, ontologySymbol);
//            sql = sql.substring(0, sql.length() - 1);
//            transaction.run(sql);
//        }
//
//        // create relation
//        for(Entry<String, String> entry: graphMap.entrySet()){
//            sql = String.format("match (n1 {name:'%s', graphSymbol:'%s'}), (n2 {name:'%s', graphSymbol:'%s'}) create (n1)-[r:%s]->(n2) return r", entry.getKey(), graphSymbol, entry.getValue(), graphSymbol, "gnstzcgx");
//            transaction.run(sql);
//        }
//
//        transaction.success();
//        session.close();
//        // driver.close();
//    }

    public static void insertAndMergeWithSameName(Map<String, String> graphMap, String entityType, String ontologySymbol, String graphSymbol){
        Set<String> ens = new HashSet<>();
        ens.addAll(graphMap.keySet());
        ens.addAll(graphMap.values());
        
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        String sql = "";
        // create node
        for(String en: ens){
            sql = String.format("create (n:evolution {name:'%s', entityType:'%s', graphSymbol:'%s', ontologySymbol:'%s'}) return n,", en, entityType, graphSymbol, ontologySymbol);
            sql = sql.substring(0, sql.length() - 1);
            transaction.run(sql);
        }

        // create relation
        for(Entry<String, String> entry: graphMap.entrySet()){
            sql = String.format("match (n1:evolution {name:'%s', graphSymbol:'%s'}), (n2:evolution {name:'%s', graphSymbol:'%s'}) merge (n1)-[r:%s]->(n2) return r", entry.getKey(), graphSymbol, entry.getValue(), graphSymbol, "gnstzcgx");
            transaction.run(sql);
        }
        
        // merge with same node
        sql = String.format("match (n:evolution {graphSymbol:'%s'}) with n.name as name, collect(n) as nodes call apoc.refactor.mergeNodes(nodes, {mergeRels:true}) yield node return *", graphSymbol);
        transaction.run(sql);
        transaction.success();
        session.close();
//        System.out.println();
        // driver.close();
    }


    public static void insertAndMergeWithSameName(String name, String entityType, String ontologySymbol, String graphSymbol){
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        String sql = "";
        // create node

        sql = String.format("create (n:evolution {name:'%s', entityType:'%s', graphSymbol:'%s', ontologySymbol:'%s'}) return n,", name, entityType, graphSymbol, ontologySymbol);
        sql = sql.substring(0, sql.length() - 1);
        transaction.run(sql);


        // merge with same node
        sql = String.format("match (n:evolution {graphSymbol:'%s'}) with n.name as name, collect(n) as nodes call apoc.refactor.mergeNodes(nodes, {mergeRels:true}) yield node return *", graphSymbol);
        transaction.run(sql);
        transaction.success();
        session.close();
//        System.out.println();
        // driver.close();
    }

    public static void insertAndMergeWithSameName(List<Triple<String, String, String>> triples, String entityType, String ontologySymbol, String graphSymbol){
        Set<String> ens = new HashSet<>();
        for(Triple<String, String, String> triple: triples){
            ens.add(triple.getLeft());
            ens.add(triple.getRight());
        }

        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        String sql = "";
        // create node
        for(String en: ens){
            sql = String.format("create (n:evolution {name:'%s', entityType:'%s', graphSymbol:'%s', ontologySymbol:'%s'}) return n,", en, entityType, graphSymbol, ontologySymbol);
            sql = sql.substring(0, sql.length() - 1);
            transaction.run(sql);
        }

        // create relation
        for(Triple<String, String, String> triple: triples){
            String head = triple.getLeft();
            String tail = triple.getRight();
            String relation = triple.getMiddle();
            sql = String.format("match (n1:evolution {name:'%s', graphSymbol:'%s'}), (n2:evolution {name:'%s', graphSymbol:'%s'}) merge (n1)-[r:%s]->(n2) return r", head, graphSymbol, tail, graphSymbol, relation);
            transaction.run(sql);
        }

        // merge with same node
        sql = String.format("match (n:evolution {graphSymbol:'%s'}) with n.name as name, collect(n) as nodes call apoc.refactor.mergeNodes(nodes, {mergeRels:true}) yield node return *", graphSymbol);
        transaction.run(sql);
        transaction.success();
        session.close();
//        System.out.println();
        // driver.close();
    }


    public static List<Record> queryIsolatedNode(String graphSymbol){
        String param = "'" + graphSymbol + "'";
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        StatementResult result = null;
        result = transaction.run(String.format("match (n {graphSymbol:%s}) where not (n)-[]-() return n", param));
        transaction.success();
        session.close();
        List<Record> records = new ArrayList<>();
        while (result.hasNext()) {
            Record record = (Record) result.next();
            records.add(record);
        }
        // driver.close();
        return records;
    }

    public static List<Record> CreateNewNode(String graphSymbol){
        String param = "'" + graphSymbol + "'";
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        StatementResult result = null;
        result = transaction.run(String.format("create (n:GraphEntity {graphSymbol:%s}) return id(n)", param));
        transaction.success();
        session.close();
        List<Record> records = new ArrayList<>();
        while (result.hasNext()) {
            Record record = (Record) result.next();
            records.add(record);
        }
        // driver.close();
        return records;
    }


    public static List<Record> querySchema(){
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        StatementResult result = null;
        result = transaction.run(String.format("match (n:misre_km_zsk_gnst {graphSymbol:'大数据平台概念层次图谱'})-[r]->(n2:misre_km_zsk_gnst {graphSymbol:'大数据平台概念层次图谱'}) return n.name as parent,n2.name as child"));
        transaction.success();
        session.close();
        List<Record> records = new ArrayList<>();
        while (result.hasNext()) {
            Record record = (Record) result.next();
            records.add(record);
        }
        // driver.close();
        return records;
    }


    public static void deleteByids(List<Integer> ids){
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        for (Integer id: ids){
            transaction.run(String.format("match (n) where id(n) = %d detach delete n", id));
        }
        transaction.success();
        session.close();
        // driver.close();
    }

    // key1:实体id key2:属性 value:更新后的属性值
    public static void updataByids(Map<Integer, Map<String, String>> updateInfo){
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        for (Entry<Integer, Map<String, String>> entry: updateInfo.entrySet()){
            Integer id = entry.getKey();
            Map<String, String> propInfo = entry.getValue();
            for (String proName: propInfo.keySet()){
                String newValue = propInfo.get(proName);
//                if(proName.equals("srcIds")){
//                    Boolean isNull = BasicOperation.judgingSrcIds(id);
//                    if(isNull == true){
//                        transaction.run(String.format("match (n) where id(n) = %d set n.%s = '%s'", id, proName, newValue));
//                    }else{
//                        transaction.run(String.format("match (n) where id(n) = %d set n.%s = n.%s + '%s'", id, proName, proName, newValue));
//                    }
//
//                }else{
//                    transaction.run(String.format("match (n) where id(n) = %d set n.%s = '%s'", id, proName, newValue));
//                }
                transaction.run(String.format("match (n) where id(n) = %d set n.%s = '%s'", id, proName, newValue));

            }
        }
        transaction.success();
        session.close();
        // driver.close();
    }

    public static void updataByidInteger(Map<Integer, Map<String, Integer>> updateInfo){
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        for (Entry<Integer, Map<String, Integer>> entry: updateInfo.entrySet()){
            Integer id = entry.getKey();
            Map<String, Integer> propInfo = entry.getValue();
            for (String proName: propInfo.keySet()){
                int newValue = propInfo.get(proName);
                transaction.run(String.format("match (n) where id(n) = %d set n.%s=%d", id, proName, newValue));

            }
        }
        transaction.success();
        session.close();
        // driver.close();
    }

    public static void updataByidDouble(Map<Integer, Map<String, Double>> updateInfo){
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        for (Entry<Integer, Map<String, Double>> entry: updateInfo.entrySet()){
            Integer id = entry.getKey();
            Map<String, Double> propInfo = entry.getValue();
            for (String proName: propInfo.keySet()){
                double newValue = propInfo.get(proName);
                transaction.run(String.format("match (n) where id(n) = %d set n.%s=%f ", id, proName, newValue));

            }
        }
        transaction.success();
        session.close();
        // driver.close();
    }

    // 左边的表示待合并实体，右边表示将被合并的相似实体
    public static void mergeNodes(Map<Integer, List<Integer>> simMap){
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        for(Entry<Integer, List<Integer>> entry: simMap.entrySet()){
            Integer leftId = entry.getKey();
            for(Integer rightId: simMap.get(leftId)){
                transaction.run(String.format("MATCH (a1) where id(a1)=%d match (a2) where id(a2)=%d WITH head(collect([a1,a2])) as nodes CALL apoc.refactor.mergeNodes(nodes,{properties:'discard', mergeRels:true}) YIELD node return count(*)", leftId, rightId)); 
            }
        }
        transaction.success();
        session.close();
        // driver.close(); 
    }
    // 左边的表示待合并实体，右边表示将被合并的相似实体
    public static void mergeNodes_AimNull(Map<Integer, List<Integer>> simMap){
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        for(Entry<Integer, List<Integer>> entry: simMap.entrySet()){
            Integer leftId = entry.getKey();
            for(Integer rightId: simMap.get(leftId)){
                transaction.run(String.format("MATCH (a1) where id(a1)=%d match (a2) where id(a2)=%d WITH head(collect([a1,a2])) as nodes CALL apoc.refactor.mergeNodes(nodes,{properties:'combine', mergeRels:true}) YIELD node return count(*)", leftId, rightId));
            }
        }
        transaction.success();
        session.close();
        // driver.close();
    }



    public static void mergeNodesWithSameName(String graphName){
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        transaction.run(String.format("MATCH (n) where n.graphSymbol = '%s' WITH toLower(n.name) as name, collect(n) as nodes CALL apoc.refactor.mergeNodes(nodes, {properties:'discard', mergeRels:true}) yield node return *", graphName));
        transaction.success();
        session.close();
        // driver.close();
    }

    public static void setProperty(String s, String destination, String prop, Long value) {
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        transaction.run(String.format("MATCH (n) where n.graphSymbol = '%s' and n.name = '%s' set n."+prop+"=" +value, destination, s, prop, value));
        transaction.success();
        session.close();
        // driver.close();
    }

    public static void setProperty1(String id1, String symbol, String prop, String value) {
        int id = Integer.valueOf(id1);
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        transaction.run(String.format("MATCH (n) where id(n)=%d and n.graphSymbol = '%s'  set n."+prop+"=" +"'" +value+ "'" , id, symbol, prop, value));
        transaction.success();
        session.close();
        // driver.close();
    }

    public static void setPropertyRelation(String symbol) {
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        transaction.run(String.format("MATCH (n1 {graphSymbol:'%s'})-[r]-(n2 {graphSymbol:'%s'}) set r.graphSymbol ='%s'",symbol,symbol,symbol));
        transaction.success();
        session.close();
        // driver.close();
    }

    public static void setPropertyRelation_pre(String symbol) {
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        transaction.run(String.format("MATCH (n1 {graphSymbol:'%s'})-[r]-(n2 {graphSymbol:'%s'}) set r={graphSymbol:'%s'}",symbol,symbol,symbol));
        transaction.success();
        session.close();
        // driver.close();
    }

    // 原始和目标 id 映射
    public static Map<Integer, Integer> copySubgraphAccSimMap(Map<Integer, List<Integer>> simMap){
        Map<Integer, Integer> res = new HashMap<>();
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        StatementResult result = null;
        // get values from Map<Integer, List<Integer>>
        Set<Integer> beMergedNodes = new HashSet<>();
        for(Entry<Integer, List<Integer>> entry: simMap.entrySet()){
            Integer leftId = entry.getKey();
            for(Integer rightId: simMap.get(leftId)){
                beMergedNodes.add(rightId);
            }
        }
//        String sql = "match (n) where id(n) in " + beMergedNodes.toString() + " with collect(n) as ps call apoc.path.subgraphAll(ps, {maxLevel:2}) yield nodes, relationships call apoc.refactor.cloneSubgraph(nodes) yield input, output, error with collect(output) as res, input as inp foreach (t in res | set t.synthesizeTag = 1) return inp, res";

        String sql = "match (n) where id(n) in " + beMergedNodes.toString() + " with collect(n) as ps call apoc.path.subgraphAll(ps, {maxLevel:2}) yield nodes, relationships call apoc.refactor.cloneSubgraph(nodes) yield input, output, error with collect(output) as res, input as inp return inp, res";
        result = transaction.run(sql);
        transaction.success();
        session.close();
        List<Record> records = new ArrayList<>();
        while (result.hasNext()) {
            Record record = (Record) result.next();
            records.add(record);
        }

        for(Record record: records){
            Integer source = Math.toIntExact(record.get("inp").asInt());
            Integer copyId = Math.toIntExact(record.get("res").get(0).asNode().id());
            res.put(source, copyId);
        }

        return res;
    }



    // todo added by dai
    public static List<String> queryDistinctProp(String graphSymbol, String prop){
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        StatementResult result = transaction.run(String.format("match (n) where n.graphSymbol = '%s' return distinct n."+prop+" as res", graphSymbol));
        List<String> res = new ArrayList<>();
        while (result.hasNext()) {
            Record record = (Record) result.next();
            res.add(record.get("res").asString());
        }
        transaction.success();
        session.close();
        // driver.close();
        return res;
    }


    public static List<EntityEasy> queryEntityEasy(String graphSymbol, String ons){
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        StatementResult result = transaction.run(String.format("match (n) where n.graphSymbol = '%s' and n.ontologySymbol = '%s' return n", graphSymbol, ons));
        List<EntityEasy> res = new ArrayList<>();
        while (result.hasNext()) {
            Record record = (Record) result.next();
            Node node = record.get("n").asNode();
            res.add(new EntityEasy(node.id(), node.get("name").toString(), node.get("ontologySymbol").asString(), node.get("entityType").asString()));
        }
        transaction.success();
        session.close();
        return res;
    }

    public static Node queryNodeById(long id){
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        StatementResult result = transaction.run(String.format("match (n) where id(n) = %d return n", id));
        Node res = null;
        while (result.hasNext()) {
            Record record = (Record) result.next();
            res = record.get("n").asNode();
        }
        transaction.success();
        session.close();
        return res;
    }


    // todo 服务于两个新增的演化接口
    public static List<EntityEasyForNodesCluster> queryEntityEasyForNodesCluster(String ids){
        List<Long> idss = new ArrayList<>();
        for(String id: ids.split(",")){
            idss.add(Long.valueOf(id.trim()));
        }
        String param = "";
        for (Long reId : idss){
            param += "" + reId + ",";
        }
        param = param.substring(0, param.length() - 1);
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        StatementResult result = transaction.run(String.format("match (n) where id(n) in [%s] return n", param));
        List<EntityEasyForNodesCluster> res = new ArrayList<>();
        while (result.hasNext()) {
            Record record = (Record) result.next();
            Node node = record.get("n").asNode();
            res.add(new EntityEasyForNodesCluster(node.id(), node.get("name").toString(), node.get("ontologySymbol").asString(), node.get("entityType").asString(), node.get("subOntologySymbol").asString(), node.get("subEntityClass").asString()));
        }
        transaction.success();
        session.close();
        return res;
    }

    public static List<EntityEasyForNodesCluster> queryEntityEasyForNodesClusterByConditions(Map<String, String> conditions){
        String param = "";
        for(Entry<String, String> entry: conditions.entrySet()){
            param += "n." + entry.getKey() + " = '" + entry.getValue() + "' and ";
        }
        param = param.substring(0, param.length() - 4);
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        StatementResult result = transaction.run(String.format("match (n) where %s return n", param));
        List<EntityEasyForNodesCluster> res = new ArrayList<>();
        while (result.hasNext()) {
            Record record = (Record) result.next();
            Node node = record.get("n").asNode();
            res.add(new EntityEasyForNodesCluster(node.id(), node.get("name").toString(), node.get("ontologySymbol").asString(), node.get("entityType").asString(), node.get("subOntologySymbol").asString(), node.get("subEntityClass").asString()));
        }
        transaction.success();
        session.close();
        return res;
    }


    // todo 层级消歧使用
    public static List<Long> getAllNodeIds(String graphSymbol){
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        StatementResult result = transaction.run(String.format("match (n) where n.graphSymbol = '%s' return id(n) as res", graphSymbol));
        List<Long> res = new ArrayList<>();
        while (result.hasNext()) {
            Record record = (Record) result.next();
            res.add((long) record.get("res").asInt());
        }
        transaction.success();
        session.close();
        return res;
    }

    public static List<String> getAllRelTypes(String graphSymol){
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        String query = String.format("match (n)-[r]->(m) where n.graphSymbol = '%s' and m.graphSymbol = '%s' return distinct type(r) as res", graphSymol, graphSymol);
        StatementResult result = transaction.run(query);
        List<String> res = new ArrayList<>();
        while (result.hasNext()) {
            Record record = (Record) result.next();
            res.add(record.get("res").asString());
        }
        transaction.success();
        session.close();
        return res;
    }

    public static List<Path> findAllPathsBetweenTwoNodesWithOneRelType(Long from, Long to, String relType){
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        StatementResult result = transaction.run(String.format("match p = (n1)-[r:%s*]->(n2) where id(n1) = %d and id(n2) = %d return p", relType, from, to));
        List<Path> res = new ArrayList<>();
        while (result.hasNext()) {
            Record record = (Record) result.next();
            res.add(record.get("p").asPath());
        }
        transaction.success();
        session.close();
        return res;
    }


    // ! 下面的查询规则形似度计算用
    public static Set<EnSim> getNodes(String graphSymbol){
        Set<EnSim> res = new HashSet<>();
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        StatementResult result = transaction.run(String.format("match (n) where n.graphSymbol = '%s' return n", graphSymbol));
        List<Record> records = new ArrayList<>();
        while (result.hasNext()) {
            Record record = (Record) result.next();
            records.add(record);
        }
        for(Record record: records){
            Node newnode = record.get("n").asNode();
            Long id = newnode.id();
//            if(!newnode.get("name").type().name().equals("STRING")){
//                System.out.println(newnode.get("name").type().name());
//                System.out.println(newnode.get("name"));
//            }
            String name = newnode.get("name").toString();
            String type = newnode.get("entityType").asString();
            res.add(new EnSim(id, name, type));
        }
        transaction.success();
        session.close();
        return res;
    }

    public static Map<EnSim, Set<EnSim>> getParents(String graph){
        Map<EnSim, Set<EnSim>> res = new HashMap<>();
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        StatementResult result = transaction.run(String.format("match (n1)-[r]->(n2) where n1.graphSymbol = '%s' and r.name = '组成' and n2.graphSymbol='%s' return n1,n2",graph,graph));
        List<Record> records = new ArrayList<>();
        while (result.hasNext()) {
            Record record = (Record) result.next();
            records.add(record);
        }
        for(Record record: records){
            Node n1 = record.get("n1").asNode();  // parent
            Node n2 = record.get("n2").asNode();  // child
            EnSim parent = new EnSim(n1.id(), n1.get("name").toString());
            EnSim child = new EnSim(n2.id(), n2.get("name").toString());
            if(res.containsKey(child)){
                res.get(child).add(parent);
            }else{
                Set<EnSim> parents = new HashSet<>();
                parents.add(parent);
                res.put(child, parents);
            }
        }
        transaction.success();
        session.close();
        return res;
    }

    public static Map<EnSim, Set<EnSim>> getChildren(String graph){
        Map<EnSim, Set<EnSim>> res = new HashMap<>();
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        StatementResult result = transaction.run(String.format("match (n1)-[r]->(n2)  where n1.graphSymbol = '%s' and r.name = '组成' and n2.graphSymbol='%s' return n1,n2", graph,graph));
        List<Record> records = new ArrayList<>();
        while (result.hasNext()) {
            Record record = (Record) result.next();
            records.add(record);
        }
        for(Record record: records){
            Node n1 = record.get("n1").asNode();  // parent
            Node n2 = record.get("n2").asNode();  // child
            EnSim parent = new EnSim(n1.id(), n1.get("name").toString());
            EnSim child = new EnSim(n2.id(), n2.get("name").toString());
            if(res.containsKey(parent)){
                res.get(parent).add(child);
            }else{
                Set<EnSim> children = new HashSet<>();
                children.add(child);
                res.put(parent, children);
            }
        }
        return res;
    }

    public static Map<EnSim, Set<EnSim>> getNeighbors(String graph){
        Map<EnSim, Set<EnSim>> res = new HashMap<>();
        List<Integer> ids = BasicOperation.queryIdsBySymbol(graph);
        String param = "";
        for (Integer reId : ids){
            param += "" + reId + ",";
        }
        if(param.length()==0){
            return res;
        }
        param = param.substring(0, param.length() - 1);
        Driver driver = Neo4jUtils.getDriver();
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        StatementResult result = transaction.run(String.format("match (source)-[r]-(neighbour) where id(source) in [%s] return source, neighbour", param));
        transaction.success();
        session.close();
        List<Record> records = new ArrayList<>();
        while (result.hasNext()) {
            Record record = (Record) result.next();
            records.add(record);
        }
        // driver.close();
        for(Record record: records){
            Node source = record.get("source").asNode();  // center
            Node neighbour = record.get("neighbour").asNode();  // neighbor
            EnSim center = new EnSim(source.id(), source.get("name").toString());
            EnSim neighbor = new EnSim(neighbour.id(), neighbour.get("name").toString());
            if(res.containsKey(center)){
                res.get(center).add(neighbor);
            }else{
                Set<EnSim> neighbors = new HashSet<>();
                neighbors.add(neighbor);
                res.put(center, neighbors);
            }
        }
        return res;
    }



    public static void main(String... args){
        // List<Record> res = BasicQuery.queryGraph("SOA服务基础设施", false);
        // for (Record record: res){
        //     Node node1 = record.get("n1").asNode();
        //     Node node2 = record.get("n2").asNode();
        //     String relation = record.get("rel").asString();
        //     System.out.println(node1.get("name").asString() + " " + node1.get("graphSymbol").asString() + " " + node2.get("name").asString() + " " + node2.get("graphSymbol").asString() + " " + relation);
        // }

        // BasicQuery.deleteNode(81);
        // Map<Integer, Map<String, String>> updateInfo = new HashMap<>();
        // Map<String, String> propInfo = new HashMap<>();
        // propInfo.put("message", "hello更新了");
        // updateInfo.put(81, propInfo);
        // BasicQuery.updataByids(updateInfo);

        // Map<Integer, Integer> simMap = new HashMap<>();
        // simMap.put(295, 234);
        // BasicQuery.mergeNodes(simMap);

        // Boolean res = BasicOperation.judingNullAccordingGraphSymbol("大数据哦");
        // System.out.println(res);

        // Node node = BasicOperation.getRandomNode("SOA服务基础设施");
        // System.out.println(node.get("graphSymbol"));


        // Driver driver = Neo4jUtils.getDriver();
        // Session session = driver.session();
        // Transaction transaction = session.beginTransaction();
        // // get values from Map<Integer, List<Integer>>
        // Set<Integer> beMergedNodes = new HashSet<>();
        // beMergedNodes.add(133);
        // beMergedNodes.add(140);
        // String sql = "match (n) where id(n) in " + beMergedNodes.toString() + " with collect(n) as ps call apoc.path.subgraphAll(ps, {maxLevel:2}) yield nodes, relationships call apoc.refactor.cloneSubgraph(nodes) yield input, output, error with collect(output) as res foreach (t in res | set t.synthesizeTag = 1) return res";
        // transaction.run(sql);
        // transaction.success();
        // session.close();

        // Boolean res = BasicOperation.judingNullAccordingGraphSymbolAndSynTag("hahaha", 1);
        // System.out.println(res);

//        System.out.println(1);
//        System.out.println();

//        Map<Integer, List<Integer>> testMap = new HashMap<>();
//        List<Integer> li = new ArrayList<>();
//        li.add(133);
//        li.add(140);
//        testMap.put(1, li);
//        BasicOperation.copySubgraphAccSimMap(testMap);

//        Boolean res = BasicOperation.judgingSrcId(7040);
//        System.out.println(res);

//        System.out.println(BasicOperation.queryIdx2name(new HashSet<Integer>(){{add(1657); add(1659);}}));
//        System.out.println(BasicOperation.checkGraphExist("2d3a3afb-4589-40f7-b7b5-44955810e393"));
//        System.out.println(BasicOperation.checkAllNodeExist(new HashSet<Integer>(){{add(1657); add(1659);add(1);}}));

//        BasicOperation.mergeNodesWithSameName("f3ffc60e-6c6d-4f14-a679-8739bbb06512");

//        Set<Integer> tests = new HashSet<Integer>(){{add(6505);}};
//        Set<Integer> res = BasicOperation.queryNeighbours(tests);
//        System.out.println(res);
//        List<String> enids = new ArrayList<>();
//        enids.add("6426");
//        System.out.println(BasicOperation.queryItemIdsByEntityIds(enids));


//        BasicOperation.insertAndMergeWithSameName("新增", "x", "x", "ok");
//        System.out.println(BasicOperation.checkNodeProp("安全监管", "item_id", "ok"));
        // BasicOperation.setProperty("大数据分析支撑", "ok", "item_id", 10000L);


        List<Record> res1 = CreateNewNode("虚拟化平台");
        System.out.println("test");
    }




}
