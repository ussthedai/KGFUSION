package com.usst.kgfusion.entrance;

//import com.exp.constructer.GraphConstructer;



import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.usst.kgfusion.module.simFusion;
import com.usst.kgfusion.module.topsim_grm;

public class kgmergeStructure {
//
//       public Map<String,Object> runMergeStructure(KG kg) throws IOException {
////           方法1 原始topsim
//        topsim               sim = new topsim(kg);
//        Map<Integer, HashMap<Integer,Double>> simMap    =sim.TopSimSM(kg);
////        fusion
//        simFusion                          simFusion = new simFusion(simMap, kg);
//        Map<String,Object>  res  = (Map<String, Object>) simFusion.entityFusion(simMap, kg);
//        return res;
//    }

    public Map<String,Object> runMergeStructure_grm(Map<String,Object> res){
//方法2 加了grm


        topsim_grm sim_grm=new topsim_grm(res);
        Map<Integer, HashMap<Integer,Double>> simMap_grm    =sim_grm.TopSimSM(res);
        Map simMap2=sim_grm.weightNormalized(simMap_grm);

        simFusion          simFusion = new simFusion(simMap2, res);
        Map<String,Object> simres    = (Map<String, Object>) simFusion.entityFusion(simMap2, res);
        return simres;
    }

}
