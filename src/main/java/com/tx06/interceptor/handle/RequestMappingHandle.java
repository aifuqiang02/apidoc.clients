package com.tx06.interceptor.handle;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Aspect
@Component
public class RequestMappingHandle extends DefaultMappingHandle{
    @Override
    public void initMethodTitle() {
        //apidoc.setTitle(((RequestMapping)requestMapping).name());
    }

    @Override
    public void initMethodType() {
        //apidoc.setMethod(request.getMethod());
    }

    @Override
    public String getMappingValue() {
        String [] arr = ((RequestMapping)requestMapping).value();
        return arr.length == 0 ? "" : arr[0];
    }
}
