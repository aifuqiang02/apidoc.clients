package com.tx06.entity;

public interface Callback {
    void onSuccess(Apidoc apidoc);
    void onFailure(Apidoc apidoc,Exception exception);
}
