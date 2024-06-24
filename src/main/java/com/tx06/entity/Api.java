package com.tx06.entity;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import lombok.Data;

import java.util.List;

/**
 * 【请填写功能名称】对象 t_api
 *
 * @author ruoyi
 * @date 2024-06-05
 */
@Data
public class Api {
    private static final long serialVersionUID = 1L;

    /**
     * $column.columnComment
     */
    private Long id;

    /**
     * API
     */
    private String apiUuid;


    /**
     * 项目uuid
     */
    private String projectUuid;


    /**
     * 命名空间uuid
     */
    private String workSpaceUuid;

    /**
     * 组ID
     */
    private Long groupId;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 生命周期
     */
    private Long lifecycle;

    /**
     * 名称
     */
    private String name;

    /**
     * URI
     */
    private String uri;

    /**
     * 协议
     */
    private Long protocol;

    /**
     * 状态
     */
    private Long status;

    /**
     * 收藏
     */
    private Long starred;

    /**
     * 编码
     */
    private String encoding;

    /**
     * 是否共享
     */
    private Long isShared;

    /**
     * 标签
     */
    private String tag;

    /**
     * 排序编号
     */
    private Long orderNum;

    /**
     * 哈希键
     */
    private String hashkey;

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
     * 简介
     */
    private String introduction;

    /**
     * 关系
     */
    private String relation;

    /**
     * 关系
     */
    private RelationInfo relationInfo;


    /**
     * api属性
     */
    private String apiAttr;

    /**
     * api属性
     */
    private ApiAttrInfo apiAttrInfo;


    /**
     * 认证信息
     */
    private String authInfoText;

    /**
     * 请求参数
     */
    private RequestParams requestParams;


    /**
     * 请求参数
     */
    private String requestParamsText;

    /**
     * 请求参数
     */
    private List<Response> responseList;


    /**
     * 请求参数
     */
    private String responseListText;

    /**
     * 脚本
     */
    private List<Script> scriptList;


    /**
     * 脚本文本
     */
    private String scriptListText;
    /**
     * 返回信息
     */
    private List<Result> resultList;


    /**
     * 返回信息
     */
    private String resultListText;

    /**
     * api 唯一标识
     */
    private String uniqueIdentifier;




    /**
     * 认证信息
     */
    private AuthInfo authInfo = new AuthInfo();

    /**获取唯一标识*/
    public String getUniqueIdentifier(){
        return this.getUri() + this.getApiAttrInfo().getRequestMethod();
    }

    public AuthInfo getAuthInfo() {
        if (this.authInfoText != null) {
            authInfo = JSON.parseObject(this.authInfoText, AuthInfo.class);
        }
        return authInfo;
    }

    public ApiAttrInfo getApiAttrInfo() {
        if (this.apiAttr != null) {
            apiAttrInfo = JSON.parseObject(this.apiAttr, ApiAttrInfo.class);
        }
        if(apiAttrInfo == null){
            apiAttrInfo = new ApiAttrInfo();
        }
        return apiAttrInfo;
    }

    public RelationInfo getRelationInfo() {
        if (this.relation != null) {
            relationInfo = JSON.parseObject(this.relation, RelationInfo.class);
        }
        return relationInfo;
    }

    public RequestParams getRequestParams() {
        if (this.requestParamsText != null) {
            requestParams = JSON.parseObject(this.requestParamsText, RequestParams.class);
        }
        return requestParams;
    }

    public List<Response> getResponseList() {
        if (this.responseListText != null) {
            responseList = JSON.parseArray(this.responseListText, Response.class);
        }
        return responseList;
    }

    public List<Script> getScriptList() {
        if (this.scriptListText != null) {
            scriptList = JSON.parseArray(this.scriptListText, Script.class);
        }
        return scriptList;
    }

    public List<Result> getResultList() {
        if (this.resultListText != null) {
            resultList = JSON.parseArray(this.resultListText, Result.class);
        }
        return resultList;
    }


    public String getApiAttr() {
        if (this.apiAttrInfo != null) {
            apiAttr = JSON.toJSONString(this.apiAttrInfo, JSONWriter.Feature.WriteMapNullValue);
        }
        return apiAttr;
    }

    public String getRequestParamsText() {
        if (this.requestParams != null) {
            requestParamsText = JSON.toJSONString(this.requestParams, JSONWriter.Feature.WriteMapNullValue);
        }
        return requestParamsText;
    }

    public String getResponseListText() {
        if (this.responseList != null) {
            responseListText = JSON.toJSONString(this.responseList, JSONWriter.Feature.WriteMapNullValue);
        }
        return responseListText;
    }

    public String getScriptListText() {
        if (this.scriptList != null) {
            scriptListText = JSON.toJSONString(this.scriptList, JSONWriter.Feature.WriteMapNullValue);
        }
        return scriptListText;
    }


    public String getResultListText() {
        if (this.resultList != null) {
            resultListText = JSON.toJSONString(this.resultList, JSONWriter.Feature.WriteMapNullValue);
        }
        return resultListText;
    }

    public Group toGroup() {
        Group group = new Group();
        group.setId(this.id);
        group.setParentId(this.groupId);
        group.setName(this.name);
        group.setModuleGroupId(this.id);
        group.setAuthInfo(this.getAuthInfo());
        group.setDisabled(false);
        group.setModule("API_DOC");
        group.setType(2L);
        group.setRelationInfo(this.getRelationInfo());
        return group;
    }



}
