package com.usst.kgfusion.pojo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Data;

@Data
public class EnSim {
    private Long id;
    private String name;
    private String type;

    public EnSim(Long id, String name) {
        this.id = id;
        this.name = name;
    }
    public EnSim(Long id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }



    public static void main(String[] args) {
        Map<EnSim, Set<EnSim>> map = new HashMap<>();
        EnSim enSim1 = new EnSim(1L, "1");
        EnSim enSim2 = new EnSim(2L, "1");
        EnSim enSim3 = new EnSim(3L, "3");
        EnSim enSim4 = new EnSim(2L, "1");

        map.put(enSim1, new HashSet<>());
        map.get(enSim1).add(enSim2);
        map.put(enSim3, new HashSet<>());
        map.get(enSim3).add(enSim4);
        System.out.println(map);
        System.out.println(map.get(enSim1).contains(enSim4));

        
    }
}
