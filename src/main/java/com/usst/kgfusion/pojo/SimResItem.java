package com.usst.kgfusion.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * @program: kgfusion
 * @description: 相似度计算记过
 * @author: JH_D
 * @create: 2022-01-03 18:36
 **/



@Data
@AllArgsConstructor
@ToString
@NoArgsConstructor
public class SimResItem {
    private Long aimNodeId;
    private String aimNodeName;
    private List<EnSimpleInfo> simRes;
}
