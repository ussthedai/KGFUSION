package com.usst.kgfusion.pojo;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class KG {
    private List<EntityRaw> entities; 
    private List<Triple> triples;
    private Map<EntityRaw, List<EntityRaw>> edges;
    private Map<EntityRaw, List<Integer>> directions; //方向 0 out, 1 in

    public KG(List<EntityRaw> ens, List<Triple> triples){
        this.entities = ens;
        this.triples = triples;
    }

    public KG(List<EntityRaw> ens, Map<EntityRaw, List<EntityRaw>> edges, Map<EntityRaw, List<Integer>> directions){
        this.entities = ens;
        this.edges = edges;
        this.directions = directions;
    }

    
}
