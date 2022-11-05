package com.usst.kgfusion.constructer;

import java.util.*;

import com.usst.kgfusion.databaseQuery.BasicOperation;
import com.usst.kgfusion.pojo.EntityRaw;
import com.usst.kgfusion.pojo.KG;
import com.usst.kgfusion.pojo.RelationEasy;
import com.usst.kgfusion.pojo.Triple;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Relationship;

import javax.management.relation.Relation;

public class GraphReader {
    /**
     * input: be merged graph, mergegraph
     * Reads the graph from the database.
     * @return the graph
     */
    public static List<Record> query(String source, String destination, Boolean bidirectional) {
        List<Record> res = new ArrayList<>();
        List<Record> leftGraph = queryEdge(source, bidirectional);
        List<Record> rightGraph = queryEdge(destination, bidirectional);
        List<Record> leftNode = queryIsolatedNode(source);
        List<Record> rightNode = queryIsolatedNode(destination);
        // Neo4jUtils.close(); // 统一关闭driver
        res.addAll(leftGraph);
        res.addAll(rightGraph);
        res.addAll(leftNode);
        res.addAll(rightNode);
        return res;
    }


    public static List<Record> query2(String source, Boolean bidirectional) {
        List<Record> res = new ArrayList<>();
        List<Record> leftGraph = queryEdge(source, bidirectional);
        List<Record> leftNode = queryIsolatedNode(source);
        // Neo4jUtils.close(); // 统一关闭driver
        res.addAll(leftGraph);
        res.addAll(leftNode);
        return res;
    }

    public static List<Record> queryEdge(String graphSymbol, Boolean bidirectional){
        List<Record> res = BasicOperation.queryGraph(graphSymbol, bidirectional);
        return res;
    }

    public static List<Record> queryIsolatedNode(String graphSymbol){
        List<Record> res = BasicOperation.queryIsolatedNode(graphSymbol);
        return res;
    }

    public static KG  readGraph(List<Record> records, String graphType){
        KG res = new KG();
        if(records.size() == 0) return res;
        List<EntityRaw> ens = new ArrayList<>();  // 这个结合中排除了孤立节点，正常图谱中不应该有孤立节点
        List<Triple> triples = new ArrayList<>();
        Map<Long, Boolean> visited = new HashMap<>(); // 判断是否需要创建新的实体，根据id，可唯一标识
        Map<Long, EntityRaw> entityMap = new HashMap<>(); // 方便根据id找到entity，name相同的节点后续会合并
        Map<EntityRaw, List<EntityRaw>> edges = new LinkedHashMap<>();
        Map<EntityRaw, List<Integer>> directions = new LinkedHashMap<>();
        int tripleCount = 0;
        for (Record record: records){
            if (record.containsKey("n")){
                Node node = record.get("n").asNode();
                if (!visited.containsKey(node.id())){
                    visited.put(node.id(), true);
                    if(graphType.equals("zonghe")){
                        EntityRaw entity = new EntityRaw(Long.toString(node.id()), node.get("name").toString(), node.get("entityType").asString(), node.get("entitySubClass").asString(), node.get("graphSymbol").asString(), node.get("ItemId").asString(), "0");
                        entityMap.put(node.id(), entity);
                        ens.add(entity);
                    }
                    if(graphType.equals("raw")){
                        EntityRaw entity = new EntityRaw(Long.toString(node.id()), node.get("name").toString(), node.get("entityType").asString(), node.get("entitySubClass").asString(), node.get("graphSymbol").asString(), node.get("sentence_original_id").asString(), "0");
                        entityMap.put(node.id(), entity);
                        ens.add(entity);
                    }
                    if(graphType.equals("concept")){
                        EntityRaw entity = new EntityRaw(Long.toString(node.id()), node.get("name").toString(), node.get("entityType").asString(), node.get("entitySubClass").asString(), node.get("graphSymbol").asString(), Long.toString(node.get("item_id").asLong()), "0");
                        entityMap.put(node.id(), entity);
                        ens.add(entity);
                    }
                    continue;
                }
            }
            Node head = record.get("n1").asNode();
            Node tail = record.get("n2").asNode();
            String relation = record.get("rel").asString();
            if (!visited.containsKey(head.id())){
                visited.put(head.id(), true);
                if(graphType.equals("raw")){
                    EntityRaw newEntity = new EntityRaw(Long.toString(head.id()), head.get("name").toString(), head.get("entityType").asString(), head.get("entitySubClass").asString(), head.get("graphSymbol").asString(), head.get("sentence_original_id").asString(), "0");
                    entityMap.put(head.id(), newEntity);
                    ens.add(newEntity);
                }
                if(graphType.equals("zonghe")){
                    EntityRaw newEntity = new EntityRaw(Long.toString(head.id()), head.get("name").toString(), head.get("entityType").asString(), head.get("entitySubClass").asString(), head.get("graphSymbol").asString(), head.get("ItemId").asString(), "0");
                    entityMap.put(head.id(), newEntity);
                    ens.add(newEntity);
                }
                if(graphType.equals("raw")){
                    EntityRaw newEntity = new EntityRaw(Long.toString(head.id()), head.get("name").toString(), head.get("entityType").asString(), head.get("entitySubClass").asString(), head.get("graphSymbol").asString(), Long.toString(head.get("item_id").asLong()), "0");
                    entityMap.put(head.id(), newEntity);
                    ens.add(newEntity);
                }


            }
            if (!visited.containsKey(tail.id())){
                visited.put(tail.id(), true);
//                Entity newEntity = new Entity(Long.toString(tail.id()), tail.get("name").asString(), tail.get("entityType").asString(), tail.get("entitySubClass").asString(), tail.get("graphSymbol").asString(), tail.get("sentence_original_id").asString(), "0");
//                entityMap.put(tail.id(), newEntity);
//                ens.add(newEntity);
                if(graphType.equals("raw")){
                    EntityRaw newEntity = new EntityRaw(Long.toString(tail.id()), tail.get("name").toString(), tail.get("entityType").asString(), tail.get("entitySubClass").asString(), tail.get("graphSymbol").asString(), tail.get("sentence_original_id").asString(), "0");
                    entityMap.put(tail.id(), newEntity);
                    ens.add(newEntity);
                }
                if(graphType.equals("zonghe")){
                    EntityRaw newEntity = new EntityRaw(Long.toString(tail.id()), tail.get("name").toString(), tail.get("entityType").asString(), tail.get("entitySubClass").asString(), tail.get("graphSymbol").asString(), tail.get("ItemId").asString(), "0");
                    entityMap.put(tail.id(), newEntity);
                    ens.add(newEntity);
                }
                if(graphType.equals("concept")){
                    EntityRaw newEntity = new EntityRaw(Long.toString(tail.id()), tail.get("name").toString(), tail.get("entityType").asString(), tail.get("entitySubClass").asString(), tail.get("graphSymbol").asString(), Long.toString(tail.get("item_id").asLong()), "0");
                    entityMap.put(tail.id(), newEntity);
                    ens.add(newEntity);
                }
            }
            if(tripleCount <= Integer.MAX_VALUE-1){
                triples.add(new Triple(Integer.toString(tripleCount++),entityMap.get(head.id()), relation, entityMap.get(tail.id())));
            }else{
                break;
            }
            // add edges and directions
            if (!edges.containsKey(entityMap.get(head.id()))){
                edges.put(entityMap.get(head.id()), new ArrayList<>());
                directions.put(entityMap.get(head.id()), new ArrayList<>());
            }
            if (!edges.containsKey(entityMap.get(tail.id()))){
                edges.put(entityMap.get(tail.id()), new ArrayList<>());
                directions.put(entityMap.get(tail.id()), new ArrayList<>());
            }
            edges.get(entityMap.get(head.id())).add(entityMap.get(tail.id()));
            directions.get(entityMap.get(head.id())).add(0);
            edges.get(entityMap.get(tail.id())).add(entityMap.get(head.id()));
            directions.get(entityMap.get(tail.id())).add(1);

        }
        res.setEntities(new ArrayList<>(ens));
        res.setTriples(new ArrayList<>(triples));
        res.setEdges(edges);
        res.setDirections(directions);
        return res;
    }

    public static KG readGraph2(List<Record> records ){
        KG res = new KG();
        if(records.size() == 0) return res;
        List<EntityRaw> ens = new ArrayList<>();  // 这个结合中排除了孤立节点，正常图谱中不应该有孤立节点
        List<Triple> triples = new ArrayList<>();
        Map<Long, Boolean> visited = new HashMap<>(); // 判断是否需要创建新的实体，根据id，可唯一标识
        Map<Long, EntityRaw> entityMap = new HashMap<>(); // 方便根据id找到entity，name相同的节点后续会合并
        Map<EntityRaw, List<EntityRaw>> edges = new LinkedHashMap<>();
        Map<EntityRaw, List<Integer>> directions = new LinkedHashMap<>();

//        List<Object> headItemids = new ArrayList<>();
//
//        List<Object> tailItemids = new ArrayList<>();
        int tripleCount = 0;
        for (Record record: records){
            if (record.containsKey("n")){
                Node node = record.get("n").asNode();
                if (!visited.containsKey(node.id())){
                    visited.put(node.id(), true);


                    Value itemss = node.get("itemIds");
                    String typena = "";
                    if(itemss.type().name().equals("STRING")){
                        typena = itemss.asString();

                    }
                    if(itemss.type().name().equals("LIST")){
                        typena = itemss.asList().toString();
                        System.out.println(typena);
                    }


                    if(Long.toString(node.id()).equals("6285")){

                        System.out.println(typena);
                    }

                    EntityRaw entity = new EntityRaw(Long.toString(node.id()), node.get("name").toString(), node.get("entityType").asString(), node.get("entitySubClass").asString(), node.get("graphSymbol").asString(), typena, "0");
                    entityMap.put(node.id(), entity);
                    ens.add(entity);

                   // headItemids.addAll(node.get("itemIds").asList());


                    continue;
                }
            }
            Node head = record.get("n1").asNode();
            Node tail = record.get("n2").asNode();
            String relation = record.get("rel").asString();

            if (!visited.containsKey(head.id())){
                visited.put(head.id(), true);
                    Value itemss = head.get("itemIds");
                    String typena = "";
                    if(itemss.type().name().equals("STRING")){
                        typena = itemss.asString();

                    }
                    if(itemss.type().name().equals("LIST")){
                        typena = itemss.asList().toString();
                        System.out.println(typena);
                    }
                if(Long.toString(head.id()).equals("6285")){

                    System.out.println(typena);
                }

                    EntityRaw newEntity = new EntityRaw(Long.toString(head.id()), head.get("name").toString(), head.get("entityType").toString(), head.get("entitySubClass").toString(), head.get("graphSymbol").toString(),typena, "0");
                    entityMap.put(head.id(), newEntity);
                    ens.add(newEntity);

                    //headItemids.addAll(head.get("itemIds").asList());


            }
            if (!visited.containsKey(tail.id())){
                visited.put(tail.id(), true);
//                Entity newEntity = new Entity(Long.toString(tail.id()), tail.get("name").asString(), tail.get("entityType").asString(), tail.get("entitySubClass").asString(), tail.get("graphSymbol").asString(), tail.get("sentence_original_id").asString(), "0");
//                entityMap.put(tail.id(), newEntity);
//                ens.add(newEntity);
                Value itemss = tail.get("itemIds");
                String typena = "";
                if(itemss.type().name().equals("STRING")){
                    typena = itemss.asString();

                }
                if(itemss.type().name().equals("LIST")){
                    typena = itemss.asList().toString();
                    System.out.println(typena);
                }
                if(Long.toString(tail.id()).equals("6285")){

                    System.out.println(typena);
                }
                    EntityRaw newEntity = new EntityRaw(Long.toString(tail.id()), tail.get("name").toString(), tail.get("entityType").toString(), tail.get("entitySubClass").asString(), tail.get("graphSymbol").asString(), typena, "0");
                    entityMap.put(tail.id(), newEntity);
                    ens.add(newEntity);
                    //headItemids.addAll(tail.get("itemIds").asList());
            }
            if(tripleCount <= Integer.MAX_VALUE-1){
                triples.add(new Triple(Integer.toString(tripleCount++),entityMap.get(head.id()), relation, entityMap.get(tail.id())));
            }else{
                break;
            }
            // add edges and directions
            if (!edges.containsKey(entityMap.get(head.id()))){
                edges.put(entityMap.get(head.id()), new ArrayList<>());
                directions.put(entityMap.get(head.id()), new ArrayList<>());
            }
            if (!edges.containsKey(entityMap.get(tail.id()))){
                edges.put(entityMap.get(tail.id()), new ArrayList<>());
                directions.put(entityMap.get(tail.id()), new ArrayList<>());
            }
            edges.get(entityMap.get(head.id())).add(entityMap.get(tail.id()));
            directions.get(entityMap.get(head.id())).add(0);
            edges.get(entityMap.get(tail.id())).add(entityMap.get(head.id()));
            directions.get(entityMap.get(tail.id())).add(1);

        }
        res.setEntities(new ArrayList<>(ens));
        res.setTriples(new ArrayList<>(triples));
        res.setEdges(edges);
        res.setDirections(directions);
        return res;
    }

    public static Map<Long, List<RelationEasy>> readNodeRel(List<Record> records){
        Map<Long, List<RelationEasy>> res = new LinkedHashMap<>();

        if(records.size() == 0) return res;
        Map<Long,RelationEasy> visitedRel = new LinkedHashMap<>();// 判断是否需要创建新的关系对象，根据id，可唯一标识

        for (Record record: records){
            Node head = record.get("n1").asNode();
            Node tail = record.get("n2").asNode();
            Relationship relation = record.get("r").asRelationship();


            if (!visitedRel.containsKey(relation.id())){

                RelationEasy re = new RelationEasy(relation.id(), relation.get("name").asString());
                if(relation.get("relationType")!=null && !relation.get("relationType").toString().equals("NULL")){
                    re.setRalationType(relation.get("relationType").asString());
                }
                if(relation.get("ontologySymbol")!=null && !relation.get("ontologySymbol").toString().equals("NULL")){
                    re.setOntologySymbol(relation.get("ontologySymbol").asString());
                }
                if(relation.get("relationSubType")!=null && !relation.get("relationSubType").toString().equals("NULL")){
                    re.setRelationSubType(relation.get("relationSubType").asString());
                }
                if(relation.get("subOntologySymbol")!=null && !relation.get("subOntologySymbol").toString().equals("NULL")){
                    re.setSubOntologySymbol(relation.get("subOntologySymbol").asString());
                }

                List<RelationEasy> temp_list_h = res.getOrDefault(head.id(), new ArrayList<RelationEasy>());
                if(!temp_list_h.contains(re)){
                    temp_list_h.add(re);
                }
                res.put(head.id(),temp_list_h);

                List<RelationEasy> temp_list_t = res.getOrDefault(tail.id(), new ArrayList<RelationEasy>());
                if(!temp_list_t.contains(re)){
                    temp_list_t.add(re);
                }
                res.put(tail.id(),temp_list_t);

                visitedRel.put(relation.id(),re);

            }else {
                RelationEasy relationEasy_old = visitedRel.get(relation.id());
                List<RelationEasy> temp_list_h = res.getOrDefault(head.id(), new ArrayList<RelationEasy>());
                if(!temp_list_h.contains(relationEasy_old)){
                    temp_list_h.add(relationEasy_old);
                }
                res.put(head.id(),temp_list_h);

                List<RelationEasy> temp_list_t = res.getOrDefault(tail.id(), new ArrayList<RelationEasy>());
                if(!temp_list_t.contains(relationEasy_old)){
                    temp_list_t.add(relationEasy_old);
                }
                res.put(tail.id(),temp_list_t);



            }


        }
        return res;
    }

    public static void main(String[] args) {
//        KG kg = GraphReader.readGraph(GraphReader.query("SOA服务基础设施", "虚拟化平台", false));
//        System.out.println();
    }
}   
