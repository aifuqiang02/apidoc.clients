package com.tx06.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Apidoc {
    private List<ApidocFieldDict> apidocParameter;
    private List<ApidocFieldDict> apidocResponse;

    /**
     * 主键
     */
    private Integer id;

    /**
     * 所属用户
     */
    private Integer u_member_id;

    /**
     * 所属项目
     */
    private String u_project_uuid;
    private String version;

    /**
     * 所属目录
     */
    private Integer u_apidoc_menu_id;

    /**
     * 标题
     */
    private String title;

    /**
     * 完整标题
     */
    private String full_title;

    /**
     * 访问请求地址
     */
    private String url;

    private String context_path;

    /**
     * 访问请求地址参数
     */
    private String url_parameter;

    /**
     * 请求示例参数
     */
    private String parameter_examples;
    /**
     * 智能识别参数+用户自定义参数
     */
    private String parameter;

    /**
     * 头文件
     */
    private String header;

    /**
     * 返回结果示例
     */
    private String response_examples;

    /**
     * 智能识别返回结果+用户自定义返回结果
     */
    private String response;

    /**
     * 备注信息
     */
    private String remarks;

    /**
     * 请求方式get|post
     */
    private String method;
    /**
     *md5
     */
    private String md5;

    /**
     * 内容类型
     */
    private String content_type;

    /**
     * 创建时间
     */
    private String createtime;
    /**
     * 是否已确认  1是2否
     */
    private String confirmed;

    /**
     * 无备注字段的个数
     */
    private Integer no_tips_field_count;

    /**
     * 字段的个数
     */
    private Integer field_count;

    public List<ApidocFieldDict> getApidocParameter() {
        if(apidocParameter == null){
            apidocParameter = new ArrayList<>();
        }
        return apidocParameter;
    }

    public List<ApidocFieldDict> getApidocResponse() {
        if(apidocResponse == null){
            apidocResponse = new ArrayList<>();
        }
        return apidocResponse;
    }

}
