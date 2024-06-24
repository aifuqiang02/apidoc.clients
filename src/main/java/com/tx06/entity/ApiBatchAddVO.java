package com.tx06.entity;

import lombok.Data;

import java.util.List;

@Data
public class ApiBatchAddVO {
    private List<Api> apiList;
    private Api api;

    private String projectUuid;
    private String workSpaceUuid;
}


