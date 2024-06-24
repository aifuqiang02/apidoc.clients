package com.tx06.entity;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;

// 请求参数单项实体类
@Data
public class RequestParam {
    private Long id; // ID
    private Long parentId; // 父 ID
    private String apiUuid; // API UUID
    private String responseUuid; // 响应 UUID
    private String name; // 名称
    private Integer paramType; // 参数类型
    private Integer partType; // 部分类型
    private Integer dataType; // 数据类型
    private String dataTypeValue; // 数据类型值
    private Long structureId; // 结构 ID
    private String structureParamId; // 结构参数 ID
    private Object contentType; // 内容类型
    private Integer isRequired; // 是否必需
    private String binaryRawData; // 二进制原始数据
    private String description; // 描述
    private Integer orderNo; // 排序号
    private Object isDefault; // 是否默认
    private ParamAttr paramAttr; // 参数属性
    private List<RequestParam> childList; // 子项列表

    public List<RequestParam> getChildList() {
        if (childList == null) {
            childList = new ArrayList<>();
        }
        return childList;
    }

    // Getter 和 Setter 省略
}
