package com.tx06.interceptor.handle;

import java.io.IOException;

public interface MappingHandle {
    public String getMappingValue();
    public void initMethodTitle();
    public void initFullTitle();
    public void initMethodType();
    public void initUrl();
    public void initParameter() throws IOException;
    public void initResponse();
    public void initApiDoc();
    public Object sendApi() throws Throwable;
}
