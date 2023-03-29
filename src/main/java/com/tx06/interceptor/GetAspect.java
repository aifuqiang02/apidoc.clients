package com.tx06.interceptor;
import cn.hutool.core.util.StrUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Aspect
@Component
public class GetAspect extends AbstractApidocAspect{


    @Pointcut("@annotation(org.springframework.web.bind.annotation.GetMapping)")
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
        sendApidoc(ret);
        return ret;
    }

    @Override
    protected String getMethodName() {
        this.requestMethod = RequestMethod.GET.name();
        return method.getAnnotation(GetMapping.class).name();
    }
}
