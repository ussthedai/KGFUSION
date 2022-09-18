package com.usst.kgfusion.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MatrixUtil {

    // matrix multiplication linear algebra
    public static <T> Map<T, Map<T, Double>> multiply_2map(Map<T, Map<T, Double>> map1, Map<T, Map<T, Double>> map2) {
        Map<T, Map<T, Double>> map2_transpose = transpose(map2);
        Map<T, Map<T, Double>> result = new LinkedHashMap<>();
        for (T key1 : map1.keySet()) {
            Map<T, Double> map1_row = map1.get(key1);
            Map<T, Double> result_row = new LinkedHashMap<>();
            for (T key2 : map2_transpose.keySet()) {
                Map<T, Double> map2_col = map2_transpose.get(key2);
                double sum = 0;
                for (T key3 : map1_row.keySet()) {
                    sum += map1_row.get(key3) * map2_col.get(key3);
                }
                result_row.put(key2, sum);
            }
            result.put(key1, result_row);
        }
        return result;
    }

    public static <T> Map<T, Map<T, Double>> add_2map(Map<T, Map<T, Double>> map1, Map<T, Map<T, Double>> map2) {
        Map<T, Map<T, Double>> res = new LinkedHashMap<>();
        for(T key1: map1.keySet()){
            Map<T, Double> map1_row = map1.get(key1);
            if(map2.containsKey(key1)){
                for(T key2: map2.get(key1).keySet()){
                    if(map1_row.containsKey(key2)){
                        map1_row.put(key2, map1_row.get(key2) + map2.get(key1).get(key2));
                    }else{
                        map1_row.put(key2, map2.get(key1).get(key2));
                    }
                }
            }else{
                res.put(key1, map1_row);
            }
            res.put(key1, map1_row);
        }

        for(T remain: map2.keySet()){
            if(!map1.containsKey(remain)){
                res.put(remain, map2.get(remain));
            }
        }
        return res;
    }

    // transpose matrix
    public static <T> Map<T, Map<T, Double>> transpose(Map<T, Map<T, Double>> map) {
        Map<T, Map<T, Double>> res = new LinkedHashMap<>();
        for (T key : map.keySet()) {
            for (T key2 : map.get(key).keySet()) {
                if (!res.containsKey(key2)) {
                    Map<T, Double> temp = new LinkedHashMap<>();
                    temp.put(key, map.get(key).get(key2));
                    res.put(key2, temp);
                } else {
                    res.get(key2).put(key, map.get(key).get(key2));
                }
            }
        }
        return res;
    }

    // matrix add map
    public static <T> Map<T, Map<T, Double>> add(Map<T, Map<T, Double>> map1, Map<T, Map<T, Double>> map2) {
        Map<T, Map<T, Double>> res = new LinkedHashMap<>();
        for (T key : map1.keySet()) {
            Map<T, Double> temp = new LinkedHashMap<>();
            for (T key2 : map1.get(key).keySet()) {
                temp.put(key2, map1.get(key).get(key2) + map2.get(key).get(key2));
            }
            res.put(key, temp);
        }
        return res;
    }

    // print map with generic parameter
    public static <T> void print(Map<T, Map<T, Double>> m) {
        for (T key1 : m.keySet()) {
            System.out.print(key1 + ": ");
            Map<T, Double> row = m.get(key1);
            for (T key2 : row.keySet()) {
                System.out.print(key2 + ":" + row.get(key2) + " ");
            }
            System.out.println();
        }
    }

    // value mutiplication map
    public static <T> Map<T, Map<T, Double>> multiply(Map<T, Map<T, Double>> map, Double value) {
        Map<T, Map<T, Double>> res = new LinkedHashMap<>();
        for (T key : map.keySet()) {
            Map<T, Double> temp = new LinkedHashMap<>();
            for (T key2 : map.get(key).keySet()) {
                temp.put(key2, map.get(key).get(key2) * value);
            }
            res.put(key, temp);
        }
        return res;
    }

    // get value from map
    public static <T> Double getValue(Map<T, Map<T, Double>> map, T key1, T key2) {
        return map.get(key1).get(key2);
    }

    // set value to map
    public static <T> void setValue(Map<T, Map<T, Double>> map, T key1, T key2, Double value) {
        map.get(key1).put(key2, value);
    }

    // update value to map
    public static <T> void updateValue(Map<T, Map<T, Double>> map, T key1, T key2, Double value) {
        map.get(key1).put(key2, map.get(key1).get(key2) + value);
    }

    // get row from map
    public static <T> Map<T, Double> getRow(Map<T, Map<T, Double>> map, T key) {
        return map.get(key);
    }

    // get column from map
    public static <T> Map<T, Double> getColumn(Map<T, Map<T, Double>> map, T key) {
        Map<T, Double> res = new LinkedHashMap<>();
        for (T key2 : map.keySet()) {
            res.put(key2, map.get(key2).get(key));
        }
        return res;
    }

    // get row according to List
    public static <T> Map<T, Map<T, Double>> getRows(Map<T, Map<T, Double>> map, List<T> key) {
        Map<T, Map<T, Double>> res = new LinkedHashMap<>();
        for (T key2 : key) {
            res.put(key2, map.get(key2));
        }
        return res;
    }

    // normalize map
    public static <T> Map<T, Map<T, Double>> normalize(Map<T, Map<T, Double>> map) {
        Map<T, Map<T, Double>> res = new LinkedHashMap<>();
        for (T key : map.keySet()) {
            Map<T, Double> temp = new LinkedHashMap<>();
            Double sum = 0.0;
            Set<T> keys = map.get(key).keySet();
            for (T key2 : keys) {
                sum += map.get(key).get(key2);
            }
            if (sum == 0.0) {
                for (T key2 : keys) {
                    temp.put(key2, 0.0);
                }
            } else {
                for (T key2 : keys) {
                    temp.put(key2, map.get(key).get(key2) / sum);
                }
            }

            res.put(key, temp);
        }
        return res;
    }

    // get column according to List
    public static <T> Map<T, Map<T, Double>> getColumns(Map<T, Map<T, Double>> map, List<T> key) {
        Map<T, Map<T, Double>> res = new LinkedHashMap<>();
        for (T key2 : key) {
            Map<T, Double> temp = new LinkedHashMap<>();
            for (T key3 : map.keySet()) {
                temp.put(key3, map.get(key3).get(key2));
            }
            res.put(key2, temp);
        }
        return res;
    }

    public static void main(String[] args) {

        // generate a random matrix
        Map<Integer, Map<Integer, Double>> map = new LinkedHashMap<>();
        for (int i = 0; i < 3; i++) {
            Map<Integer, Double> temp = new LinkedHashMap<>();
            for (int j = 0; j < 3; j++) {
                temp.put(Integer.valueOf(j), 1.0);
            }
            map.put(Integer.valueOf(i), temp);
        }

        print(map);
        System.out.println();

        // // generate a random matrix
        // Map<Integer, Map<Integer, Double>> map2 = new LinkedHashMap<>();
        // for (int i = 0; i < 3; i++) {
        //     Map<Integer, Double> temp = new LinkedHashMap<>();
        //     for (int j = 0; j < 3; j++) {
        //         temp.put(Integer.valueOf(j), (int) (Math.random() * 10) + 1.0);
        //     }
        //     map2.put(Integer.valueOf(i), temp);
        // }

        // print(map2);
        // System.out.println();

        // // test matrix multiplication
        // Map<Integer, Map<Integer, Double>> res = MatrixUtil.multiply(map, map2);
        // print(res);
        // System.out.println();

        // // test matrix add
        // Map<Integer, Map<Integer, Double>> res2 = MatrixUtil.add(map, map2);
        // print(res2);
        // System.out.println();

        // // test matrix transpose
        // Map<Integer, Map<Integer, Double>> res3 = MatrixUtil.transpose(map);
        // print(res3);
        // System.out.println();

        // // test matrix multiplication
        // Map<Integer, Map<Integer, Double>> res4 = MatrixUtil.multiply(map, 2.0);
        // print(res4);
        // System.out.println();

        // // test getrow
        // Map<Integer, Double> res5 = MatrixUtil.getRow(map, 0);
        // System.out.println(res5);
        // System.out.println();

        // // test getcolumn
        // Map<Integer, Double> res6 = MatrixUtil.getColumn(map, 0);
        // System.out.println(res6);
        // System.out.println();

        // // test get value
        // System.out.println(MatrixUtil.getValue(map, 0, 0));
        // System.out.println();

        // // test set value
        // MatrixUtil.setValue(map, 0, 0, 10.0);
        // print(map);
        // System.out.println();

        // // test update value
        // MatrixUtil.updateValue(map, 0, 0, 10.0);
        // print(map);
        // System.out.println();

        // test normalize
        Map<Integer, Map<Integer, Double>> res7 = MatrixUtil.normalize(map);
        print(res7);
        System.out.println();

        // // test get rows
        // List<Integer> key = new ArrayList<>();
        // key.add(0);
        // key.add(1);
        // Map<Integer, Map<Integer, Double>> res8 = MatrixUtil.getRows(map, key);
        // print(res8);
        // System.out.println();

        // // test get columns
        // List<Integer> key2 = new ArrayList<>();
        // key2.add(0);
        // key2.add(1);
        // Map<Integer, Map<Integer, Double>> res9 = MatrixUtil.getColumns(map, key2);
        // print(res9);
        // System.out.println();

        

    }
}
