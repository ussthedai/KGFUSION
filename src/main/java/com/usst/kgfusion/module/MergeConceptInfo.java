package com.usst.kgfusion.module;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
     * @param concept_id
     * @return
     */
    public List<String> getItemText(Integer concept_id){
        return null;
    }

    public String aggText(List<String> concept_related_text){
        return null;
    }

    public void update(Map<Integer, String> concept_aggText_map){

    }

}
