package com.usst.kgfusion.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EvoRecEnParam {
    private String entityId;
    private String entityName;
    private String entityType;
    private String ontologySymbol;

}
