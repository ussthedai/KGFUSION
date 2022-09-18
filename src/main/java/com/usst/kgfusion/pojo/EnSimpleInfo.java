package com.usst.kgfusion.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

/**
 * @program: kgfusion
 * @description:
 * @author: JH_D
 * @create: 2022-01-03 18:47
 **/

@Data
@AllArgsConstructor
@ToString
public class EnSimpleInfo{
    private Long sourceNodeId;
    private String sourceNodeName;
//    private Double score;
}
