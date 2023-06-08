package com.tx06.interceptor.handle;

public class NullMappingHandle extends DefaultMappingHandle{

    @Override
    public Object sendApi() throws Throwable {
        Object ret = pjp.proceed();
        return ret;
    }
}
