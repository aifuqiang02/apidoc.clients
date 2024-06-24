package com.tx06.handle;

import java.util.Map;

public class RequestParamHandleFactory {
    public static BaseRequestParamHandle getHandle(String className) {
        if(className.equals(Map.class.getName()) || className.contains("java.util.Map")  || className.contains("java.util.HashMap") ){
            return new MapRequestHandle();
        }else if(className.contains("com.baomidou.mybatisplus.extension.plugins.pagination.Page")){
            return new PageRequestHandle();
        }
        return null;
    }
}
