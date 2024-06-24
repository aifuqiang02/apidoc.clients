package com.tx06.config;

public enum BodyContentTypeEnum {
    FROM_DATA(0),
    RAW(1),
    JSON_OBJECT(2),
    XML(3),
    BINARY(4),
    OTHER(5),
    JSON_ARRAY(6);

    private final int value;

    BodyContentTypeEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static BodyContentTypeEnum fromValue(int value) {
        for (BodyContentTypeEnum type : BodyContentTypeEnum.values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid value for BodyContentType: " + value);
    }
}
