package com.tx06.entity;


import lombok.Data;

import java.util.List;

// 请求参数实体类
@Data
public class RequestParams {
    private List<RequestParam> headerParams; // 头部参数
    private List<RequestParam> bodyParams; // 主体参数
    private List<RequestParam> queryParams; // 查询参数
    private List<RequestParam> restParams; // REST 参数

    // Getter 和 Setter 省略
}