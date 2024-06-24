package com.tx06.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 【请填写功能名称】对象 t_relation_info
 *
 * @author ruoyi
 * @date 2024-06-12
 */
@Data
public class RelationInfo {
    private static final long serialVersionUID = 1L;

    /**
     * 编号
     */
    private Long id;

    /**
     * API
     */
    private String apiUuid;

    /**
     * API
     */
    private Long apiId;

    /**
     * 生命周期
     */
    private Long lifecycle;

    /**
     * 创建方式
     */
    private String createWay;
    /**
     * 名称
     */
    private String name;

    /**
     * URI
     */
    private String uri;

    /**
     * 标签
     */
    private String tag;

    /**
     * 状态
     */
    private Long status;

    /**
     * 收藏
     */
    private Long starred;

    /**
     * 组ID
     */
    private Long groupId;

    /**
     * 组名
     */
    private String groupName;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 是否共享
     */
    private Long isShared;

    /**
     * 管理者ID
     */
    private Long managerId;

    /**
     * 更新用户ID
     */
    private Long updateUserId;

    /**
     * 创建用户ID
     */
    private Long createUserId;

    /**
     * 管理者名称
     */
    private String managerName;

    /**
     * 更新用户名称
     */
    private String updateUserName;

    /**
     * 创建用户名称
     */
    private String createUserName;

    /**
     * 协议
     */
    private Long protocol;

    /**
     * 请求方法
     */
    private Long requestMethod;

    /**
     * 内容类型
     */
    private Long contentType;

    /**
     * 测试用例数
     */
    private Long testCaseNum;

    /**
     * 测试成功率
     */
    private BigDecimal testSuccessRate;

    /**
     * 测试状态
     */
    private Long testStatus;

    /**
     * API
     */
    private Long apiCreateTime;

    /**
     * API
     */
    private Long apiUpdateTime;

    /**
     * 排序编号
     */
    private Long orderNum;


}
