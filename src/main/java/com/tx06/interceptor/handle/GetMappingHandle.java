package com.tx06.interceptor.handle;

import org.springframework.web.bind.annotation.GetMapping;

public class GetMappingHandle extends DefaultMappingHandle{

    @Override
    public void initMethodTitle() {
        //apidoc.setTitle(((GetMapping)requestMapping).name());
    }

    @Override
    public void initMethodType() {
        //apidoc.setMethod("GET");
    }

    @Override
    public String getMappingValue() {
        String [] arr = ((GetMapping)requestMapping).value();
        return arr.length == 0 ? "" : arr[0];
    }

}
