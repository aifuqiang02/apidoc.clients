package com.tx06.interceptor;
import com.tx06.interceptor.handle.MappingHandle;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ControllerRequestAspect{

    //RequestMapping 拦截
    @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) ||" +
            "@annotation(org.springframework.web.bind.annotation.GetMapping) ||" +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) ||" +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public void pointCutRequest() {
    }

    @Around("pointCutRequest()")
    public Object doAroundRequest(ProceedingJoinPoint pjp) throws Throwable {
        MappingHandle mappingHandle = MappingHandleBuilder.create(pjp).build();
        return mappingHandle.sendApi();
    }



}
