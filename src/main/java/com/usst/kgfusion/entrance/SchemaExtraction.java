package com.usst.kgfusion.entrance;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.usst.kgfusion.constructer.SchecmaReader;
import com.usst.kgfusion.module.ForwardPush;
import com.usst.kgfusion.module.GRM;
import com.usst.kgfusion.pojo.EntityRaw;
import com.usst.kgfusion.pojo.KG;
import com.usst.kgfusion.util.Algorithm;
import com.usst.kgfusion.util.JsonUtil;
import com.usst.kgfusion.util.MatrixUtil;

import java.io.InputStreamReader;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SchemaExtraction {

    private static final Logger logger = LoggerFactory.getLogger("com.usst.test");

    private Map<String, String> seeds;
    private Map<String, String> typeParentMap;
    private KG kg;
    private ForwardPush<String> forwardPush;
    private Map<String, Map<String, Double>> outMap;
    private Set<String> matchedEntitySet;
    private Set<String> queryIds; // all entity from query
    private Map<String, String> id2name;
    private Set<String> choices;

    public SchemaExtraction(KG inKG){
        this.kg = inKG;
        try {
            init();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            logger.error("图初始化错误");
        }
    }
    
    public void init() throws IOException {
        geSeeds();
        geTypeParentMap();
        geOutMap();
        geQueryNamesAndIdx2NameMap();
        geChoices();
    }

    public void geChoices(){
        Set<String> res = this.typeParentMap.keySet();
        this.choices = res;
    }

    public void geQueryNamesAndIdx2NameMap(){
        if(this.queryIds == null){
            this.queryIds = new HashSet<>();
        }
        if(this.id2name == null){
            this.id2name = new LinkedHashMap<>();
        }
        List<EntityRaw> entities = kg.getEntities();
        for (EntityRaw entity : entities) {
            queryIds.add(entity.getEntityId());
            id2name.put(entity.getEntityId(), entity.getName());
        }
    }

    public void geSeeds() throws IOException {
        // String seedsFile = "src\\main\\resources\\static\\seed.json";
        // String jsonStr = JsonUtil.readJsonStr(seedsFile);
        String jsonStr = "";
        InputStream inputStream = null;
        try{
            inputStream = this.getClass().getClassLoader().getResourceAsStream("static/seed.json");
            jsonStr = JsonUtil.readJsonStr(inputStream);
        }catch(IOException e){
            logger.error("读取seed.json错误");
        }finally{
            if(inputStream != null){
                try{
                    inputStream.close();
                }catch(IOException e){
                    logger.error("failed to close file");
                }
                
            }
        }
        this.seeds = JSON.parseObject(jsonStr, new TypeReference<LinkedHashMap<String, String>>() {}); 
    }

    public void geTypeParentMap() throws IOException {
        // this.typeParentMap = SchecmaReader.getSchema(); // read from neo4j
        // read schema from local file
        // String schemaFile = "src\\main\\resources\\static\\c_info.csv";
        String line = null;
        InputStream inputStream = null;
        BufferedReader br = null;
        try{
            inputStream = this.getClass().getClassLoader().getResourceAsStream("static/c_info.csv");
            br = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
            while ((line = br.readLine()) != null) {
                String[] strs = line.split(",");
                String child = strs[0];
                String parent = strs[2];
                if (typeParentMap == null) {
                    typeParentMap = new HashMap<>();
                }
                typeParentMap.put(child, parent);
            }
        }catch(IOException e){
            logger.error("c_info文件读取错误");
        }finally{
            if(inputStream != null){
                try{
                    inputStream.close();
                }catch(IOException e){
                    logger.error("failed to close file");
                }
            }
            if(br != null){
                try{
                    br.close();
                }catch(IOException e){
                    logger.error("failed to close file");
                }
                
            }
        }
        

    }

    public void geOutMap() {
        this.outMap = MatrixUtil.normalize(GRM.geNodesWeightMap(this.kg));
        this.forwardPush = new ForwardPush<String>(0.2, 1.0, this.outMap);
    }

    public Set<String> choice(){
        Set<String> res = new HashSet<>();
        int num = 1;
//        Random random = new Random(100);
        for(int i = 0; i < num; i++){
            int idx = (int) (Math.random() * this.choices.size());
//            int idx = (int)(random.nextDouble() * this.choices.size());
            res.add(this.choices.toArray()[idx].toString());
        }
        return res;
    }

    public Boolean match(){
        if(this.matchedEntitySet == null){
            this.matchedEntitySet = new HashSet<>();
        }
        Set<String> seedsSet = new HashSet<String>(this.seeds.keySet());
        List<EntityRaw> ens = this.kg.getEntities();
        for(EntityRaw en : ens){
            if(seedsSet.contains(en.getName())){
                matchedEntitySet.add(en.getEntityId());
//                System.out.println(en.getName());
            }
        }
        if(matchedEntitySet.size() > 0){
            return true;
        }
        return false;
    }


    /**
     * generate random res
     * @return
     */
    public Map<String, Map<String, Double>> randomWalk(){
        Map<String, Map<String, Double>> res = new LinkedHashMap<String, Map<String, Double>>();
        for(String queryId: queryIds){
            forwardPush.computeWholeGraphPPR(queryId, 0.00001);
            res.put(queryId, forwardPush.getReserveCopy());
        }
        return res;
    }

    // for graph cluster
    public Set<String> getConceptSet(Set<Set<String>> cluster){
        Set<String> res = new HashSet<>();
        for(Set<String> item: cluster){
            double maxValue = 0.0;
            String bestChoice = "";
            for(String choiceName: this.choices){
                double sum = 0.0;
                for(String enName: item){
                    double simValue = Algorithm.levenshtein(enName, choiceName);
                    sum += simValue;
                }
                if(sum > maxValue){
                    maxValue = sum;
                    bestChoice = choiceName;
                }
            }
            res.add(bestChoice);
        }
        return res;
    }

    public Map<String, String> getConceptSetWithParent(Set<Set<String>> cluster){
        Map<String, String> ans = new HashMap<>();
        Set<String> res = new HashSet<>();
        for(Set<String> item: cluster){
            double maxValue = 0.0;
            String bestChoice = "";
            for(String choiceName: this.choices){
                double sum = 0.0;
                for(String enName: item){
                    double simValue = Algorithm.levenshtein(enName, choiceName);
                    sum += simValue;
                }
                if(sum > maxValue){
                    maxValue = sum;
                    bestChoice = choiceName;
                }
            }
            res.add(bestChoice);
        }

        if(res.size() > 0){
            for(String item: res){
                ans.put(typeParentMap.get(item), item);
            }
        }
        return ans;
    }

    /**
     * recommend types
     * @return
     */
    public Set<String> evoRecommend(){
        Boolean flag = match();
        if(!flag){
//            Set<String> choices = choice();
//            return choices;
            // return new HashSet<>();

            // use edit distance to return res
            Set<String> res = new HashSet<>();  // select from choices
            List<EntityRaw> ens = this.kg.getEntities();
            List<String> ensNames = new ArrayList<>();
            for(EntityRaw entity: ens){
                ensNames.add(entity.getName());
            }
            double maxValue = 0.0;
            String bestChoice = "";
            for(String choiceName: this.choices){
                double sum = 0.0;
                for(String enName: ensNames){
                    double simValue = Algorithm.levenshtein(enName, choiceName);
                    sum += simValue;
                }
                if(sum > maxValue){
                    maxValue = sum;
                    bestChoice = choiceName;
                }
            }
            res.add(bestChoice);
            return res;

        }
        Set<String> res = new HashSet<>();
        Map<String, Map<String, Double>> randomWalkRes = randomWalk();
        for(Entry<String, Map<String, Double>> entry : randomWalkRes.entrySet()){
            Map<String, Double> map = entry.getValue();
            // find max value from queryIds
            Double maxValue = 0.0;
            String maxKey = "";
            for(String queryId: matchedEntitySet){
                if(map.containsKey(queryId) && map.get(queryId) > maxValue){
                    maxValue = map.get(queryId);
                    maxKey = queryId;
                }
                
            }

            res.add(seeds.get(id2name.get(maxKey)));
        }
        return res;
    }


    public Map<String, String> evoAuto(){
        Boolean flag = match();
        if(!flag){
            Map<String, String> res = new LinkedHashMap<String, String>();
//            Set<String> choices = choice();
            Set<String> choices = new HashSet<>();  // select from choices
            List<EntityRaw> ens = this.kg.getEntities();
            List<String> ensNames = new ArrayList<>();
            for(EntityRaw entity: ens){
                ensNames.add(entity.getName());
            }

            double maxValue = 0.0;
            String bestAnswer = "";
            for(String choiceName: this.choices){
                double sum = 0.0;
                for(String enName: ensNames){
                    double simValue = Algorithm.levenshtein(enName, choiceName);
                    sum += simValue;
                }
                if(sum > maxValue){
                    maxValue = sum;
                    bestAnswer = choiceName;
                }
                if(sum / ensNames.size() > 0.5){
                    choices.add(choiceName);
                }
            }

            if(choices.size() > 0){
                for(String choice : choices){
                    if(typeParentMap.containsKey(choice)){
                        res.put(typeParentMap.get(choice), choice);
                    }

                }
            }else{
                if(bestAnswer.length() > 0){
                    res.put(typeParentMap.get(bestAnswer), bestAnswer);
                }
            }

            return res;
        }
        Set<String> typeLevel1s = new HashSet<>();
        Map<String, Map<String, Double>> randomWalkRes = randomWalk();
        for(Entry<String, Map<String, Double>> entry : randomWalkRes.entrySet()){
            Map<String, Double> map = entry.getValue();
            // find max value from queryIds
            Double maxValue = 0.0;
            String maxKey = "";
            for(String queryId: matchedEntitySet){
                if(map.containsKey(queryId) && map.get(queryId) > maxValue){
                    maxValue = map.get(queryId);
                    maxKey = queryId;
                }
                
            }
            typeLevel1s.add(seeds.get(id2name.get(maxKey)));
        }

        Map<String, String> evoAutoRes = new HashMap<>();
        for(String typeLevel1: typeLevel1s){
            if(typeParentMap.containsKey(typeLevel1)){
                evoAutoRes.put(typeParentMap.get(typeLevel1), typeLevel1); // parent child
            }
            
        }
        return evoAutoRes;
    }


    
}
