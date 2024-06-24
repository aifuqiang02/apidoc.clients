package com.tx06.entity;


import lombok.Data;

// API 属性信息实体类
@Data
public class ApiAttrInfo {
    private Long id; // ID
    private String apiUuid; // API UUID
    private String beforeInject; // 注入前
    private String afterInject; // 注入后
    private String authInfo; // 认证信息
    private Integer requestMethod; // 请求方法
    private Integer contentType; // 内容类型
    private Long updateUserId; // 更新用户 ID
    private Long createUserId; // 创建用户 ID

    // Getter 和 Setter 省略
}