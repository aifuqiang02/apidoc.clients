package com.tx06.interceptor;

import com.tx06.interceptor.handle.DefaultMappingHandle;
import com.tx06.interceptor.handle.GetMappingHandle;
import com.tx06.interceptor.handle.PostMappingHandle;
import com.tx06.interceptor.handle.RequestMappingHandle;

//工厂模式
public class MappingHandleFactory {

    public static DefaultMappingHandle getInstance(String className){
        if("GetMapping".equals(className)){
            return new GetMappingHandle();
        }else if("PostMapping".equals(className)){
            return new PostMappingHandle();
        }else if("RequestMapping".equals(className)){
            return new RequestMappingHandle();
        }else{
            return new DefaultMappingHandle();
        }
    }
}
