package com.usst.kgfusion.pojo;

import java.sql.Timestamp;

public class LevelAmbiguTaskRes {
    private String record_id;
    private String task_id;
    private String create_time;
    private int handle_status = 1;
    private Timestamp handle_time;
    private int datasouce_type = 2;
    private int ambiguity_type = 4;
    private int id_numbers;
    private String ambiguity_ids;
    private String ambiguity_names;
    private int ambiguity_relation_depth;
    private String ambiguity_relation_ids;
    private String need_delete_relation_ids;

    // constructor
    public LevelAmbiguTaskRes(String record_id, String task_id, String create_time, int handle_status, Timestamp handle_time, int datasouce_type, int ambiguous_type, int id_numbers, String ambiguity_ids, String ambiguity_names, int ambiguity_relation_depth, String ambiguity_relation_ids, String need_delete_relation_ids){
        this.record_id = record_id;
        this.task_id = task_id;
        this.create_time = create_time;
        this.handle_status = handle_status;
        this.handle_time = handle_time;
        this.datasouce_type = datasouce_type;
        this.ambiguity_type = ambiguous_type;
        this.id_numbers = id_numbers;
        this.ambiguity_ids = ambiguity_ids;
        this.ambiguity_names = ambiguity_names;
        this.ambiguity_relation_depth = ambiguity_relation_depth;
        this.ambiguity_relation_ids = ambiguity_relation_ids;
        this.need_delete_relation_ids = need_delete_relation_ids;
    }

    // get
    public String getRecord_id() {
        return record_id;
    }
    public String getTask_id() {
        return task_id;
    }
    public int getHandle_status() {
        return handle_status;
    }
    public Timestamp getHandle_time() {
        return handle_time;
    }
    public int getAmbiguity_type() {
        return ambiguity_type;
    }
    public int getId_numbers() {
        return id_numbers;
    }
    public String getAmbiguity_ids() {
        return ambiguity_ids;
    }
    public String getAmbiguity_names() {
        return ambiguity_names;
    }
    public int getAmbiguity_relation_depth() {
        return ambiguity_relation_depth;
    }
    public String getAmbiguity_relation_ids() {
        return ambiguity_relation_ids;
    }
    public String getNeed_delete_relation_ids() {
        return need_delete_relation_ids;
    }
    public String getCreate_time() {
        return create_time;
    }
    public int getDatasouce_type() {
        return datasouce_type;
    }

}
