package com.tx06.config;

public enum RequestMethodEnum {
    POST(0),
    GET(1),
    PUT(2),
    DELETE(3),
    HEAD(4),
    OPTIONS(5),
    PATCH(6);

    private final int value;

    RequestMethodEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static RequestMethodEnum fromValue(int value) {
        for (RequestMethodEnum method : RequestMethodEnum.values()) {
            if (method.value == value) {
                return method;
            }
        }
        throw new IllegalArgumentException("Invalid value for RequestMethod: " + value);
    }

    public static int getByName(String name) {
        for (RequestMethodEnum method : RequestMethodEnum.values()) {
            if (method.name().equals(name)) {
                return method.value;
            }
        }
        return 1;
    }

}
