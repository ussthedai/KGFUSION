package com.usst.kgfusion.entrance;

import com.hankcs.hanlp.HanLP;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: kgfusion
 * @description: mergeItemsAndEntities
 * @author: JH_D
 * @create: 2021-12-06 18:22
 **/

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MergeConceptInfo {
    private Map<Integer, List<Integer>> raw_concept_ens_map;
    private Map<Integer, List<Integer>> raw_concept_item_map;

    private Map<Integer, List<Integer>> new_type_ens_map;

    public void addComputedMap(Map<Integer, List<Integer>> last_res){
        this.new_type_ens_map = last_res;
    }

    public void init(){
        // read
        raw_concept_ens_map = new HashMap<>();
        raw_concept_item_map = new HashMap<>();
    }

    /**
     * 查询与概念相关联的条目文本集合
     * @return
     */
    public List<String> getItemText(Integer concept_id){
        return null;
    }

    public static String aggText(List<String> concept_related_text){
        String str = "";
        for(String s: concept_related_text){
            str += s;
        }
//        提取摘要
        List<String> sentenceList = HanLP.extractSummary(str, str.length());
        String str1 = "";
        for(String s: sentenceList){
            str1 += s;
        }
        return str1;
    }

    public void update(Map<Integer, String> concept_aggText_map){
    }

    public static void main(String[] args) {
        List<String> texts = new ArrayList<>();
        texts.add("文学（Astronomy）是研究宇宙空间天体、宇宙的结构和发展的学科。内容包括天体的构造、性质和运行规律等。天文学是一门古老的科学，自有人类文明史以来，天文学就有重要的地位。主要通过观测天体发射到地球的辐射，发现并测量它们的位置、探索它们的运动规律、研究它们的物理性质、化学组成、内部结构、能量来源及其演化规律。");
        texts.add("天文学的研究对于我们的生活有很大的实际意义，对于人类的自然观有很大的影响。古代的天文学家通过观测太阳、月球和其他一些天体及天象，确定了时间、方向和历法。这也是天体测量学的开端。如果从人类观测天体，记录天象算起，天文学的历史至少已经有五六千年了。天文学在人类早期的文明史中，占有非常重要的地位。埃及的金字塔、欧洲的巨石阵都是很著名的史前天文遗址。哥白尼的日心说曾经使自然科学从神学中解放出来；康德和拉普拉斯关于太阳系起源的星云说，在十八世纪形而上学的自然观上打开了第一个缺口。");
        texts.add("hello");
        System.out.println(new MergeConceptInfo().aggText(texts));
    }

}
