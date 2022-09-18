package com.usst.kgfusion.constructer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.usst.kgfusion.databaseQuery.BasicOperation;
import org.neo4j.driver.v1.Record;

public class SchecmaReader {
    public static Map<String, String> getSchema(){
        Map<String, String> res = new HashMap<>();
        List<Record> records = BasicOperation.querySchema();
        for(Record record: records){
            String parent = record.asMap().get("parent").toString();
            String child = record.asMap().get("child").toString();
            res.put(child, parent);
        }

        return res;
        
    }

}
