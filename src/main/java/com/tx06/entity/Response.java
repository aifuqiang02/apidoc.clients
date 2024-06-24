package com.tx06.entity;


import lombok.Data;

// 响应实体类
@Data
public class Response {
    private Long id; // ID
    private String responseUuid; // 响应 UUID
    private String apiUuid; // API UUID
    private Long oldId; // 旧 ID
    private String name; // 名称
    private String httpCode; // HTTP 状态码
    private Integer contentType; // 内容类型
    private Integer isDefault; // 是否默认
    private Long updateUserId; // 更新用户 ID
    private Long createUserId; // 创建用户 ID
    private Long createTime; // 创建时间
    private Long updateTime; // 更新时间
    private ResponseParams responseParams; // 响应参数

    // Getter 和 Setter 省略
}
