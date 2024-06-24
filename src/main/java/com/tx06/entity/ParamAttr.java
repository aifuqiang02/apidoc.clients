package com.tx06.entity;


import lombok.Data;

// 参数属性实体类
@Data
public class ParamAttr {
    private Long id; // ID
    private Long apiParamId; // API 参数 ID
    private Integer minLength; // 最小长度
    private Integer maxLength; // 最大长度
    private double minValue; // 最小值
    private double maxValue; // 最大值
    private String paramLimit; // 参数限制
    private String paramValueList; // 参数值列表
    private String paramMock; // 参数模拟
    private String attr; // 属性
    private Integer structureIsHide; // 结构是否隐藏
    private String example; // 示例
    private String dbArr; // 数据库数组
    private String paramNote; // 参数注释

    // Getter 和 Setter 省略
}