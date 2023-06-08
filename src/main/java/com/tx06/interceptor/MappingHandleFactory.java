package com.tx06.interceptor;

import com.tx06.interceptor.handle.DefaultMappingHandle;
import com.tx06.interceptor.handle.GetMappingHandle;
import com.tx06.interceptor.handle.PostMappingHandle;
import com.tx06.interceptor.handle.RequestMappingHandle;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

public class MappingHandleFactory {
    private static Map<String, DefaultMappingHandle> requestHandleMap = new HashMap<>();
    static {
        requestHandleMap.put(GetMapping.class.getSimpleName(),new GetMappingHandle());
        requestHandleMap.put(PostMapping.class.getSimpleName(),new PostMappingHandle());
        requestHandleMap.put(RequestMapping.class.getSimpleName(),new RequestMappingHandle());
        requestHandleMap.put(PutMapping.class.getSimpleName(),new DefaultMappingHandle());
        requestHandleMap.put(DeleteMapping.class.getSimpleName(),new DefaultMappingHandle());
    }

    public static DefaultMappingHandle getInstance(String className){
        return requestHandleMap.get(className);
    }
}
