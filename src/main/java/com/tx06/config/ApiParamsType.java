package com.tx06.config;

import cn.hutool.core.date.DateTime;
import com.alibaba.fastjson2.JSONObject;

import java.io.File;
import java.lang.reflect.Array;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public enum ApiParamsType {
    STRING(0),
    FILE(1),
    BYTE(9),
    JSON(2),
    INT(3),
    FLOAT(4),
    DOUBLE(5),
    DATE(6),
    DATETIME(7),
    BOOLEAN(8),
    SHORT(10),
    LONG(11),
    ARRAY(12),
    OBJECT(13),
    NUMBER(14),
    NULL(15);

    private final int value;

    ApiParamsType(int value) {
        this.value = value;
    }

    public static Integer getByType(Class<?> type) {
        if (type == String.class) {
            return 0;
        } else if (type == File.class) {
            return 1;
        } else if (type == Byte.class) {
            return 9;
        } else if (type == JSONObject.class || type == Map.class || type == HashMap.class) {
            return 2;
        } else if (type == Integer.class) {
            return 3;
        } else if (type == Float.class) {
            return 4;
        } else if (type == Double.class) {
            return 5;
        } else if (type == Date.class) {
            return 6;
        } else if (type == DateTime.class) {
            return 7;
        } else if (type == Boolean.class) {
            return 8;
        } else if (type == Short.class) {
            return 10;
        } else if (type == Long.class) {
            return 11;
        } else if (type == Array.class) {
            return 12;
        } else if (type == Object.class) {
            return 13;
        } else if (type == Number.class) {
            return 14;
        } else if (type == null) {
            return 15;
        } else {
            return 0;
        }
    }

    public int getValue() {
        return value;
    }

    public static ApiParamsType fromValue(int value) {
        for (ApiParamsType type : ApiParamsType.values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown enum value: " + value);
    }
}
