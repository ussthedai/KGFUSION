package com.usst.kgfusion.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WSDData{
    private String aim_neighbor_ids;
    private String aim_neighbor_relations;
    private List<Integer> remove_ids;
    private int aim_id;
}
