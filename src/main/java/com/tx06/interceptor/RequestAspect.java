package com.tx06.interceptor;
import cn.hutool.core.util.StrUtil;
import com.tx06.config.Prop;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.sql.DataSource;

@Aspect
@Component
public class RequestAspect extends AbstractApidocAspect{

    @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public void pointCutMethod() {
    }

    // 声明环绕通知
    @Around("pointCutMethod()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        this.proceedingJoinPoint = pjp;
        //判断环境是否运行在线接口文档
        if(!this.isRun()){
            return pjp.proceed();
        }

        this.initBefore();

        //判断类、方法是否有name
        this.setFullTitleName();
        if(!checkCanSend()){
            return pjp.proceed();
        }
        Object ret = pjp.proceed();
        this.sendApidoc(ret);
        return ret;
    }

    protected String getMethodName() {
        this.requestMethod = RequestMethod.POST.name();
        return method.getAnnotation(RequestMapping.class).name();
    }
}
