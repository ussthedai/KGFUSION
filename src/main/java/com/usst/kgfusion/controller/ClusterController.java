package com.usst.kgfusion.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.usst.kgfusion.databaseQuery.BasicOperation;
import org.neo4j.driver.v1.types.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.usst.kgfusion.databaseQuery.ItemQuery;
import com.usst.kgfusion.entrance.MergeConceptInfo;
import com.usst.kgfusion.pojo.EntityEasy;
import com.usst.kgfusion.pojo.Res;
import com.usst.kgfusion.util.Algorithm;
import com.usst.kgfusion.util.JsonUtil;

/**
 * @program: KGFusion
 * @description: 聚类分析
 * @author: JH_D
 * @create: 2022-07-02 15:21
 **/

@RestController
@RequestMapping("/api/graph/evolution")
public class ClusterController {
    private static final Logger logger = LoggerFactory.getLogger("com.usst.test");

    // 静态标注数据
    private static Map<String, Set<String>> labeled_data; // 类型： 包含的实体列表
    private static Map<String, Object> seeds; // 实体：所属类型
    private static Set<String> exist_types;
    private static Map<String, String> relation_types; // 同种类型之间的关系

    public ClusterController() throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = getClass().getClassLoader().getResourceAsStream("static/seed.json");
            String jsonStr = JsonUtil.readJsonStr(inputStream);
            seeds = JSON.parseObject(jsonStr);
            // inver_table
            labeled_data = new HashMap<>();
            for (Entry<String, Object> entry : seeds.entrySet()) {
                String key = entry.getKey();
                String value = (String) entry.getValue();
                if (labeled_data.containsKey(value)) {
                    labeled_data.get(value).add(key);
                } else {
                    Set<String> set = new HashSet<>();
                    set.add(key);
                    labeled_data.put(value, set);
                }
            }
        } catch (IOException e) {
            logger.error("seed.json文件读取失败");
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }

        exist_types = labeled_data.keySet();

        // 固定的关系模式
        relation_types = new HashMap<>();
        // relation_types.put("功能", "流程");
        relation_types.put("功能", "组成"); // 还是组成更合适一些
        relation_types.put("系统", "组成");
        relation_types.put("数据", "组成");
        relation_types.put("组织", "组成");

    }

    public double sim_score(String name, String type) {
        if (!exist_types.contains(type))
            return Algorithm.levenshtein(name, type); // 该类型没被标注过，计算相似度
        if (labeled_data.get(type).contains(name))
            return 1.0; // 直接就是了
        Set<String> ens = labeled_data.get(type); // 模糊计算
        double sum_score = 0.0;
        for (String en : ens) {
            sum_score += Algorithm.levenshtein(name, en);
        }
        return sum_score / ens.size(); // 取平均值更合理一些，不然会存在类型优势的问题
    }

    /**
     * @description: 聚类分析
     * @param: 实体集合
     * @return: 推荐的新节点结合，以及对应的聚类结果
     * @throws IOException
     */
    public List<Map<List<String>, List<EntityEasy>>> makeClusters(List<EntityEasy> ens, int maxCategory,
            Map<String, EntityEasy> name_entity, Map<EntityEasy, String> entity_name) throws IOException {
        // given entity names, cluster them
        List<String> query_names = new ArrayList<>();
        for (EntityEasy en : ens) {
            query_names.add(entity_name.get(en));
        }

        // type choices
        Map<String, String> typeParentMap = new HashMap<>();
        BufferedReader br = null;
        try {
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("static/c_info.csv");
            br = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] strs = line.split(",");
                String child = strs[0];
                String parent = strs[2];
                if (typeParentMap == null) {
                    typeParentMap = new HashMap<>();
                }
                typeParentMap.put(child, parent);
            }
        } catch (IOException e) {
            logger.error("c_info.csv文件读取失败");
        } finally {
            if (br != null) {
                br.close();
            }
        }
        Set<String> choices = typeParentMap.keySet();

        // ! cluster, compute relevanct score between each entity and type
        Map<String, Map<String, Double>> score_map = new HashMap<>(); // ? key: type, vlaue : each entiy score
        for (String type : choices) {
            Map<String, Double> type_score = new HashMap<>();
            // ?类似管理这种词匹配的概率较高，但是其它的一些词效果就比较差，考虑修改score计算方法
            // ?维护已标注类型实体倒排表，实体和类型之间的相似度等于实体和类型拥有的所有实体词的相似度之和
            for (String name : query_names) {
                // double score = Algorithm.levenshtein(name, type); // 这种方法效果不太好
                double score = sim_score(name, type); // 效果可能好一点但是效率会下降，实际上效果也一般，但鲁棒性更好
                type_score.put(name, score);
            }
            score_map.put(type, type_score);
        }
        // 计算每个类型的总分
        Map<String, Double> type_score_sum = new HashMap<>();
        for (String type : choices) {
            double sum = 0;
            for (String name : query_names) {
                sum += score_map.get(type).get(name);
            }
            type_score_sum.put(type, sum / score_map.get(type).size());
        }
        // type_score_sum 按照value排序
        List<Entry<String, Double>> type_score_sum_list = new ArrayList<>(type_score_sum.entrySet());
        type_score_sum_list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        // 取前maxCategory个类型
        List<String> type_list = new ArrayList<>();
        for (int i = 0; i < maxCategory; i++) {
            type_list.add(type_score_sum_list.get(i).getKey());
        }
        // 将所有实体按照得分大小归到类型中
        Map<String, List<String>> cluster_en = new HashMap<>();
        for (String name : query_names) {
            double max_score = 0;
            String max_type = "";
            // !这个地方会出现和所有类型的相似度都为0，导致一部分实体无法归类
            for (String type : type_list) {
                double score = score_map.get(type).get(name);
                if (score > max_score) {
                    max_score = score;
                    max_type = type;
                }
            }

            // 无法归类的实体处理，比较hashcode的距离，距离越近，越可能归类到同一类型中
            if (max_score == 0.0 || max_type.equals("")) {
                long hashcode = name.hashCode();
                long min_val = Long.MAX_VALUE;
                for (String type : type_list) {
                    long val = Math.abs(hashcode - type.hashCode());
                    if (val < min_val) {
                        min_val = val;
                        max_type = type;
                    }
                }

            }

            if (cluster_en.get(max_type) == null) {
                cluster_en.put(max_type, new ArrayList<>());
            }
            cluster_en.get(max_type).add(name);
        }

        // ?对每个聚类结果的type 扩展出2个类型，采用类型相似度
        Map<String, List<String>> expand_type = new HashMap<>();
        Map<String, Map<String, Double>> type_sim = new HashMap<>();
        for (String cur_type : type_list) {
            Map<String, Double> sim_map = new HashMap<>();
            for (String type : choices) {
                double sim = Algorithm.levenshtein(cur_type, type);
                sim_map.put(type, sim);
            }
            type_sim.put(cur_type, sim_map);
        }
        for (String cur_type : type_list) {
            Map<String, Double> sim_map = type_sim.get(cur_type);
            // 排序
            List<Entry<String, Double>> sim_list = new ArrayList<>(sim_map.entrySet());
            sim_list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
            // 取除自己的前2个类型
            List<String> sim_type_list = new ArrayList<>();
            if (sim_list.size() > 3) {
                for (int i = 1; i < 3; i++) {
                    sim_type_list.add(sim_list.get(i).getKey());
                }
            } else if (sim_list.size() > 1) {
                for (int i = 1; i < sim_list.size(); i++) {
                    sim_type_list.add(sim_list.get(i).getKey());
                }
            }
            expand_type.put(cur_type, sim_type_list);
        }

        // 组装聚类结果
        List<Map<List<String>, List<EntityEasy>>> res = new ArrayList<>();
        for (Entry<String, List<String>> entry : expand_type.entrySet()) {
            Map<List<String>, List<EntityEasy>> cls = new HashMap<>();
            // key
            List<String> types_cls = new ArrayList<>();
            String recommend_type = entry.getKey();
            List<String> exp_types = entry.getValue();
            types_cls.add(recommend_type);
            types_cls.addAll(exp_types);
            // vlaue
            List<String> ens_cls = cluster_en.get(recommend_type);

            // 有时该推荐类型下没有实体，导致该类型无法推荐，跳过
            if (ens_cls == null) {
                continue;
            }

            List<EntityEasy> ens_cls_easy = new ArrayList<>();
            for (String name : ens_cls) {
                ens_cls_easy.add(name_entity.get(name));
            }
            // compose
            cls.put(types_cls, ens_cls_easy);
            res.add(cls);
        }
        return res;
    }

    @PostMapping("/cluster")
    public String evolution_auto(@RequestBody String jsonStr) throws IOException{
        // input: graphSymbol, maxCategory
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        String graphSymbol = jsonObject.getString("graphSymbol");
        int maxCategory = jsonObject.getInteger("maxCategory");

        // 参数及异常情况校验
        if (!jsonObject.containsKey("graphSymbol") || !jsonObject.containsKey("maxCategory")) {
            return JSON.toJSONString(new Res(false, 1002, "参数缺失", null));
        }
        Node node = BasicOperation.getRandomNode(graphSymbol);
        if (node == null) {
            return JSON.toJSONString(new Res(false, 1002, "该图无数据，请检查graphSymbol对应的图是否真实存在", null));
        }

        // todo 利用ontologySymbol对指定graph进行初步分类
        List<String> ons = BasicOperation.queryDistinctProp(graphSymbol, "ontologySymbol");

        if (ons.get(0).equals("null")) { // ons.size()无法限定住
            return JSON.toJSONString(new Res(false, 1002, "该图缺少元模型信息，无法完成聚类", null));
        }

        Map<String, List<EntityEasy>> groups_onto = new HashMap<>();
        for (String on : ons) {
            List<EntityEasy> entities = BasicOperation.queryEntityEasy(graphSymbol, on);
            groups_onto.put(on, entities);
        }
        // ! 对entityType为空的实体进行重新标注，这里可以使用金融的接口也可以自己实现,简单的方法
        // mapping entity and name
        Map<String, EntityEasy> name_entity = new HashMap<>();
        Map<EntityEasy, String> entity_name = new HashMap<>();
        List<String> entitynamelist = new ArrayList<>();
        for (Entry<String, List<EntityEasy>> entry : groups_onto.entrySet()) {
            List<EntityEasy> entities = entry.getValue();
            for (EntityEasy entity : entities) {
                if (entity.getEntityType() == null || entity.getEntityType().equals("")) {
                    entitynamelist.add(entity.getName());
                }
                name_entity.put(entity.getName(), entity);
                entity_name.put(entity, entity.getName());
            }
        }
        // ? 使用金融的接口
        // Map<String, Object> SendEntityName = new HashMap<>();
        // SendEntityName.put("ins",entitynamelist);
        // String Typejson = RequestHelper.sendJsonWithHttp(url_Type,
        // JSON.toJSONString(SendEntityName));
        // Map maps = (Map)JSON.parse(Typejson);
        // JSONArray typelist = (JSONArray) maps.get("classify_name");
        // for(int i = 0; i < typelist.size(); i++) {
        // if(typelist.get(i).equals("none")){
        // EntityEasy en = name_entity.get(entitynamelist.get(i));
        // en.setEntityType("未识别");
        // }else{
        // JSONObject obj = (JSONObject) typelist.get(i);
        // String type = (String) obj.get("n.entityType");
        // EntityEasy en = name_entity.get(entitynamelist.get(i));
        // en.setEntityType(type);
        // }
        // }

        // ? 使用自己的方法，基于匹配
        Map<String, Set<String>> trigger_map = new HashMap<>();
        Set<String> system_trigger = new HashSet<>(Arrays.asList("系", "系统", "体", "体系"));
        Set<String> function_trigger = new HashSet<>(Arrays.asList("增", "增加", "删", "删除", "改", "改变", "修改", "查", "查询",
                "支撑", "采", "采集", "传", "同步", "存", "计算", "分析", "管理", "可视化", "共享"));
        Set<String> data_trigger = new HashSet<>(
                Arrays.asList("文本", "文件", "视频", "文档", "数据流", "摘要", "图片", "影片", "影", "图像", "流媒体"));
        Set<String> role_tirgger = new HashSet<>(Arrays.asList("人员", "者", "工人", "业务员", "管理员", "员", "客户端", "服务端", "对象"));
        Set<String> organize_tirgger = new HashSet<>(Arrays.asList("组织", "机构", "部门", "间", "组", "部"));

        Set<String> task_trigger = new HashSet<>(Arrays.asList("任务", "支线"));
        Set<String> model_tirgger = new HashSet<>(Arrays.asList("模型", "模板", "模型库", "模板库"));
        Set<String> product_trigger = new HashSet<>(Arrays.asList("制品", "产品", "产品线", "产品线库"));
        Set<String> workflow_tirgger = new HashSet<>(Arrays.asList("流程", "工作流", "工作流线", "工作流线库", "流水线", "流水线库"));
        Set<String> process_trigger = new HashSet<>(Arrays.asList("过程", "步骤"));
        Set<String> config_trigger = new HashSet<>(Arrays.asList("配置", "配"));
        Set<String> baseline_trigger = new HashSet<>(Arrays.asList("基线", "标准", "基准"));
        trigger_map.put("系统", system_trigger);
        trigger_map.put("功能", function_trigger);
        trigger_map.put("数据", data_trigger);
        trigger_map.put("角色", role_tirgger);
        trigger_map.put("组织", organize_tirgger);
        trigger_map.put("任务", task_trigger);
        trigger_map.put("模型", model_tirgger);
        trigger_map.put("制品", product_trigger);
        trigger_map.put("工作流", workflow_tirgger);
        trigger_map.put("过程", process_trigger);
        trigger_map.put("配置项", config_trigger);
        trigger_map.put("基线", baseline_trigger);
        for (Entry<String, List<EntityEasy>> entry : groups_onto.entrySet()) {
            List<EntityEasy> entities = entry.getValue();
            for (EntityEasy entity : entities) {
                String type = entity.getEntityType();
                if (type == null || type.equals("")) {
                    type = "未识别";
                }
                for (Entry<String, Set<String>> entry_trigger : trigger_map.entrySet()) {
                    if (entry_trigger.getValue().contains(type)) {
                        entity.setEntityType(entry_trigger.getKey()); // 存在匹配先后顺序导致的误差
                        break;
                    }
                }
            }
        }

        /*
         * all entity have already have entityType,
         * now group them, the num is num_ontologySymbol * entityType
         */
        Map<String, List<EntityEasy>> groups = new HashMap<>();
        String on = "";
        String tp = "";
        for (Entry<String, List<EntityEasy>> entry : groups_onto.entrySet()) {
            String group_key = "";
            on = entry.getKey();
            List<EntityEasy> entities = entry.getValue();
            for (EntityEasy entity : entities) {
                tp = entity.getEntityType();
                group_key = on + "-" + tp;
                if (groups.containsKey(group_key)) {
                    groups.get(group_key).add(entity);
                } else {
                    List<EntityEasy> new_list = new ArrayList<>();
                    new_list.add(entity);
                    groups.put(group_key, new_list);
                }
            }

        }

        // todo 对groups中的每一个group进行聚类
        // ? key: class, value: clusters, value-item: recommend newNodes, cluster
        Map<String, List<Map<List<String>, List<EntityEasy>>>> res = new HashMap<>();
        for (Entry<String, List<EntityEasy>> entry : groups.entrySet()) {
            // 这个类别下的所聚类公用信息
            String main_key = entry.getKey();
            String parent_type = entry.getKey().split("\\-")[1];
            List<EntityEasy> entities = entry.getValue();

            // 如果是非功能实体，那么该类无法聚类
            if (!main_key.contains("功能")) {
                List<Map<List<String>, List<EntityEasy>>> clusters = new ArrayList<>();
                Map<List<String>, List<EntityEasy>> one_cluster = new HashMap<>();
                one_cluster.put(new ArrayList<String>(Arrays.asList(parent_type)), entities);
                clusters.add(one_cluster);
                res.put(main_key, clusters);
                continue;
            }

            List<Map<List<String>, List<EntityEasy>>> clusters = makeClusters(entities, maxCategory, name_entity,
                    entity_name);
            res.put(main_key, clusters);
        }

        // todo 封装聚类结果
        String fix_relation = "组成";
        int type_count = 0;
        List<ClusterWrapperWrapper> cluster_wrappers = new ArrayList<>();
        for (Entry<String, List<Map<List<String>, List<EntityEasy>>>> entry : res.entrySet()) { // 根据typidx的聚类结果
            String main_type = entry.getKey();
            // 公用信息
            String ontologySymbol = main_type.split("\\-")[0];
            String entityType = main_type.split("\\-")[1];

            // 每个big cluster的信息
            ClusterWrapperWrapper cls_wwper = new ClusterWrapperWrapper();
            if(type_count <= Integer.MAX_VALUE -1){
                cls_wwper.setTypeIdx(type_count++);
            }else{
                break;
            }
            

            List<ClusterWrapper> cluster_list = new ArrayList<>();
            for (Map<List<String>, List<EntityEasy>> bigcluster : entry.getValue()) { // 每个小的cluster，包括sourceIds,summary...

                Entry<List<String>, List<EntityEasy>> entry_cluster = bigcluster.entrySet().iterator().next();
                List<String> recommend_nodes = entry_cluster.getKey();
                List<EntityEasy> entities = entry_cluster.getValue();

                // 公用信息
                String sourceIds = "";
                String summary = "";

                List<String> ids = new ArrayList<>();
                for (EntityEasy entity : entities) {
                    sourceIds += entity.getId() + ",";
                    ids.add(Long.toString(entity.getId()));
                }
                sourceIds = sourceIds.substring(0, sourceIds.length() - 1);
                Set<String> item_ids = BasicOperation.queryItemIdsByEntityIds(ids, "zonghe");
                if (item_ids.size() == 0) {
                    summary = "";
                }
                Map<String, String> item_from_zonghe = ItemQuery.queryItem(item_ids, "misre_km_wdtph_tmnr");
                Map<String, String> item_from_raw = ItemQuery.queryItem(item_ids, "misre_km_bz_bzjznr");
                Set<String> texts = new HashSet<>();
                if (item_from_zonghe.size() != 0) {
                    for (String text : item_from_zonghe.values()) {
                        texts.add(text);
                    }
                }
                if (item_from_raw.size() != 0) {
                    for (String text : item_from_raw.values()) {
                        texts.add(text);
                    }
                }
                if (texts.size() > 0) {
                    summary = MergeConceptInfo.aggText(new LinkedList<>(texts));
                }

                // ! 封装N和R, NR是一一对应的
                List<N> recNs = new ArrayList<>();
                List<R> recRs = new ArrayList<>();
                long count_index = 0;
                for (String newNode : recommend_nodes) {
                    N n = new N();
                    if(count_index <= Integer.MAX_VALUE-1){
                        n.setId(count_index++);
                    }else{
                        break;
                    }
                    
                    n.setName(newNode);
                    n.setEntityType(entityType);
                    n.setEntitySubClass(newNode);
                    n.setOntologySymbol(ontologySymbol);
                    n.setSubOntologySymbol(ontologySymbol);

                    if(count_index >= Integer.MIN_VALUE-1){
                        count_index--;
                    }else{
                        break;
                    }
                    

                    R r = new R();
                    if(count_index <= (Integer.MAX_VALUE-1)){
                        r.setId(count_index++);
                    }else{
                        break;
                    }
                    
                    r.setName(fix_relation);
                    r.setRelationType(fix_relation);
                    r.setRelationSubType("");
                    r.setOntologySymbol(ontologySymbol);
                    r.setSubOntologySymbol(ontologySymbol);

                    recNs.add(n);
                    recRs.add(r);
                }

                ClusterWrapper clusterWrapper = new ClusterWrapper(sourceIds, summary, recNs, recRs);
                // clusterWrapper.setSourceIds(sourceIds);
                // clusterWrapper.setSummary(summary);
                // clusterWrapper.setRecommendNs(recNs);
                // clusterWrapper.setRecommendRs(recRs);

                cluster_list.add(clusterWrapper);
            }
            cls_wwper.setClusters(cluster_list);

            cluster_wrappers.add(cls_wwper);
        }

        RecWrapper recWrapper = new RecWrapper();
        recWrapper.setGraphSymbol(graphSymbol);
        recWrapper.setClustersRes(cluster_wrappers);

        ResClusterWrapper resClusterWrapper = new ResClusterWrapper();
        resClusterWrapper.setSuccess(true);
        resClusterWrapper.setCode(200);
        resClusterWrapper.setMsg("操作成功");
        resClusterWrapper.setData(recWrapper);

        // return resClusterWrapper as json format, by field order
        return JSON.toJSONString(resClusterWrapper);

    }

}

class ResClusterWrapper {
    @JSONField(ordinal = 0)
    private boolean success;
    @JSONField(ordinal = 1)
    private int code;
    @JSONField(ordinal = 2)
    private String msg;
    @JSONField(ordinal = 3)
    private RecWrapper data;

    // get set
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public RecWrapper getData() {
        return data;
    }

    public void setData(RecWrapper data) {
        this.data = data;
    }

}

class RecWrapper {
    @JSONField(ordinal = 0)
    private String graphSymbol;
    @JSONField(ordinal = 1)
    private List<ClusterWrapperWrapper> clustersRes;

    // get set
    public String getGraphSymbol() {
        return graphSymbol;
    }

    public void setGraphSymbol(String graphSymbol) {
        this.graphSymbol = graphSymbol;
    }

    public List<ClusterWrapperWrapper> getClustersRes() {
        return clustersRes;
    }

    public void setClustersRes(List<ClusterWrapperWrapper> clustersRes) {
        this.clustersRes = clustersRes;
    }
}

class ClusterWrapperWrapper {
    @JSONField(ordinal = 0)
    private int typeIdx;
    @JSONField(ordinal = 1)
    private List<ClusterWrapper> clusters;

    // get set
    public int getTypeIdx() {
        return typeIdx;
    }

    public void setTypeIdx(int typeIdx) {
        this.typeIdx = typeIdx;
    }

    public List<ClusterWrapper> getClusters() {
        return clusters;
    }

    public void setClusters(List<ClusterWrapper> clusters) {
        this.clusters = clusters;
    }
}

class ClusterWrapper {
    @JSONField(ordinal = 0)
    private String sourceIds; // 逗号分隔
    @JSONField(ordinal = 1)
    private String summary;
    @JSONField(ordinal = 2)
    private List<N> recommendNs;
    @JSONField(ordinal = 3)
    private List<R> recommendRs;

    // constructor param
    public ClusterWrapper(String sourceIds, String summary, List<N> recommendNs, List<R> recommendRs) {
        this.sourceIds = sourceIds;
        this.summary = summary;
        this.recommendNs = recommendNs;
        this.recommendRs = recommendRs;
    }

    // get set
    public String getSourceIds() {
        return sourceIds;
    }

    public void setSourceIds(String sourceIds) {
        this.sourceIds = sourceIds;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<N> getRecommendNs() {
        return recommendNs;
    }

    public void setRecommendNs(List<N> recommendNs) {
        this.recommendNs = recommendNs;
    }

    public List<R> getRecommendRs() {
        return recommendRs;
    }

    public void setRecommendRs(List<R> recommendRs) {
        this.recommendRs = recommendRs;
    }

}

class N {
    @JSONField(ordinal = 0)
    private long id;
    @JSONField(ordinal = 1)
    private String name;
    @JSONField(ordinal = 2)
    private String entityType;
    @JSONField(ordinal = 3)
    private String entitySubClass;
    @JSONField(ordinal = 4)
    private String ontologySymbol;
    @JSONField(ordinal = 5)
    private String subOntologySymbol;

    // get set
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntitySubClass() {
        return entitySubClass;
    }

    public void setEntitySubClass(String entitySubClass) {
        this.entitySubClass = entitySubClass;
    }

    public String getOntologySymbol() {
        return ontologySymbol;
    }

    public void setOntologySymbol(String ontologySymbol) {
        this.ontologySymbol = ontologySymbol;
    }

    public String getSubOntologySymbol() {
        return subOntologySymbol;
    }

    public void setSubOntologySymbol(String subOntologySymbol) {
        this.subOntologySymbol = subOntologySymbol;
    }

}

class R {
    @JSONField(ordinal = 0)
    private long id;
    @JSONField(ordinal = 1)
    private String name;
    @JSONField(ordinal = 2)
    private String relationType;
    @JSONField(ordinal = 3)
    private String relationSubType;
    @JSONField(ordinal = 4)
    private String ontologySymbol;
    @JSONField(ordinal = 5)
    private String subOntologySymbol;

    // get set
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    public String getRelationSubType() {
        return relationSubType;
    }

    public void setRelationSubType(String relationSubType) {
        this.relationSubType = relationSubType;
    }

    public String getOntologySymbol() {
        return ontologySymbol;
    }

    public void setOntologySymbol(String ontologySymbol) {
        this.ontologySymbol = ontologySymbol;
    }

    public String getSubOntologySymbol() {
        return subOntologySymbol;
    }

    public void setSubOntologySymbol(String subOntologySymbol) {
        this.subOntologySymbol = subOntologySymbol;
    }
}