package com.usst.kgfusion.controller;

import java.util.*;

import com.usst.kgfusion.constructer.GraphReader;
import com.usst.kgfusion.databaseQuery.BasicOperation;
import com.usst.kgfusion.databaseQuery.KGASSETOperation;
import com.usst.kgfusion.entrance.KGsMergeBasedOnContent;
import com.usst.kgfusion.pojo.EnSim;
import com.usst.kgfusion.pojo.KG;
import com.usst.kgfusion.pojo.RelationEasy;
import com.usst.kgfusion.util.MapUtil;
import com.usst.kgfusion.util.MatrixUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 8种规则计算实体相似度
 * 每个方法返回的结果都是筛选后的值
 * @author JH
 * @date 2022年8月8日 15:36
 */
public class SimGuiZe {

    private static final Logger logger = LoggerFactory.getLogger("com.usst.test");




    public double jaccard(Set<String> set1, Set<String> set2){
        double len_jiao = 0;
        for(String s : set1){
            if(set2.contains(s)){
                len_jiao++;
            }
        }
        double len_bing = set1.size() + set2.size() - len_jiao;
        double jaccard = 1.0 * len_jiao / len_bing;
        return jaccard;
    }

    // from 为目标图谱， to 为文档图谱
    // todo 前缀相似度
    public Map<String, Map<String,Double>> qianzhui(String from, String to, double prefix_ratio){
        Map<EnSim, Set<EnSim>> res = new HashMap<>();
        Map<String, Map<String,Double>> res1 = new HashMap<>();

        Set<EnSim> froms = BasicOperation.getNodes(from);
        Set<EnSim> tos = BasicOperation.getNodes(to);

        if(froms.size() == 0 || tos.size() == 0){
            return res1;
        }
        
        for(EnSim fromEn : froms){
            if(!res.containsKey(fromEn)){
                res.put(fromEn, new HashSet<>());
            }
            if(!res1.containsKey(fromEn.getId().toString())){
                res1.put(fromEn.getId().toString(), new HashMap<>());
            }

            String prefix_from = fromEn.getName().substring(0, (int)(fromEn.getName().length() * prefix_ratio));
            for(EnSim toEn : tos){
                String prefix_to = toEn.getName().substring(0, (int)(toEn.getName().length() * prefix_ratio));
                if(prefix_from.equals(prefix_to)){
                    res1.get(fromEn.getId().toString()).put(toEn.getId().toString(),1.0);
                    res.get(fromEn).add(toEn);
                }
            }
        }

        return res1;
    }

    // todo 后缀相似度
    public Map<String, Map<String,Double>> houzhui(String from, String to, double suffix_ratio){
        Map<EnSim, Set<EnSim>> res = new HashMap<>();
        Set<EnSim> froms = BasicOperation.getNodes(from);
        Set<EnSim> tos = BasicOperation.getNodes(to);
        Map<String, Map<String,Double>> res1 = new HashMap<>();


        if(froms.size() == 0 || tos.size() == 0){
            return res1;
        }
        for(EnSim fromEn : froms){
            if(!res.containsKey(fromEn)){
                res.put(fromEn, new HashSet<>());
            }
            if(!res1.containsKey(fromEn.getId().toString())){
                res1.put(fromEn.getId().toString(), new HashMap<>());
            }
            String suffix_from = fromEn.getName().substring(fromEn.getName().length() - (int)(fromEn.getName().length() * suffix_ratio));
            for(EnSim toEn : tos){
                String suffix_to = toEn.getName().substring(toEn.getName().length() - (int)(toEn.getName().length() * suffix_ratio));
                if(suffix_from.equals(suffix_to)){
                    res.get(fromEn).add(toEn);
                    res1.get(fromEn.getId().toString()).put(toEn.getId().toString(),1.0);
                }
            }
        }
        return res1;
    }

    // todo 基于类型的相似度
    public Map<String, Map<String,Double>> leixing(String from, String to){

        Map<EnSim, Set<EnSim>> res = new HashMap<>();
        Map<String, Map<String,Double>> res1 = new HashMap<>();
        Set<EnSim> froms = BasicOperation.getNodes(from);
        Set<EnSim> tos = BasicOperation.getNodes(to);

        if(froms.size() == 0 || tos.size() == 0){
            return res1;
        }

        for(EnSim fromEn : froms){
            if(!res.containsKey(fromEn)){
                res.put(fromEn, new HashSet<>());
            }
            if(!res1.containsKey(fromEn.getId().toString())){
                res1.put(fromEn.getId().toString(), new HashMap<>());
            }

            for(EnSim toEn : tos){
                if(!fromEn.getId().equals(toEn.getId())){
                    if(fromEn.getType()!=null && toEn.getType()!=null){
                        if(fromEn.getType().equals(toEn.getType())){
                            res.get(fromEn).add(toEn);
                            res1.get(fromEn.getId().toString()).put(toEn.getId().toString(),1.0);
                        }
                    }

                }
            }
        }
        return res1;
    }

    // todo 实体名称的相似度
    public Map<String, Map<String,Double>> mingcheng(String from, String to){
        Map<EnSim, Set<EnSim>> res = new HashMap<>();
        Map<String, Map<String,Double>> res1 = new HashMap<>();
        Set<EnSim> froms = BasicOperation.getNodes(from);
        Set<EnSim> tos = BasicOperation.getNodes(to);

        if(froms.size() == 0 || tos.size() == 0){
            return res1;
        }

        for(EnSim fromEn : froms){
            if(!res.containsKey(fromEn)){
                res.put(fromEn, new HashSet<>());
            }
            if(!res1.containsKey(fromEn.getId().toString())){
                res1.put(fromEn.getId().toString(), new HashMap<>());
            }

            for(EnSim toEn : tos){
                if(!fromEn.getId().equals(toEn.getId())){
                    if(fromEn.getName().equals(toEn.getName())){
                        res.get(fromEn).add(toEn);
                        res1.get(fromEn.getId().toString()).put(toEn.getId().toString(),1.0);
                    }
                }

            }


        }

        return res1;
    }



    // todo 父节点相似度
    public Map<String, Map<String,Double>> fujiedian(String from, String to){
        Map<EnSim, Set<EnSim>> res = new HashMap<>();

        Map<String, Map<String,Double>> res1 = new HashMap<>();

        Map<EnSim, Set<EnSim>> parents_from = BasicOperation.getParents(from);
        Map<EnSim, Set<EnSim>> parents_to = BasicOperation.getParents(to);

        if(parents_from.size() == 0 || parents_to.size() == 0){
            return res1;
        }

        for(EnSim fromEn : parents_from.keySet()){
            if(!res.containsKey(fromEn)){
                res.put(fromEn, new HashSet<>());
            }
            if(!res1.containsKey(fromEn.getId().toString())){
                res1.put(fromEn.getId().toString(), new HashMap<>());
            }

            Set<String> from_names = new HashSet<>();
            for(EnSim from_parent : parents_from.get(fromEn)){
                from_names.add(from_parent.getName());
            }

            for(EnSim toEn : parents_to.keySet()){
                Set<String> to_names = new HashSet<>();
                for(EnSim to_parent : parents_to.get(toEn)){
                    to_names.add(to_parent.getName());
                }
                // 计算from_names 和 to_names 的 jaccard 相似度
                double jaccard = jaccard(from_names, to_names);
                if(jaccard>0.0){
                    res1.get(fromEn.getId().toString()).put(toEn.getId().toString(),jaccard);
                }

                res.get(fromEn).add(toEn);

            }
            
        }

        return res1;
    }

    // todo 子节点相似度
    public Map<String, Map<String,Double>> zijiedian(String from, String to){
        Map<EnSim, Set<EnSim>> res = new HashMap<>();
        Map<String, Map<String,Double>> res1 = new HashMap<>();
        Map<EnSim, Set<EnSim>> children_from = BasicOperation.getChildren(from);
        Map<EnSim, Set<EnSim>> children_to = BasicOperation.getChildren(to);
        if(children_from.size() == 0 || children_to.size() == 0){
            return res1;
        }
        for(EnSim fromEn : children_from.keySet()){
            if(!res.containsKey(fromEn)){
                res.put(fromEn, new HashSet<>());
            }
            if(!res1.containsKey(fromEn.getId().toString())){
                res1.put(fromEn.getId().toString(), new HashMap<>());
            }

            Set<String> from_names = new HashSet<>();
            for(EnSim from_child : children_from.get(fromEn)){
                from_names.add(from_child.getName());
            }

            for(EnSim toEn : children_to.keySet()){
                Set<String> to_names = new HashSet<>();
                for(EnSim to_child : children_to.get(toEn)){
                    to_names.add(to_child.getName());
                }
                
                double jaccard = jaccard(from_names, to_names);
                if(jaccard>0.0){

                    res1.get(fromEn.getId().toString()).put(toEn.getId().toString(),jaccard);
                }
                res.get(fromEn).add(toEn);

            }
        }
        return res1;
    }

    // todo 邻居节点相似度
    public Map<String, Map<String,Double>>  linjiedian(String from, String to){
        Map<EnSim, Set<EnSim>> res = new HashMap<>();
        Map<String, Map<String,Double>> res1 = new HashMap<>();
        Map<EnSim, Set<EnSim>> neighbors_from = BasicOperation.getNeighbors(from);
        Map<EnSim, Set<EnSim>> neighbors_to = BasicOperation.getNeighbors(to);
        if(neighbors_from.size() == 0 || neighbors_to.size() == 0){
            return res1;
        }

        for(EnSim fromEn : neighbors_from.keySet()){
            if(!res.containsKey(fromEn)){
                res.put(fromEn, new HashSet<>());
            }
            if(!res1.containsKey(fromEn.getId().toString())){
                res1.put(fromEn.getId().toString(), new HashMap<>());
            }


            Set<String> from_names = new HashSet<>();
            for(EnSim from_neighbor : neighbors_from.get(fromEn)){
                from_names.add(from_neighbor.getName());
            }

            for(EnSim toEn : neighbors_to.keySet()){
                Set<String> to_names = new HashSet<>();
                for(EnSim to_neighbor : neighbors_to.get(toEn)){
                    to_names.add(to_neighbor.getName());
                }



                double jaccard = jaccard(from_names, to_names);
                if(jaccard>0.0) {
                    res1.get(fromEn.getId().toString()).put(toEn.getId().toString(), jaccard);
                }

                res.get(fromEn).add(toEn);

            }
        }

        return res1;
    }


    // todo 词向量相似度
    public Map<String, Map<String,Double>> cixiangliang(String from, String to) {
        Map<EnSim, Map<EnSim,Double>> res1 = new HashMap<>();
        List<KG> kgs = new ArrayList<>();
        KG kg = new KG();
        if(!from.equals(to)){
            kg = GraphReader.readGraph2(GraphReader.query(to, from, false));
            kgs.add(kg);
        }else{
            kg = GraphReader.readGraph2(GraphReader.query2(to, false));
            kgs.add(kg);
        }

        KGsMergeBasedOnContent kgm = new KGsMergeBasedOnContent(kgs,to,from);
        Map<String, Map<String,Double>>  res = kgm.New_rule_entrance(kg,to,from);
        return res;
    }


    // todo 基于关系的相似度
    public Map<String, Map<String,Double>> RelSim(String from, String to){


        Map<String, Map<String,Double>> res_1 = new HashMap<>();
        Map<String, Map<String,Double>> res_2 = new HashMap<>();
        Map<String, Map<String,Double>> res_3 = new HashMap<>();
        Map<String, Map<String,Double>> res_4 = new HashMap<>();
        Map<String, Map<String,Double>> res_5 = new HashMap<>();
        Map<String, Map<String,Double>> res_rel = new HashMap<>();
        Map<Long, List<RelationEasy>> froms = GraphReader.readNodeRel(BasicOperation.queryNodeRel(to));
        Map<Long, List<RelationEasy>> tos = GraphReader.readNodeRel(BasicOperation.queryNodeRel(from));
        Map<Integer,Double> count_value = new HashMap<>();
        count_value.put(0,0.0);
        count_value.put(1,0.3);
        count_value.put(2,0.6);

        //计算所有关系间的相似度
        for(Map.Entry<Long, List<RelationEasy>> frome : froms.entrySet()){
                List<RelationEasy> from_rel =  frome.getValue();

                for(Map.Entry<Long, List<RelationEasy>> toe : tos.entrySet()){
                    if(!frome.getKey().equals(toe.getKey())){
                        List<RelationEasy> to_rel =  toe.getValue();
                        for(RelationEasy ref : from_rel){
                            for(RelationEasy tof: to_rel){
                                if(ref.getName()!=null &&tof.getName()!=null){
                                    if(ref.getName().equals(tof.getName())){
                                        Map<String,Double> temp1 = res_1.getOrDefault(ref.getId().toString(),new LinkedHashMap<>());
                                        temp1.put(tof.getId().toString(),0.1);
                                        res_1.put(ref.getId().toString(),temp1);
                                    }
                                }
                                if(ref.getRalationType()!=null &&tof.getRalationType()!=null){
                                    if(ref.getRalationType().equals(tof.getRalationType())){
                                        Map<String,Double> temp2 = res_2.getOrDefault(ref.getId().toString(),new LinkedHashMap<>());
                                        temp2.put(tof.getId().toString(),0.3);
                                        res_2.put(ref.getId().toString(),temp2);
                                    }

                                }
                                if(ref.getOntologySymbol()!=null &&tof.getOntologySymbol()!=null){
                                    if( ref.getOntologySymbol().equals(tof.getOntologySymbol()) ){
                                        Map<String,Double> temp3 = res_3.getOrDefault(ref.getId().toString(),new LinkedHashMap<>());
                                        temp3.put(tof.getId().toString(),0.3);
                                        res_3.put(ref.getId().toString(),temp3);
                                    }
                                }
                                if(ref.getRelationSubType()!=null &&tof.getRelationSubType()!=null){
                                    if(ref.getRelationSubType().equals(tof.getRelationSubType()) ){
                                        Map<String,Double> temp4 = res_4.getOrDefault(ref.getId().toString(),new LinkedHashMap<>());
                                        temp4.put(tof.getId().toString(),0.15);
                                        res_4.put(ref.getId().toString(),temp4);
                                    }
                                }
                                if(ref.getSubOntologySymbol()!=null &&tof.getSubOntologySymbol()!=null){

                                    if(ref.getSubOntologySymbol().equals(tof.getSubOntologySymbol()) ){
                                        Map<String,Double> temp5 = res_5.getOrDefault(ref.getId().toString(),new LinkedHashMap<>());
                                        temp5.put(tof.getId().toString(),0.15);
                                        res_5.put(ref.getId().toString(),temp5);
                                    }
                                }

                            }
                        }
                    }

                }
            }
        res_1 = MatrixUtil.add_2map(res_1,res_2);
        res_1 = MatrixUtil.add_2map(res_1,res_3);
        res_1 = MatrixUtil.add_2map(res_1,res_4);
        res_1 = MatrixUtil.add_2map(res_1,res_5);

        for(Map.Entry<Long, List<RelationEasy>> frome : froms.entrySet()){
            List<RelationEasy> from_rel =  frome.getValue();

            for(Map.Entry<Long, List<RelationEasy>> toe : tos.entrySet()){
                if(!frome.getKey().equals(toe.getKey())){
                    int count = 0;
                    List<RelationEasy> to_rel =  toe.getValue();
                    for(RelationEasy ref : from_rel){
                        for(RelationEasy tof: to_rel){
                            if(res_1.get(ref.getId().toString())!=null && res_1.get(ref.getId().toString()).get(tof.getId().toString())!=null){
                                if(res_1.get(ref.getId().toString()).get(tof.getId().toString())>=0.24){
                                    count++;
                                }
                            }

                        }
                    }
                    if(count>=1 && count<3){
                        Map<String,Double> temp5 = res_rel.getOrDefault(frome.getKey().toString(),new LinkedHashMap<>());
                        temp5.put(toe.getKey().toString(),count_value.get(count));
                        res_rel.put(frome.getKey().toString(),temp5);
                    }
                    if(count>=3){
                        Map<String,Double> temp5 = res_rel.getOrDefault(frome.getKey().toString(),new LinkedHashMap<>());
                        temp5.put(toe.getKey().toString(),0.8);
                        res_rel.put(frome.getKey().toString(),temp5);
                    }

                }
            }
        }

//        MatrixUtil.print(res_rel);
        return res_rel;
    }


    // !!!todo 相似度合并
    public Map<String, List<String>> All_sim(String des,String source,int flag ) {

        //flag 0为综合 1为歧义

        Map<String, Double> guizeMap = KGASSETOperation.read_guizequanzhong();  // 每种规则的的权重
        if(guizeMap==null){
            logger.error("无法读取相似度规则表");
        }

        double sim_value = -1.0;
        if(flag == 0){
            if(guizeMap.get("自动综合相似阈值")!=null && !guizeMap.get("自动综合相似阈值").equals("null")){

                sim_value = guizeMap.get("自动综合相似阈值");
            }
        }
        if(flag == 1){
            if(guizeMap.get("歧义分析阈值")!=null && !guizeMap.get("歧义分析阈值").equals("null")){

                sim_value = guizeMap.get("歧义分析阈值");
            }
        }

        if(sim_value == -1.0){
            sim_value = 0.5;
        }

        logger.info("相似度阈值大小为:"+sim_value);


        Map<String,List<String>> res = new HashMap<>();

        Set<EnSim> des_entitys = BasicOperation.getNodes(des);
        Set<EnSim> source_entitys = BasicOperation.getNodes(source);

        Map<String,Map<String,Double>> all_score = new HashMap<>();

        Map<String,Map<String,Double>> qianzhui = new HashMap<>();
        Map<String,Map<String,Double>> houzhui = new HashMap<>();
        Map<String,Map<String,Double>> cixiangliang = new HashMap<>();
        Map<String,Map<String,Double>> fujiedian = new HashMap<>();
        Map<String,Map<String,Double>> zijiedian = new HashMap<>();
        Map<String,Map<String,Double>> linjujiedian = new HashMap<>();
        Map<String,Map<String,Double>> mingchen = new HashMap<>();
        Map<String,Map<String,Double>> leixing = new HashMap<>();
        Map<String,Map<String,Double>> rel = new HashMap<>();


        double  value_all = 0.0;
        for(Map.Entry<String,Double> entry1 : guizeMap.entrySet()){
            if(entry1.getKey().equals("前缀")){
                qianzhui = qianzhui(des,source,0.1);
                //qianzhui = MatrixUtil.normalize(qianzhui);
                qianzhui = MatrixUtil.multiply(qianzhui,entry1.getValue());
                all_score = MatrixUtil.add_2map(all_score,qianzhui);
                value_all += entry1.getValue();
                logger.info("前缀规则计算完成");
            }
            if(entry1.getKey().equals("后缀")){
                houzhui = houzhui(des,source,0.1);
                //houzhui = MatrixUtil.normalize(houzhui);
                houzhui = MatrixUtil.multiply(houzhui,entry1.getValue());
                all_score = MatrixUtil.add_2map(all_score,houzhui);
                value_all += entry1.getValue();
                logger.info("后缀规则计算完成");
            }
            if(entry1.getKey().equals("词向量")){
                cixiangliang = cixiangliang(des,source);
                //cixiangliang = MatrixUtil.normalize(cixiangliang);
                cixiangliang = MatrixUtil.multiply(cixiangliang,entry1.getValue());
                all_score = MatrixUtil.add_2map(all_score,cixiangliang);
                value_all += entry1.getValue();
                logger.info("词向量规则计算完成");
            }
            if(entry1.getKey().equals("父节点")){
                fujiedian = fujiedian(des,source);

                //fujiedian = MatrixUtil.normalize(fujiedian);
                fujiedian = MatrixUtil.multiply(fujiedian,entry1.getValue());
                all_score = MatrixUtil.add_2map(all_score,fujiedian);
                value_all += entry1.getValue();
                logger.info("父节点规则计算完成");
            }
            if(entry1.getKey().equals("子节点")){
                zijiedian = zijiedian(des,source);

                //zijiedian = MatrixUtil.normalize(zijiedian);
                zijiedian = MatrixUtil.multiply(zijiedian,entry1.getValue());
                all_score = MatrixUtil.add_2map(all_score,zijiedian);
                value_all += entry1.getValue();
                logger.info("子节点规则计算完成");
            }
            if(entry1.getKey().equals("邻居节点信息")){
                linjujiedian = linjiedian(des,source);

                //linjujiedian = MatrixUtil.normalize(linjujiedian);
                linjujiedian = MatrixUtil.multiply(linjujiedian,entry1.getValue());
                all_score = MatrixUtil.add_2map(all_score,linjujiedian);
                value_all += entry1.getValue();
                logger.info("邻居节点规则计算完成");
            }
            if(entry1.getKey().equals("名称匹配")){
                mingchen = mingcheng(des,source);

                //mingchen = MatrixUtil.normalize(mingchen);
                mingchen = MatrixUtil.multiply(mingchen,entry1.getValue());
                all_score = MatrixUtil.add_2map(all_score,mingchen);
                value_all += entry1.getValue();
                logger.info("名称规则计算完成");
            }
            if(entry1.getKey().equals("类型匹配")){
                leixing = leixing(des,source);

                //leixing = MatrixUtil.normalize(leixing);
                leixing = MatrixUtil.multiply(leixing,entry1.getValue());
                all_score = MatrixUtil.add_2map(all_score,leixing);
                value_all += entry1.getValue();
                logger.info("类型规则计算完成");
            }
            if(entry1.getKey().equals("外延关系信息匹配")){
                rel = RelSim(des,source);

                rel = MatrixUtil.multiply(rel,entry1.getValue());
                all_score = MatrixUtil.add_2map(all_score,rel);
                value_all += entry1.getValue();
                logger.info("外延关系信息匹配规则计算完成");
            }


        }


        if(all_score!=null){

            //all_score = MatrixUtil.normalize(all_score);
            double value_weight = 1/(value_all);
            all_score = MatrixUtil.multiply(all_score,value_weight);
            for(Map.Entry<String,Map<String,Double>> entry_all: all_score.entrySet()){
                String des_id = entry_all.getKey();
                Map<String,Double> toOtherScore = entry_all.getValue();
                List<String> daihebing_ids = new ArrayList<>();
                for(Map.Entry<String,Double> to_all : toOtherScore.entrySet()){
                    String source_id = to_all.getKey();
                    if(!des_id.equals(source_id)){
                        if(to_all.getValue()>sim_value){
                            daihebing_ids.add(source_id);
                        }
                    }

                }
                if(daihebing_ids.size()>0){
                    res.put(des_id,daihebing_ids);
                }
            }
        }

        for(EnSim desEn : des_entitys){
            Long des_id = desEn.getId();
            String des_name = desEn.getName();
            String des_type = desEn.getType();

            for(EnSim sourceEn : source_entitys){
                Long source_id = sourceEn.getId();
                String source_name = sourceEn.getName();
                String source_type = sourceEn.getType();
                List<String> temp_list= new ArrayList<>();

                if(!des_id.equals(source_id)){
                    if(des_name.equals(source_name) && des_type.equals(source_type)){
                        if(res.get(des_id.toString())!=null){
                            temp_list = res.get(des_id.toString());
                            if(!temp_list.contains(source_id.toString())){
                                temp_list.add(source_id.toString());
                            }
                            res.put(des_id.toString(),temp_list);
                        }else {
                            temp_list.add(source_id.toString());
                            res.put(des_id.toString(),temp_list);
                        }
//                        if(!res.get(des_id.toString()).contains(source_id.toString())){
//                            temp_list = res.getOrDefault(des_id.toString(),new ArrayList<>());
//                            temp_list.add(source_id.toString());
//                            res.put(des_id.toString(),temp_list);
//                        }
                    }
                }
            }
        }







        return res;
    }

    public static void main(String[] args) {
//        String suffix_to = "[1357]";
//        suffix_to = suffix_to.replaceAll("\\[","");
//        suffix_to = suffix_to.replaceAll("\\]","");
//        List<String> suffix_to1 =  Arrays.asList(suffix_to.split(","));
//        List<Integer> suffix_to1_int = new ArrayList<>();
//        for(String ids: suffix_to1){
//            suffix_to1_int.add(Integer.parseInt(ids));
//        }
//        List<Integer> suffix_s =new ArrayList<>();
//        suffix_s.add(1);
//        suffix_s.add(2);
//        suffix_to1_int.addAll(suffix_s);
//        String res = suffix_to1_int.toString();
//        System.out.println(res);
//        Map<String,List<String>> test1=  new HashMap<>();
//        List<String> jsad=  new ArrayList<>();
//        jsad.add("sa");
//        jsad.add("nasd");
//        test1.put("lodo",jsad);
//        if(test1.get("lodo")!=null){
//            if(test1.get("lodo").contains("saa")){
//                System.out.println("sad");
//            }else {
//                System.out.println("sadd");
//            }
//        }
//        System.out.println(suffix_to1);

    }
}
