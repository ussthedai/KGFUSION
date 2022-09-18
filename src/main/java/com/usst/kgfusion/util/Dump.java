//package com.usst.kgfusion.util;
//
//import java.io.BufferedReader;
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Map.Entry;
//import java.util.Set;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.TypeReference;
//
//public class Dump {
//
//    public static void e2typeDump(String from, String to) throws IOException {
//        // 一个实体可有多个类型
//        HashMap<String, List<String>> map = new HashMap<>();
//        BufferedReader br = new BufferedReader(new FileReader(from));
//        int lineNumber = 1;
//        String line = "";
//        while ((line = br.readLine()) != null) {
//            ++lineNumber;
//            if (lineNumber <= 2)
//                continue;
//            String[] spLine = line.strip().split(",");
//            if (!map.containsKey(spLine[0])) {
//                map.put(spLine[0], new ArrayList<>());
//                map.get(spLine[0]).add(spLine[1]);
//            } else {
//                map.get(spLine[0]).add(spLine[1]);
//            }
//        }
//        br.close();
//        JsonUtil.writeJsonStr(map, to);
//    }
//
//    // public static void typeIdxDump(String fileName) throws IOException{
//    // HashMap<String, String> type2idx = new HashMap<>();
//    // HashMap<String, String> idx2type = new HashMap<>();
//    // BufferedReader br = new BufferedReader(new FileReader(fileName));
//    // String[] spLine = null;
//    // int typeCount = 0;
//    // String head = "";
//    // String tail = "";
//    // String line = "";
//    // int lineCount = 1;
//    // while((line = br.readLine()) != null){
//    // ++lineCount;
//    // if (lineCount <= 2) continue;
//    // spLine = line.strip().split(",");
//    // head = spLine[0];
//    // tail = spLine[2];
//    // if (!type2idx.containsKey(head)) {type2idx.put(head,
//    // Integer.toString(typeCount)); typeCount++;}
//    // if (!type2idx.containsKey(tail)) {type2idx.put(tail,
//    // Integer.toString(typeCount)); typeCount++;}
//    // }
//
//    // type2idx.forEach((k, v) -> idx2type.put(v.toString(), k));
//    // br.close();
//
//    // String t2i = PathConfig.META_DATA_DIR + "type2idx.json";
//    // String i2t = PathConfig.META_DATA_DIR + "idx2type.json";
//    // File type2idx_file = new File(t2i);
//    // File idx2type_file = new File(i2t);
//    // if (!type2idx_file.exists()) {
//    // type2idx_file.createNewFile();
//    // }
//    // if (!idx2type_file.exists()){
//    // idx2type_file.createNewFile();
//    // }
//
//    // JsonUtil.writeJsonStr(type2idx, t2i);
//    // JsonUtil.writeJsonStr(idx2type, i2t);
//    // }
//
//    /**
//     * key: text value: set<entityName>
//     *
//     * @throws IOException
//     */
//    public static Map<String, List<String>> textContainEntity(String fileName) throws IOException {
//        Map<String, Set<String>> temp = new HashMap<>();
//        Map<String, List<String>> res = new HashMap<>();
//        BufferedReader br = new BufferedReader(new FileReader(fileName));
//        String[] spLine = null;
//        String head = "";
//        String tail = "";
//        String line = "";
//        String item = "";
//        int lineCount = 1;
//        while ((line = br.readLine()) != null) {
//            ++lineCount;
//            if (lineCount <= 2)
//                continue;
//            spLine = line.strip().split(",");
//            head = spLine[0];
//            tail = spLine[2];
//            item = spLine[3];
//            if (!temp.containsKey(item)) {
//                temp.put(item, new HashSet<>());
//            }
//            temp.get(item).add(head);
//            temp.get(item).add(tail);
//        }
//        br.close();
//
//        temp.forEach((k, v) -> res.put(k, new ArrayList<>(v)));
//        return res;
//    }
//
//    public static void dumpWordGraph(Map<String, Set<String>> wordGraph, String destination) throws IOException {
//        File file = new File(destination);
//        if (!file.exists())
//            file.createNewFile();
//        BufferedWriter bw = new BufferedWriter(new FileWriter(destination));
//        for (Entry<String, Set<String>> graph : wordGraph.entrySet()) {
//            Set<String> neighbourWords = graph.getValue();
//            String name = graph.getKey();
//            for (String neighbour : neighbourWords) {
//                StringBuilder sb = new StringBuilder();
//                sb.append(name + "," + "邻词" + "," + neighbour);
//                bw.write(sb.toString());
//                bw.write("\\n");
//            }
//        }
//        bw.close();
//    }
//
//    // public static void dumpRelatedWordGraph() throws IOException{
//    // // 需要先调用模块再保存
//    // BufferedReader br = new BufferedReader(new FileReader(PathConfig.ANNOTATE_DIR
//    // + "all_triple.csv"));
//
//    // // 读数据并处理
//    // String line = "";
//    // int lineCount = 1;
//    // // 需要生成一个倒排索引: key: 实体词，value：List 实体词存在在哪些文档中，后续需要将这些条目合并未这个实体词的文本描述
//    // Map<String, Set<String>> invertIndex = new HashMap<>(); // 注意文档集合是 Set
//    // while((line = br.readLine()) != null) {
//    // if (lineCount <= 1){++lineCount; continue;}
//    // String[] spLine = line.split(",");
//    // String head = spLine[0];
//    // String tail = spLine[2];
//    // String item = spLine[3];
//    // if ((invertIndex.get(head) == null) || (invertIndex.get(tail) == null)){
//    // if (invertIndex.get(head) == null) invertIndex.put(head, new HashSet<>());
//    // if (invertIndex.get(tail) == null) invertIndex.put(tail, new HashSet<>());
//    // }
//    // invertIndex.get(head).add(item);
//    // invertIndex.get(tail).add(item);
//    // lineCount++;
//    // }
//    // br.close();
//
//    // 有一些实体是没有出现在句子中的，现在检查（由于未正确标注的实体实体实在太多，所以就不一一标注了，改用在句子中强制加入实体的策略：（头实体，尾实体））
//    // SegmentUtil util = new SegmentUtil();
//    // for (Entry<String, Set<String>> entry: invertIndex.entrySet()){
//    // String name = entry.getKey();
//    // Set<String> values = entry.getValue();
//    // for (String value : values){
//    // if (!(util.seg(value).contains(name))){
//    // System.out.println(name);
//    // }
//    // }
//    // }
//
//    // 将上面的 Map 改成 Map<String, String> 键：实体词，值：所处文档(由set合并而来)
//    // Map<String, String> entityText = new HashMap<>();
//    // for (Entry<String, Set<String>> entry: invertIndex.entrySet()){
//    // String en = entry.getKey();
//    // Set<String> sentences = entry.getValue();
//    // StringBuilder sb = new StringBuilder();
//    // for (String s: sentences) sb.append(s);
//    // entityText.put(en, sb.toString());
//    // }
//
//    // 拿到 实体词-所处文档 Map 后， 逐个调用 RelatedWordFroEntityConstructer获得相关词，并写入：Map<String,
//    // Set<String>> 实体词-topK关联词
//    // Map<String, List<String>> res = new HashMap<>(); //
//    // 不需使用Set，因为constructer里面已经编过号了，不可能重复
//    // RelatedWordForEntityConstructer constructer = new
//    // RelatedWordForEntityConstructer(3, 3, 0.8, 10); // 设置一部分参数，word,text需要手动填充
//    // for(Entry<String, String> entry: entityText.entrySet()){
//    // String entityWord = entry.getKey();
//    // String context = entry.getValue();
//    // constructer.setEntityName(entityWord);
//    // constructer.setContext(context);
//    // List<String> topKwords = constructer.geRelatedWord();
//    // res.put(entityWord, topKwords);
//    // }
//
//    // 填充完后，保存结果
//    // String destination = PathConfig.ANNOTATE_DIR + "relatedWordGraph" +
//    // "_windowSize" + constructer.getWindowSize() + "_neighbour" +
//    // constructer.getTopK() + ".csv";
//    // File file = new File(destination);
//    // if (!file.exists()) file.createNewFile();
//    // BufferedWriter bw = new BufferedWriter(new FileWriter(destination));
//    // for (String key: res.keySet()){
//    // for(String val : res.get(key)){
//    // StringBuilder sb = new StringBuilder();
//    // sb.append(key + "," + "邻词" + "," + val);
//    // bw.write(sb.toString());
//    // bw.write("\\n");
//    // }
//    // }
//    // bw.close();
//    // }
//
//    // public static void dumpTypeMatrix() throws IOException{
//    // // 加载 type2idx
//    // String jsonStrType2idx = JsonUtil.readJsonStr(PathConfig.META_DATA_DIR +
//    // "type2idx.json");
//    // @SuppressWarnings("unchecked")
//    // Map<String, String> type2idx = (Map<String, String>)
//    // JSON.parse(jsonStrType2idx);
//
//    // Map<String, List<String>> res = new HashMap<>();
//    // BufferedReader br = new BufferedReader(new FileReader(PathConfig.TYPES));
//    // String line = "";
//    // int lineCounter = 0;
//    // while((line = br.readLine()) != null) {
//    // if (lineCounter == 0){lineCounter++; continue;}
//    // String[] spLine = line.split(",");
//    // String parent = spLine[0];
//    // String child = spLine[2];
//    // if (!res.containsKey(parent)){
//    // res.put(type2idx.get(parent), new ArrayList<>());
//    // // res.put(parent, new ArrayList<>());
//    // }
//    // if (!res.containsKey(child)){
//    // res.put(type2idx.get(child), new ArrayList<>());
//    // // res.put(child, new ArrayList<>());
//    // }
//
//    // res.get(type2idx.get(parent)).add(type2idx.get(child));
//    // // res.get(parent).add(child);
//
//    // lineCounter++;
//    // }
//    // br.close();
//
//    // String destination = PathConfig.META_DATA_DIR + "typeMatrix.json";
//    // JsonUtil.writeJsonStr(res, destination);
//
//    // }
//
//    // public static void main(String[] args) throws IOException {
//    // // Dump.dumpRelatedWordGraph();
//    // Dump.dumpTypeMatrix();
//    // }
//
//    public static void main(String[] args) throws IOException {
//        // readfile
//        // Map<String, String> ens2tpyMap = new HashMap<>();
//        // String filePath = "D:\\DevelopmentProgress\\Project_VSCode\\neoKGFusion\\kgfusion\\src\\main\\resources\\static\\all_typing.csv";
//        // BufferedReader br = null;
//        // String line = "";
//        // String cvsSplitBy = ",";
//        // try {
//        //     br = new BufferedReader(new FileReader(filePath));
//        //     while ((line = br.readLine()) != null) {
//        //         String[] spLine = line.split(cvsSplitBy);
//        //         String en = spLine[0];
//        //         String tpye = spLine[1];
//        //         ens2tpyMap.put(en, tpye);
//        //     }
//        // } catch (FileNotFoundException e) {
//        //     e.printStackTrace();
//        // } catch (IOException e) {
//        //     e.printStackTrace();
//        // } finally {
//        //     if (br != null) {
//        //         try {
//        //             br.close();
//        //         } catch (IOException e) {
//        //             e.printStackTrace();
//        //         }
//        //     }
//        // }
//
//        // // // use fastjson write to file and beautify
//        // String jsonStr = JSON.toJSONString(ens2tpyMap);
//        // String destination = "D:\\DevelopmentProgress\\Project_VSCode\\neoKGFusion\\kgfusion\\src\\main\\resources\\static\\seed.json";
//        // JsonUtil.writeJsonStr(jsonStr, destination);
//
//        // // // read json to map
//        // String json = JsonUtil.readJsonStr(destination);
//        // Map<String,String> res = JSON.parseObject(json, new TypeReference<Map<String,
//        // String>>(){});
//        // System.out.println("Done");
//
//        // Map<String, Object> map = new HashMap<>();
//        // map.put("B", 2);
//        // map.put("A", 1);
//        // map.put("C", 3);
//        // String str = JSON.toJSONString(ens2tpyMap);
//        // System.out.println(str);
//        // JsonUtil.writeJsonStr(ens2tpyMap, destination);
//        // String json = JsonUtil.readJsonStr(destination);
//
//        // Map<String, Object> map1 = JSON.parseObject(json, new TypeReference<LinkedHashMap<String, Object>>() {
//        // });
//        // System.out.println(map1);
//
//        System.out.println("hello");
//
//    }
//
//}
