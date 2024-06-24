package com.tx06.entity;

public interface Callback {
    void onSuccess(Api apidoc);
    void onFailure(Api apidoc,Exception exception);
}
