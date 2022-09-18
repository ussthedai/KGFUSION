package com.usst.kgfusion.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapUtil {
    // sort map by value
    public static <T> Map<T, Double> sortByValue(Map<T, Double> map, boolean isAscending) {
        if (map == null || map.size() == 0) {
            return null;
        }
        List<Map.Entry<T, Double>> list = new LinkedList<Map.Entry<T, Double>>(map.entrySet());
        if(list.size() == 0) {
            System.out.println("list size is 0");
        }
        // revome null entry from list
        for(int i = 0; i < list.size(); i++) {
            if(list.get(i).getValue() == null) {
                list.remove(i);
            }
        }
        Collections.sort(list, new Comparator<Map.Entry<T, Double>>() {
            public int compare(Map.Entry<T, Double> o1, Map.Entry<T, Double> o2) {
                if (isAscending) {
                    return o1.getValue().compareTo(o2.getValue());
                } else {
                    return o2.getValue().compareTo(o1.getValue());
                }
            }
        });

        Map<T, Double> result = new LinkedHashMap<T, Double>();
        for (Map.Entry<T, Double> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    // find threshhold from map by value
    public static <T> Double findThreshold(Map<T, Double> map, Double mu){
        // find max and min value from map
        Double max = Double.MIN_VALUE;
        Double min = Double.MAX_VALUE;
        for (Map.Entry<T, Double> entry : map.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
            }
            if (entry.getValue() < min) {
                min = entry.getValue();
            }
        }
        // return result according (max - min)*mu + min
        return (max - min) * mu + min;
    }

    // remove entry from map which entry's value is less than threshold
    public static <T> void removeEntryWithThreshold(Map<T, Double> map, Double threshold) {
        map.values().removeIf(filter -> filter <= threshold);
    }


    // find topk indexs
    public static <T> List<T> findTopK(Map<T, Double> map, int k) {
        List<T> res = new ArrayList<>();
        Map<T, Double> temp = sortByValue(map, false);
        for (T key : temp.keySet()) {
            res.add(key);
            if (res.size() == k)
                break;
        }
        return res;
    }

    // find topk items
    public static <T> Map<T, Double> findTopKWithValue(Map<T, Double> map, int k) {
        Map<T, Double> res = new LinkedHashMap<>();
        Map<T, Double> temp = sortByValue(map, false);
        for (T key : temp.keySet()) {
            res.put(key, temp.get(key));
            if (res.size() == k)
                break;
        }
        return res;
    }

    // find indexs with threshold
    public static <T> List<T> findItemWithThreshold(Map<T, Double> map, Double threshold) {
        List<T> res = new ArrayList<>();
        for (T key : map.keySet()) {
            if (map.get(key) >= threshold)
                res.add(key);
        }
        return res;
    }

    // find items with threshold
    public static <T> Map<T, Double> findItemWithThresholdWithValue(Map<T, Double> map, Double threshold) {
        Map<T, Double> res = new LinkedHashMap<>();
        for (T key : map.keySet()) {
            if (map.get(key) >= threshold)
                res.put(key, map.get(key));
        }
        return res;
    }

    // normalize map
    public static <T> Map<T, Double> normalizeMap(Map<T, Double> map) {
        Map<T, Double> res = new LinkedHashMap<>();
        double sum = 0;
        for (T key : map.keySet()) {
            sum += map.get(key);
        }
        if (sum == 0) {
            for (T key : map.keySet()) {
                res.put(key, 0.0);
            }
        } else {
            for (T key : map.keySet()) {
                res.put(key, map.get(key) / sum);
            }
        }

        return res;
    }

    // print map
    public static <T> void printMap(Map<T, Double> map) {
        for (T key : map.keySet()) {
            System.out.println(key + ": " + map.get(key));
        }
    }

    // write map to file
    public static <T> void witeMapToFile(Map<T, Map<T, Double>> map, String filePath) throws IOException{
        BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
        try{
            for (T key : map.keySet()) {
                for (T key2 : map.get(key).keySet()) {
                    bw.write(key + "\t" + key2 + "\t" + map.get(key).get(key2)  + "\n");
                }
            }
        }catch (IOException e){
            throw e;
        }finally{
            if(bw != null){
                bw.close();
            }
        }
    }

    // remove key2 which between start and end
    public static Map<Integer, Double> selectByRange(Map<Integer, Double> map, Integer start, Integer end) {
        if(map.size() == 0 || map == null){
            return new LinkedHashMap<>();
        }

        Map<Integer, Double> res = new LinkedHashMap<>();
        for (Integer key : map.keySet()) {
            if (key >= start && key <= end) {
                res.put(key, map.get(key));
            }
        }
        return res;
    }

    public static int findIdxInOrderedMap(Map<Integer, Double> map, Integer target){  // LinkedHas
        int idx = 1;
        for(Map.Entry<Integer, Double> entry: map.entrySet()){
            if(entry.getKey() - target == 0){
                break;
            }
            idx++;
        }
        return idx;
    }


    public static int findIdxInOrderedMap2(Map<String, Double> map, String target){  // LinkedHas
        int idx = 1;
        for(Map.Entry<String, Double> entry: map.entrySet()){
            if(entry.getKey().equals(target)){
                break;
            }
            idx++;
        }
        return idx;
    }


    public static <T> Map<T, Double> removeByIds(Map<T, Double> map, Set<T> ids, Set<T> reservedIds) {
        if(map.size() == 0 || map == null){
            return new LinkedHashMap<>();
        }
        
        Map<T, Double> res = new LinkedHashMap<>();
        for (T key : map.keySet()) {
            if (!ids.contains(key)) {
                res.put(key, map.get(key));
            }
        }
        if (reservedIds.size() > 0) {
            for (T key : reservedIds) {
                res.put(key, map.get(key));
            }
        }
        return res;
    }


    // write map to file
    public static <K,V> void writeMapToFileEasy(Map<K,V> map, String filePath) throws IOException{
        BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
        try{
            for (K key : map.keySet()) {
                bw.write(key + "\t" + map.get(key) + "\n");
            }
        }catch(IOException e){
            throw e;
        }finally{
            if(bw != null){
                bw.close();
            }
        }
    }

    // add two map
    public static <T> Map<T,Double> add(Map<T, Double> map1, Map<T,Double> map2){
        Map<T, Double> res = new LinkedHashMap<>();
        for(T key: map1.keySet()){
            res.put(key, map1.get(key));
        }
        for(T key: map2.keySet()){
            if(res.containsKey(key)){
                res.put(key, res.get(key) + map2.get(key));
            }else{
                res.put(key, map2.get(key));
            }
        }
        return res;
    }
}
