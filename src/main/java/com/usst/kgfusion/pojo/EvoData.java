package com.usst.kgfusion.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EvoData{
    private String entityName;
    private String entityType;
    private String ontologySymbol;
    private String summary;
}
