package com.usst.kgfusion.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EntityRaw {
    private String entityId;
    private String name;
    private String entityType; // 实体粗类型 
    private String entitySubClass; // 实体细类型
    private String graphSymbol; // graph 标识
    private String itemId; // 所在条目
    private String srcId;  // 来源id，合并时需要更改 默认未0
    // private String ontologySymbol;


}
