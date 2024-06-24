
package com.tx06.interceptor.handle;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;

public class DeleteMappingHandle extends DefaultMappingHandle{
    @Override
    public void initMethodTitle() {
        //apidoc.setTitle(((DeleteMapping)requestMapping).name());
    }

    @Override
    public void initMethodType() {
        //apidoc.setMethod("DELETE");
    }

    @Override
    public String getMappingValue() {
        String [] arr = ((DeleteMapping)requestMapping).value();
        return arr.length == 0 ? "" : arr[0];
    }

}
