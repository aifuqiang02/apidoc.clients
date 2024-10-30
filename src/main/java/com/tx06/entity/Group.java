package com.tx06.entity;

import com.alibaba.fastjson2.JSON;
import lombok.Data;

/**
 * 【请填写功能名称】对象 t_group
 *
 * @author ruoyi
 */
@Data
public class Group {
    private static final long serialVersionUID = 1L;

    /**
     * $column.columnComment
     */
    private Long id;

    /**
     * 模块组ID
     */
    private Long moduleGroupId;

    /**
     * 模块
     */
    private String module;
    /**
     * project_uuid
     */
    private String projectUuid;

    /**
     * 类型
     */
    private Long type;

    /**
     * 名称
     */
    private String name;

    /**
     * 路径
     */
    private String path;

    /**
     * 深度
     */
    private Integer depth;

    /**
     * 父级ID
     */
    private Long parentId;

    /**
     * 排序
     */
    private Long sort;

    /**
     * 认证类型
     */
    private String authType;

    /**
     * 认证信息
     */
    private String authInfoText;

    /**
     * 认证信息
     */
    private AuthInfo authInfo = new AuthInfo();

    /**
     * 是否继承
     */
    private Long isInherited;

    /**
     * 是否禁用
     */
    private boolean disabled;
    /**
     * 关系
     */
    private RelationInfo relationInfo;

    public AuthInfo getAuthInfo() {
        if (authInfoText != null) {
            authInfo = JSON.parseObject(authInfoText, AuthInfo.class);
        }
        return authInfo;
    }
}
