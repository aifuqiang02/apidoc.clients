package com.tx06.handle;

import com.tx06.entity.RequestParam;

import java.util.List;

import static com.tx06.interceptor.StaticAnalysis.createRequestParam;

public class MapRequestHandle extends BaseRequestParamHandle{
    @Override
    public void handleRequestParam(List<RequestParam> responseParams, Class<?> type, String fieldName, int index) {
    }
}
