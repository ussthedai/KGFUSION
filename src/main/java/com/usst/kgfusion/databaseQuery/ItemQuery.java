package com.usst.kgfusion.databaseQuery;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.usst.kgfusion.util.KingBaseUtils;

public class ItemQuery {
    private static final Logger logger = LoggerFactory.getLogger("com.usst.test");

    // 测试工具类

    public static void main(String[] args) throws SQLException {
//        Set<String> ids = new HashSet<>();
//        ids.add("123321");
//        Map<String, String> res = queryItem(ids, "misre_KM_item");
//        System.out.println(res);
//        ItemQuery.insertConceptItemInfo(144481L, "hellXoo");
        ItemQuery.updateConceptInfo(123321L, "append");

    }

    public static void InsertToDataBase(Map<String,Map<String,Object>> ress){

        for(Map.Entry<String,Map<String,Object>> entry:ress.entrySet()) {

            Map<String, Object> restemp = entry.getValue();
            String itemid = (String) restemp.get("itemid");
            String symbol = (String) restemp.get("symbol");
            String itemcontent = (String) restemp.get("itemcontent");
            String USER = "admin";
            boolean type = (boolean) restemp.get("type");
            String recordSql = "";

            if(type){
                String time = new Timestamp(System.currentTimeMillis())+"";
                //recordSql = "insert into misre_km_wdtph_tmnr(ItemID,graph_symbol,sentence_content,create_time,create_by) values ('"+itemid+"','"+symbol+"','"+itemcontent+"','"+time+"','"+USER+"')";
                recordSql = "insert into misre_km_wdtph_tmnr(item_id,graph_symbol,sentence_content,create_time,create_by) values ('"+itemid+"','"+symbol+"','"+itemcontent+"','"+time+"','"+USER+"')";

                BasicOperation.setProperty1(entry.getKey(), symbol, "ItemId", itemid);
            }else {
                String time = new Timestamp(System.currentTimeMillis())+"";
                //recordSql = "UPDATE misre_km_wdtph_tmnr SET sentence_content = '"+itemcontent+"',update_time = '"+time+"',update_by = '"+USER+"' WHERE ItemID = '"+itemid+"'";
                recordSql = "UPDATE misre_km_wdtph_tmnr SET sentence_content = '"+itemcontent+"',update_time = '"+time+"',update_by = '"+USER+"' WHERE item_id = '"+itemid+"'";

            }



            Connection conn  = null;
            Statement s = null;
            ResultSet rs = null;
            try {
                //conn = KingBaseUtils.getConnection1();
                conn = KingBaseUtils.getConnection1();
                s = conn.createStatement();
                s.executeUpdate(recordSql);
            }catch (SQLException e) {
                logger.error("sql error");
            }finally {
                //KingBaseUtils.close(rs,s,conn);
                try{
                    KingBaseUtils.close(rs,s,conn);
                }catch(SQLException e){
                    logger.error("release connection failed");
                }
                
            }




        }
    }


    public static Map<String, String> queryItem(Set<String> queryIds){
        Map<String, String> res = new HashMap<>();
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            //conn = KingBaseUtils.getConnection1();
            conn = KingBaseUtils.getConnection1();
            //定义sql
            //String sql = "select tag_sentence_id, sentence_content from misre_km_bz_bzjznr where tag_sentence_id = ?";
            String sql = "select item_id, sentence_content from misre_km_wdtph_tmnr where item_id = ?";
            //获取执行sql的对象
            st = conn.prepareStatement(sql);
            //执行sql
            for (String queryId: queryIds){
                st.setString(1, queryId);
                rs = st.executeQuery();
                while (rs.next()){
                    String item_id = rs.getString("item_id");
                    String description = rs.getString("sentence_content");
                    //String item_id = rs.getString("tag_sentence_id");
                    //String description = rs.getString("sentence_content");
                    if (!res.containsKey(item_id)){
                        res.put(item_id, description);
                    }
                }
            }
            
            
        } catch (SQLException e) {
            logger.error("sql error");
        }finally {
            //KingBaseUtils.close(rs,st,conn);
            try{
                KingBaseUtils.close(rs,st,conn);
            }catch(SQLException e){
                logger.error("release connection failed");
            }
            
        }
        return res;
    }


    public static Map<String, String> queryItem(Set<String> item_ids, String table){
        Map<String, String> res = new HashMap<>();
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            //conn = KingBaseUtils.getConnection1();
            conn = KingBaseUtils.getConnection1();
            //定义sql
            if(table.equals("misre_km_bz_bzjznr")){
                String sql = "select sentence_original_id, sentence_content from " + table + " where sentence_original_id = ?";
                //获取执行sql的对象
                st = conn.prepareStatement(sql);
                //执行sql
                for (String queryId: item_ids){
                    st.setString(1, queryId);
                    rs = st.executeQuery();
                    while (rs.next()){
                        String item_id = rs.getString("sentence_original_id");
                        String description = rs.getString("sentence_content");
                        if (!res.containsKey(item_id)){
                            res.put(item_id, description);
                        }
                    }
                }
            }
            if(table.equals("misre_km_wdtph_tmnr")){
                String sql = "select item_id, sentence_content from " + table + " where item_id = ?";
                //获取执行sql的对象
                st = conn.prepareStatement(sql);
                //执行sql
                for (String queryId: item_ids){
                    st.setString(1, queryId);
                    rs = st.executeQuery();
                    while (rs.next()){
                        String item_id = rs.getString("item_id");
                        String description = rs.getString("sentence_content");
                        if (!res.containsKey(item_id)){
                            res.put(item_id, description);
                        }
                    }
                }
            }
            if(table.equals("misre_KM_item")){
                String sql = "select item_id, description from " + table + " where item_id = ?";
                //获取执行sql的对象
                st = conn.prepareStatement(sql);
                //执行sql
                for (String queryId: item_ids){
                    st.setString(1, queryId);
                    rs = st.executeQuery();
                    while (rs.next()){
                        String item_id = rs.getString("item_id");
                        String description = rs.getString("description");
                        if (!res.containsKey(item_id)){
                            res.put(item_id, description);
                        }
                    }
                }
            }


        } catch (SQLException e) {
            logger.error("sql error");
        }finally {
            //KingBaseUtils.close(rs,st,conn);
            try{
                KingBaseUtils.close(rs,st,conn);
            }catch(SQLException e){
                logger.error("release connection failed");
            }
            
        }
        return res;
    }

    public static Map<String, String> queryItemIds_new(Set<String> item_ids, int table){
        Map<String, String> res = new HashMap<>();
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            //conn = KingBaseUtils.getConnection1();
            conn = KingBaseUtils.getConnection1();
            //定义sql
            if(table==60){
                String sql = "select sentence_original_id, sentence_content from misre_km_bz_bzjznr where sentence_original_id = ?";
                //获取执行sql的对象
                st = conn.prepareStatement(sql);
                //执行sql
                for (String queryId: item_ids){
                    st.setString(1, queryId);
                    rs = st.executeQuery();
                    while (rs.next()){
                        String item_id = rs.getString("sentence_original_id");
                        String description = rs.getString("sentence_content");
                        if (!res.containsKey(item_id)){
                            res.put(item_id, description);
                        }
                    }
                }
            }
            if(table==61){
                String sql = "select item_id, sentence_content from misre_km_wdtph_tmnr where item_id = ?";
                //获取执行sql的对象
                st = conn.prepareStatement(sql);
                //执行sql
                for (String queryId: item_ids){
                    st.setString(1, queryId);
                    rs = st.executeQuery();
                    while (rs.next()){
                        String item_id = rs.getString("item_id");
                        String description = rs.getString("sentence_content");
                        if (!res.containsKey(item_id)){
                            res.put(item_id, description);
                        }
                    }
                }
            }
            if(table==10){
                String sql = "select item_id, description from misre_KM_item where item_id = ?";
                //获取执行sql的对象
                st = conn.prepareStatement(sql);
                //执行sql
                for (String queryId: item_ids){
                    st.setString(1, queryId);
                    rs = st.executeQuery();
                    while (rs.next()){
                        String item_id = rs.getString("item_id");
                        String description = rs.getString("description");
                        if (!res.containsKey(item_id)){
                            res.put(item_id, description);
                        }
                    }
                }
            }


        } catch (SQLException e) {
            logger.error("sql error");
        }finally {
            //KingBaseUtils.close(rs,st,conn);
            try{
                KingBaseUtils.close(rs,st,conn);
            }catch(SQLException e){
                logger.error("release connection failed");
            }
            
        }
        return res;
    }

    public static void insertConceptItemInfo(Long item_id, String aggText) {
        Connection conn = null;
        PreparedStatement st = null;
        try {
            //conn = KingBaseUtils.getConnection1();
            conn = KingBaseUtils.getConnection1();
            //定义sql
            // String c = "\'";
//            String sql = "INSERT INTO misre_km_item (item_id, doc_id, system_id, doc_source, version_id, disid, parent_id, name, description, item_type, table_key, dispaly_sec_num, sec_num, sort_num, remarks, source_org, source_person, source_time, confirmer, ctime, modifier, mtime, priority, difficulty, sign, stage, progress, deleted, with_sec_num, dev_type, extend_attributes, num_id, edit_state, item_source, item_state, item_disid, is_alter) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            // String sql = "insert into misre_km_item set " + "item_id=" + item_id.toString() + "," + "description="+ c +aggText+c + ","+ "doc_id=" + 0L + ","+ "doc_source=" + "''" + ","+ "version_id=" + 0L + ","+ "parent_id=" + 0L + ","+ "sort_num=" + 0.0 + ","+ "modifier=" + 0L;
            
            String sql = "insert into misre_km_item set item_id = ?, description = ?, doc_id = ?, doc_source = ?, version_id = ?, parent_id = ?, sort_num = ?, modifier = ?";

            //获取执行sql的对象
            st = conn.prepareStatement(sql);
            st.setLong(1, item_id);
            st.setString(2, aggText);
            st.setLong(3, 0L);
            st.setString(4, "''");
            st.setLong(5, 0L);
            st.setLong(6, 0L);
            st.setDouble(7, 0.0);
            st.setLong(8, 0L);
            //执行sql
            st.executeUpdate();
        } catch (SQLException e) {
            logger.error("sql error");
        }finally {
            //KingBaseUtils.close(st,conn);
            try{
                KingBaseUtils.close(st,conn);
            }catch(SQLException e){
                logger.error("release connection failed");
            }
            
        }
    }


    public static void updateConceptInfo(Long item_id, String aggText) {
        Connection conn = null;
        PreparedStatement st = null;
        try {
            //conn = KingBaseUtils.getConnection1();
            conn = KingBaseUtils.getConnection1();
            //定义sql
            String c = "\'";
            // update misre_km_item set description = concat(description, "hello") where item_id = 1233
            // String sql = "update misre_km_item set description = concat(description," + c + aggText + c + ") where item_id="+ item_id;
            String sql = "updata_misre_km_item set description = concat(description, ? ) where item_id = ?";
            //获取执行sql的对象
            st = conn.prepareStatement(sql);
            st.setString(1, aggText);
            st.setLong(2, item_id);
            //执行sql
            st.executeUpdate();

        } catch (SQLException e) {
            logger.error("sql error");
        }finally {
            //KingBaseUtils.close(st,conn);
            try{
                KingBaseUtils.close(st,conn);
            }catch(SQLException e){
                logger.error("release connection failed");
            }
            
            
            
        }

    }

}
