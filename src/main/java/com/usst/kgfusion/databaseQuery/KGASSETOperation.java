package com.usst.kgfusion.databaseQuery;

import com.usst.kgfusion.pojo.EntityRaw;
import com.usst.kgfusion.pojo.LevelAmbiguTaskRecord;
import com.usst.kgfusion.pojo.LevelAmbiguTaskRes;
import com.usst.kgfusion.util.KingBaseUtils;
import com.usst.kgfusion.util.KingBaseUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;

/**
 * @program: kgfusion
 * @description: 记录综合过程
 * @author: JH_D
 * @create: 2022-01-06 14:42
 **/

public class KGASSETOperation {

    private static final Logger logger = LoggerFactory.getLogger("com.usst.test");


    public static void insertIntoKgAsset(Map<String, String> record){
        Connection conn = null;
        PreparedStatement st = null;
        // ResultSet rs = null;
        String synthesize_link_id = "";
        String kg_symbol_synthesize = "";
        String kg_symbol_doc = "";
        String generate_status = "";
        String generate_begin_time = "";
        String generate_end_time = "";
        String generate_msg = "";
        String synthesize_status = "";
        String synthesize_begin_time = "";
        String synthesize_end_time = "";
        String synthesize_msg = "";
        String analyse_status = "";
        String analyse_begin_time = "";
        String analyse_end_time = "";
        String analyse_msg = "";

        int generate_status_int = 2;
        int synthesize_status_int = 0;
        int analyse_status_int = 0;
        Timestamp generate_begin_time_ts = null;
        Timestamp generate_end_time_ts = null;
        Timestamp synthesize_begin_time_ts = null;
        Timestamp synthesize_end_time_ts = null;
        Timestamp analyse_begin_time_ts = null;
        Timestamp analyse_end_time_ts = null;
        Map<String,Object> columnName = new LinkedHashMap<String,Object>();
        List<String> IntegerName = new ArrayList<>();
        IntegerName.add("generate_status");
        IntegerName.add("synthesize_status");
        IntegerName.add("analyse_status");

        if(record != null){
            if(record.containsKey("synthesize_link_id")){
                synthesize_link_id = record.get("synthesize_link_id");

                columnName.put("synthesize_link_id",synthesize_link_id);
            }
            if(record.containsKey("kg_symbol_synthesize")){
                kg_symbol_synthesize = record.get("kg_symbol_synthesize");
            }
            if(record.containsKey("kg_symbol_doc")){
                kg_symbol_doc = record.get("kg_symbol_doc");
            }
            if(record.containsKey("generate_status")){
                generate_status = record.get("generate_status");
                generate_status_int = Integer.parseInt(generate_status);

                columnName.put("generate_status",generate_status_int);
            }
            if(record.containsKey("generate_begin_time")){
                generate_begin_time = record.get("generate_begin_time");
                generate_begin_time_ts = Timestamp.valueOf(generate_begin_time);

                columnName.put("generate_begin_time",generate_begin_time_ts);
            }
            if(record.containsKey("generate_end_time")){
                generate_end_time = record.get("generate_end_time");
                generate_end_time_ts = Timestamp.valueOf(generate_end_time);

                columnName.put("generate_end_time",generate_end_time_ts);
            }
            if(record.containsKey("generate_msg")){
                generate_msg = record.get("generate_msg");

                columnName.put("generate_msg",generate_msg);
            }
            if(record.containsKey("synthesize_status")){
                synthesize_status = record.get("synthesize_status");
                synthesize_status_int = Integer.parseInt(synthesize_status);

                columnName.put("synthesize_status",synthesize_status_int);
            }
            if(record.containsKey("synthesize_begin_time")){
                synthesize_begin_time = record.get("synthesize_begin_time");
                synthesize_begin_time_ts = Timestamp.valueOf(synthesize_begin_time);

                columnName.put("synthesize_begin_time",synthesize_begin_time_ts);
            }
            if(record.containsKey("synthesize_end_time")){
                synthesize_end_time = record.get("synthesize_end_time");
                synthesize_end_time_ts = Timestamp.valueOf(synthesize_end_time);

                columnName.put("synthesize_end_time",synthesize_end_time_ts);
            }
            if(record.containsKey("synthesize_msg")){
                synthesize_msg = record.get("synthesize_msg");

                columnName.put("synthesize_msg",synthesize_msg);
            }
            if(record.containsKey("analyse_status")){
                analyse_status = record.get("analyse_status");
                analyse_status_int = Integer.parseInt(analyse_status);

                columnName.put("analyse_status",analyse_status_int);
            }
            if(record.containsKey("analyse_begin_time")){
                analyse_begin_time = record.get("analyse_begin_time");
                analyse_begin_time_ts = Timestamp.valueOf(analyse_begin_time);

                columnName.put("analyse_begin_time",analyse_begin_time_ts);
            }
            if(record.containsKey("analyse_end_time")){
                analyse_end_time = record.get("analyse_end_time");
                analyse_end_time_ts = Timestamp.valueOf(analyse_end_time);

                columnName.put("analyse_end_time",analyse_end_time_ts);
            }
            if(record.containsKey("analyse_msg")){
                analyse_msg = record.get("analyse_msg");

                columnName.put("analyse_msg",analyse_msg);
            }

            if(queryIsNull(kg_symbol_synthesize,kg_symbol_doc)){
                String sql = "insert into misre_km_wdtph_tpxx_zhypz(synthesize_link_id, kg_symbol_synthesize, kg_symbol_doc, generate_status, generate_begin_time, generate_end_time, generate_msg, synthesize_status, synthesize_begin_time, synthesize_end_time, synthesize_msg, analyse_status, analyse_begin_time, analyse_end_time, analyse_msg) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                try {
                    //conn = KingBaseUtils.getConnection2();
                    conn = KingBaseUtils.getConnection1();
                    st = conn.prepareStatement(sql);
                    st.setString(1, synthesize_link_id);
                    st.setString(2, kg_symbol_synthesize);
                    st.setString(3, kg_symbol_doc);
                    st.setInt(4, generate_status_int);
                    st.setTimestamp(5, generate_begin_time_ts);
                    st.setTimestamp(6, generate_end_time_ts);
                    st.setString(7, generate_msg);
                    st.setInt(8, synthesize_status_int);
                    st.setTimestamp(9, synthesize_begin_time_ts);
                    st.setTimestamp(10, synthesize_end_time_ts);
                    st.setString(11, synthesize_msg);
                    st.setInt(12, analyse_status_int);
                    st.setTimestamp(13, analyse_begin_time_ts);
                    st.setTimestamp(14, analyse_end_time_ts);
                    st.setString(15, analyse_msg);

                    st.executeUpdate();
                } catch (SQLException e) {
                    logger.error("sql执行错误");
                } finally {
                    try{
                        KingBaseUtils.close(st, conn);
                    }catch(SQLException e){
                        logger.error("release connection failed");
                    }
                    
                    //KingBaseUtils.close(st, conn);
                }
            }

            else{
                String sql2 = "update misre_km_wdtph_tpxx_zhypz set(synthesize_link_id, generate_status, generate_begin_time, generate_end_time, generate_msg, synthesize_status, synthesize_begin_time, synthesize_end_time, synthesize_msg, analyse_status, analyse_begin_time, analyse_end_time, analyse_msg)=(?,?,?,?,?,?,?,?,?,?,?,?,?)  " +
                        "where kg_symbol_synthesize = '"+ kg_symbol_synthesize +"' and kg_symbol_doc= '"+kg_symbol_doc +"'";

                String sql = "update misre_km_wdtph_tpxx_zhypz set ";
                int size = 0;
                for(Map.Entry<String,Object> entry: columnName.entrySet()){
                    if(IntegerName.contains(entry.getKey())){
                        sql += entry.getKey() +"="+entry.getValue();
                    }else{
                        sql += entry.getKey() +"="+"'"+entry.getValue()+"'";
                    }
                    if(size+1!=columnName.size()){
                        sql += ",";
                    }
                    size++;
                }
                sql +=" where kg_symbol_synthesize = '"+ kg_symbol_synthesize +"' and kg_symbol_doc= '"+kg_symbol_doc +"'";

                try {
                    //conn = KingBaseUtils.getConnection2();
                    conn = KingBaseUtils.getConnection1();
                    st = conn.prepareStatement(sql);
//                    st.setString(1, synthesize_link_id);
//                    st.setInt(2, generate_status_int);
//                    st.setTimestamp(3, generate_begin_time_ts);
//                    st.setTimestamp(4, generate_end_time_ts);
//                    st.setString(5, generate_msg);
//                    st.setInt(6, synthesize_status_int);
//                    st.setTimestamp(7, synthesize_begin_time_ts);
//                    st.setTimestamp(8, synthesize_end_time_ts);
//                    st.setString(9, synthesize_msg);
//                    st.setInt(10, analyse_status_int);
//                    st.setTimestamp(11, analyse_begin_time_ts);
//                    st.setTimestamp(12, analyse_end_time_ts);
//                    st.setString(13, analyse_msg);
//                    System.out.println(st);

                    st.executeUpdate();
                } catch (SQLException e) {
                    logger.error("sql执行错误");
                } finally {
                    try{
                        KingBaseUtils.close(st, conn);
                    }catch(SQLException e){
                        logger.error("release connection failed");
                    }
                    
                    
                    //KingBaseUtils.close(st, conn);
                }
            }


        }

    }

    public static void WSDTask_gl_insertIntoKgAsset(Map<String, String> record){
        Connection conn = null;
        PreparedStatement st = null;

        String task_id = "";
        String graph_symbol = "";
        String task_type = "";
        String datasource_type = "";
        String task_status = "";
        String task_status_desc = "";
        String begin_time =" ";
        String end_time ="";

        int task_type_int = 0;
        int datasource_type_int = 2;
        int task_status_int = 0;
        Timestamp begin_time_ts = null;
        Timestamp end_time_ts = null;


        Map<String,Object> columnName = new LinkedHashMap<String,Object>();
        List<String> IntegerName = new ArrayList<>();
        Map<String,Object> columnTime = new LinkedHashMap<String,Object>();

        IntegerName.add("task_type");
        IntegerName.add("datasource_type");
        IntegerName.add("task_status");

        if(record!=null){
            if(record.containsKey("task_id")){
                task_id = record.get("task_id");
                columnName.put("task_id",task_id);
            }
            if(record.containsKey("graph_symbol")){
                graph_symbol = record.get("graph_symbol");
                columnName.put("graph_symbol",graph_symbol);
            }
            if(record.containsKey("task_type")){
                task_type = record.get("task_type");
                task_type_int = Integer.parseInt(task_type);
                columnName.put("task_type",task_type_int);
            }
            if(record.containsKey("datasource_type")){
                datasource_type = record.get("datasource_type");
                datasource_type_int = Integer.parseInt(datasource_type);
                columnName.put("datasouce_type",datasource_type_int);
            }
            if(record.containsKey("task_status")){
                task_status = record.get("task_status");
                task_status_int = Integer.parseInt(task_status);
                columnName.put("task_status",task_status_int);
            }
            if(record.containsKey("task_status_desc")){
                task_status_desc = record.get("task_status_desc");
                columnName.put("task_status_desc",task_status_desc);
            }
            if(record.containsKey("begin_time")){
                begin_time = record.get("begin_time");
                begin_time_ts = Timestamp.valueOf(begin_time);
                columnName.put("begin_time",begin_time_ts);
            }
            if(record.containsKey("end_time")){
                end_time = record.get("end_time");
                end_time_ts = Timestamp.valueOf(end_time);
                columnName.put("end_time",end_time_ts);
            }

            if(WSDFX_queryIsNull(graph_symbol)){
                String sql = "insert into misre_km_fx_tp_qyfxrw(task_id, graph_symbol, task_type, datasouce_type,task_status, task_status_desc, begin_time, end_time) values(?,?,?,?,?,?,?,?)";
                try {
                    //conn = KingBaseUtils.getConnection2();
                    conn = KingBaseUtils.getConnection1();
                    st = conn.prepareStatement(sql);
                    st.setString(1, task_id);
                    st.setString(2, graph_symbol);
                    st.setInt(3, task_type_int);
                    st.setInt(4, datasource_type_int);
                    st.setInt(5, task_status_int);
                    st.setString(6, task_status_desc);
                    st.setTimestamp(7, begin_time_ts);
                    st.setTimestamp(8, end_time_ts);

                    st.executeUpdate();
                } catch (SQLException e) {
                    logger.error("sql执行错误");
                } finally {
                    try{
                        
                        KingBaseUtils.close(st, conn);
                    }catch(SQLException e){
                        logger.error("release connection failed");
                    }
                    
                    //KingBaseUtils.close(st, conn);
                }
            }else{
                String sql = "update misre_km_fx_tp_qyfxrw set ";
                int size = 0;
                for(Map.Entry<String,Object> entry: columnName.entrySet()){
                    if(IntegerName.contains(entry.getKey())){
                        sql += entry.getKey() +"="+entry.getValue();
                    }else{
                        sql += entry.getKey() +"="+"'"+entry.getValue()+"'";
                    }
                    if(size+1!=columnName.size()){
                        sql += ",";
                    }
                    size++;
                }
                sql +=" where graph_symbol = '"+ graph_symbol  +"'" +"and task_type = 0";
                //System.out.println(sql);
                try {
                    //conn = KingBaseUtils.getConnection2();
                    conn = KingBaseUtils.getConnection1();
                    st = conn.prepareStatement(sql);
//                    st.setString(1, synthesize_link_id);
//                    st.setInt(2, generate_status_int);
//                    st.setTimestamp(3, generate_begin_time_ts);
//                    st.setTimestamp(4, generate_end_time_ts);
//                    st.setString(5, generate_msg);
//                    st.setInt(6, synthesize_status_int);
//                    st.setTimestamp(7, synthesize_begin_time_ts);
//                    st.setTimestamp(8, synthesize_end_time_ts);
//                    st.setString(9, synthesize_msg);
//                    st.setInt(10, analyse_status_int);
//                    st.setTimestamp(11, analyse_begin_time_ts);
//                    st.setTimestamp(12, analyse_end_time_ts);
//                    st.setString(13, analyse_msg);
//                    System.out.println(st);

                    st.executeUpdate();
                } catch (SQLException e) {
                    logger.error("sql执行错误");
                } finally {
                    try{
                        KingBaseUtils.close(st, conn);
                    }catch(SQLException e){
                        logger.error("release connection failed");
                    }
                    
                    //KingBaseUtils.close(st, conn);
                }
            }







        }


    }

    public static void WSDTask_Word_jg_insertIntoKgAsset(Map<String, Object> record){
        Connection conn = null;
        PreparedStatement st = null;

        String record_id = "";
        String task_id = "";
        String create_time = "";
        String handle_status = "";
        String handle_time = "";
        String datasouce_type = "";
        String ambiguity_type = "";
        String id_numbers = "";
        String ambiguity_ids =" ";
        String ambiguity_names =" ";

        int handle_status_int = 0;
        int datasouce_type_int = 2;
        int ambiguity_type_int = 0;
        int id_numbers_int = 0;
        Timestamp handle_time_ts = null;


        Map<String,Object> columnName = new LinkedHashMap<String,Object>();
        List<String> IntegerName = new ArrayList<>();
        IntegerName.add("handle_status");
        IntegerName.add("datasouce_type");
        IntegerName.add("ambiguity_type");
        IntegerName.add("id_numbers");

        if(record!=null){
            if(record.containsKey("record_id")){
                record_id = (String) record.get("record_id");
                columnName.put("record_id",record_id);
            }
            if(record.containsKey("task_id")){
                task_id = (String) record.get("task_id");
                columnName.put("task_id",task_id);
            }
            if(record.containsKey("create_time")){
                create_time = (String) record.get("create_time");
                columnName.put("create_time",create_time);
            }


            if(record.containsKey("handle_status")){
                //handle_status = (String)record.get("handle_status");
                handle_status_int = (Integer) record.get("handle_status");
                columnName.put("handle_status",handle_status);
            }

            if(record.containsKey("handle_time")){
                //handle_time = (String)record.get("handle_time");
                handle_time_ts = (Timestamp)record.get("handle_time");
                columnName.put("handle_time",handle_time);
            }


            if(record.containsKey("datasouce_type")){
                //datasouce_type = (String)record.get("datasouce_type");
                datasouce_type_int = (Integer) record.get("datasouce_type");
                columnName.put("datasouce_type",datasouce_type);
            }
            if(record.containsKey("ambiguity_type")){
                //ambiguity_type =(String) record.get("ambiguity_type");
                ambiguity_type_int = (Integer) record.get("ambiguity_type");
                columnName.put("ambiguity_type",ambiguity_type);
            }
            if(record.containsKey("id_numbers")){
                //id_numbers = (String)record.get("id_numbers");
                id_numbers_int = (Integer) record.get("id_numbers");
                columnName.put("id_numbers",id_numbers);
            }

            if(record.containsKey("ambiguity_ids")){
                ambiguity_ids =(String) record.get("ambiguity_ids");
                columnName.put("ambiguity_ids",ambiguity_ids);
            }
            if(record.containsKey("ambiguity_names")){
                create_time = (String) record.get("ambiguity_names");
                columnName.put("ambiguity_names",ambiguity_names);
            }


            String sql = "insert into misre_km_fx_tp_qyfxjg(record_id, task_id, create_time, handle_status, handle_time,datasouce_type, ambiguity_type, id_numbers, ambiguity_ids,ambiguity_names) values(?,?,?,?,?,?,?,?,?,?)";
            try {
                //conn = KingBaseUtils.getConnection2();
                conn = KingBaseUtils.getConnection1();
                st = conn.prepareStatement(sql);
                st.setString(1, record_id);
                st.setString(2, task_id);
                st.setString(3, create_time);
                st.setInt(4, handle_status_int);
                st.setTimestamp(5, handle_time_ts);
                st.setInt(6, datasouce_type_int);
                st.setInt(7, ambiguity_type_int);
                st.setInt(8, id_numbers_int);
                st.setString(9, ambiguity_ids);
                st.setString(10, ambiguity_names);

                st.executeUpdate();
            } catch (SQLException e) {
                logger.error("sql执行错误");
            } finally {
                try{
                    KingBaseUtils.close(st, conn);
                }catch(SQLException e){
                    logger.error("release connection failed");
                }
                
                //KingBaseUtils.close(st, conn);
            }






        }


    }


    public static boolean queryIsNull(String syth,String doc){
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        int recordCount = 0 ;
        try {
            //conn = KingBaseUtils.getConnection1();
            conn = KingBaseUtils.getConnection1();
            //定义sql
            String sql = "select COUNT(*) from misre_km_wdtph_tpxx_zhypz where kg_symbol_synthesize = ? and kg_symbol_doc = ?";
            //获取执行sql的对象
            st = conn.prepareStatement(sql);
            //执行sql
            st.setString(1, syth);
            st.setString(2, doc);
            rs = st.executeQuery();
            if (rs.next())
            {
                recordCount = rs.getInt(1);
            }

        } catch (SQLException e) {
            logger.error("sql执行错误");
        }finally {
            try{
                KingBaseUtils.close(rs,st,conn);
            }catch(SQLException e){
                logger.error("release connection failed");
            }
            //KingBaseUtils.close(rs,st,conn);
            
        }
        boolean re = (recordCount==0);
        return re;
    }

    public static boolean WSDFX_queryIsNull(String symbol){
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        int recordCount = 0 ;
        try {
            //conn = KingBaseUtils.getConnection1();
            conn = KingBaseUtils.getConnection1();
            //定义sql
            String sql = "select COUNT(*) from misre_km_fx_tp_qyfxrw where graph_symbol = ? and task_type = 0 ";
            //获取执行sql的对象
            st = conn.prepareStatement(sql);
            //执行sql
            st.setString(1, symbol);
            rs = st.executeQuery();
            if (rs.next())
            {
                recordCount = rs.getInt(1);
            }

        } catch (SQLException e) {
            logger.error("sql执行错误");
        }finally {
            //KingBaseUtils.close(rs,st,conn);
            try{
                KingBaseUtils.close(rs,st,conn);
            }catch(SQLException e){
                logger.error("release connection failed");
            }
            
        }
        boolean re = (recordCount==0);
        return re;
    }

    public static String queryLinkId(String syth,String doc){
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        String ida = "" ;
        try {
            //conn = KingBaseUtils.getConnection1();
            conn = KingBaseUtils.getConnection1();
            //定义sql
            String sql = "select synthesize_link_id from misre_km_wdtph_tpxx_zhypz where kg_symbol_synthesize = ? and kg_symbol_doc = ?";
            //获取执行sql的对象
            st = conn.prepareStatement(sql);
            //执行sql
            st.setString(1, syth);
            st.setString(2, doc);
            rs = st.executeQuery();
            if (rs.next())
            {
                ida = rs.getString("synthesize_link_id");
            }

        } catch (SQLException e) {
            logger.error("sql执行错误");
        }finally {
            try{
                KingBaseUtils.close(rs,st,conn);
            }catch(SQLException e){
                logger.error("release connection failed");
            }
            //KingBaseUtils.close(rs,st,conn);
            
        }

        return ida;
    }

    public static String queryTaskId_wsd(String syth){
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        String ida = "" ;
        try {
            //conn = KingBaseUtils.getConnection1();
            conn = KingBaseUtils.getConnection1();
            //定义sql
            String sql = "select task_id from misre_km_fx_tp_qyfxrw where graph_symbol = ? and task_type = 0";
            //获取执行sql的对象
            st = conn.prepareStatement(sql);
            //执行sql
            st.setString(1, syth);
            rs = st.executeQuery();
            if (rs.next())
            {
                ida = rs.getString("task_id");
            }

        } catch (SQLException e) {
            logger.error("sql执行错误");
        }finally {
            //KingBaseUtils.close(rs,st,conn);
            try{
                KingBaseUtils.close(rs,st,conn);
            }catch(SQLException e){
                logger.error("release connection failed");
            }
            
        }

        return ida;
    }

    // ? 层级消歧义使用
    public static String queryTaskId(String grpahSymbol){
        String taskId = "";
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        String sql = "select task_id from misre_km_fx_tp_qyfxrw where graph_symbol = ? and task_type = 1";
        try {
            conn = KingBaseUtils.getConnection1();
            st = conn.prepareStatement(sql);
            st.setString(1, grpahSymbol);
            rs = st.executeQuery();
            while(rs.next()){
                taskId = rs.getString("task_id");
            }
        } catch (SQLException e) {
            logger.error("sql执行错误");
        } finally {
            try{
                KingBaseUtils.close(rs, st, conn);
            }catch(SQLException ex){
                logger.error("release connection failed");
            }
            
        }
        return taskId;
    }

    public static void updateLevelAmbiguTaskRecord(LevelAmbiguTaskRecord record){
        Connection conn = null;
        PreparedStatement st = null;
        String sql = "update misre_km_fx_tp_qyfxrw set end_time = ? where task_id = ?";
        try {
            conn = KingBaseUtils.getConnection1();
            st = conn.prepareStatement(sql);
            st.setTimestamp(1, record.getEnd_time());
            st.setString(2, record.getTask_id());
            st.executeUpdate();
        } catch (SQLException e) {
            logger.error("sql执行错误");
        } finally {
            try{
                KingBaseUtils.close(st, conn);
            }catch(SQLException e1){
                logger.error("release connection failed");
            }
            
        }
    }

    public static void insertLevelAmbiguTaskRecord(LevelAmbiguTaskRecord record){
        Connection conn = null;
        PreparedStatement st = null;
        String sql = "insert into misre_km_fx_tp_qyfxrw(task_id, graph_symbol, task_type, datasouce_type, task_status, task_status_desc, begin_time, end_time) values(?,?,?,?,?,?,?,?)";
        try {
            conn = KingBaseUtils.getConnection1();
            st = conn.prepareStatement(sql);
            st.setString(1, record.getTask_id());
            st.setString(2, record.getGraph_symbol());
            st.setInt(3, record.getTask_type());
            st.setInt(4, record.getDatasouce_type());
            st.setInt(5, record.getTask_status());
            st.setString(6, record.getTask_status_desc());
            st.setTimestamp(7, record.getBegin_time());
            st.setTimestamp(8, record.getEnd_time());

            st.executeUpdate();
        } catch (SQLException e) {
            logger.error("sql执行错误");
        } finally {
            try{
                KingBaseUtils.close(st, conn);
            }catch(SQLException e){
                logger.error("release connection failed");
            }
            
        }
    }

    public static void insertLevelAmbiguTaskResults(List<LevelAmbiguTaskRes> results){
        Connection conn = null;
        PreparedStatement st = null;
        String sql = "insert into misre_km_fx_tp_qyfxjg(record_id, task_id, create_time, handle_status, handle_time, datasouce_type, ambiguity_type, id_numbers, ambiguity_ids, ambiguity_names, ambiguity_relation_depth, ambiguity_relation_ids, need_delete_relation_ids) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
        for(LevelAmbiguTaskRes result : results){
            try {
                conn = KingBaseUtils.getConnection1();
                st = conn.prepareStatement(sql);
                st.setString(1, result.getRecord_id());
                st.setString(2, result.getTask_id());
                st.setString(3, result.getCreate_time());
                st.setInt(4, result.getHandle_status());
                st.setTimestamp(5, result.getHandle_time());
                st.setInt(6, result.getDatasouce_type());
                st.setInt(7, result.getAmbiguity_type());
                st.setInt(8, result.getId_numbers());
                st.setString(9, result.getAmbiguity_ids());
                st.setString(10, result.getAmbiguity_names());
                st.setInt(11, result.getAmbiguity_relation_depth());
                st.setString(12, result.getAmbiguity_relation_ids());
                st.setString(13, result.getNeed_delete_relation_ids());
                st.executeUpdate();
            } catch (SQLException e) {
                logger.error("sql执行错误");
            } finally {
                try{
                    KingBaseUtils.close(st, conn);
                }catch(SQLException e){
                    logger.error("release connection failed");
                }
                
            }
        }
    }

    // ! 下面的方法给规则相似度用
    public static Map<String, Double> read_guizequanzhong(){
        Map<String, Double> res = new HashMap<>();
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        String sql = "SELECT rule_name, value_use from misre_km_stdq_gz where rule_enable = 1";
        try {
            conn = KingBaseUtils.getConnection1();
            st = conn.prepareStatement(sql);
            rs = st.executeQuery();
            while(rs.next()){
                String rule_name = rs.getString(1);
                Double rule_use = rs.getDouble(2);
                res.put(rule_name, rule_use);
            }

        } catch (SQLException e) {
            logger.error("sql执行错误");
        } finally {
            try{
                KingBaseUtils.close(st, conn);
            }catch(SQLException e){
                logger.error("release connection failed");
            }
            
        }

        return res;
    }

    public static void main(String[] args) {
        Map<String, String> record = new HashMap<String, String>();
        record.put("synthesize_link_id", "3");
        record.put("kg_symbol_synthesize", "大数据平台呢");
        record.put("kg_symbol_doc", "id1131");
        record.put("generate_status", "4");
        record.put("generate_begin_time", new Timestamp(System.currentTimeMillis())+"");
        record.put("generate_end_time", new Timestamp(System.currentTimeMillis())+"");
        record.put("generate_msg", "7");
        record.put("synthesize_status", "8");
        record.put("synthesize_begin_time", new Timestamp(System.currentTimeMillis())+"");
        record.put("synthesize_end_time",  new Timestamp(System.currentTimeMillis())+"");
        record.put("synthesize_msg", "11");
        record.put("analyse_status", "12");
        record.put("analyse_begin_time", new Timestamp(System.currentTimeMillis())+"");
        record.put("analyse_end_time", new Timestamp(System.currentTimeMillis())+"");
        record.put("analyse_msg", "");
        KGASSETOperation.insertIntoKgAsset(record);
        boolean re= KGASSETOperation.queryIsNull("大数据平台呢","6ecdefc9-c531-4f23-b0d9-d880adb55e8d");

        System.out.println("1");
    }
}
