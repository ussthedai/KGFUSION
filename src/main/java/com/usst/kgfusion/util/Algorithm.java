package com.usst.kgfusion.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Algorithm {

    public static List<Integer> topKIndex(double[] arr,  int k){
        BigDecimal[] decimals = new BigDecimal[arr.length];
        for (int i = 0; i < arr.length; i++){
            decimals[i] = new BigDecimal(arr[i]);
        }

        TreeMap<BigDecimal, Integer> map = new TreeMap<BigDecimal, Integer>();
        for (int i = 0; i < arr.length; i++) {
            map.put(decimals[i], i); // 将arr的“值-索引”关系存入Map集合
        }

        if (map.size() == 1) return new ArrayList<>();  // 此时entity 谁都到不了

        int[] sortedIndex = new int[map.size()];
        int n = 0;
        for (Map.Entry<BigDecimal, Integer> me : map.entrySet()) {
            sortedIndex[n++] = me.getValue();
        }


        List<Integer> indexes = new ArrayList<>();
        for (int index : sortedIndex) {
            indexes.add(index);
        }
        
        Collections.reverse(indexes);
        List<Integer> res = new ArrayList<>();
        // 可能出现的情况 res.size() < k
        for (int i = 0; i < k; i++) {
            if (i < indexes.size())
            res.add(indexes.get(i));
        }
        return res;
    }

    public static float levenshtein(String str1, String str2) {

        if(str1 == null || str2 == null) {
            return 0.0f;
        }
        // 计算两个字符串的长度。
        int len1 = str1.length();
        int len2 = str2.length();
        // 建立上面说的数组，比字符长度大一个空间
        int[][] dif = new int[len1 + 1][len2 + 1];
        // 赋初值，步骤B。
        for (int a = 0; a <= len1; a++) {
            dif[a][0] = a;
        }
        for (int a = 0; a <= len2; a++) {
            dif[0][a] = a;
        }
        // 计算两个字符是否一样，计算左上的值
        int temp;
        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {

//                System.out.println("i = " + i + " j = " + j + " str1 = "
//                        + str1.charAt(i - 1) + " str2 = " + str2.charAt(j - 1));
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    temp = 0;
                } else {
                    temp = 1;
                }
                // 取三个值中最小的
                dif[i][j] = Math.min(Math.min(dif[i - 1][j - 1] + temp, dif[i][j - 1] + 1), dif[i - 1][j] + 1);

//                System.out.println("i = " + i + ", j = " + j + ", dif[i][j] = "
//                        + dif[i][j]);
            }
        }
//        System.out.println("字符串\"" + str1 + "\"与\"" + str2 + "\"的比较");
        // 取数组右下角的值，同样不同位置代表不同字符串的比较
//        System.out.println("差异步骤：" + dif[len1][len2]);
        // 计算相似度
        float similarity = 1 - (float) dif[len1][len2]
                / Math.max(str1.length(), str2.length());
//        System.out.println("相似度：" + similarity);
        return similarity;
    }


    public static void main(String[] args) {
        String str1 = "知识图谱演化管理服务类";
        String str2 = "表元数据管理";
        String str3 = "数据管理";
        System.out.println(Algorithm.levenshtein(str1, str2));
    }
}
