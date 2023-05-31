package com.tx06.entity;

import com.alibaba.fastjson.JSON;
import lombok.Data;

import java.util.LinkedHashMap;

/**
 * @author 艾付强
 */
@Data
public class TMessage {
    private String title;
    private String content;
    private String noticeUserPhone;
    private String messageExt;
    private LinkedHashMap<String,String> messageExtMap = new LinkedHashMap<>();

    public TMessage addExt(String label, String value){
        messageExtMap.put(label,value);
        return this;
    }

    public String getMessageExt() {
        return JSON.toJSONString(messageExt);
    }
}
