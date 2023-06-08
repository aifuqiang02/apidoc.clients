package com.tx06.interceptor;

import cn.hutool.extra.spring.SpringUtil;
import com.tx06.config.ApiDocProp;
import com.tx06.interceptor.handle.DefaultMappingHandle;
import com.tx06.interceptor.handle.MappingHandle;
import com.tx06.interceptor.handle.NullMappingHandle;
import lombok.Data;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

//建造者模式
@Data
public class MappingHandleBuilder {
    private DefaultMappingHandle mappingHandle;

    //单例模式
    private static ApiDocProp prop;
    private static JdbcTemplate jdbcTemplate;
    static {
        prop = SpringUtil.getBean(ApiDocProp.class);
        jdbcTemplate = SpringUtil.getBean(JdbcTemplate.class);
    }

    public static ApiDocProp getProp() {
        return prop;
    }

    public static JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public static MappingHandleBuilder create(ProceedingJoinPoint pjp){
        MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        RestController restController = pjp.getTarget().getClass().getAnnotation(RestController.class);

        Method method = methodSignature.getMethod();
        Annotation mappingAnnotation = Arrays.stream(method.getAnnotations()).filter(annotation ->
             annotation.annotationType().getName().contains("org.springframework.web.bind.annotation")
        ).findAny().orElse(null);

        MappingHandleBuilder builder = new MappingHandleBuilder();
        DefaultMappingHandle mappingHandle ;
        if(mappingAnnotation == null){
            mappingHandle = new NullMappingHandle();
        }else if(restController == null){
            mappingHandle = new NullMappingHandle();
        }else if(!prop.server.getRun()){
            mappingHandle = new NullMappingHandle();
        }else {
            mappingHandle = MappingHandleFactory.getInstance(mappingAnnotation.annotationType().getSimpleName());
        }
        mappingHandle.pjp = pjp;
        mappingHandle.method = method;
        mappingHandle.request = attributes.getRequest();
        mappingHandle.requestMapping = mappingAnnotation;
        mappingHandle.restController = restController;
        mappingHandle.prop = prop;
        builder.mappingHandle = mappingHandle;

        return builder;
    }

    public MappingHandle build(){

        return this.mappingHandle;
    }

}
