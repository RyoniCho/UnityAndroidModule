package com.controlroom.androidmodule;

public interface PluginCallback {

    void OnDefaultCallback(boolean success);
    void OnOkButtonTouched();
    void OnCancelButtonTouched();

}