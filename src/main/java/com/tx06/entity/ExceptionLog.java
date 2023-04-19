package com.tx06.entity;

import lombok.Data;

import javax.lang.model.element.NestingKind;

/**
 * @author 艾付强
 */
@Data
public class ExceptionLog {
    private String reqId;
    private String projectUuid;
    private String content;
    private String param;
    private String url;

}
