package com.tx06.entity;

import lombok.Data;

@Data
public class ApidocFieldDict {

    /**
     * 主键
     */
    private Integer id;

    /**
     * 所属用户
     */
    private Integer u_member_id;

    /**
     * api的编号
     */
    private Integer apidoc_id;



    /**
     * 所属项目
     */
    private String u_project_uuid;

    /**
     * 所属目录
     */
    private Integer u_apidoc_menu_id;

    /**
     * 中文含义
     */
    private String name;
    /**
     * 是否是全局变量
     */
    private String global;
    /**
     * 全key
     */
    private String full_key;

    /**
     * 是否使用特征
     */
    private String use_features;

    /**
     * 是否使用特征
     */
    private String auto_insert;

    /**
     * 字段名称
     */
    private String field;

    /**
     * 字段类型
     */
    private String type;

    private String data_type;

}
