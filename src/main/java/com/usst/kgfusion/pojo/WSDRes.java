package com.usst.kgfusion.pojo;

import java.util.List;

import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WSDRes {
    private Boolean success; //目标节点外部关联节点 数组
    private Integer code; //目标节点外部关联关系 关系对组
    private String msg; //移除id 数组
    private String aim_id; //目标节点id
}

