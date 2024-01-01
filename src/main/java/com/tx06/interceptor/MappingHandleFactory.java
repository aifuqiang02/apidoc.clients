package com.tx06.interceptor;

import com.tx06.interceptor.handle.DefaultMappingHandle;
import com.tx06.interceptor.handle.DeleteMappingHandle;
import com.tx06.interceptor.handle.GetMappingHandle;
import com.tx06.interceptor.handle.MappingHandle;
import com.tx06.interceptor.handle.PostMappingHandle;
import com.tx06.interceptor.handle.PutMappingHandle;
import com.tx06.interceptor.handle.RequestMappingHandle;

//工厂模式
public class MappingHandleFactory {

    public static DefaultMappingHandle getInstance(String className){
        if("GetMapping".equals(className)){
            return new GetMappingHandle();
        }else if("PostMapping".equals(className)){
            return new PostMappingHandle();
        }else if("PutMapping".equals(className)){
            return new PutMappingHandle();
        }else if("DeleteMapping".equals(className)){
            return new DeleteMappingHandle();
        }else if("RequestMapping".equals(className)){
            return new RequestMappingHandle();
        }else{
            return new DefaultMappingHandle();
        }
    }
}
