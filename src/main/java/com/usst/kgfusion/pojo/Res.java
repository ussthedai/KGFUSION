package com.usst.kgfusion.pojo;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Res {
    private Boolean success;
    private Integer code;
    private String msg;
    private List<EvoData> data;
}


