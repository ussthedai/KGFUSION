package com.usst.kgfusion.module;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.usst.kgfusion.pojo.EntityRaw;
import com.usst.kgfusion.pojo.KG;
import com.usst.kgfusion.pojo.Triple;

public class topsim_grm {

    //没有对出度进行归一化 相似度计算公式为 c*关系重要性/typenum
    //    static int numNodes;
    static double C = 0.8;//衰减因子
    static int N = 6;//迭代次数
    static int K = 50;//top-k
    Map<String,Object> res;



    public topsim_grm(Map<String,Object> res){
        this.res=res;
    }
    //生成节点的入度
    public static Map InDegreeMap(KG kg) {

        List<Triple> triples = kg.getTriples();


        Map<Integer, Set<Integer>> InLinksMap = new HashMap<>();

        Set<Integer> nodelinks = new HashSet<>();
        Set<Integer> nodes = new HashSet<>();

        //kg中对应的实体与边的集合
        int triplesNum = triples.size();
        Integer headEntityID = 0;
        Integer tailEntityID = 0;

        
            for (int i = 0; i < triplesNum; i++) {

                headEntityID = Integer.valueOf(triples.get(i).getHead().getEntityId()); //获取第i个元组的头实
                tailEntityID = Integer.valueOf(triples.get(i).getTail().getEntityId());
                nodes.add(headEntityID);
                nodes.add(tailEntityID);

//tail-head indegree
                if (InLinksMap.containsKey(tailEntityID)) {
                    nodelinks = InLinksMap.get(tailEntityID);
                    nodelinks.add(headEntityID);
                    InLinksMap.put(tailEntityID, nodelinks);

                } else {
                    nodelinks = new HashSet<>();
                    nodelinks.add(headEntityID);
                    InLinksMap.put(tailEntityID, nodelinks);
                }
            }
//            numNodes = nodes.size();
        
        return InLinksMap;
    }

    public static Map OutDegreeMap(KG kg) {

        List<Triple> triples = kg.getTriples();
        Map<Integer, Set<Integer>> OutLinksMap = new HashMap<Integer, Set<Integer>>();

        Set<Integer> nodelinks = new HashSet<>();
        Set<Integer> nodes = new HashSet<>();

        //kg中对应的实体与边的集合
        int triplesNum = triples.size();
        Integer headEntityID = 0;
        Integer tailEntityID = 0;

        
            for (int i = 0; i < triplesNum; i++) {

                headEntityID = Integer.valueOf(triples.get(i).getHead().getEntityId()); //获取第i个元组的头实
                tailEntityID = Integer.valueOf(triples.get(i).getTail().getEntityId());
                nodes.add(headEntityID);
                nodes.add(tailEntityID);

//tail-head indegree
                if (OutLinksMap.containsKey(headEntityID)) {
                    nodelinks = OutLinksMap.get(headEntityID);
                    nodelinks.add(tailEntityID);
                    OutLinksMap.put(headEntityID, nodelinks);

                } else {
                    nodelinks = new HashSet<>();
                    nodelinks.add(tailEntityID);
                    OutLinksMap.put(headEntityID, nodelinks);
                }
            }
        
        return OutLinksMap;
    }

    //    给定id 输出实体的type
    public  String getType(List<EntityRaw> entityList, int entityID){
        String entityType=null;
        for(EntityRaw entity:entityList){
            if(Integer.parseInt(entity.getEntityId())==entityID){
//                entityType=entity.getTypeId();
                entityType=entity.getEntityType();
            }
        }
        return entityType;
    }

    //返回一个给定集合，返回集合中属于某类型的节点个数
    public int getTypeNum(Set<Integer> outDegree1,List<EntityRaw> entityList,String typej){
        int typeNum=1;
        for(Integer entityId:outDegree1){
            for(EntityRaw entity:entityList){
                if(entityId==Integer.parseInt(entity.getEntityId())){
                    if(entity.getEntityType().equals(typej)){
                        typeNum+=1;
                    }
                }
            }
        }
        return typeNum;
    }
    //给定int类型的两个实体 返回实体之间的关系重要性值
    public double getRw(Map<String, Map<String, Double>> rwScoreMap,int id1,int id2,List<EntityRaw> entityList){
        double sc=0.0;
        String type1=getType(entityList,id1);
        String type2=getType(entityList,id2);
        Iterator<Map.Entry<String,Map<String,Double>>> rwscitr = rwScoreMap.entrySet().iterator();
        while (rwscitr.hasNext()) {
            Map.Entry<String, Map<String, Double>> rwscmap = rwscitr.next();
            String key1 = rwscmap.getKey();
            if (type1.equals(key1)) {
//            scmap存放了类型和关系重要性
                Map<String, Double> rwmap = rwscmap.getValue();
                Iterator<Map.Entry<String, Double>> scitr = rwmap.entrySet().iterator();
                while (scitr.hasNext()) {
                    Map.Entry<String, Double> scmap = scitr.next();
                    String key2 = scmap.getKey();
                    if (type2.equals(key2)) {
                        sc = scmap.getValue();
                    }
                }
            }
        }
        return sc;
    }

    //计算topsim值
    public Map TopSimSM(Map<String,Object> res) {
//调用方法 生成入度和出度的map
        KG kg= (KG) res.get("kg");
        HashMap<String,List<String>> querynodes= (HashMap<String, List<String>>) res.get("queryNodes");
        List<EntityRaw> entitiesList = kg.getEntities();//获取融合后的所有实体
        Map<Integer, Set<Integer>> IndDegreeMap=InDegreeMap(kg);
        Map<Integer, Set<Integer>> OutDegreeMap=OutDegreeMap(kg);
        Map<String, Map<String, Double>> rwScoreMap= GRM.geRw(kg);

        Set<String> entities =querynodes.keySet();//获取融合后的所有实体
        System.out.println(entities.size());
//        存放长度和节点 就是0 为待查询 1为待查询的入度 2 待查询的入度的入度集合
        Map<Integer, Set<Integer>> mapSet = new HashMap<>();
        Map<Integer, HashMap> storeSM = new HashMap<>();

        for (String entity : entities) {
            int q = Integer.parseInt(entity);
//            存放相似度分数
            Map<Integer, HashMap> SM = new HashMap();
//            计算l-1暂时存放sm分数
            Map<Integer, HashMap> tempSM = new HashMap();
            Set<Integer> setT1 = new HashSet();
            setT1.add(q);
//            存放待查询的节点 q
            mapSet.put(0, setT1);
            for (int l = 1; l <= N - 1; l++) {
                if (mapSet.containsKey(l - 1)) {
//                    获取待查询的集合 0 为q
                    Set<Integer> set = mapSet.get(l - 1);
                    Set<Integer> setIn = new HashSet<>();
                    for (int i : set) {
                        if (IndDegreeMap.containsKey(i)) {
//                            get入度集合中查询的q的入度集合 再循环。。。
                            Set<Integer> s = IndDegreeMap.get(i);
                            for (int inId : s) {
//                                第一层存放q的入度节点
                                setIn.add(inId);
                            }
                        }
                    }
//                    存放长度和入度节点
                    mapSet.put(l, setIn);
                }
            }

            double score, addScore, tempScore;
            int It, Ij;
            Map<Integer, Double> hm = new HashMap<>();
            Set<Integer> inDegree1 = new HashSet<>();
            Set<Integer> outDegree1 = new HashSet<>();


            for (int l = N - 1; l >= 0; l--) {
                Set<Integer> SetT = mapSet.get(l);
                if (l == N - 1) {
                    for (int t : SetT) {
                        HashMap innerMap = SM.getOrDefault(t, new HashMap<Integer, Double>());
//						如果文件中入度节点包含节点t
                        if (IndDegreeMap.containsKey(t)) {
//							获取节点t的所有出度和权重
                            inDegree1 = IndDegreeMap.get(t);
                            for (int i : inDegree1) {
                                if (OutDegreeMap.containsKey(i)) {
                                    outDegree1 = OutDegreeMap.get(i);
                                    for (int j : outDegree1) {
                                        if (getType(entitiesList, j).equals(getType(entitiesList, t))) {
                                            if (t != j) {
//                                            修改为1与t的所有出度节点中类型与j节点类型相同的个数比值
                                                String typej = getType(entitiesList, j);
                                                if (OutDegreeMap.containsKey(t)) {
                                                    Set<Integer> outDegreet = OutDegreeMap.get(t);
                                                    int          typeNum    = getTypeNum(outDegreet, entitiesList, typej);
                                                    double       sc         = getRw(rwScoreMap, t, j, entitiesList);
//                                           实现rw之后就换为rw*typenum
                                                    score = (C * sc) / (double) (typeNum);
//												如果sm已经存在 t的key 加上之前的值
                                                    if (SM.containsKey(t) && SM.get(t).containsKey(j)) {
                                                        tempScore = (double) SM.get(t).get(j);
                                                        addScore = tempScore + score;
                                                        innerMap.put(j, addScore);
                                                        SM.put(t, innerMap);
                                                    } else {
                                                        innerMap.put(j, score);
                                                        SM.put(t, innerMap);
                                                    }

                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
//						如果不包含t第一次添加 为1.0
                        innerMap.put(t, 1.0);
                        SM.put(t, innerMap);
                    }
                    tempSM = SM;
                } else {
//					l=n-1已经结束 现在判断l不等于n-1的情况
                    Map<Integer, HashMap> resultSM = new HashMap();
                    for (int t : SetT) {
                        HashMap innerMap = resultSM.getOrDefault(t, new HashMap<Integer, Double>());
                        if (IndDegreeMap.containsKey(t)) {
                            inDegree1 = IndDegreeMap.get(t);
                            for (int i : inDegree1) {
                                if (tempSM.containsKey(i)) {
                                    hm = tempSM.get(i);
                                    Iterator<Map.Entry<Integer, Double>> it_2 = hm.entrySet().iterator();
                                    while (it_2.hasNext()) {
                                        Map.Entry entry1 = it_2.next();
                                        int key = (int) entry1.getKey();
                                        double value = (double) entry1.getValue();
                                        if (OutDegreeMap.containsKey(key)) {
                                            outDegree1 = OutDegreeMap.get(key);
                                            for (int j : outDegree1) {
                                                if (IndDegreeMap.containsKey(t) && IndDegreeMap.containsKey(j)) {
                                                    if (getType(entitiesList, j).equals(getType(entitiesList, t))) {
                                                        if (t != j) {
                                                            String typej = getType(entitiesList, j);
                                                            if (OutDegreeMap.containsKey(t)) {
                                                                Set<Integer> outDegreet = OutDegreeMap.get(t);
                                                                int          typeNum    = getTypeNum(outDegreet, entitiesList, typej);
                                                                double       sc         = getRw(rwScoreMap, t, j, entitiesList);
//                                           实现rw之后就换为rw*typenum
                                                                score = (C * sc * value) / (double) (typeNum);
                                                                if (resultSM.containsKey(t) && resultSM.get(t).containsKey(j)) {
                                                                    tempScore = (double) resultSM.get(t).get(j);
                                                                    addScore = score + tempScore;
                                                                    innerMap.put(j, addScore);
                                                                    resultSM.put(t, innerMap);
                                                                } else {
                                                                    innerMap.put(j, score);
                                                                    resultSM.put(t, innerMap);
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        innerMap.put(t, 1.0);
                        resultSM.put(t, innerMap);
                    }
                    tempSM = resultSM;
                }
            }
            storeSM.putAll(tempSM);

        }
//top-k排序
        Map<Integer, HashMap<Integer, Double>> sortMatrix = new HashMap<Integer, HashMap<Integer, Double>>();
        List<HashMap.Entry<Integer, Double>> list1 = new ArrayList<HashMap.Entry<Integer, Double>>();
        Map<Integer, Double> hm = new HashMap<Integer, Double>();
        Iterator<Map.Entry<Integer, HashMap>> it = storeSM.entrySet().iterator();
//		得到矩阵的迭代器
        while (it.hasNext()) {
            Map.Entry entry1 = it.next();
            int k1 = (int) entry1.getKey();
//            暂时存放相似节点和相似值
            hm = (HashMap<Integer, Double>) entry1.getValue();
            //遍历 hm.
            for (Map.Entry<Integer, Double> entry : hm.entrySet()) {
                list1.add(entry); //将map中的元素放入list中
            }
            list1.sort(new Comparator<HashMap.Entry<Integer, Double>>() {
                           public int compare(HashMap.Entry<Integer, Double> o1, HashMap.Entry<Integer, Double> o2) {
                               double result = o2.getValue() - o1.getValue();
                               if (result > 0)
                                   return 1;
                               else if (result == 0)
                                   return 0;
                               else
                                   return -1;
                           }
                       }
            );
//            排序后的结果存放再sortMatrix
            HashMap innerIdWeight = sortMatrix.getOrDefault(k1, new HashMap<Integer, Double>());
            innerIdWeight = new LinkedHashMap<Integer, Double>();
            //output k similar nodes
            if (list1.size() > K) {
                for (Map.Entry<Integer, Double> temp : list1.subList(0, K)) {
                    innerIdWeight.put(temp.getKey(), temp.getValue());
                    sortMatrix.put(k1, innerIdWeight);
                }
            } else {
                for (Map.Entry<Integer, Double> temp : list1) {
                    innerIdWeight.put(temp.getKey(), temp.getValue());
                    sortMatrix.put(k1, innerIdWeight);
                }
            }
            list1.clear();
        }
        return sortMatrix;

    }


    public Map weightNormalized(Map storeWeight){
//            归一化
        Iterator<Map.Entry<Integer, HashMap>> itr        = storeWeight.entrySet().iterator();
        Map<Integer, Map<Integer, Double>>    weightMap = new HashMap<>();
        while (itr.hasNext()) {
            Map.Entry<Integer, HashMap> entry = itr.next();
//            key为类型1
            Integer key = entry.getKey();
//            value为类型2 类型1，类型2的相连个数
            Map<Integer, Double> tpMap = entry.getValue();
            double sumNum   = 0.0;
//            得到类型1 相连的所有类型的总和
            for (Double num : tpMap.values()) {
                sumNum += num;
            }
            Iterator<Map.Entry<Integer, Double>> itr2  = tpMap.entrySet().iterator();
            Map<Integer, Double> weightscore = new HashMap<>();
            while (itr2.hasNext()) {
                Map.Entry<Integer, Double> entry2 = itr2.next();
                int key2   = entry2.getKey();
                double num2   = entry2.getValue();
                double wscore = num2 / sumNum;
                weightscore.put(key2, wscore);
//                System.out.println(key+" "+key2+" "+wscore);
            }
            weightMap.put(key, weightscore);
        }
        return weightMap;
    }
//    public static void main(String[] args) throws IOException {
//        KG kg = GraphReader.readGraph(GraphReader.query("SOA服务基础设施", "大数据平台"));
//        topsim_grm simJoinModule = new topsim_grm(kg);
//        Map simMap=simJoinModule.TopSimSM(kg);
//        creatFile.getFile("G://simResult//newProjectResult//topsim_grm_val.txt",simMap);
//    }

}
