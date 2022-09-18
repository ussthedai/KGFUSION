package com.usst.kgfusion.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Path;
import org.neo4j.driver.v1.types.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.usst.kgfusion.databaseQuery.BasicOperation;
import com.usst.kgfusion.databaseQuery.KGASSETOperation;
import com.usst.kgfusion.pojo.LevelAmbiguTaskRecord;
import com.usst.kgfusion.pojo.LevelAmbiguTaskRes;
import com.usst.kgfusion.pojo.Res;

/**
 * 层级关系歧义分析
 */

//@RestController
//@RequestMapping("/api/graph/evolution")
//public class LevelAmbigu {
//
//
//    @PostMapping("/ttt")
//    public void ttt() throws Exception {
//        long from = 7729;
//        long to = 7723;
//        List<Path> paths = BasicOperation.findAllPathsBetweenTwoNodesWithOneRelType(from, to, "KNOWS");
//        System.out.println("done");
//    }
//
//    @PostMapping("/levelambigu")
//    public String process(@RequestBody String jsonStr){
//        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
//
//        // 参数校验
//        if(!jsonObject.containsKey("graphSymbol") ){
//            return JSON.toJSONString(new Res(false, 1002, "参数错误", null));
//        }
//        String graphSymbol = jsonObject.getString("graphSymbol");
//        if(graphSymbol == null || graphSymbol.equals("")){
//            return JSON.toJSONString(new Res(false, 1002, "graphSymbol不能为空", null));
//        }
//        Node node = BasicOperation.getRandomNode(graphSymbol);
//        if (node == null) {
//            return JSON.toJSONString(new Res(false, 1007, "该图无数据，请检查graphSymbol对应的图是否真实存在", null));
//        }
//
//        // 查询该图的所有节点和所有的关系种类
//        List<Long> ids = BasicOperation.getAllNodeIds(graphSymbol);
//        List<String> relTypes = BasicOperation.getAllRelTypes(graphSymbol);
//
//
//        // 处理
//        String task_id = KGASSETOperation.queryTaskId(graphSymbol);
//        boolean task_id_exist = true;
//        if(task_id == null || task_id.equals("")){
//            task_id_exist = false;
//            task_id = UUID.randomUUID().toString();
//        }
//
//
//        Timestamp start = new Timestamp(System.currentTimeMillis());
//        List<LevelAmbiguTaskRes> LevelAmbiguTaskResults = new java.util.ArrayList<LevelAmbiguTaskRes>();
//        Set<Long> ambigusRs = new HashSet<>();  // 歧义关系id集合
//        Set<Long> rec_ambigusRs = new HashSet<>(); // 推荐消除的歧义关系id集合
//        Set<Long> related_ambiguity_nodeids = new HashSet<>(); // 关联歧义节点id集合
//        Set<String> related_ambiguity_names = new HashSet<>();
//
//        for(String relType : relTypes){
//            for(Long from : ids){
//                for(Long to : ids){
//                    int maxDepth = -1;
//                    ambigusRs.clear();
//                    rec_ambigusRs.clear();
//                    related_ambiguity_nodeids.clear();
//                    related_ambiguity_names.clear();
//
//                    // 环时直接退出
//                    if(from.equals(to) || from == to){
//                        continue;
//                    }
//
//                    List<Path> paths = BasicOperation.findAllPathsBetweenTwoNodesWithOneRelType(from, to, relType);
//
//                    // 没有路径或只有一条路径时直接退出，这是不可能歧义
//                    if(paths.size() == 0 || paths.size() == 1){
//                        continue;
//                    }
//
//                    // !大于等于两条路径时, 《并且长短不一时》，一定是有歧义的
//                    // 检查所有路径长度是否一致，一致的时候退出
//                    boolean flag = true;
//                    int pathLength = paths.get(0).length();
//                    for(Path path : paths){
//                        if(path.length() != pathLength){
//                            flag = false;
//                            break;
//                        }
//                    }
//                    if(flag){  // 所有路径长度一致，直接退出
//                        continue;
//                    }
//
//                    // 路径长度不一致，必定有歧义，这里选取最长的路径作为正确路径,最小的路径作为歧义路径
//                    int minPathLength = paths.get(0).length();
//                    int maxPathLength = paths.get(0).length();
//                    Path minPath = paths.get(0);
//                    Path maxPath = paths.get(0);
//                    for(Path path : paths){
//                        if(path.length() < minPathLength){
//                            minPathLength = path.length();
//                            minPath = path;
//                        }
//                        if(path.length() > maxPathLength){
//                            maxPathLength = path.length();
//                            maxPath = path;
//                        }
//                    }
//
//                    // 填写结果
//                    Iterator<Relationship> min_rel_iter = minPath.relationships().iterator();
//                    while(min_rel_iter.hasNext()){
//                        Relationship relation = min_rel_iter.next();
//                        ambigusRs.add(relation.id());
//                        rec_ambigusRs.add(relation.id());
//                        related_ambiguity_nodeids.add(relation.startNodeId());
//                        related_ambiguity_nodeids.add(relation.endNodeId());
//                    }
//                    Iterator<Relationship> max_rel_iter = maxPath.relationships().iterator();
//                    while(max_rel_iter.hasNext()){
//                        Relationship relation = max_rel_iter.next();
//                        ambigusRs.add(relation.id());
//                        related_ambiguity_nodeids.add(relation.startNodeId());
//                        related_ambiguity_nodeids.add(relation.endNodeId());
//                    }
//                    Iterator<Node> min_node_iter = minPath.nodes().iterator();
//                    while(min_node_iter.hasNext()){
//                        Node n = min_node_iter.next();
//                        if(n.get("name") != null){
//                            related_ambiguity_names.add(n.get("name").toString());
//                        }
//                    }
//                    Iterator<Node> max_node_iter = maxPath.nodes().iterator();
//                    while(max_node_iter.hasNext()){
//                        Node n = max_node_iter.next();
//                        if(n.get("name") != null){
//                            related_ambiguity_names.add(n.get("name").toString());
//                        }
//                    }
//                    maxDepth = maxPathLength;
//                    // 填写结果
//                    String related_ambiguity_nodeids_str = "";
//                    for(Long id : related_ambiguity_nodeids){
//                        related_ambiguity_nodeids_str += id + ",";
//                    }
//                    related_ambiguity_nodeids_str = related_ambiguity_nodeids_str.substring(0, related_ambiguity_nodeids_str.length() - 1);
//                    String related_ambiguity_names_str = "";
//                    for(String name : related_ambiguity_names){
//                        related_ambiguity_names_str += name + ",";
//                    }
//                    related_ambiguity_names_str = related_ambiguity_names_str.substring(0, related_ambiguity_names_str.length() - 1);
//                    String ambigusRs_str = "";
//                    for(Long id : ambigusRs){
//                        ambigusRs_str += id + ",";
//                    }
//                    ambigusRs_str = ambigusRs_str.substring(0, ambigusRs_str.length() - 1);
//                    String rec_ambigusRs_str = "";
//                    for(Long id : rec_ambigusRs){
//                        rec_ambigusRs_str += id + ",";
//                    }
//                    rec_ambigusRs_str = rec_ambigusRs_str.substring(0, rec_ambigusRs_str.length() - 1);
//                    LevelAmbiguTaskRes LevelAmbiguTaskRes = new LevelAmbiguTaskRes(UUID.randomUUID().toString(), task_id, new Timestamp(System.currentTimeMillis()).toString(),0, new Timestamp(System.currentTimeMillis()), 2, 4, related_ambiguity_nodeids.size(), related_ambiguity_nodeids_str, related_ambiguity_names_str, maxDepth, ambigusRs_str, rec_ambigusRs_str);
//                    LevelAmbiguTaskResults.add(LevelAmbiguTaskRes);
//                }
//            }
//        }
//
//        Timestamp end = new Timestamp(System.currentTimeMillis());
//        LevelAmbiguTaskRecord LevelAmbiguTaskRecord = new LevelAmbiguTaskRecord(task_id, graphSymbol, 1, 2, 2, "分析完成", start, end);
//        if(task_id_exist){
//            // 更新任务记录
//            KGASSETOperation.updateLevelAmbiguTaskRecord(LevelAmbiguTaskRecord);
//        }else{
//            // 插入任务记录
//            KGASSETOperation.insertLevelAmbiguTaskRecord(LevelAmbiguTaskRecord);
//        }
//
//        // 批量插入所有任务结果
//        KGASSETOperation.insertLevelAmbiguTaskResults(LevelAmbiguTaskResults);
//
//        return JSON.toJSONString(new Res(true, 200, "分析成功", null));
//    }
//
//}


/**
 * 层级关系歧义分析
 */

@RestController
@EnableAsync
@RequestMapping("/api/graph/evolution")
public class
LevelAmbigu {

    @Autowired
    private HeXiaoqi heXiaoqi;


    // @PostMapping("/ttt")
    // public void ttt() throws Exception {
    //     long from = 7729;
    //     long to = 7723;
    //     List<Path> paths = BasicOperation.findAllPathsBetweenTwoNodesWithOneRelType(from, to, "KNOWS");
    //     System.out.println("done");
    // }

    @PostMapping("/levelambigu")
    public String process(@RequestBody String jsonStr){
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);

        // 参数校验
        if(!jsonObject.containsKey("graphSymbol") ){
            return JSON.toJSONString(new Res(false, 1002, "参数错误", null));
        }
        String graphSymbol = jsonObject.getString("graphSymbol");
        if(graphSymbol == null || graphSymbol.equals("")){
            return JSON.toJSONString(new Res(false, 1002, "graphSymbol不能为空", null));
        }
        Node node = BasicOperation.getRandomNode(graphSymbol);
        if (node == null) {
            return JSON.toJSONString(new Res(false, 1007, "该图无数据，请检查graphSymbol对应的图是否真实存在", null));
        }

        // fenxi
        heXiaoqi.fenxi(graphSymbol);

        // return JSON.toJSONString(new Res(true, 200, "分析成功", null));
        return JSON.toJSONString(new Res(true, 200, "层级消歧已接受请求,分析中...", null));

    }

}


@Service
class HeXiaoqi{
    private static final Logger logger = LoggerFactory.getLogger("com.usst.test");
    @Async
    public void fenxi(String graphSymbol){
        logger.info("层级消歧已接收参数,图谱symbol:" + graphSymbol);
        // 查询该图的所有节点和所有的关系种类
        List<Long> ids = BasicOperation.getAllNodeIds(graphSymbol);
        List<String> relTypes = BasicOperation.getAllRelTypes(graphSymbol);


        // 处理
        String task_id = KGASSETOperation.queryTaskId(graphSymbol);
        boolean task_id_exist = true;
        if(task_id == null || task_id.equals("")){
            task_id_exist = false;
            task_id = UUID.randomUUID().toString();
        }


        Timestamp start = new Timestamp(System.currentTimeMillis());
        List<LevelAmbiguTaskRes> LevelAmbiguTaskResults = new java.util.ArrayList<LevelAmbiguTaskRes>();
        Set<String> ambigusRs = new LinkedHashSet<>();  // 歧义关系id集合
        List<Long> rec_ambigusRs = new ArrayList<>(); // 推荐消除的歧义关系id集合
        Set<Long> related_ambiguity_nodeids = new HashSet<>(); // 关联歧义节点id集合
        Set<String> related_ambiguity_names = new HashSet<>();
        Map<Long, String> id2name = new HashMap<>();

        for(String relType : relTypes){
            for(Long from : ids){
                for(Long to : ids){
                    int maxDepth = -1;
                    ambigusRs.clear();
                    rec_ambigusRs.clear();
                    related_ambiguity_nodeids.clear();
                    related_ambiguity_names.clear();

                    // 环时直接退出
                    if(from.equals(to) || from == to){
                        continue;
                    }

                    List<Path> paths = BasicOperation.findAllPathsBetweenTwoNodesWithOneRelType(from, to, relType);

                    // 没有路径或只有一条路径时直接退出，这是不可能歧义
                    if(paths.size() == 0 || paths.size() == 1){
                        continue;
                    }

                    // !大于等于两条路径时, 《并且长短不一时》，一定是有歧义的
                    // 检查所有路径长度是否一致，一致的时候退出
                    boolean flag = true;
                    int pathLength = paths.get(0).length();
                    for(Path path : paths){
                        if(path.length() != pathLength){
                            flag = false;
                            break;
                        }
                    }
                    if(flag){  // 所有路径长度一致，直接退出
                        continue;
                    }

                    // 路径长度不一致，必定有歧义，这里选取最长的路径作为正确路径,最小的路径作为歧义路径
                    int minPathLength = paths.get(0).length();
                    int maxPathLength = paths.get(0).length();
                    Path minPath = paths.get(0);
                    Path maxPath = paths.get(0);
                    for(Path path : paths){
                        if(path.length() < minPathLength){
                            minPathLength = path.length();
                            minPath = path;
                        }
                        if(path.length() > maxPathLength){
                            maxPathLength = path.length();
                            maxPath = path;
                        }
                    }

                    // 填写结果
                    Iterator<Node> min_node_iter = minPath.nodes().iterator();
                    while(min_node_iter.hasNext()){
                        Node n = min_node_iter.next();
                        if(n.get("name") != null){

                            related_ambiguity_names.add(n.id() + ":" + n.get("name").toString().substring(1, n.get("name").toString().length() - 1));
                            id2name.put(n.id(), n.get("name").toString().substring(1, n.get("name").toString().length() - 1));
                        }
                    }
                    Iterator<Node> max_node_iter = maxPath.nodes().iterator();
                    while(max_node_iter.hasNext()){
                        Node n = max_node_iter.next();
                        if(n.get("name") != null){
                            related_ambiguity_names.add(n.id() + ":" + n.get("name").toString().substring(1, n.get("name").toString().length() - 1));
                            id2name.put(n.id(), n.get("name").toString().substring(1, n.get("name").toString().length() - 1));
                        }
                    }

                    Iterator<Relationship> max_rel_iter = maxPath.relationships().iterator();
                    while(max_rel_iter.hasNext()){
                        Relationship relation = max_rel_iter.next();
                        Long id = relation.id();
                        Long startId = relation.startNodeId();
                        Long endId = relation.endNodeId();
                        String relationName = "";
                        if(relation.asMap().containsKey("name")){
                            relationName = relation.get("name").toString().substring(1, relation.get("name").toString().length() - 1);
                        }

                        if(id2name.containsKey(startId) && id2name.containsKey(endId) && !relationName.equals("")){
                            String startName = id2name.get(startId);
                            String endName = id2name.get(endId);
                            ambigusRs.add(id + ":" + startName + ":" + relationName + ":" + endName);
                        }
                        // ambigusRs.add(relation.id());
                        // related_ambiguity_nodeids.add(relation.startNodeId());
                        // related_ambiguity_nodeids.add(relation.endNodeId());
                    }

                    Iterator<Relationship> min_rel_iter = minPath.relationships().iterator();
                    while(min_rel_iter.hasNext()){
                        Relationship relation = min_rel_iter.next();
                        Long id = relation.id();
                        Long startId = relation.startNodeId();
                        Long endId = relation.endNodeId();
                        String relationName = "";
                        if(relation.asMap().containsKey("name")){
                            relationName = relation.get("name").toString().substring(1, relation.get("name").toString().length() - 1);
                        }

                        if(id2name.containsKey(startId) && id2name.containsKey(endId) && !relationName.equals("")){
                            String startName = id2name.get(startId);
                            String endName = id2name.get(endId);
                            ambigusRs.add(id + ":" + startName + ":" + relationName + ":" + endName);
                        }

                        rec_ambigusRs.add(relation.id());
                        // related_ambiguity_nodeids.add(relation.startNodeId());
                        // related_ambiguity_nodeids.add(relation.endNodeId());
                    }
                    
                    // List<String> ambiguity_res = new ArrayList<>(ambigusRs);
                    // ambiguity_res.remove(0);
                    maxDepth = maxPathLength;
                    // rec_ambigusRs.remove(0);

                    // 填写结果
                    // String related_ambiguity_nodeids_str = "";
                    // for(Long id : related_ambiguity_nodeids){
                    //     related_ambiguity_nodeids_str += id + ";";
                    // }
                    // related_ambiguity_nodeids_str = related_ambiguity_nodeids_str.substring(0, related_ambiguity_nodeids_str.length() - 1);
                    String related_ambiguity_names_str = "";
                    for(String name : related_ambiguity_names){
                        related_ambiguity_names_str += name + ";";
                    }
                    // related_ambiguity_names_str = related_ambiguity_names_str.substring(0, related_ambiguity_names_str.length() - 1);
                    String ambigusRs_str = "";
                    for(String item : ambigusRs){
                        ambigusRs_str += item + ";";
                    }
                    // ambigusRs_str = ambigusRs_str.substring(0, ambigusRs_str.length() - 1);
                    String rec_ambigusRs_str = "";
                    for(Long id : rec_ambigusRs){
                        rec_ambigusRs_str += id + ";";
                    }
                    // rec_ambigusRs_str = rec_ambigusRs_str.substring(0, rec_ambigusRs_str.length() - 1);
                    if(!ambigusRs_str.equals("") && ambigusRs_str.length() > 0 && !rec_ambigusRs_str.equals("") && rec_ambigusRs_str.length() > 0){
                        // check result according ambigusRs_str, relation name need unique
                        Set<String> checklist = new HashSet<>();
                        String[] strs = ambigusRs_str.split(";");
                        for(String s : strs){
                            checklist.add(s.split(":")[2]); // obtain relation name
                        }
                        
                        if(checklist.size() == 1){  
                            LevelAmbiguTaskRes LevelAmbiguTaskRes = new LevelAmbiguTaskRes(UUID.randomUUID().toString(), task_id, new Timestamp(System.currentTimeMillis()).toString(),0, new Timestamp(System.currentTimeMillis()), 2, 4, related_ambiguity_names.size(), related_ambiguity_names_str, "", maxDepth, ambigusRs_str, rec_ambigusRs_str);
                            LevelAmbiguTaskResults.add(LevelAmbiguTaskRes);
                        }
                        
                    }


                }
            }
        }

        Timestamp end = new Timestamp(System.currentTimeMillis());
        LevelAmbiguTaskRecord LevelAmbiguTaskRecord = new LevelAmbiguTaskRecord(task_id, graphSymbol, 1, 2, 2, "分析完成", start, end);
        if(task_id_exist){
            // 更新任务记录
            logger.info("该图层级分析任务已存在，现执行更新操作");
            KGASSETOperation.updateLevelAmbiguTaskRecord(LevelAmbiguTaskRecord);
        }else{
            // 插入任务记录
            logger.info("插入新的任务记录");
            KGASSETOperation.insertLevelAmbiguTaskRecord(LevelAmbiguTaskRecord);
        }
        logger.info("层级分析任务记录已入库");

        // 批量插入所有任务结果
        KGASSETOperation.insertLevelAmbiguTaskResults(LevelAmbiguTaskResults);
        logger.info("层级分析结果已入库");
    }
}






