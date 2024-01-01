
package com.tx06.interceptor.handle;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

public class PutMappingHandle extends DefaultMappingHandle{
    @Override
    public void initMethodTitle() {
        apidoc.setTitle(((PutMapping)requestMapping).name());
    }

    @Override
    public void initMethodType() {
        apidoc.setMethod("PUT");
    }

    @Override
    public String getMappingValue() {
        String [] arr = ((PutMapping)requestMapping).value();
        return arr.length == 0 ? "" : arr[0];
    }

}
