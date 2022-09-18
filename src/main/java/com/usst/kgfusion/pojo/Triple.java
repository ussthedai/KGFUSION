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
    private Entity head;//头实体
    @NonNull
    private Entity tail;//尾实体
    @NonNull
    private String rela;//关系名称
    // private String itemId;//所属条目的id(从这个条目中抽取出来的)

    public Triple(Entity head, String relation, Entity tail){
        this.head = head;
        this.tail = tail;
        this.rela = relation;
    }

    public Triple(String tripleId, Entity head, String relation, Entity tail){
        this.tripleId = tripleId;
        this.head = head;
        this.tail = tail;
        this.rela = relation;
    }

    public Triple(Entity head, Entity tail){
        this.head = head;
        this.tail = tail;
    }
}

