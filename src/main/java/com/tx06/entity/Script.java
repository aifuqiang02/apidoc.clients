package com.tx06.entity;


import lombok.Data;

// 脚本实体类
@Data
public class Script {
    private Long id; // ID
    private String scriptUuid; // 脚本 UUID
    private Integer scriptType; // 脚本类型
    private Integer type; // 类型
    private String data; // 数据
    private Integer sort; // 排序
    private Integer status; // 状态
    private String name; // 名称
    private String targetId; // 目标 ID
    private Integer targetType; // 目标类型
    private String workSpaceUuid; // 工作空间 UUID
    private String projectUuid; // 项目 UUID
    private Long createUserId; // 创建用户 ID
    private Long createTime; // 创建时间
    private Long updateTime; // 更新时间

    // Getter 和 Setter 省略
}

