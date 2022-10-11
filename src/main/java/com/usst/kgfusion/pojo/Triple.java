package com.usst.kgfusion.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Triple {
    private String tripleId;//三元组id

    @NonNull
    private EntityRaw head;//头实体
    @NonNull
    private EntityRaw tail;//尾实体
    @NonNull
    private String rela;//关系名称
    // private String itemId;//所属条目的id(从这个条目中抽取出来的)

    public Triple(EntityRaw head, String relation, EntityRaw tail){
        this.head = head;
        this.tail = tail;
        this.rela = relation;
    }

    public Triple(String tripleId, EntityRaw head, String relation, EntityRaw tail){
        this.tripleId = tripleId;
        this.head = head;
        this.tail = tail;
        this.rela = relation;
    }

    public Triple(EntityRaw head, EntityRaw tail){
        this.head = head;
        this.tail = tail;
    }
}

