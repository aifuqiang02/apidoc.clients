package com.tx06.interceptor.handle;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

public class PostMappingHandle extends DefaultMappingHandle{
    @Override
    public void initMethodTitle() {
        apidoc.setTitle(((PostMapping)requestMapping).name());
    }

    @Override
    public void initMethodType() {
        apidoc.setMethod("POST");
    }

    @Override
    public String getMappingValue() {
        String [] arr = ((PostMapping)requestMapping).value();
        return arr.length == 0 ? "" : arr[0];
    }

}
