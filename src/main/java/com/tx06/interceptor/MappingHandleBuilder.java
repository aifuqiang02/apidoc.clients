package com.tx06.interceptor;

import cn.hutool.extra.spring.SpringUtil;
import com.tx06.config.ApiDocProp;
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
    private ProceedingJoinPoint pjp;

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
        MappingHandleBuilder builder = new MappingHandleBuilder();
        builder.pjp = pjp;

        return builder;
    }

}
