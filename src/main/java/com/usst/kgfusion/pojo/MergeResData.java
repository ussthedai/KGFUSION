package com.usst.kgfusion.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @program: kgfusion
 * @description: 返回相似度计算结果
 * @author: JH_D
 * @create: 2022-01-03 18:49
 **/

@Data
@AllArgsConstructor
@ToString
public class MergeResData {
    private Boolean success;
    private Integer code;
    private String msg;
    private List<SimResItem> data;
}
