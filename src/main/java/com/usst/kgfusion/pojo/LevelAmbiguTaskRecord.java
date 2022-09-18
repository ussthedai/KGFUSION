package com.usst.kgfusion.pojo;

import java.sql.Timestamp;

public class LevelAmbiguTaskRecord {
    private String task_id;
    private String graph_symbol;
    private int task_type = 1;
    private int datasouce_type = 2;
    private int task_status = 2;
    private String task_status_desc;
    private Timestamp begin_time;
    private Timestamp end_time;

    // constructor
    public LevelAmbiguTaskRecord(String task_id, String graph_symbol, int taks_type, int datasouce_type, int task_status, String task_status_desc, Timestamp begin_time, Timestamp end_time){
        this.task_id = task_id;
        this.graph_symbol = graph_symbol;
        this.task_type = taks_type;
        this.datasouce_type = datasouce_type;
        this.task_status = task_status;
        this.task_status_desc = task_status_desc;
        this.begin_time = begin_time;
        this.end_time = end_time;
    }

    // get
    public String getTask_id() {
        return task_id;
    }
    public String getGraph_symbol() {
        return graph_symbol;
    }
    public int getTask_type() {
        return task_type;
    }
    public int getTask_status() {
        return task_status;
    }
    public String getTask_status_desc() {
        return task_status_desc;
    }
    public Timestamp getBegin_time() {
        return begin_time;
    }
    public Timestamp getEnd_time() {
        return end_time;
    }
    public int getDatasouce_type() {
        return datasouce_type;
    }




}


