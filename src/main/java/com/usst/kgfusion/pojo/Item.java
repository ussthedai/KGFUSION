package com.usst.kgfusion.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Item {
    private String itemTitle;  //条目标题
    private String itemText;//条目内容
    private String itemId;//条目id
    private String chapterId;//章节号
    private String docId;//文档id
    private Float score = 0.0f; //匹配分数（计算相似度时）
    private String entityIds;//从这个条目中抽取出来的实体的id, id之间用‘,’隔开
    private String tripleIds;//从这个条目中抽取出来的三元组的id, id之间用‘,’隔开

    public Item(String id, String text){
        this.itemId = id;
        this.itemText = text;
    }
}
