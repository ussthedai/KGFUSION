package com.usst.kgfusion.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EvoRecReParam {
    private String relationId;
    private String headId;
    private String tailId;
    private String relationType;
    private String ontologySymbol;
    
}
