package com.tx06.entity;


import lombok.Data;

// 认证信息实体类
@Data
public class AuthInfo {
    private String authType; // 认证类型
    private String authInfo; // 认证信息
    private Integer isInherited; // 是否继承

    // Getter 和 Setter 省略
}

